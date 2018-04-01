package us.drullk.personalbeacon;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;

public class ItemPersonalBeacon extends Item implements IModelRegisterCallback {
    private static final String NBT_BEACON_VALID = "beaconactive";
    private static final String NBT_POTION_EFFECT = "beaconeffect";
    private static final String NBT_POTION_COUNTDOWN = "beaconcountdown";
    private static final String NBT_POTION_INTENSITY = "beaconintensity";

    public ItemPersonalBeacon() {
        this.addPropertyOverride(
                new ResourceLocation("active"),
                (stack, worldIn, entityIn) -> {
                    NBTTagCompound compound = stack.getTagCompound();
                    return compound != null ? compound.getBoolean(NBT_BEACON_VALID) ? 1 : 0 : 0;
                }
        );
    }

    public void onUpdate(ItemStack personalBeacon, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!(entityIn instanceof EntityPlayer) || entityIn instanceof FakePlayer || entityIn.getEntityWorld().getTotalWorldTime() % 80L != 0L) return;

        boolean isValid = false;
        int levels = 0;

        EntityPlayer player = (EntityPlayer) entityIn;
        List<ItemStack> playerInventory = player.inventory.mainInventory;

        if (itemSlot > 8 && itemSlot % 9 != 0 && itemSlot % 9 != 8) {
            isValid = true;

            itemSlot -= 9;

            int maxSize = playerInventory.size();

            for (int row = 1; isValid && row < maxSize / 9; row++) {
                int minSlot = itemSlot + ( 8 * row);
                int maxSlot = itemSlot + (10 * row);

                int requiredRow = row + (itemSlot / 9);

                if (isValid = (
                        minSlot / 9 == requiredRow
                        && maxSlot / 9 == requiredRow
                        && maxSlot < maxSize
                        && scanInventoryRow(minSlot, maxSlot, playerInventory)))
                    levels++;
            }

            isValid = levels > 0;
        }

        NBTTagCompound compound = personalBeacon.getTagCompound();

        if (compound == null) {
            compound = new NBTTagCompound();
            compound.setBoolean(NBT_BEACON_VALID, isValid);
            personalBeacon.setTagCompound(compound);
        } else compound.setBoolean(NBT_BEACON_VALID, isValid);

        if (!isValid) return; // Are we all good? Let's continue to actual effects then.

        String beaconEffect = compound.getString(NBT_POTION_EFFECT);

        itemSlot += 9;

        if("".equals(beaconEffect)) {
            ItemStack leftStack = playerInventory.get(itemSlot - 1);
            ItemStack rightStack = playerInventory.get(itemSlot + 1);

            if (!leftStack.isEmpty() && !rightStack.isEmpty()) {
                Item rightItem = rightStack.getItem();
                Item leftItem = leftStack.getItem();

                if (leftItem.isBeaconPayment(leftStack)) {
                    if (!(rightItem instanceof ItemPotion)) return;

                    handleActivation(rightStack, leftStack, compound);
                } else if (rightItem.isBeaconPayment(rightStack)) {
                    if (!(leftItem instanceof ItemPotion)) return;

                    handleActivation(leftStack, rightStack, compound);
                }
            }
        } else {
            int timeLeft = compound.getInteger(NBT_POTION_COUNTDOWN);
            int duration;

            if (timeLeft > 80) {
                duration = Math.min(280, timeLeft);
                compound.setInteger(NBT_POTION_COUNTDOWN, timeLeft - 80);
            } else {
                duration = timeLeft;
                compound.setInteger(NBT_POTION_COUNTDOWN, 0);
                compound.setString(NBT_POTION_EFFECT, "");
            }

            if (timeLeft > 0) {
                RegistryNamespaced<ResourceLocation, Potion> potionRegistry = Potion.REGISTRY;

                Potion effect = potionRegistry.getObject(new ResourceLocation(beaconEffect));

                if (effect != null) {
                    int range = levels * 2;
                    AxisAlignedBB box = player.getEntityBoundingBox().grow(range);
                    List<EntityPlayer> players = player.getEntityWorld().getEntitiesWithinAABB(EntityPlayer.class, box);

                    for (EntityPlayer aPlayer : players)
                        aPlayer.addPotionEffect(new PotionEffect(effect, duration, compound.getInteger(NBT_POTION_INTENSITY)));
                }
            }
        }
    }

    private static void handleActivation(ItemStack potionStack, ItemStack paymentStack, NBTTagCompound compound) {
        List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potionStack);

        if(effects.size() > 0) {
            PotionEffect effect = effects.get(0);

            ResourceLocation potionRegName = effect.getPotion().getRegistryName();

            if (potionRegName != null) {
                compound.setString(NBT_POTION_EFFECT, potionRegName.toString());
                compound.setInteger(NBT_POTION_COUNTDOWN, effect.getDuration());
                compound.setInteger(NBT_POTION_INTENSITY, effect.getAmplifier());

                potionStack.shrink(1);
                paymentStack.shrink(1);
            }
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
            } else return false;
        }

        return bool;
    }

    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }
}