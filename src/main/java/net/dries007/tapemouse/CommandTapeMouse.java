package net.dries007.tapemouse;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.util.List;

/**
 * @author Dries007
 */
public class CommandTapeMouse extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "tapemouse";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/tapemouse <left|right|off> [delay]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length == 0) throw new WrongUsageException(getCommandUsage(sender));
        if (args.length > 1) TapeMouse.delay = parseIntWithMin(sender, args[1], 0);
        switch (args[0])
        {
            case "off":
                Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = true;
                TapeMouse.state = TapeMouse.State.DISABLE;
                TapeMouse.i = 0;
                sender.addChatMessage(new ChatComponentText("TapeMouse off."));
                break;
            case "left":
                Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
                TapeMouse.state = TapeMouse.State.LEFT;
                sender.addChatMessage(new ChatComponentText("TapeMouse on: left with delay: " + TapeMouse.delay));
                break;
            case "right":
                Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
                TapeMouse.state = TapeMouse.State.RIGHT;
                sender.addChatMessage(new ChatComponentText("TapeMouse on: right with delay: " + TapeMouse.delay));
                break;
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] ars)
    {
        return getListOfStringsMatchingLastWord(ars, "off", "left", "right");
    }
}
