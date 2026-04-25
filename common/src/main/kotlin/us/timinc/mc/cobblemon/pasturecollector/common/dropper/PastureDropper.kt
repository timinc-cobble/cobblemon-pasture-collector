package us.timinc.mc.cobblemon.pasturecollector.common.dropper

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.toBlockPos
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParam
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import us.timinc.mc.cobblemon.droploottables.api.DropContext
import us.timinc.mc.cobblemon.droploottables.api.Dropper
import us.timinc.mc.cobblemon.droploottables.api.Dropper.Companion.CodecPieces
import us.timinc.mc.cobblemon.droploottables.api.DropperType
import us.timinc.mc.cobblemon.pasturecollector.common.PastureCollector
import us.timinc.mc.cobblemon.timcore.getCompoundOrNull
import us.timinc.mc.cobblemon.timcore.getIntOrNull
import kotlin.jvm.optionals.getOrNull

class PastureDropper(
    override val trigger: ResourceLocation,
    override val lootTables: List<ResourceLocation>,
    override val conditions: List<LootItemCondition>,
    override val dropTarget: ResourceLocation?,
    val cooldown: Int,
) : Dropper<PastureDropper.Context>() {
    override fun getType(): DropperType<*, *> = PastureCollector.DropperTypes.PASTURE

    companion object {
        val CODEC: MapCodec<PastureDropper> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecPieces.getTrigger(PastureDropper::trigger),
                CodecPieces.getTables(PastureDropper::lootTables),
                CodecPieces.getConditions(PastureDropper::conditions),
                CodecPieces.getDropTarget(PastureDropper::dropTarget),
                Codec.INT.fieldOf("cooldown").forGetter(PastureDropper::cooldown),
            ).apply(instance) { trigger, lootTables, conditions, dropTarget, cooldown ->
                PastureDropper(
                    trigger,
                    lootTables,
                    conditions,
                    dropTarget.getOrNull(),
                    cooldown,
                )
            }
        }

        val DROPPER_TYPE = DropperType(CODEC)
    }

    class Context(
        override val level: ServerLevel,
        val pokemonEntity: PokemonEntity,
    ) : DropContext {
        override fun toLootParams(): LootParams {
            val params = mutableMapOf<LootContextParam<out Any>, Any>(
                LootContextParams.ORIGIN to pokemonEntity.position().toBlockPos(),
                LootContextParams.THIS_ENTITY to pokemonEntity,
            )

            return LootParams(
                level,
                params,
                mapOf(),
                0F
            )
        }
    }

    override fun canDrop(context: Context): Boolean =
        !context.pokemonEntity.isBusy
                &&
                let {
                    if (cooldown <= 1) return@let true

                    val id = id?.toString() ?: return@let false
                    val persistentData = context.pokemonEntity.pokemon.persistentData
                    val cooldownCollection = persistentData.getCompoundOrNull("pasturecollector:cooldowns") ?: let {
                        val newCollection = CompoundTag()
                        persistentData.put("pasturecollector:cooldowns", newCollection)
                        newCollection
                    }

                    val myCooldown = cooldownCollection.getIntOrNull(id) ?: cooldown
                    cooldownCollection.putInt(id, myCooldown - 1)
                    return@let myCooldown <= 0
                }
                && super.canDrop(context)
}
