package net.dries007.tapemouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dries007
 */
public class CommandTapeMouse extends CommandBase
{
    private static final List<KeyBinding> KEYBIND_ARRAY = ReflectionHelper.getPrivateValue(KeyBinding.class, null, "KEYBIND_ARRAY", "field_74516_a");

    @Override
    public String getCommandName()
    {
        return TapeMouse.MODID.toLowerCase();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return '/' + getCommandName() + " [off|keybinding name] [delay] => Use no arguments to get a list of keybindings.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            sender.addChatMessage(new TextComponentString("List of keybindings").setStyle(new Style().setColor(TextFormatting.AQUA)));
            sender.addChatMessage(new TextComponentString("Key => NAME (category)").setStyle(new Style().setColor(TextFormatting.AQUA)));
            for (KeyBinding keyBinding : KEYBIND_ARRAY)
            {
                if (keyBinding == null) continue;
                String name = keyBinding.getKeyDescription();
                if (name == null) continue;
                name = name.replaceFirst("^key\\.", "");

                String cat = keyBinding.getKeyCategory();
                if (cat == null) continue;
                cat = cat.replaceFirst("^key\\.", "");
                cat = cat.replaceFirst("^categories\\.", "");

                sender.addChatMessage(new TextComponentString(keyBinding.getDisplayName() + " => " + name + " (" + cat + ")"));
            }
            return;
        }
        if (args.length > 1) TapeMouse.delay = parseInt(args[1], 0);
        if (args[0].equalsIgnoreCase("off"))
        {
            Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = true;
            TapeMouse.keyBinding = null;
            TapeMouse.i = 0;
            sender.addChatMessage(new TextComponentString("TapeMouse off."));
        }
        else
        {
            for (KeyBinding keyBinding : KEYBIND_ARRAY)
            {
                if (keyBinding == null) continue;
                String name = keyBinding.getKeyDescription();
                if (name == null) continue;
                name = name.replaceFirst("^key\\.", "");
                if (args[0].equalsIgnoreCase(name))
                {
                    Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
                    TapeMouse.keyBinding = keyBinding;
                    sender.addChatMessage(new TextComponentString("TapeMouse on '" + keyBinding.getDisplayName() + "' with delay " + TapeMouse.delay + " ticks."));
                    return;
                }
            }
            throw new CommandException("Unknown keybinding.");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
        {
            List<String> list = new ArrayList<>();
            list.add("off");
            for (KeyBinding keyBinding : KEYBIND_ARRAY)
            {
                if (keyBinding == null) continue;
                String name = keyBinding.getKeyDescription();
                if (name == null) continue;
                name = name.replaceFirst("^key\\.", "");
                list.add(name);
            }
            return getListOfStringsMatchingLastWord(args, list);
        }
        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
