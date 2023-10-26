/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.projectile;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.api.IWorldPreciseEvents;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin {

    @Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;levelEvent(ILnet/minecraft/util/math/BlockPos;I)V"))
    void onImpact(World self, int type, BlockPos blockPos, int data, RayTraceResult result) {
        Vector3d pos = result.getLocation();
        double size = ((ISized) this).getSizeCM();
        if (result instanceof BlockRayTraceResult) {
            Direction dir = ((BlockRayTraceResult) result).getDirection();
            double off = size * 0.5;
            pos.add(dir.getStepX() * off, dir.getStepY() * off, dir.getStepZ() * off);
        }
        ((IWorldPreciseEvents) self).levelEvent(null, type, blockPos, data, size, pos);
    }

    @ModifyVariable(method = "makeAreaOfEffectCloud", at = @At("STORE"))
    AreaEffectCloudEntity makeAreaOfEffectCloud(AreaEffectCloudEntity entity) {
        ((ISized) entity).setSizeCM(((ISized) this).getSizeCM());
        return entity;
    }

    @ModifyConstant(method = "applyWater", constant = {
        @Constant(doubleValue = 2.0),
        @Constant(doubleValue = 4.0),
    })
    double applyWater(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    @ModifyVariable(method = "applyWater", ordinal = 0, at = @At("STORE"))
    double applyWaterSqDist(double variable) {
        double size = ((ISized) this).getSizeCM();
        return variable / size / size;
    }

    @ModifyConstant(method = "applySplash", constant = {
        @Constant(doubleValue = 2.0),
        @Constant(doubleValue = 4.0, ordinal = 0),
        @Constant(doubleValue = 4.0, ordinal = 1),
    })
    double applySplash(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    @ModifyVariable(method = "applySplash", ordinal = 0, at = @At("STORE"))
    double applySplashSqDist(double variable) {
        double size = ((ISized) this).getSizeCM();
        return variable / size / size;
    }
}
