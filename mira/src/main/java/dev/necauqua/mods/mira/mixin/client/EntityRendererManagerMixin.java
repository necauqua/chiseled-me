/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.necauqua.mods.mira.Config;
import dev.necauqua.mods.mira.api.IRenderSized;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRendererManager.class)
public abstract class EntityRendererManagerMixin {

    private static double $cm$sizeStatic = 1.0;
    private double $cm$size = 1.0;

    // shadow radius
    @ModifyVariable(method = "renderShadow", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private static float renderShadowScaleRadius(float radius, MatrixStack matrixStack, IRenderTypeBuffer buffer,
                                                 Entity entity, float strength, float partialTicks) {
        $cm$sizeStatic = ((IRenderSized) entity).getSizeCM(partialTicks);
        return (float) (radius * Math.min($cm$sizeStatic, Config.maxShadowSize.get()));
    }

    // shadow strength
    @ModifyConstant(method = "renderBlockShadow", constant = @Constant(doubleValue = 2.0))
    private static double renderShadowScaleRadius(double constant) {
        return constant * $cm$sizeStatic;
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    Vector3d renderOffset(Vector3d offset, Entity entity, double x, double y, double z, float rotationYaw,
                          float partialTicks, MatrixStack matrixStack) {
        $cm$size = ((IRenderSized) entity).getSizeCM(partialTicks);
        return $cm$size != 1.0 ? offset.multiply($cm$size, $cm$size, $cm$size) : offset;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V", ordinal = 0, shift = Shift.AFTER))
    void renderScale(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks,
                     MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, CallbackInfo ci) {
        float size = (float) $cm$size;
        matrixStack.scale(size, size, size);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V", ordinal = 1, shift = Shift.BEFORE))
    void renderDeScale(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks,
                       MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, CallbackInfo ci) {
        float invSize = (float) (1.0 / $cm$size);
        matrixStack.scale(invSize, invSize, invSize);
    }

    @ModifyVariable(method = "renderFlame", at = @At(value = "STORE"), ordinal = 0)
    float renderFlame(float f) {
        return (float) (f / $cm$size);
    }

    @ModifyConstant(method = "renderHitbox", constant = @Constant(floatValue = 0.01f))
    float renderHitboxEyeBoxFix(float constant) {
        return (float) (constant * $cm$size);
    }

    @ModifyConstant(method = "renderHitbox", constant = @Constant(doubleValue = 2.0))
    double renderHitboxEyeVecFix(double constant) {
        return constant * $cm$size;
    }

    // unscale distance from eyes to feet used in shadow strength calculation
    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 6)
    double renderShadowDistance(double dist) {
        return dist / $cm$size / $cm$size;
    }
}
