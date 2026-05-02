package us.timinc.mc.cobblemon.pasturecollector.common.dropper

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.toBlockPos
import com.cobblemon.mod.common.util.toVec3d
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
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
import us.timinc.mc.cobblemon.pasturecollector.common.extensions.tickPastureBinCooldown
import kotlin.jvm.optionals.getOrNull

class PastureDropper(
    override val trigger: ResourceLocation,
    override val lootTables: List<ResourceLocation>,
    override val conditions: List<LootItemCondition>,
    override val dropTarget: ResourceLocation?,
    val cooldown: Int = 0,
) : Dropper<PastureDropper.Context>() {
    override fun getType(): DropperType<*, *> = PastureCollector.DropperTypes.PASTURE

    companion object {
        val CODEC: MapCodec<PastureDropper> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecPieces.getTrigger(PastureDropper::trigger),
                CodecPieces.getTables(PastureDropper::lootTables),
                CodecPieces.getConditions(PastureDropper::conditions),
                CodecPieces.getDropTarget(PastureDropper::dropTarget),
                Codec.INT.optionalFieldOf("cooldown", 0).forGetter(PastureDropper::cooldown),
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
                LootContextParams.ORIGIN to pokemonEntity.position().toBlockPos().toVec3d(),
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
                && (id?.let { context.pokemonEntity.pokemon.tickPastureBinCooldown(it, cooldown) } ?: false)
                && super.canDrop(context)
}
