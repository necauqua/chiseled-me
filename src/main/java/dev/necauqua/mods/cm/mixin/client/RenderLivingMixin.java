/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderLiving.class)
public abstract class RenderLivingMixin {

    private double $cm$self, $cm$coarseSelf, $cm$holder;

    // undo the scaling of the leash

    @ModifyConstant(method = "renderLeash", constant = @Constant(doubleValue = 1.6))
    double renderLeashHeight(double constant, EntityLiving entityLiving, double x, double y, double z, float entityYaw, float partialTicks) {
        $cm$coarseSelf = ((IRenderSized) entityLiving).getSizeCM();
        $cm$self = ((IRenderSized) entityLiving).getSizeCM(partialTicks);
        $cm$holder = ((IRenderSized) entityLiving.getLeashHolder()).getSizeCM(partialTicks);

        return constant - entityLiving.height + entityLiving.height / $cm$coarseSelf * $cm$self;
    }

    @ModifyVariable(method = "renderLeash", ordinal = 14, at = @At("STORE"))
    double renderLeashHeightFix(double variable, EntityLiving entityLiving) {
        return variable - entityLiving.height / $cm$coarseSelf * $cm$self;
    }

    @ModifyConstant(method = "renderLeash", constant = @Constant(doubleValue = 0.4))
    double renderLeashWidth(double constant, EntityLiving entityLiving) {
        return constant * $cm$self / $cm$coarseSelf;
    }

    @ModifyVariable(method = "renderLeash", ordinal = 16, at = @At("STORE"))
    double renderLeashX(double variable) {
        return variable / $cm$self;
    }

    @ModifyVariable(method = "renderLeash", ordinal = 17, at = @At("STORE"))
    double renderLeashY(double variable) {
        return variable / $cm$self;
    }

    @ModifyVariable(method = "renderLeash", ordinal = 18, at = @At("STORE"))
    double renderLeashZ(double variable) {
        return variable / $cm$self;
    }

    @ModifyConstant(method = "renderLeash", constant = @Constant(doubleValue = 0.025))
    double renderLeashScale(double constant) {
        return constant / $cm$self * $cm$holder;
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
        return constant * $cm$holder;
    }
}
