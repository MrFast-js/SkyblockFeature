package mrfast.sbf.gui.components;

import gg.essential.elementa.UIComponent;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.state.BasicState;
import gg.essential.elementa.state.State;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import mrfast.sbf.gui.ProfileViewerGui;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackComponent extends UIComponent {
    private final State<ItemStack> state;

    public ItemStackComponent(ItemStack stack) {
        this(new BasicState<>(stack));
    }

    public ItemStackComponent(Item item, int metadata) {
        this(new ItemStack(item, 1, metadata));
    }

    public ItemStackComponent(State<ItemStack> state) {
        this.state = state;
    }

    @Override
    public void draw(UMatrixStack matrixStack) {
        ItemStack item = this.state.get();
        if (item == null) return;

        beforeDraw(matrixStack);
        super.draw(matrixStack);

        if (getWidth() == 0) setWidth(new PixelConstraint(16f));
        if (getHeight() == 0) setHeight(new PixelConstraint(16f));

        matrixStack.push();
        matrixStack.translate(getLeft(), getTop(), 100f);
        matrixStack.scale(getWidth() / 16f, getHeight() / 16f, 0f);

        UGraphics.color4f(1f, 1f, 1f, 1f);
        UGraphics.disableLighting();

        matrixStack.runWithGlobalState(() -> {
            RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
            RenderHelper.enableGUIStandardItemLighting();
            itemRender.zLevel = -145; // Negates the z-offset of the below method.
            itemRender.renderItemIntoGUI(item, 0, 0);
            itemRender.renderItemOverlays(Utils.GetMC().fontRendererObj, item, 0, 0);
            RenderHelper.disableStandardItemLighting();
        });

        UGraphics.disableLighting();
        matrixStack.pop();

        if (!item.hasDisplayName()) return;
        if (item.getDisplayName().trim().isEmpty()) return;
        
        if (this.isHovered()) {
            ProfileViewerGui.renderTooltip = item.getTooltip(Utils.GetMC().thePlayer, false);
        }
    }
}