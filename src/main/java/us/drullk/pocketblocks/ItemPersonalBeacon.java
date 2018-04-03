package us.drullk.pocketblocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPersonalBeacon extends Item implements IModelRegisterCallback {
    //private static final String NBT_BEACON_COMPOUND = "Bacondata";
    private static final String NBT_BEACON_LEVELS = "Levels";
    private static final String NBT_POTION_ARRAY = "CustomPotionEffects";
    private static final String NBT_POTION_EFFECT = "Id";
    private static final String NBT_POTION_COUNTDOWN = "Duration";
    private static final String NBT_POTION_INTENSITY = "Amplifier";

    private static final String LANG_INVALID = "personalbeacon.invalid";
    private static final String LANG_VALID = "personalbeacon.valid";

    public ItemPersonalBeacon() {
        this.setCreativeTab(CreativeTabs.BREWING);
        this.addPropertyOverride(
                new ResourceLocation("active"),
                (stack, worldIn, entityIn) -> {
                    NBTTagCompound compound = stack.getTagCompound();
                    return compound != null ? compound.getInteger(NBT_BEACON_LEVELS) > 0 ? 1 : 0 : 0;
                }
        );
    }

    public void onUpdate(ItemStack personalBeacon, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote || !(entityIn instanceof EntityPlayer) || entityIn instanceof FakePlayer || entityIn.getEntityWorld().getTotalWorldTime() % 80L != 0L) return;

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

        NBTTagCompound compoundTest = personalBeacon.getTagCompound();

        NBTTagCompound compound = compoundTest == null ? new NBTTagCompound() : compoundTest;
        compound.setInteger(NBT_BEACON_LEVELS, levels);

        if (!isValid) return; // Are we all good? Let's continue to actual effects then.

        itemSlot += 9;
        NBTTagList beaconEffects = compound.getTagList(NBT_POTION_ARRAY, Constants.NBT.TAG_COMPOUND);

        if(beaconEffects.hasNoTags()) {
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

                personalBeacon.setTagCompound(compound);
            }
        } else {
            RegistryNamespaced<ResourceLocation, Potion> potionRegistry = Potion.REGISTRY;

            boolean allCountsZero = true;
            boolean allTagsInvalid = true;

            NBTTagList modifiedEffects = new NBTTagList();

            for (NBTBase beaconEffect : beaconEffects) {
                if (beaconEffect instanceof NBTTagCompound) {
                    allTagsInvalid = false;
                    NBTTagCompound effectNBT = (NBTTagCompound) beaconEffect;

                    int timeLeft = effectNBT.getInteger(NBT_POTION_COUNTDOWN);

                    if (timeLeft != 0) {
                        Potion effect = Potion.getPotionById(effectNBT.getInteger(NBT_POTION_EFFECT));

                        int duration;

                        if (timeLeft > 80) {
                            duration = Math.min(280, timeLeft);
                            effectNBT.setInteger(NBT_POTION_COUNTDOWN, timeLeft - 80);
                        } else {
                            duration = timeLeft;
                            effectNBT.setInteger(NBT_POTION_COUNTDOWN, 0);
                            effectNBT.setInteger(NBT_POTION_EFFECT, 0);
                        }

                        if (timeLeft > 0) allCountsZero = false;

                        if (effect != null) {
                            int range = levels * 2;
                            AxisAlignedBB box = player.getEntityBoundingBox().grow(range);
                            List<EntityPlayer> players = player.getEntityWorld().getEntitiesWithinAABB(EntityPlayer.class, box);

                            for (EntityPlayer aPlayer : players)
                                aPlayer.addPotionEffect(new PotionEffect(effect, duration, effectNBT.getInteger(NBT_POTION_INTENSITY)));

                            modifiedEffects.appendTag(effectNBT);
                        }
                    }
                }
            }

            // Overwrite with a new list either way to nuke garbage compounds in array
            compound.setTag(NBT_POTION_ARRAY, allTagsInvalid || allCountsZero ? new NBTTagList() : modifiedEffects);

            personalBeacon.setTagCompound(compound);
        }
    }

    private static void handleActivation(ItemStack potionStack, ItemStack paymentStack, NBTTagCompound compound) {
        List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potionStack);
        NBTTagList effectList = new NBTTagList();

        for (PotionEffect effect : effects) {
            NBTTagCompound effectCompound = new NBTTagCompound();

            int potionID = Potion.getIdFromPotion(effect.getPotion());

            if (potionID == 0) continue;

            effectCompound.setInteger(NBT_POTION_EFFECT, potionID);
            effectCompound.setInteger(NBT_POTION_COUNTDOWN, effect.getDuration());
            effectCompound.setInteger(NBT_POTION_INTENSITY, effect.getAmplifier());

            effectList.appendTag(effectCompound);
        }

        potionStack.shrink(1);
        paymentStack.shrink(1);

        compound.setTag(NBT_POTION_ARRAY, effectList);
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

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        NBTTagCompound compound = stack.getTagCompound();

        if (compound != null) {
            int levels = compound.getInteger(NBT_BEACON_LEVELS);

            if (levels > 0) tooltip.add(I18n.translateToLocalFormatted(LANG_VALID, levels, 2*levels));
            else {
                tooltip.add(I18n.translateToLocal(LANG_INVALID));
            }
        } else if (worldIn != null) {

        }

        PotionUtils.addPotionTooltip(stack, tooltip, 1.0F);
    }

    static int getPrimaryColorFromStack(ItemStack stack) {
        int returned = 0xFF_00_00_00;

        NBTTagCompound compound = stack.getTagCompound();

        if (compound == null) return returned;

        NBTTagList list = compound.getTagList(ItemPersonalBeacon.NBT_POTION_ARRAY, Constants.NBT.TAG_COMPOUND);

        if (list.tagCount() < 1) return returned;

        NBTBase potionCompound = list.get(0);

        if (!(potionCompound instanceof NBTTagCompound)) return returned;

        Potion potion = Potion.getPotionById(((NBTTagCompound) potionCompound).getInteger(ItemPersonalBeacon.NBT_POTION_EFFECT));

        if (potion == null) return returned;

        return potion.getLiquidColor();
    }
}