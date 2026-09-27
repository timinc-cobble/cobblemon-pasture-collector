package us.timinc.mc.cobblemon.pasturecollector.neoforge

import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import us.timinc.mc.cobblemon.pasturecollector.common.MOD_ID
import us.timinc.mc.cobblemon.pasturecollector.common.PastureCollector.Registries.Block.PASTURE_COLLECTOR
import us.timinc.mc.cobblemon.pasturecollector.common.PastureCollector.Registries.Menu.PASTURE_COLLECTOR_MENU
import us.timinc.mc.cobblemon.pasturecollector.common.client.menu.PastureCollectorBlockScreen

@EventBusSubscriber(modid = MOD_ID, value = [Dist.CLIENT])
object PastureCollectorNeoForgeClient {
    @SubscribeEvent
    fun registerMenuScreens(event: RegisterMenuScreensEvent) {
        event.register(PASTURE_COLLECTOR_MENU.type, ::PastureCollectorBlockScreen)
    }

    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            @Suppress("DEPRECATION")
            ItemBlockRenderTypes.setRenderLayer(PASTURE_COLLECTOR.block, RenderType.cutout())
        }
    }
}
