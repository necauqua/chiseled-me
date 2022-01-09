/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.theta.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.necauqua.mods.theta.api.IRenderSized;
import dev.necauqua.mods.theta.size.MixinHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.necauqua.mods.theta.size.MixinHelpers.renderingNearPatch;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    private boolean renderHand;
    private float $cm$bobbingHack = 1.0f;

    @ModifyConstant(method = "getProjectionMatrix", constant = @Constant(floatValue = 0.05f))
    float getProjectionMatrixNearPlane(float nearPlane, ActiveRenderInfo ari, float partialTick) {
        if (!renderingNearPatch) {
            return nearPlane;
        }
        Entity view = minecraft.getCameraEntity();
        assert view != null;
        double size = ((IRenderSized) view).getSizeCM(partialTick);
        return size < 1.0f ? nearPlane * (float) size : nearPlane;
    }

    @ModifyArg(method = "getProjectionMatrix", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/vector/Matrix4f;perspective(DFFF)Lnet/minecraft/util/math/vector/Matrix4f;"), index = 3)
    float getProjectionMatrixFarPlane(float farPlane) {
        return renderingNearPatch ? 0.06f : farPlane;
    }

    @Inject(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z", shift = Shift.BEFORE))
    void renderDoublePass(float partialTick, long lastTimeNanos, MatrixStack matrixStack, CallbackInfo ci) {
        if (renderingNearPatch) {
            return;
        }
        renderingNearPatch = true;
        renderHand = false;
        minecraft.getProfiler().push("thetaNearPass");
        renderLevel(partialTick, lastTimeNanos, new MatrixStack());
        minecraft.getProfiler().pop();
        renderHand = true;
        renderingNearPatch = false;
    }

    @ModifyConstant(method = "pick", constant = {
        @Constant(doubleValue = 1.0),
        @Constant(doubleValue = 3.0),
        @Constant(doubleValue = 6.0),
    })
    double pick(double constant) {
        return constant * MixinHelpers.getViewerSize();
    }

    // region scale bobbing screen translation

    @ModifyVariable(method = "bobView", at = @At(value = "STORE"), ordinal = 3)
    float bobViewScale(float f2) {
        return f2 * $cm$bobbingHack;
    }

    @ModifyVariable(method = "bobView", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V"), ordinal = 3)
    float bobViewUnScale(float f2) {
        return f2 / $cm$bobbingHack;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", shift = Shift.BEFORE))
    void renderLevelBeforeBob(float partialTick, long finishTimeNano, MatrixStack matrixStack, CallbackInfo ci) {
        $cm$bobbingHack = (float) MixinHelpers.getViewerSize(partialTick);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", shift = Shift.AFTER))
    void renderLevelAfterBob(float partialTick, long finishTimeNano, MatrixStack matrixStack, CallbackInfo ci) {
        $cm$bobbingHack = 1.0f;
    }

    // endregion

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void renderLevel(float p_228378_1_, long p_228378_2_, MatrixStack p_228378_4_);
}
