package us.drullk.personalbeacon;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber( modid = Reference.MOD_ID )
public class RegisterEvent {
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> items = event.getRegistry();
        items.register(new ItemPersonalBeacon().setUnlocalizedName("personalbeacon").setRegistryName(new ResourceLocation(Reference.MOD_ID, "personalbeacon")));
    }
}
