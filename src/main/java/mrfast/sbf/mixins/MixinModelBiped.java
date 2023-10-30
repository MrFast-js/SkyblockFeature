package mrfast.sbf.mixins;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mrfast.sbf.SkyblockFeatures;

/**
 * Taken from OldAnimations under GNU Lesser General Public License v3.0
 * https://github.com/Sk1erLLC/OldAnimations/blob/master/LICENSE
 *
 * @author Sk1erLLC
 */
@Mixin(ModelBiped.class)
public class MixinModelBiped extends ModelBase
{
    @Shadow
    public ModelRenderer bipedRightArm;

    @Inject(method = { "setRotationAngles" }, at = { @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelRenderer;rotateAngleY:F", ordinal = 6, shift = At.Shift.AFTER) })
    private void setRotationAngleY(final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scaleFactor, final Entity entityIn, final CallbackInfo ci) {
        this.bipedRightArm.rotateAngleY = (SkyblockFeatures.config.oldAnimations ? 0.0f : -0.5235988f); // for future, this is the block animation thing
    }
}
