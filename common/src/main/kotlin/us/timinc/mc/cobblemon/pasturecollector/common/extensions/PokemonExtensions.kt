package us.timinc.mc.cobblemon.pasturecollector.common.extensions

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import us.timinc.mc.cobblemon.pasturecollector.common.PastureCollector
import us.timinc.mc.cobblemon.timcore.getCompoundOrNull
import us.timinc.mc.cobblemon.timcore.getIntOrNull

fun Pokemon.tickPastureBinCooldown(id: ResourceLocation, cooldown: Int): Boolean {
    if (cooldown <= 1) return true

    val cooldownCollection =
        persistentData.getCompoundOrNull(PastureCollector.DataKeys.PersistentData.COOLDOWNS.toString()) ?: let {
            val newCollection = CompoundTag()
            persistentData.put(PastureCollector.DataKeys.PersistentData.COOLDOWNS.toString(), newCollection)
            newCollection
        }

    val targetCooldown = cooldownCollection.getIntOrNull(id.toString()) ?: cooldown
    if (targetCooldown <= 0) {
        cooldownCollection.putInt(id.toString(), cooldown)
        return true
    }
    cooldownCollection.putInt(id.toString(), targetCooldown - 1)
    return false
}