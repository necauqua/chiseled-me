/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.theta.mixin.entity;

import dev.necauqua.mods.theta.api.ISized;
import dev.necauqua.mods.theta.size.ScaledParticleData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityClientMixin {

    @ModifyArg(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData attemptTeleport(IParticleData data) {
        return ScaledParticleData.wrap(data, ((ISized) this).getSizeCM());
    }
}
