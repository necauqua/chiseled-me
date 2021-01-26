package dev.necauqua.size.mixins.entity.monster;

import dev.necauqua.size.mixins.entity.EntityMixin;
import dev.necauqua.size.EntitySizeInteractions;
import net.minecraft.entity.monster.EntityEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityEnderman.class)
public abstract class EntityEndermanMixin extends EntityMixin {

    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.25))
    double onLivingUpdate(double constant) {
        return constant * $size$it;
    }

    @ModifyArg(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onLivingUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args, $size$it);
    }
}
