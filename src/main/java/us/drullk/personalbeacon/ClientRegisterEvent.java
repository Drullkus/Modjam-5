package us.drullk.personalbeacon;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ClientRegisterEvent {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (IModelRegisterCallback models : RegisterEvent.models) {
            models.registerModel();
        }
    }
}