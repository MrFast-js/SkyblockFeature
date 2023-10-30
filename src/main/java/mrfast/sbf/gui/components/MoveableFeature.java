package mrfast.sbf.gui.components;

import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;

public class MoveableFeature extends GuiButton {

    public UIElement element;
    public float x;
    public float y;
    public float Width;
    public float Height;

    public MoveableFeature(UIElement element) {
        super(-1, 0, 0, null);
        this.element = element;
        updateLocations();
    }

    public MoveableFeature(int buttonId, UIElement element) {
        super(-1, 0, 0, null);
        this.element = element;
    }

    private void updateLocations() {
        x = element.getX();
        y = element.getY();
        Width = element.getWidth();
        Height = element.getHeight();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        updateLocations();
        ScaledResolution sr = new ScaledResolution(mc);

        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        float actualX = screenWidth*x;
        float actualY = screenHeight*y;
        float xWidth = actualX+element.getWidth()+4;
        float yHeight = actualY+element.getHeight()+4;

        hovered = mouseX >= actualX && mouseY >= actualY && mouseX < xWidth && mouseY < yHeight;
        Color c = new Color(255, 255, 255, hovered ? 100 : 40);
        Utils.drawGraySquare(0, 0, element.getWidth() + 4, element.getHeight() + 4, 3, c);

        GlStateManager.pushMatrix();
        GlStateManager.translate(2, 2, 0);
        element.drawElementExample();
        GlStateManager.translate(-2, -2, 0);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return enabled && visible && hovered;
    }

    public UIElement getElement() {
        return element;
    }
}