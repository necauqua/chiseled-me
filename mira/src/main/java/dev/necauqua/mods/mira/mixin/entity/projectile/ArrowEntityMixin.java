/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.projectile;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.particles.IParticleData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin {

    @ModifyArg(method = "makeParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData onHit(IParticleData particle) {
        return ScaledParticleData.wrap(particle, ((ISized) this).getSizeCM());
    }
}
