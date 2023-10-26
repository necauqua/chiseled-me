/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity;

import dev.necauqua.mods.mira.Config;
import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import dev.necauqua.mods.mira.size.SizedReachAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraftforge.common.ForgeMod.REACH_DISTANCE;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    // limb swing speed
    @ModifyConstant(method = "calculateEntityAnimation", constant = @Constant(floatValue = 4.0f))
    float calculateEntityAnimationDist(float dist) {
        return (float) (dist / ((ISized) this).getSizeCM());
    }

    // that little rotation animation
    @ModifyVariable(method = "tick", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    float onUpdate(float constant) {
        double size = ((ISized) this).getSizeCM();
        return (float) (constant / size / size);
    }

    // universal reach
    @Inject(method = "getAttribute", at = @At("RETURN"), cancellable = true)
    void getEntityAttribute(Attribute attribute, CallbackInfoReturnable<ModifiableAttributeInstance> cir) {
        if (attribute == REACH_DISTANCE.get()) {
            cir.setReturnValue(new SizedReachAttribute(cir.getReturnValue(), (ISized) this));
        }
    }

    // scale XP orbs
    @ModifyArg(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addFreshEntity(Lnet/minecraft/entity/Entity;)Z"))
    Entity onDeathUpdate(Entity entity) {
        ((ISized) entity).setSizeCM(((ISized) this).getSizeCM());
        return entity;
    }

    // vertical fluid collision offset
    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.6000000238418579))
    double travel(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    // damage scaling
    @ModifyVariable(method = "hurt", ordinal = 0, at = @At("HEAD"), argsOnly = true)
    float hurt(float amount, DamageSource source) {
        Entity attacker = source.getDirectEntity();
        // scaling only entity-to-entity damage
        if (attacker == null) {
            return amount;
        }
        double size = ((ISized) this).getSizeCM();
        double attackerSize = ((ISized) attacker).getSizeCM();
        if (Config.scaleDamageDealtSmall.get() && attackerSize < 1.0
            || Config.scaleDamageDealtBig.get() && attackerSize > 1.0) {
            amount *= attackerSize;
        }
        if (Config.scaleDamageReceivedSmall.get() && size < 1.0 || Config.scaleDamageReceivedBig.get() && size > 1.0) {
            amount /= size;
        }
        return amount;
    }

    // knockback
    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V"))
    void attackEntityFrom(LivingEntity self, float strength, double xRatio, double zRatio, DamageSource source) {
        Entity attacker = source.getEntity();
        assert attacker != null; // never null here
        double size = ((ISized) this).getSizeCM();
        if (Config.scaleMassSmall.get() && size < 1.0 || Config.scaleMassBig.get() && size > 1.0) {
            strength = (float) (strength * ((ISized) attacker).getSizeCM() / size);
        }
        self.knockback(strength, xRatio, zRatio);
    }

    // shield knockback
    @ModifyConstant(method = "blockedByShield", constant = @Constant(floatValue = 0.5f))
    float blockUsingShield(float constant, LivingEntity attacker) {
        double size = ((ISized) this).getSizeCM();
        return Config.scaleMassSmall.get() && size < 1.0 || Config.scaleMassBig.get() && size > 1.0
            ? (float) (constant * size / ((ISized) attacker).getSizeCM())
            : constant;
    }

    // region particles

    @ModifyVariable(method = "baseTick", ordinal = 0, at = @At(value = "STORE", ordinal = 1))
    double baseTickWaterBubbleX(double d2) {
        return d2 * ((ISized) this).getSizeCM();
    }

    @ModifyVariable(method = "baseTick", ordinal = 1, at = @At(value = "STORE", ordinal = 1))
    double baseTickWaterBubbleY(double d3) {
        return d3 * ((ISized) this).getSizeCM();
    }

    @ModifyVariable(method = "baseTick", ordinal = 2, at = @At(value = "STORE"))
    double baseTickWaterBubbleZ(double d4) {
        return d4 * ((ISized) this).getSizeCM();
    }

    @ModifyArg(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData createRunningParticles(IParticleData data) {
        return ScaledParticleData.wrap(data, ((ISized) this).getSizeCM());
    }

    @ModifyArg(method = "tickDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData tickDeath(IParticleData data) {
        return ScaledParticleData.wrap(data, ((ISized) this).getSizeCM());
    }

    @ModifyConstant(method = "spawnSoulSpeedParticle", constant = @Constant(doubleValue = 0.1))
    double blockUsingShield(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    @ModifyArg(method = "spawnSoulSpeedParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData spawnSoulSpeedParticle(IParticleData data) {
        return ScaledParticleData.wrap(data, ((ISized) this).getSizeCM());
    }

    @ModifyArg(method = "tickEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData tickEffects(IParticleData data) {
        return ScaledParticleData.wrap(data, ((ISized) this).getSizeCM());
    }

    @ModifyArg(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;sendParticles(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"))
    IParticleData checkFallDamage(IParticleData data) {
        return ScaledParticleData.wrap(data, ((ISized) this).getSizeCM());
    }

    @ModifyVariable(method = "spawnItemParticles", ordinal = 1, at = @At(value = "STORE", ordinal = 0))
    Vector3d spawnItemParticles(Vector3d vector3d1) {
        return vector3d1.scale(((ISized) this).getSizeCM());
    }

    @ModifyArg(method = "spawnItemParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;sendParticles(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"))
    IParticleData spawnItemParticles(IParticleData data) {
        return ScaledParticleData.wrap(data, ((ISized) this).getSizeCM());
    }

    @ModifyArg(method = "spawnItemParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData spawnItemParticlesClient(IParticleData data) {
        return ScaledParticleData.wrap(data, ((ISized) this).getSizeCM());
    }

    // endregion
}
