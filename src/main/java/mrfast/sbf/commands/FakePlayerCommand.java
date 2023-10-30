package mrfast.sbf.commands;

import com.mojang.authlib.GameProfile;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.UUID;

public class FakePlayerCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "fakeplayer";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fakeplayer [playerName]";
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

    EntityOtherPlayerMP fakePlayer = null;
    static Minecraft mc = Utils.GetMC();
    
	@Override
	public void processCommand(ICommandSender arg0, String[] args) throws CommandException {
        if(fakePlayer != null) {
            mc.theWorld.removeEntity(fakePlayer);
        }
        UUID uuid = UUID.fromString(APIUtils.getUUID(args[0],true));
		fakePlayer = new EntityOtherPlayerMP(mc.theWorld, new GameProfile(uuid, args[0]));
        fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer);
        fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead;

        mc.theWorld.addEntityToWorld(-1, fakePlayer);
	}


}
