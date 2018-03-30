package us.drullk.personalbeacon;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;

public class ItemPersonalBeacon extends Item {
    public void onUpdate(ItemStack personalBeacon, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityPlayer && !(entityIn instanceof FakePlayer)) {
            EntityPlayer player = (EntityPlayer) entityIn;

            List<ItemStack> playerInventory = player.inventory.mainInventory;

            for (int i = ((itemSlot / 9) + 1) * 9; i < playerInventory.size(); i++) {
                ItemStack stack = playerInventory.get(i);

                if (!stack.isEmpty()) {
                    Block block = Block.getBlockFromItem(stack.getItem());

                    BlockPos dummyPos = BlockPos.ORIGIN.down();

                    if (block.isBeaconBase(null, dummyPos, dummyPos)) {

                    }
                }
            }
        }
    }
}