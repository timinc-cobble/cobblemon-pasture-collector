package us.timinc.mc.cobblemon.pasturecollector.common.extensions

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import us.timinc.mc.cobblemon.pasturecollector.common.PastureCollector
import us.timinc.mc.cobblemon.timcore.getCompoundOrNull
import us.timinc.mc.cobblemon.timcore.getIntOrNull

private fun Pokemon.getPastureBinCooldown(id: ResourceLocation): Int? =
    persistentData
        .getCompoundOrNull(PastureCollector.DataKeys.PersistentData.COOLDOWNS.toString())
        ?.getIntOrNull(id.toString())

fun Pokemon.isPastureBinCooldownReady(id: ResourceLocation, cooldown: Int): Boolean =
    cooldown <= 1 || (getPastureBinCooldown(id) ?: cooldown) <= 0

fun Pokemon.advancePastureBinCooldown(id: ResourceLocation, cooldown: Int) {
    if (cooldown <= 1) return

    val currentRemaining = getPastureBinCooldown(id) ?: cooldown
    val nextRemaining = if (currentRemaining <= 0) cooldown else currentRemaining - 1

    val cooldownCollection =
        persistentData.getCompoundOrNull(PastureCollector.DataKeys.PersistentData.COOLDOWNS.toString()) ?: let {
            val newCollection = CompoundTag()
            persistentData.put(PastureCollector.DataKeys.PersistentData.COOLDOWNS.toString(), newCollection)
            newCollection
        }

    cooldownCollection.putInt(id.toString(), nextRemaining)
}
