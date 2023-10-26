/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.api.IRenderSized;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.entity.monster.GuardianEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuardianRenderer.class)
public abstract class GuardianRendererMixin {

    // undo the scaling of the guardian beam
    @ModifyVariable(method = "render(Lnet/minecraft/entity/monster/GuardianEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", ordinal = 6, at = @At(value = "LOAD", ordinal = 0))
    float doRender(float f4, GuardianEntity entity, float entityYaw, float partialTicks) {
        return (float) (f4 / ((IRenderSized) entity).getSizeCM(partialTicks));
    }
}
