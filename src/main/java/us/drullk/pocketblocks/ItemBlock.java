package us.drullk.pocketblocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlock extends net.minecraft.item.ItemBlock {
    public ItemBlock(Block block) {
        super(block);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return this.block.getLocalizedName();
    }
}
