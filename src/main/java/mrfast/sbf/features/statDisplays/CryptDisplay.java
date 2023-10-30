package mrfast.sbf.features.statDisplays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.TabListUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CryptDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new cryptDisplayGUI();
    }

    public static class cryptDisplayGUI extends UIElement {

        public cryptDisplayGUI() {
            super("Crypt Display", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        private static final Pattern cryptsPattern = Pattern.compile("§r Crypts: §r§6(?<crypts>\\d+)§r");
        
        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inDungeons) return;
            int crypts = 0;
            for (NetworkPlayerInfo pi : TabListUtils.getTabEntries()) {
                try {
                    String name = mc.ingameGUI.getTabList().getPlayerName(pi);
                    if (name.contains("Crypts:")) {
                        Matcher matcher = cryptsPattern.matcher(name);
                        if (matcher.find()) {
                            crypts = Integer.parseInt(matcher.group("crypts"));
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }

            int color = crypts >= 5?0x55FF55:0xeb4034;

            float scale = 2f;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                GlStateManager.scale(scale, scale, 0);
                Utils.GetMC().fontRendererObj.drawStringWithShadow("Crypts: "+crypts, 0, 0, color);
                GlStateManager.scale(1/scale, 1/scale, 0);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;

            int color = 0xeb4034;
            float scale = 2f;
            GlStateManager.scale(scale, scale, 0);
            Utils.GetMC().fontRendererObj.drawStringWithShadow("Crypts: 2", 0, 0, color);
            GlStateManager.scale(1/scale, 1/scale, 0);
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.cryptCount;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*2;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("§6Estimated Secret C");
        }
    }
}
