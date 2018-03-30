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

    /*

    TODO 1. Personal Enchantment table
    TODO 2. Combo beacon base block + enchantment table bookshelf - OCTUPLE COMPRESSED BOOKSHELF MAXIMUM EFFECT
    TODO 3. Personal Furnace?
    E E E E E E E E E
    B B B B B E E E E
    B E E E B E E E E
    B E T E B E E E E

     */
}
