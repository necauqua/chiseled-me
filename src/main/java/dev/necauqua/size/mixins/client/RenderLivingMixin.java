package dev.necauqua.size.mixins.client;

import dev.necauqua.size.api.IRenderSized;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import static dev.necauqua.size.EntitySizeInteractions.getViewerSize;

@Mixin(RenderLiving.class)
public abstract class RenderLivingMixin {

    private double $size$self, $size$coarseSelf, $size$holder;

    // undo the scaling of the leash

    @ModifyConstant(method = "renderLeash", constant = @Constant(doubleValue = 1.6))
    double renderLeashHeight(double constant, EntityLiving entityLiving, double x, double y, double z, float entityYaw, float partialTicks) {
        $size$coarseSelf = ((IRenderSized) entityLiving).getSize();
        $size$self = ((IRenderSized) entityLiving).getSize(partialTicks);
        $size$holder = ((IRenderSized) entityLiving.getLeashHolder()).getSize(partialTicks);

        return constant - entityLiving.height + entityLiving.height / $size$coarseSelf * $size$self;
    }

    @ModifyVariable(method = "renderLeash", ordinal = 14, at = @At("STORE"))
    double renderLeashHeightFix(double variable, EntityLiving entityLiving) {
        return variable - entityLiving.height / $size$coarseSelf * $size$self;
    }

    @ModifyConstant(method = "renderLeash", constant = @Constant(doubleValue = 0.4))
    double renderLeashWidth(double constant, EntityLiving entityLiving) {
        return constant * $size$self / $size$coarseSelf;
    }

    @ModifyVariable(method = "renderLeash", ordinal = 16, at = @At("STORE"))
    double renderLeashX(double variable) {
        return variable / $size$self;
    }

    @ModifyVariable(method = "renderLeash", ordinal = 17, at = @At("STORE"))
    double renderLeashY(double variable) {
        return variable / $size$self;
    }

    @ModifyVariable(method = "renderLeash", ordinal = 18, at = @At("STORE"))
    double renderLeashZ(double variable) {
        return variable / $size$self;
    }

    @ModifyConstant(method = "renderLeash", constant = @Constant(doubleValue = 0.025))
    double renderLeashScale(double constant) {
        return constant / $size$self * $size$holder;
    }

    @ModifyConstant(method = "renderLeash", constant = {
            @Constant(doubleValue = 0.25),
            @Constant(doubleValue = 0.7, ordinal = 0),
            @Constant(doubleValue = 0.7, ordinal = 3), // except second and third 0.7

            @Constant(doubleValue = 0.5, ordinal = 1), // except first 0.5
            @Constant(doubleValue = 0.5, ordinal = 2),
            @Constant(doubleValue = 0.5, ordinal = 3),
    })
    double renderLeashHolderOffsets(double constant) {
        return constant * $size$holder;
    }
}
