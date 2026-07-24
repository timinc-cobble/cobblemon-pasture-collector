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
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.api.DropContext
import us.timinc.mc.cobblemon.droploottables.api.Dropper
import us.timinc.mc.cobblemon.droploottables.api.Dropper.Companion.CodecPieces
import us.timinc.mc.cobblemon.droploottables.api.DropperType
import us.timinc.mc.cobblemon.pasturecollector.common.PastureCollector
import us.timinc.mc.cobblemon.pasturecollector.common.extensions.advancePastureBinCooldown
import us.timinc.mc.cobblemon.pasturecollector.common.extensions.isPastureBinCooldownReady
import kotlin.jvm.optionals.getOrNull

class PastureDropper(
    override val trigger: ResourceLocation,
    override val lootTables: List<ResourceLocation>,
    override val conditions: List<LootItemCondition>,
    override val dropTarget: ResourceLocation?,
    val cooldown: Int = 0,
    val preserveBaseDrops: Boolean = false,
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
                Codec.BOOL.optionalFieldOf("preserve_base_drops", false)
                    .forGetter(PastureDropper::preserveBaseDrops),
            ).apply(instance) { trigger, lootTables, conditions, dropTarget, cooldown, preserveBaseDrops ->
                PastureDropper(
                    trigger,
                    lootTables,
                    conditions,
                    dropTarget.getOrNull(),
                    cooldown,
                    preserveBaseDrops,
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
                DropLootTables.LootParams.FOCUS_POKEMON to pokemonEntity.pokemon,
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
        id != null && !context.pokemonEntity.isBusy && super.canDrop(context)

    fun isCooldownReady(context: Context): Boolean =
        id?.let { context.pokemonEntity.pokemon.isPastureBinCooldownReady(it, cooldown) } ?: false

    fun advanceCooldown(context: Context) {
        id?.let { context.pokemonEntity.pokemon.advancePastureBinCooldown(it, cooldown) }
    }
}
