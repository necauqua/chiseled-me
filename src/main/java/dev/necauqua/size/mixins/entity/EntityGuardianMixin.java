package dev.necauqua.size.mixins.entity;

import dev.necauqua.size.EntitySizeInteractions;
import net.minecraft.entity.monster.EntityGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityGuardian.class)
public abstract class EntityGuardianMixin extends EntityMixin {

    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 1.5))
    double spawnParticles(double constant) {
        return constant * $size$it;
    }

    @ModifyArg(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] spawnParticles(int[] args) {
        return EntitySizeInteractions.appendSize(args, $size$it);
    }
}
