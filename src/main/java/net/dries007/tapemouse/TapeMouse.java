package net.dries007.tapemouse;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;

/**
 * Main mod file
 *
 * @author Dries007. Claycorp
 */
@Mod("tapemouse")
public class TapeMouse
{
    public static final String NAME = "TapeMouse";
    static int delay = 10;
    private static final Map<String, KeyBinding> KEYBIND_ARRAY = ObfuscationReflectionHelper.getPrivateValue(KeyBinding.class, null, "field_74516_a");
    static int i = 0;
    static KeyBinding keyToPress;

    public TapeMouse()
    {
        //Because forge is a bunch of dunces and make devs do all this here instead of just marking the side in the mods.toml file... We get all this great shit.
        //can't wait to see how may devs fuck this all up... :rolling_eyes:
        if (FMLEnvironment.dist == Dist.CLIENT)
            MinecraftForge.EVENT_BUS.register(this);
        //To stop the server reporting this mod is missing because forge is fucking stupid.
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    //This whole thing is a total hack till client commands are a thing (if that's ever)
    @SubscribeEvent
    public void onChat(ClientChatEvent event)
    {
        String originalMessage = event.getOriginalMessage();
        NewChatGui clientChat = Minecraft.getInstance().ingameGUI.getChatGUI();
        boolean prevPauseSetting = true;

        if (originalMessage.contains("/tapemouse"))
        {
            String[] args = originalMessage.split("\\s", 3);

            switch (args.length)
            {
                case 1:
                {
                    clientChat.printChatMessage(new TranslationTextComponent("tapemouse.command.keybindlist").setStyle(new Style().setColor(TextFormatting.AQUA)));
                    clientChat.printChatMessage(new TranslationTextComponent("tapemouse.command.keybindlistinfo").setStyle(new Style().setColor(TextFormatting.AQUA)));

                    for (KeyBinding keyBinding : KEYBIND_ARRAY.values())
                    {
                        if (keyBinding == null || keyBinding.isInvalid()) continue;
                        String name = keyBinding.getKeyDescription();
                        name = name.replaceFirst("^key\\.", "");

                        String cat = keyBinding.getKeyCategory();
                        cat = cat.replaceFirst("^key\\.", "");
                        cat = cat.replaceFirst("^categories\\.", "");

                        clientChat.printChatMessage(new StringTextComponent(keyBinding.getLocalizedName() + " => " + name + " (" + cat + ")"));
                    }
                    break;
                }
                case 2:
                {
                    if (args[1].equals("off"))
                    {
                        Minecraft.getInstance().gameSettings.pauseOnLostFocus = prevPauseSetting;
                        keyToPress = null;
                        i = 0;
                        clientChat.printChatMessage(new TranslationTextComponent("tapemouse.command.off")
                                .setStyle(new Style().setBold(true).setColor(TextFormatting.RED)));
                    }
                    else
                    {
                        clientChat.printChatMessage(new TranslationTextComponent("tapemouse.command.missingdelay")
                                .setStyle(new Style().setBold(true).setColor(TextFormatting.RED)));
                    }
                    break;
                }
                case 3:

                    delay = Integer.parseInt(args[2]);

                    for (KeyBinding keyBinding : KEYBIND_ARRAY.values())
                    {
                        if (keyBinding == null) continue;
                        String name = keyBinding.getKeyDescription();
                        name = name.replaceFirst("^key\\.", "");
                        if (args[1].equalsIgnoreCase(name))
                        {
                            //prevPauseSetting = Minecraft.getInstance().gameSettings.pauseOnLostFocus;
                            Minecraft.getInstance().gameSettings.pauseOnLostFocus = false;
                            keyToPress = keyBinding;

                            clientChat.printChatMessage(new TranslationTextComponent("tapemouse.command.set", name, keyBinding.getLocalizedName(), TapeMouse.delay)
                                    .setStyle(new Style().setBold(true).setColor(TextFormatting.GREEN)));
                            return;
                        }
                    }
            }

            clientChat.addToSentMessages(originalMessage);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void textRenderEvent(RenderGameOverlayEvent.Text event)
    {
        if (keyToPress == null) return;
        if (Minecraft.getInstance().currentScreen instanceof MainMenuScreen)
        {
            event.getLeft().add(new TranslationTextComponent("tapemouse.paused.mainmenu")
                    .setStyle(new Style().setBold(true).setColor(TextFormatting.RED)).getFormattedText());
        }
        else if (Minecraft.getInstance().currentScreen instanceof ChatScreen)
        {
            event.getLeft().add(new TranslationTextComponent("tapemouse.paused.chat")
                    .setStyle(new Style().setBold(true).setColor(TextFormatting.RED)).getFormattedText());
        }
        else
        {
            event.getLeft().add(new TranslationTextComponent("tapemouse.active.info", keyToPress.getLocalizedName(), keyToPress.getKeyDescription().replaceFirst("^key\\.", ""))
                    .setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD)).getFormattedText());

            event.getLeft().add(new TranslationTextComponent("tapemouse.active.delay", i, delay)
                    .setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD)).getFormattedText());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void tickEvent(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START && !event.side.isServer()) return;
        if (Minecraft.getInstance().currentScreen instanceof MainMenuScreen) return;
        if (Minecraft.getInstance().currentScreen instanceof ChatScreen) return;
        if (keyToPress == null) return;
        if (i++ < delay) return;
        i = 0;
        if (delay == 0) KeyBinding.setKeyBindState(keyToPress.getKey(), true);
        //KeyBinding.onTick(keyToPress.getKey());
        if (keyToPress.getKey().getType() == InputMappings.Type.KEYSYM)
        {
            KeyBinding.setKeyBindState(keyToPress.getKey(), true);
            KeyBinding.onTick(keyToPress.getKey());
            KeyBinding.setKeyBindState(keyToPress.getKey(), false);
        }
    }
}
