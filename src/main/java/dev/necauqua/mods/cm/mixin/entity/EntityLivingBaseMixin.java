/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import dev.necauqua.mods.cm.size.SizedReachAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends EntityMixin {

    // universal reach
    @Inject(method = "getEntityAttribute", at = @At("RETURN"), cancellable = true)
    void getEntityAttribute(IAttribute attribute, CallbackInfoReturnable<IAttributeInstance> cir) {
        if (attribute == EntityPlayer.REACH_DISTANCE) {
            cir.setReturnValue(new SizedReachAttribute(cir.getReturnValue(), this));
        }
    }

    // knockback
    @Redirect(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;knockBack(Lnet/minecraft/entity/Entity;FDD)V"))
    void attackEntityFrom(EntityLivingBase self, Entity attacker, float strength, double xRatio, double zRatio) {
        self.knockBack(attacker, (float) (strength * ((ISized) attacker).getSizeCM() / $cm$size), xRatio, zRatio);
    }

    // shield reverse knockback
    @ModifyConstant(method = "blockUsingShield", constant = @Constant(floatValue = 0.5f))
    float blockUsingShield(float constant, EntityLivingBase attacker) {
        return (float) (constant * $cm$size / ((ISized) attacker).getSizeCM());
    }

    // xp orbs spawn at living death
    @ModifyArg(method = "onDeathUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    Entity onDeathUpdate(Entity entity) {
        ((ISized) entity).setSizeCM($cm$size);
        return entity;
    }

    // vertical fluid collision offset
    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.6000000238418579))
    double travel(double constant) {
        return constant * $cm$size;
    }

    // limb swing animation
    @ModifyConstant(method = "travel", constant = @Constant(floatValue = 4.0f))
    float travelDiv(float constant) {
        return (float) (constant / $cm$size);
    }

    // that little rotation animation
    @ModifyVariable(method = "onUpdate", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    float onUpdate(float constant) {
        return (float) (constant / $cm$size / $cm$size);
    }

    // region particles

    @ModifyVariable(method = "onEntityUpdate", ordinal = 0, at = @At("STORE"))
    float onEntityUpdateWaterBubbleX(float f2) {
        return (float) (f2 * $cm$size);
    }

    @ModifyVariable(method = "onEntityUpdate", ordinal = 1, at = @At("STORE"))
    float onEntityUpdateWaterBubbleY(float f) {
        return (float) (f * $cm$size);
    }

    @ModifyVariable(method = "onEntityUpdate", ordinal = 2, at = @At("STORE"))
    float onEntityUpdateWaterBubbleZ(float f1) {
        return (float) (f1 * $cm$size);
    }

    @ModifyArg(method = "onEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] createRunningParticles(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @ModifyArg(method = "onDeathUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onDeathUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @ModifyArg(method = "updateFallState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"))
    int[] updateFallState(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @ModifyArg(method = "updatePotionEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] updatePotionEffects(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @ModifyArg(method = "attemptTeleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] attemptTeleport(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @ModifyVariable(method = "updateItemUse", ordinal = 1, at = @At(value = "STORE", ordinal = 0))
    Vec3d updateItemUse(Vec3d vec3d1) {
        return vec3d1.scale($cm$size);
    }

    @ModifyArg(method = "updateItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] updateItemUse(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    // endregion
}
