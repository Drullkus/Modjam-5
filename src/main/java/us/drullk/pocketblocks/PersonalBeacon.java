package us.drullk.pocketblocks;

import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class PersonalBeacon {
    public static Logger logger = LogManager.getLogger(Reference.MOD_ID);

    @SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.COMMON_PROXY, modId = Reference.MOD_ID)
    public static CommonProxy proxy;

    public PersonalBeacon() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void beaconToolTip(RenderTooltipEvent.Color event) {
        if (!(event.getStack().getItem() instanceof ItemPersonalBeacon)) return;

        int color = (ItemPersonalBeacon.getPrimaryColorFromStack(event.getStack()) | 0xF0_00_00_00) & 0xF0_FF_FF_FF;

        if (color == 0xF0_00_00_00) return;

        event.setBorderStart(color);
        event.setBorderEnd(color & 0xF0_F0_F0_F0);
    }
}