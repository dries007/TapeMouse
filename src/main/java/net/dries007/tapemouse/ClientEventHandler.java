package net.dries007.tapemouse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import static net.dries007.tapemouse.TapeMouse.LOGGER;
import static net.minecraftforge.eventbus.api.EventPriority.HIGHEST;

/**
 * Client side only code
 * @author Dries007
 */
public class ClientEventHandler
{
    private static final Map<String, KeyBinding> KEYBIND_ARRAY = ObfuscationReflectionHelper.getPrivateValue(KeyBinding.class, null, "field_74516_a");
    private final Minecraft mc;

    private int delay;
    private KeyBinding keyBinding;
    private int i;

    public ClientEventHandler()
    {
        this.mc = Minecraft.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
        if (KEYBIND_ARRAY == null)
        {
            RuntimeException e = new NullPointerException("KEYBIND_ARRAY was null.");
            LOGGER.fatal("Something has gone wrong fetching the keybinding list. I guess we die now.", e);
            throw e;
        }
    }

    /**
     * Draw info on the screen
     */
    @SubscribeEvent
    public void textRenderEvent(RenderGameOverlayEvent.Text event)
    {
        if (keyBinding == null) return;
        if (mc.currentScreen instanceof MainMenuScreen || mc.currentScreen instanceof ChatScreen)
        {
            event.getLeft().add("TapeMouse paused. If you want to AFK, use ALT+TAB.");
            return;
        }
        event.getLeft().add("TapeMouse active: " + keyBinding.getLocalizedName() + " (" + keyBinding.getKeyDescription().replaceFirst("^key\\.", "") + ')');
        event.getLeft().add("Delay: " + i + " / " + delay);
    }

    /**
     * Actually trigger the keybinding.
     */
    @SubscribeEvent(priority = HIGHEST)
    public void tickEvent(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.currentScreen instanceof MainMenuScreen || mc.currentScreen instanceof ChatScreen) return;
        if (keyBinding == null) return;
        if (i++ < delay) return;
        i = 0;
        if (delay == 0) KeyBinding.setKeyBindState(keyBinding.getKey(), true);
        KeyBinding.onTick(keyBinding.getKey());
    }

    /**
     * DIY Client side command
     */
    @SubscribeEvent(priority = HIGHEST)
    public void chatEvent(ClientChatEvent event)
    {
        if (!event.getMessage().startsWith("/tapemouse")) return;
        event.setCanceled(true);
        String[] args = event.getOriginalMessage().split("\\s");
        NewChatGui gui = mc.ingameGUI.getChatGUI();
        gui.addToSentMessages(event.getOriginalMessage());
        try
        {
            handleCommand(gui, args);
        }
        catch (Exception e)
        {
            gui.printChatMessage(new StringTextComponent("An error occurred trying to run the tapemouse command:").setStyle(new Style().setColor(TextFormatting.RED)));
            gui.printChatMessage(new StringTextComponent(e.toString()).setStyle(new Style().setColor(TextFormatting.RED)));
            LOGGER.error("An error occurred trying to run the tapemouse command:", e);
        }
    }

    private void handleCommand(NewChatGui gui, String[] args) throws Exception
    {
        switch (args.length)
        {
            default:
                gui.printChatMessage(new StringTextComponent("TapeMouse help: ").setStyle(new Style().setColor(TextFormatting.AQUA)));
                gui.printChatMessage(new StringTextComponent("Run '/tapemouse list' to get a list of keybindings."));
                gui.printChatMessage(new StringTextComponent("Run '/tapemouse off' to stop TapeMouse."));
                gui.printChatMessage(new StringTextComponent("Run '/tapemouse <binding> <delay>' to start TapeMouse."));
                gui.printChatMessage(new StringTextComponent("  delay is the number of ticks between every keypress. Set to 0 to hold down the key."));
                return;
            case 2:
                if (args[1].equalsIgnoreCase("off"))
                {
                    this.keyBinding = null;
                    return;
                }
                else if (args[1].equalsIgnoreCase("list"))
                {
                    List<String> keys = KEYBIND_ARRAY.keySet().stream().map(k -> k.replaceFirst("^key\\.", "")).sorted().collect(Collectors.toList());
                    gui.printChatMessage(new StringTextComponent(String.join(", ", keys)));
                }
                else
                {
                    gui.printChatMessage(new StringTextComponent("Missing delay parameter.").setStyle(new Style().setColor(TextFormatting.RED)));
                }
                break;
            case 3:
            {
                KeyBinding keyBinding = KEYBIND_ARRAY.get("key." + args[1]);
                if (keyBinding == null)
                {
                    keyBinding = KEYBIND_ARRAY.get(args[1]);
                }
                if (keyBinding == null)
                {
                    gui.printChatMessage(new StringTextComponent(args[1] + " is not a valid keybinding.").setStyle(new Style().setColor(TextFormatting.RED)));
                    return;
                }
                int delay;
                try
                {
                    delay = Integer.parseInt(args[2]);
                     if (delay < 0) throw new Exception("bad user");
                }
                catch (Exception e)
                {
                    gui.printChatMessage(new StringTextComponent(args[1] + " is not a positive number or 0.").setStyle(new Style().setColor(TextFormatting.RED)));
                    return;
                }

                this.delay = delay;
                this.i = 0;
                this.keyBinding = keyBinding;
            }
            break;
        }
    }
}
