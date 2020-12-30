package dev.necauqua.mods.cm.mixin.entity.projectile;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin({EntityThrowable.class, EntityArrow.class})
public abstract class EntityThrowableAndArrowMixin extends EntityMixin {

    @ModifyConstant(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)V", constant = @Constant(doubleValue = 0.10000000149011612))
    private static double constructor(double constant, World world, EntityLivingBase shooter) {
        return constant * ((ISized) shooter).getSizeCM();
    }

    // water bubble particles motion
    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 0.25))
    double onUpdate(double constant) {
        return constant * $cm$size;
    }

    // also crit particles in EntityArrow
    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }
}
