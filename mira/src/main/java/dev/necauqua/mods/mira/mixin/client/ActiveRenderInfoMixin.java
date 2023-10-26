/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.api.IRenderSized;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ActiveRenderInfo.class)
public abstract class ActiveRenderInfoMixin {

    private float $cm$size = 1.0f;

    // scale 3rd person camera distance
    @ModifyConstant(method = "setup", constant = @Constant(doubleValue = 4.0))
    double setupMaxDetachedDistance(double constant, IBlockReader blockReader, Entity viewEntity, boolean detached,
                                    boolean mirror, float partialTicks) {
        double size = ((IRenderSized) viewEntity).getSizeCM(partialTicks);
        $cm$size = (float) size;
        return constant * size;
    }

    // fix 3rd person camera raytracing
    @ModifyConstant(method = "getMaxZoom", constant = @Constant(floatValue = 0.1F))
    float getMaxZoomStartOffset(float constant) {
        return constant * $cm$size;
    }

    // some sleeping offset, todo look wtf is this
    @ModifyConstant(method = "setup", constant = @Constant(doubleValue = 0.3))
    double setupSleepingOffset(double constant, IBlockReader blockReader, Entity viewEntity, boolean detached,
                               boolean mirror, float partialTicks) {
        return constant * ((IRenderSized) viewEntity).getSizeCM(partialTicks);
    }
}
