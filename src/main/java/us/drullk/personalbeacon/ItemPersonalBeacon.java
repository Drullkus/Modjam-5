package us.drullk.personalbeacon;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;

public class ItemPersonalBeacon extends Item implements IModelRegisterCallback {
    public ItemPersonalBeacon() {
        this.addPropertyOverride(
                new ResourceLocation("active"),
                (stack, worldIn, entityIn) -> {
                    NBTTagCompound compound = stack.getTagCompound();
                    return compound != null ? compound.getBoolean("beaconactive") ? 1 : 0 : 0;
                }
        );
    }

    public void onUpdate(ItemStack personalBeacon, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityPlayer && itemSlot > 8 && !(entityIn instanceof FakePlayer)) {
            itemSlot -= 9;

            EntityPlayer player = (EntityPlayer) entityIn;

            List<ItemStack> playerInventory = player.inventory.mainInventory;

            int maxSize = playerInventory.size();
            int tier = 0;
            boolean isGood = true;

            for (int row = 1; isGood && row < maxSize / 9; row++) {
                int minSlot = itemSlot + ( 8 * row);
                int maxSlot = itemSlot + (10 * row);

                int requiredRow = row + (itemSlot / 9);

                if (isGood = (
                        minSlot / 9 == requiredRow
                        && maxSlot / 9 == requiredRow
                        && maxSlot < maxSize
                        && scanInventoryRow(minSlot, maxSlot, playerInventory)))
                    tier++;
            }

            isGood = tier > 0;

            // if (tier == 0) return;

            // TODO beacon actions

            NBTTagCompound compound = personalBeacon.getTagCompound();

            if (compound == null) {
                compound = new NBTTagCompound();
                compound.setBoolean("beaconactive", isGood);
                personalBeacon.setTagCompound(compound);
            } else compound.setBoolean("beaconactive", isGood);
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

    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }
}