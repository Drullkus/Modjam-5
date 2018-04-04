package us.drullk.pocketblocks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static us.drullk.pocketblocks.Reference.BOOKSHELF.*;

public class BlockCompressedBookshelf extends Block implements IModelRegisterCallback {
    private final int level;
    private final int power;
    private static final int[] powers;

    static {
        powers = new int[8];

        for (int i = 0; i < powers.length; i++) powers[i] = pow(9, i + 1);
    }

    private static int pow(int num, int power) {
        if (power == 0) return 1;

        int returnInt = num;

        for (int i = 1; i < power; i++) returnInt *= num;

        return returnInt;
    }

    BlockCompressedBookshelf(int level) {
        super(Material.WOOD);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.level = level;
        this.power = powers[level-1];
    }

    @Override
    public String getLocalizedName() {
        return this.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName() {
        return this.level == 1
                ? I18n.translateToLocalFormatted(LANG_COMPRESSED, Blocks.BOOKSHELF.getLocalizedName())
                : I18n.translateToLocalFormatted(LANG_MULTICOMPRESSED, I18n.translateToLocal(LANG_TUPLES[level -1]), Blocks.BOOKSHELF.getLocalizedName());
    }

    @Override
    public float getEnchantPowerBonus(World world, BlockPos block) {
        return power;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        if (level == 1) tooltip.add(I18n.translateToLocalFormatted(LANG_BOOKSHELF_COMPRESSION_ONE_TIMES, power, I18n.translateToLocalFormatted(LANG_NUMBERS[level -1])));
        else            tooltip.add(I18n.translateToLocalFormatted(LANG_BOOKSHELF_COMPRESSION, power, I18n.translateToLocalFormatted(LANG_NUMBERS[level -1])));
        tooltip.add(I18n.translateToLocal(LANG_BOOKSHELF_INFO));
    }

    private static ResourceLocation blockstateLocation = new ResourceLocation(Reference.MOD_ID, "compressed_bookshelf");

    @Override
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);

        final ModelResourceLocation mrl = new ModelResourceLocation(blockstateLocation, this.getRegistryName().getResourcePath());

        ModelLoader.setCustomModelResourceLocation(item, 0, mrl);
        ModelLoader.setCustomStateMapper(this, blockIn -> ImmutableMap.of(blockIn.getDefaultState(), mrl)
        );
    }
}
