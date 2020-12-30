package dev.necauqua.mods.cm.mixin.entity;

import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityVillager.class)
public abstract class EntityVillagerMixin extends EntityMixin {

    @ModifyConstant(method = "spawnParticles", constant = @Constant(doubleValue = 1.0))
    double spawnParticles(double constant) {
        return constant * $cm$size;
    }

    @ModifyArg(method = "spawnParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] spawnParticles(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }
}
