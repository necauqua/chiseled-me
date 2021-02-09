/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.necauqua.mods.cm.size.EntitySizeInteractions.getViewerSize;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    private float $cm$bobbingHack = 1.0f;

    // camera height
    @ModifyVariable(method = "orientCamera", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    float orientCamera(float f, float partialTicks) {
        assert mc.getRenderViewEntity() != null;
        double tickSize = ((IRenderSized) mc.getRenderViewEntity()).getSizeCM();
        return (float) (f / tickSize * getViewerSize(partialTicks));
    }

    // 3rd person view raytracing fix for VanillaFix
    @ModifyVariable(method = "orientCamera", at = @At("STORE"))
    RayTraceResult orientCameraVanillaFix(RayTraceResult result) {
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS ? result : null;
    }

    // default (when not facing a wall) third person distance
    @ModifyVariable(method = "orientCamera", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
    double orientCamera(double d3, float partialTicks) {
        return d3 * getViewerSize(partialTicks);
    }

    @ModifyConstant(method = "orientCamera", constant = {
            @Constant(floatValue = 0.1f), // third person distance
            @Constant(floatValue = 0.05f), // small first person shift
    })
    float orientCameraConstants(float constant, float partialTicks) {
        return (float) (constant * getViewerSize(partialTicks));
    }

    @Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;applyBobbing(F)V"))
    void setupCameraTransformBeforeBobbing(float partialTicks, int pass, CallbackInfo ci) {
        $cm$bobbingHack = (float) getViewerSize(partialTicks);
        // we need a state boolean anyway, might as well cache the size idk
        // because the float field is not necessary as we use the size of the viewing entity
        // but we need to multiply/divide by it only in one `applyBobbing` call, this one
    }

    @Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/renderer/EntityRenderer;applyBobbing(F)V"))
    void setupCameraTransformAfterBobbing(float partialTicks, int pass, CallbackInfo ci) {
        $cm$bobbingHack = 1.0f;
    }

    @ModifyVariable(method = "applyBobbing", ordinal = 3, at = @At(value = "LOAD", ordinal = 0))
    float applyBobbing(float f, float partialTicks) {
        return f * $cm$bobbingHack;
    }

    @ModifyVariable(method = "applyBobbing", ordinal = 3, at = @At(value = "LOAD", ordinal = 2))
    float applyBobbingReset(float f, float partialTicks) {
        return f / $cm$bobbingHack;
    }

    // optional to allow betterfps to disable fog
    @ModifyVariable(method = "setupFog", ordinal = 2, at = @At("STORE"), require = 0, expect = 0)
    float setupFog(float fog, int startCoords, float partialTicks) {
        return (float) (fog * Math.sqrt(getViewerSize(partialTicks)));
    }

    @ModifyConstant(method = "getMouseOver", constant = {
            @Constant(doubleValue = 3.0),
            @Constant(doubleValue = 6.0), // this LCD 6.0 might or might not exist if some sort of astral sorcery also patches reach
            @Constant(doubleValue = 1.0),
    })
    double getMouseOver(double constant, float partialTicks) {
        return constant * getViewerSize(partialTicks);
    }

    // region clipping

    @ModifyConstant(method = "setupCameraTransform", constant = @Constant(floatValue = 0.05f))
    float setupCameraTransform(float constant, float partialTicks) {
        double size = getViewerSize(partialTicks);
        return size < 1.0 ? (float) (constant * size) : constant;
    }

    @ModifyConstant(method = "renderWorldPass", constant = @Constant(floatValue = 0.05f))
    float renderWorldPass(float constant, int pass, float partialTicks) {
        double size = getViewerSize(partialTicks);
        return size < 1.0 ? (float) (constant * size) : constant;
    }

    @ModifyConstant(method = "renderCloudsCheck", constant = @Constant(floatValue = 0.05f))
    float renderCloudsCheck(float constant, RenderGlobal renderGlobalIn, float partialTicks) {
        double size = getViewerSize(partialTicks);
        return size < 1.0 ? (float) (constant * size) : constant;
    }

    @Group(name = "renderHand", min = 1, max = 1)
    @ModifyConstant(method = "renderHand", constant = @Constant(floatValue = 0.05f), require = 0, expect = 0)
    float renderHand(float constant, float partialTicks) {
        double size = getViewerSize(partialTicks);
        return size < 1.0 ? (float) (constant * size) : constant;
    }

    @Dynamic("optifine redirect")
    @SuppressWarnings("target")
    @Group(name = "renderHand", min = 1, max = 1)
    @ModifyConstant(method = "renderHand(FIZZZ)V", remap = false, constant = @Constant(floatValue = 0.05f), require = 0, expect = 0)
    float renderHandOptifine(float constant, float partialTicks) {
        double size = getViewerSize(partialTicks);
        return size < 1.0 ? (float) (constant * size) : constant;
    }

    // endregion

    @Shadow
    @Final
    private Minecraft mc;
}
