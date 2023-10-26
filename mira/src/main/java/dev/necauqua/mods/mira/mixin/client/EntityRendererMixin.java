/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.api.IRenderSized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    // entity height is scaled but so is the model matrix here, undo the first one
    @ModifyVariable(method = "renderNameTag", at = @At("STORE"), ordinal = 0)
    float renderNameTagUnscaleHeight(float f1, Entity entity) {
        return (float) ((f1 - 0.5f) / ((IRenderSized) entity).getSizeCM(Minecraft.getInstance().getDeltaFrameTime()))
            + 0.5f;
    }
}
