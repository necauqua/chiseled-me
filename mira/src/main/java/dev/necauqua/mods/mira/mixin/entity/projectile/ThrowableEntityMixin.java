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
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ThrowableEntity.class)
public abstract class ThrowableEntityMixin {

    private Vector3d $cm$originalVelocity = null;

    // yay more hardcoded vertical offsets
    @ModifyConstant(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;)V", constant = @Constant(doubleValue = 0.10000000149011612))
    private static double constructor(double constant, EntityType<? extends AbstractArrowEntity> type,
                                      LivingEntity entity) {
        return constant * ((ISized) entity).getSizeCM();
    }

    // scale velocity for collisions and particles
    @ModifyVariable(method = "tick", at = @At("STORE"), ordinal = 0)
    Vector3d tickScaleVelocity(Vector3d velocity) {
        $cm$originalVelocity = velocity;
        double size = ((ISized) this).getSizeCM();
        return velocity.multiply(size, size, size);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData onHit(IParticleData particle) {
        return ScaledParticleData.wrap(particle, ((ISized) this).getSizeCM());
    }

    // unscale it back before setting velocity with drag
    @ModifyVariable(method = "tick", at = @At(value = "LOAD", ordinal = 9), ordinal = 0)
    Vector3d tickUnscaleVelocity(Vector3d velocity) {
        return $cm$originalVelocity;
    }
}
