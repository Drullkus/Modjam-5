package us.drullk.personalbeacon;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber( modid = Reference.MOD_ID )
public class RegisterEvent {
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> items = event.getRegistry();
        registerItem(items, new ItemPersonalBeacon(), "personalbeacon");
    }

    private static void registerItem(IForgeRegistry<Item> items, Item item, String regname) {
        items.register(item.setUnlocalizedName(regname).setRegistryName(new ResourceLocation(Reference.MOD_ID, regname)));

        if (item instanceof IModelRegisterCallback)
            models.add((IModelRegisterCallback) item);
    }

    static List<IModelRegisterCallback> models = new ArrayList<>();

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
