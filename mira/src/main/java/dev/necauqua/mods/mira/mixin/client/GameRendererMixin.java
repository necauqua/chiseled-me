/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.necauqua.mods.mira.api.IRenderSized;
import dev.necauqua.mods.mira.size.MixinHelpers;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static dev.necauqua.mods.mira.size.MixinHelpers.*;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Unique
    private float $cm$bobbingHack = 1.0f;

    @Shadow
    private boolean renderHand;
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyConstant(method = "getProjectionMatrix", constant = @Constant(floatValue = 0.05f))
    float getProjectionMatrixNearPlane(float nearPlane, ActiveRenderInfo ari, float partialTick) {
//        return nearPlane;
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
//        return renderingNearPatch ? (float) (0.06f / MixinHelpers.getViewerSize()) : farPlane;
        return renderingNearPatch ? 0.06f : farPlane;
    }

    @Inject(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z", shift = Shift.BEFORE))
    void renderDoublePass(float partialTick, long lastTimeNanos, MatrixStack matrixStack, CallbackInfo ci) {
        if (renderingNearPatch) {
            return;
        }
        renderingNearPatch = true;
        renderHand = false;
        minecraft.getProfiler().push("miraNearPass");

        float invSize = (float) (1.0 / MixinHelpers.getViewerSize(partialTick));
        MatrixStack scaled = new MatrixStack();
//        scaled.scale(invSize, invSize, invSize);

        // no idea how tf this works but seems to work.. (aka dumbass heuristic)

        if (invSize > 64) {
            // a linear function with two points:
            //   f(64) = 0.99975586
            //   f(4096) = 0.9
            viewOffsetZLayeringScale = (float) (-2.4741036706349E-5 * invSize + 1.0013392863492);
        }

        // todo:
        //  So I set the far plane above to 0.06 instead of 0.05 so that the little overlap hides any possible holes
        //  between the main near clip and the near patch we're hacking in here
        //  But that does not work too well with transparency
        //  *
        //  The only solution I can think of right now is to render the near patch to a framebuffer,
        //  and then slap it over the main one with the blending configured accordingly

        // main render target here must be our near patch one
        minecraft.getMainRenderTarget().clear(Minecraft.ON_OSX);

        renderLevel(partialTick, lastTimeNanos, scaled);

        viewOffsetZLayeringScale = 0.99975586f;

        minecraft.getProfiler().pop();
        renderHand = true;
        renderingNearPatch = false;

        minecraft.getMainRenderTarget().bindWrite(false);
        MainWindow w = minecraft.getWindow();
        getNearPatchRenderTarget(w).blitToScreen(w.getWidth(), w.getHeight());
    }

//    private static void _blitToScreen(Framebuffer fb, int w, int h, boolean flag) {
//        GlStateManager._colorMask(true, true, true, false);
//        GlStateManager._disableDepthTest();
//        GlStateManager._depthMask(false);
//        GlStateManager._matrixMode(5889);
//        GlStateManager._loadIdentity();
//        GlStateManager._ortho(0.0D, w, h, 0.0D, 1000.0D, 3000.0D);
//        GlStateManager._matrixMode(5888);
//        GlStateManager._loadIdentity();
//        GlStateManager._translatef(0.0F, 0.0F, -2000.0F);
//        GlStateManager._viewport(0, 0, w, h);
//        GlStateManager._enableTexture();
//        GlStateManager._disableLighting();
//        GlStateManager._disableAlphaTest();
//        if (flag) {
//            GlStateManager._disableBlend();
//            GlStateManager._enableColorMaterial();
//        }
//
//        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
//
//        fb.bindRead();
////        GlStateManager._bindTexture(fb.getDepthTextureId());
//
//        float f = (float)w;
//        float f1 = (float)h;
//        float f2 = (float)fb.viewWidth / (float)fb.width;
//        float f3 = (float)fb.viewHeight / (float)fb.height;
//        Tessellator tessellator = RenderSystem.renderThreadTesselator();
//        BufferBuilder bufferbuilder = tessellator.getBuilder();
//        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//        bufferbuilder.vertex(0.0D, f1, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
//        bufferbuilder.vertex(f, f1, 0.0D).uv(f2, 0.0F).color(255, 255, 255, 255).endVertex();
//        bufferbuilder.vertex(f, 0.0D, 0.0D).uv(f2, f3).color(255, 255, 255, 255).endVertex();
//        bufferbuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, f3).color(255, 255, 255, 255).endVertex();
//        tessellator.end();
//        fb.unbindRead();
//        GlStateManager._depthMask(true);
//        GlStateManager._colorMask(true, true, true, true);
//    }

    @Shadow
    public abstract void renderLevel(float p_228378_1_, long p_228378_2_, MatrixStack p_228378_4_);

    // region scale bobbing screen translation

    @Inject(method = "getRenderDistance", at = @At("RETURN"), cancellable = true)
    public void getRenderDistance(CallbackInfoReturnable<Float> cir) {
        if (renderingNearPatch) {
            cir.setReturnValue((float) (cir.getReturnValueF() / MixinHelpers.getViewerSize()));
        }
    }

    @ModifyConstant(method = "pick", constant = {
        @Constant(doubleValue = 1.0),
        @Constant(doubleValue = 3.0),
        @Constant(doubleValue = 6.0),
    })
    double pick(double constant) {
        return constant * MixinHelpers.getViewerSize();
    }

    @ModifyVariable(method = "bobView", at = @At(value = "STORE"), ordinal = 3)
    float bobViewScale(float f2) {
        return f2 * $cm$bobbingHack;
    }

    @ModifyVariable(method = "bobView", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V"), ordinal = 3)
    float bobViewUnScale(float f2) {
        return f2 / $cm$bobbingHack;
    }

    // endregion

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", shift = Shift.BEFORE))
    void renderLevelBeforeBob(float partialTick, long finishTimeNano, MatrixStack matrixStack, CallbackInfo ci) {
        $cm$bobbingHack = (float) MixinHelpers.getViewerSize(partialTick);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", shift = Shift.AFTER))
    void renderLevelAfterBob(float partialTick, long finishTimeNano, MatrixStack matrixStack, CallbackInfo ci) {
        $cm$bobbingHack = 1.0f;
    }
}
