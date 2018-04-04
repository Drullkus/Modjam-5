package us.drullk.pocketblocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("SameParameterValue")
@Mod.EventBusSubscriber( modid = Reference.MOD_ID )
public class RegisterEvent {
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> blocks = event.getRegistry();

        for (int i = 1; i <= 8; i++) registerBlockWithItem(blocks, new BlockCompressedBookshelf(i), "compressed_bookshelf_" + i);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> items = event.getRegistry();
        registerItem(items, new ItemPersonalBeacon(), "personalbeacon");

        for (Block block : blocksForItems) {
            registerItem(items, new ItemBlock(block), block.getRegistryName());
        }
    }

    private static void registerBlock(IForgeRegistry<Block> blocks, Block block, String regname) {
        blocks.register(block.setUnlocalizedName(regname).setRegistryName(new ResourceLocation(Reference.MOD_ID, regname)));

        if (block instanceof IModelRegisterCallback)
            models.add((IModelRegisterCallback) block);
    }

    private static void registerBlockWithItem(IForgeRegistry<Block> blocks, Block block, String regname) {
        registerBlock(blocks, block, regname);

        blocksForItems.add(block);
    }

    private static void registerItem(IForgeRegistry<Item> items, Item item, String regname) {
        registerItem(items, item, new ResourceLocation(Reference.MOD_ID, regname));
    }

    private static void registerItem(IForgeRegistry<Item> items, Item item, ResourceLocation regname) {
        items.register(item.setUnlocalizedName(regname.getResourcePath()).setRegistryName(regname));

        if (item instanceof IModelRegisterCallback)
            models.add((IModelRegisterCallback) item);
    }

    static Set<IModelRegisterCallback> models = new HashSet<>();
    private static List<Block> blocksForItems = new ArrayList<>();

    /*

    TODO 1. Personal Enchantment table
    TODO 3. Personal Furnace?

     */
}
