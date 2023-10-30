package mrfast.sbf.commands;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import gg.essential.api.utils.GuiUtil;
import mrfast.sbf.gui.ProfileViewerGui;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.List;

public class pvCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "sfpv";
    }

    @Override
    public List<String> getCommandAliases() {
        return Lists.newArrayList("sfpv","pv");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return (args.length >= 1) ? getListOfStringsMatchingLastWord(args, Utils.getListOfPlayerUsernames()) : null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            String username = Utils.GetMC().thePlayer.getName();

            Utils.SendMessage(ChatFormatting.YELLOW+"Opening "+username+"'s Profile "+ChatFormatting.GRAY+"(This may take a second)");
            GuiUtil.open(new ProfileViewerGui(true,username,"auto"));
        } else {
            String playerUuid = APIUtils.getUUID(args[0]);
            if(playerUuid==null) {
                Utils.SendMessage(ChatFormatting.RED+"This player doesn't exist or has never played Skyblock");
                return;
            }
            Utils.SendMessage(ChatFormatting.YELLOW+"Opening "+args[0]+"'s Profile "+ChatFormatting.GRAY+"(This may take a second)");
            GuiUtil.open(new ProfileViewerGui(true,args[0],"auto"));
        }
    }
}