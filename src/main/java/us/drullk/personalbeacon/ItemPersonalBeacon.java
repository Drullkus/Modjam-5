package us.drullk.personalbeacon;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;

public class ItemPersonalBeacon extends Item {
    public void onUpdate(ItemStack personalBeacon, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityPlayer && !(entityIn instanceof FakePlayer) && itemSlot > 8) {
            itemSlot -= 9;

            EntityPlayer player = (EntityPlayer) entityIn;

            List<ItemStack> playerInventory = player.inventory.mainInventory;

            int maxSize = playerInventory.size();
            int tier = 0;
            boolean isGood = true;

            for (int row = (itemSlot / 9) + 1; isGood && row < maxSize / 9; row++) {
                int minSlot = itemSlot + (8 * row);
                int maxSlot = itemSlot + (10 * row);

                if (isGood = (minSlot/9 == row && maxSlot/9 == row && maxSlot < maxSize && scanInventoryRow(minSlot, maxSlot, playerInventory)))
                    tier++;
            }

            //if (tier == 0) return;

            // TODO beacon actions
        }
    }

    private static boolean scanInventoryRow(int startSlot, int endSlot, List<ItemStack> inventory) {
        boolean bool = true;

        for (int counter = startSlot; bool && counter < inventory.size() + 9 && counter <= endSlot; counter++) {
            int c = counter + 9 < inventory.size() ? counter + 9 : counter - 27;

            ItemStack stack = inventory.get(c);

            if (!stack.isEmpty()) {
                Block block = Block.getBlockFromItem(stack.getItem());

                BlockPos dummyPos = BlockPos.ORIGIN.down();

                bool = block.isBeaconBase(null, dummyPos, dummyPos);

                if (bool) inventory.set(c, new ItemStack(Blocks.DIAMOND_BLOCK));
            } else return false;
        }

        return bool;
    }
}