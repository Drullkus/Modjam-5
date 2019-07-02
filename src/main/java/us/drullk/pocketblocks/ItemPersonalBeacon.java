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
    private static final int DEFAULT_BEAM_COLOR = 0xFF_FF_FF_FF;

    private static final String NBT_BEACON_LEVELS = "Levels";
    private static final String NBT_POTION_ARRAY = "CustomPotionEffects";
    private static final String NBT_POTION_EFFECT = "Id";
    private static final String NBT_POTION_COUNTDOWN = "Duration";
    private static final String NBT_POTION_INTENSITY = "Amplifier";

    private static final String LANG_BEACON_INVALID = "personalbeacon.invalid";
    private static final String LANG_BEACON_VALID = "personalbeacon.valid";

    public ItemPersonalBeacon() {
        this.setCreativeTab(CreativeTabs.BREWING);
        this.addPropertyOverride(
                new ResourceLocation("active"),
                (stack, worldIn, entityIn) -> {
                    NBTTagCompound compound = stack.getTagCompound();
                    return compound != null && compound.getInteger(NBT_BEACON_LEVELS) > 0 ? 1 : 0;
                }
        );
    }

    @Override
    public void onUpdate(ItemStack personalBeacon, World worldIn, Entity entityIn, final int itemSlot, boolean isSelected) {
        if (worldIn.isRemote) return;
        if (!(entityIn instanceof EntityPlayer) || entityIn instanceof FakePlayer) return;
        if (entityIn.getEntityWorld().getTotalWorldTime() % 80L != 0L) return;
        if (itemSlot <= 8) return; // tests if it's not in hotbar
        if (itemSlot % 9 == 0 || itemSlot % 9 == 8) return; // tests if it's not on the sides of the player inventory.

        EntityPlayer player = (EntityPlayer) entityIn;
        List<ItemStack> playerInventory = player.inventory.mainInventory;

        int upperSlot = itemSlot - 9;

        int maxSize = playerInventory.size();

        int levels = 0;

        for (int row = 1; row < maxSize / 9; row++) {
            int minSlot = upperSlot + ( 8 * row);
            int maxSlot = upperSlot + (10 * row);

            int requiredRow = row + (upperSlot / 9);

            if (minSlot / 9 != requiredRow
                || maxSlot / 9 != requiredRow
                || maxSlot >= maxSize
                || !scanInventoryRow(minSlot, maxSlot, playerInventory))
                break;

            levels++;
        }

        if (levels == 0) return; // Are we all good? Let's continue to actual effects then.

        NBTTagCompound compoundTest = personalBeacon.getTagCompound();

        NBTTagCompound compound = compoundTest == null ? new NBTTagCompound() : compoundTest;
        compound.setInteger(NBT_BEACON_LEVELS, levels);

        NBTTagList beaconEffects = compound.getTagList(NBT_POTION_ARRAY, Constants.NBT.TAG_COMPOUND);

        if(beaconEffects.hasNoTags()) {
            setEffects(personalBeacon, itemSlot, playerInventory, compound);
        } else {
            emitEffects(personalBeacon, levels, player, compound, beaconEffects);
        }
    }

    private void emitEffects(ItemStack personalBeacon, int levels, EntityPlayer player, NBTTagCompound compound, NBTTagList beaconEffects) {
        boolean allCountsZero = true;
        boolean allTagsInvalid = true; // if taglist contains only unreadable tags

        NBTTagList modifiedEffects = new NBTTagList(); // moving onto the next

        for (NBTBase beaconEffect : beaconEffects) {
            if (!(beaconEffect instanceof NBTTagCompound)) continue;

            allTagsInvalid = false;
            NBTTagCompound effectNBT = (NBTTagCompound) beaconEffect;

            int timeLeft = effectNBT.getInteger(NBT_POTION_COUNTDOWN);
            if (timeLeft == 0) continue;

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

            if (effect == null) continue;

            int range = levels * 2;
            AxisAlignedBB box = player.getEntityBoundingBox().grow(range);
            List<EntityPlayer> players = player.getEntityWorld().getEntitiesWithinAABB(EntityPlayer.class, box);

            for (EntityPlayer aPlayer : players)
                aPlayer.addPotionEffect(new PotionEffect(effect, duration, effectNBT.getInteger(NBT_POTION_INTENSITY)));

            modifiedEffects.appendTag(effectNBT);
        }

        // Overwrite with a new list either way to nuke garbage compounds in array
        compound.setTag(NBT_POTION_ARRAY, allTagsInvalid || allCountsZero ? new NBTTagList() : modifiedEffects);
        personalBeacon.setTagCompound(compound);
    }

    private void setEffects(ItemStack personalBeacon, int itemSlot, List<ItemStack> playerInventory, NBTTagCompound compound) {
        ItemStack leftStack = playerInventory.get(itemSlot - 1);
        ItemStack rightStack = playerInventory.get(itemSlot + 1);

        if (!leftStack.isEmpty() && !rightStack.isEmpty()) {
            if (!tryHandleActivation(leftStack, rightStack, compound)) {
                tryHandleActivation(rightStack, leftStack, compound);
            }

            personalBeacon.setTagCompound(compound);
        }
    }

    private boolean tryHandleActivation(ItemStack first, ItemStack second, NBTTagCompound compound) {
        if (!first.getItem().isBeaconPayment(first)) return false;
        if (!(second.getItem() instanceof ItemPotion)) return false;

        handleActivation(second, first, compound);
        return true;
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
        for (int counter = startSlot; counter < inventory.size() + 9 && counter <= endSlot; counter++) {
            int slot = counter + 9 < inventory.size() ? counter + 9 : counter - 27;

            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) return false;

            Block block = Block.getBlockFromItem(stack.getItem());
            BlockPos dummyPos = BlockPos.ORIGIN.down();
            if (!block.isBeaconBase(null, dummyPos, dummyPos))
                return false;
        }

        return true;
    }

    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        NBTTagCompound compound = stack.getTagCompound();

        if (compound != null) {
            int levels = compound.getInteger(NBT_BEACON_LEVELS);

            if (levels > 0) {
                tooltip.add(I18n.translateToLocalFormatted(LANG_BEACON_VALID, levels, 2 * levels));
            } else {
                tooltip.add(I18n.translateToLocal(LANG_BEACON_INVALID));
            }
        } else {
            tooltip.add(I18n.translateToLocal(LANG_BEACON_INVALID));
        }

        PotionUtils.addPotionTooltip(stack, tooltip, 1.0F);
    }

    static int getPrimaryColorFromStack(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null)
            return DEFAULT_BEAM_COLOR;

        NBTTagList list = compound.getTagList(NBT_POTION_ARRAY, Constants.NBT.TAG_COMPOUND);
        if (list.tagCount() < 1)
            return DEFAULT_BEAM_COLOR;

        NBTBase potionCompound = list.get(0);
        if (!(potionCompound instanceof NBTTagCompound))
            return DEFAULT_BEAM_COLOR;

        Potion potion = Potion.getPotionById(((NBTTagCompound) potionCompound).getInteger(NBT_POTION_EFFECT));
        if (potion == null)
            return DEFAULT_BEAM_COLOR;

        return potion.getLiquidColor();
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }
}