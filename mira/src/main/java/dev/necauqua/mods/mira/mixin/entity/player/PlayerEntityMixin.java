/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.player;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Nullable
    private Entity $cm$attacked = null;

    // cloak limits
    @ModifyConstant(method = "moveCloak", constant = {
        @Constant(doubleValue = 10.0),
        @Constant(doubleValue = -10.0),
    })
    double moveCloakChecks(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    // player entity collisions (items etc)
    @ModifyConstant(method = "aiStep", constant = {
        @Constant(doubleValue = 1.0),
        @Constant(doubleValue = 0.5),
    })
    double aiStep(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    // dropped item vertical offset
    @ModifyConstant(method = "drop(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/ItemEntity;", constant = @Constant(doubleValue = 0.30000001192092896))
    double dropItem(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    @Inject(method = "attack", at = @At("HEAD"))
    void attack(Entity entity, CallbackInfo ci) {
        $cm$attacked = entity;
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;sendParticles(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"))
    IParticleData attack(IParticleData particleData) {
        return $cm$attacked != null ? ScaledParticleData.wrap(particleData, ((ISized) $cm$attacked).getSizeCM())
            : particleData;
    }

    // region sweep particle

    @ModifyArg(method = "sweepAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;sendParticles(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"))
    IParticleData sweepAttack(IParticleData particleData) {
        return ScaledParticleData.wrap(particleData, ((ISized) this).getSizeCM());
    }

    @ModifyVariable(method = "sweepAttack", ordinal = 0, at = @At("STORE"))
    double sweepAttackOffsetX(double d0) {
        return d0 * ((ISized) this).getSizeCM();
    }

    @ModifyVariable(method = "sweepAttack", ordinal = 1, at = @At("STORE"))
    double sweepAttackOffsetZ(double d1) {
        return d1 * ((ISized) this).getSizeCM();
    }

    // endregion

    // riding, lul
    @ModifyConstant(method = "getMyRidingOffset", constant = @Constant(doubleValue = -0.35))
    double getMyRidingOffset(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }
}
