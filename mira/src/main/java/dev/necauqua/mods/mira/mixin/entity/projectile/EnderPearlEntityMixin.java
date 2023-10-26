/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.projectile;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.particles.IParticleData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin {

    // scale spawned endermites
    @ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addFreshEntity(Lnet/minecraft/entity/Entity;)Z"))
    Entity onHit(Entity entity) {
        ((ISized) entity).setSizeCM(((ISized) this).getSizeCM());
        return entity;
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData onHit(IParticleData particle) {
        return ScaledParticleData.wrap(particle, ((ISized) this).getSizeCM());
    }

    // particles vertical offset
    @ModifyConstant(method = "onHit", constant = @Constant(doubleValue = 2.0))
    double onHit(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }
}
