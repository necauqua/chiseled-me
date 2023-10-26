/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.projectile;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;

@Mixin(AbstractArrowEntity.class)
public abstract class AbstractArrowEntityMixin {

    private double $cm$size = 1.0;
    private double $cm$originalVx = 0.0;
    private double $cm$originalVy = 0.0;
    private double $cm$originalVz = 0.0;

    // yay more hardcoded vertical offsets
    @ModifyConstant(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;)V", constant = @Constant(doubleValue = 0.10000000149011612))
    private static double constructor(double constant, EntityType<? extends AbstractArrowEntity> type,
                                      LivingEntity entity) {
        return constant * ((ISized) entity).getSizeCM();
    }

    // region arrow collision
    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/vector/Vector3d;add(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;"))
    Vector3d tickCollision(Vector3d velocity) {
        $cm$size = ((ISized) this).getSizeCM();
        return velocity.multiply($cm$size, $cm$size, $cm$size);
    }

    @ModifyConstant(method = "shouldFall", constant = @Constant(doubleValue = 0.06))
    double shouldFall(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    @ModifyConstant(method = "onHitBlock", constant = @Constant(doubleValue = 0.05000000074505806))
    double onHitBlock(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    @ModifyVariable(method = "onHitBlock", at = @At("STORE"), ordinal = 0)
    Vector3d onHitBlock(Vector3d velocity) {
        double invSize = 1 / ((ISized) this).getSizeCM();
        return velocity.multiply(invSize, invSize, invSize);
    }

    // endregion

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData onHit(IParticleData particle) {
        return ScaledParticleData.wrap(particle, ((ISized) this).getSizeCM());
    }

    // scale velocity for particles
    @ModifyVariable(method = "tick", at = @At("STORE"), ordinal = 0)
    double tickX1(double vx) {
        $cm$originalVx = vx;
        return vx * $cm$size;
    }

    @ModifyVariable(method = "tick", at = @At("STORE"), ordinal = 1)
    double tickY1(double vy) {
        $cm$originalVy = vy;
        return vy * $cm$size;
    }

    @ModifyVariable(method = "tick", at = @At("STORE"), ordinal = 2)
    double tickZ1(double vz) {
        $cm$originalVz = vz;
        return vz * $cm$size;
    }

    // (precisely) undo scaling for rotation
    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sqrt(D)F", ordinal = 1, shift = Shift.BEFORE), ordinal = 0)
    double tickX2(double vx) {
        return $cm$originalVx;
    }

    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sqrt(D)F", ordinal = 1, shift = Shift.BEFORE), ordinal = 1)
    double tickY2(double vy) {
        return $cm$originalVy;
    }

    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sqrt(D)F", ordinal = 1, shift = Shift.BEFORE), ordinal = 2)
    double tickZ2(double vz) {
        return $cm$originalVz;
    }

    // scale again for more particles
    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/AbstractArrowEntity;isInWater()Z", shift = Shift.BEFORE), ordinal = 0)
    double tickX3(double vx) {
        return vx * $cm$size;
    }

    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/AbstractArrowEntity;isInWater()Z", shift = Shift.BEFORE), ordinal = 1)
    double tickY3(double vy) {
        return vy * $cm$size;
    }

    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/AbstractArrowEntity;isInWater()Z", shift = Shift.BEFORE), ordinal = 2)
    double tickZ3(double vz) {
        return vz * $cm$size;
    }
}
