/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.api.IRenderSized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OverlayRenderer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(OverlayRenderer.class)
public abstract class OverlayRendererMixin {

    @ModifyConstant(method = "getOverlayBlock", remap = false, constant = @Constant(floatValue = 0.1f))
    private static float getOverlayBlockVerticalOffset(float offset, PlayerEntity player) {
        return (float) (offset * ((IRenderSized) player).getSizeCM(Minecraft.getInstance().getDeltaFrameTime()));
    }
}
