package net.dries007.tapemouse;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * Main mod file
 *
 * @author Dries007
 */
@Mod(modid = TapeMouse.MODID, name = TapeMouse.MODID, dependencies = "before:*", useMetadata = false)
public class TapeMouse
{
    public static final String MODID = "TapeMouse";
    public static State state = State.DISABLE;
    public static int delay = 10;

    public enum State
    {
        DISABLE, LEFT, RIGHT
    }

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event)
    {
        FMLCommonHandler.instance().bus().register(this);
        if (event.getSide().isClient())
        {
            ClientCommandHandler.instance.registerCommand(new CommandTapeMouse());
        }
    }

    static int i = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void tickEvent(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;
        if (i++ < delay) return;
        i = 0;
        switch (state)
        {
            case LEFT:
                KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode());
                break;
            case RIGHT:
                KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode());
                break;
        }
    }
}
