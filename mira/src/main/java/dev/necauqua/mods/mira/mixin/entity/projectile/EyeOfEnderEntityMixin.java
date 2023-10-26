/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.projectile;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EyeOfEnderEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;

@Mixin(EyeOfEnderEntity.class)
public abstract class EyeOfEnderEntityMixin {

    private Vector3d $cm$originalVelocity = null;
    private Vector3d $cm$velocity = null;

    // scale velocity for collisions (and particles)
    @ModifyVariable(method = "tick", at = @At(value = "STORE", ordinal = 0))
    Vector3d tickScaleVelocity(Vector3d velocity) {
        $cm$originalVelocity = velocity;
        double size = ((ISized) this).getSizeCM();
        return $cm$velocity = velocity.multiply(size, size, size);
    }

    // unscale it back before doing rotations (to avoid precision issues) and
    // velocity updates
    @ModifyVariable(method = "tick", at = @At(value = "LOAD", ordinal = 3), ordinal = 0)
    Vector3d tickUnscaleVelocity(Vector3d velocity) {
        return $cm$originalVelocity;
    }

    // scale back (for particles)
    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/EyeOfEnderEntity;isInWater()Z", shift = Shift.BEFORE))
    Vector3d tickScaleVelocity2(Vector3d velocity) {
        return $cm$velocity;
    }

    // scale the dropped eye
    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addFreshEntity(Lnet/minecraft/entity/Entity;)Z"))
    Entity tick(Entity entity) {
        ((ISized) entity).setSizeCM(((ISized) this).getSizeCM());
        return entity;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData onHit(IParticleData particle) {
        return ScaledParticleData.wrap(particle, ((ISized) this).getSizeCM());
    }

    // portal particles
    @ModifyConstant(method = "tick", constant = {
        @Constant(doubleValue = 0.3),
        @Constant(doubleValue = 0.5),
        @Constant(doubleValue = 0.6),
    })
    double onHit(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }
}
