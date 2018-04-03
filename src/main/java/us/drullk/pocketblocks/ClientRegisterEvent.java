package us.drullk.pocketblocks;

import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ClientRegisterEvent {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (IModelRegisterCallback models : RegisterEvent.models) {
            models.registerModel();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
        event.getItemColors().registerItemColorHandler((stack, tintIndex) ->
                tintIndex == 1 && stack.getItem() instanceof ItemPersonalBeacon ? ItemPersonalBeacon.getPrimaryColorFromStack(stack) : 0xFF_FF_FF_FF,
                ModItems.personalbeacon);
    }
}