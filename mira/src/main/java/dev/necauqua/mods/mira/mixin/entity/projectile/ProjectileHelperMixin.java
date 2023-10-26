/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.projectile;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ProjectileHelper.class)
public abstract class ProjectileHelperMixin {

    @ModifyConstant(method = "getHitResult", constant = @Constant(doubleValue = 1.0))
    private static double getHitResult(double constant, Entity entity) {
        double size = ((ISized) entity).getSizeCM();
        return size < 1.0 ? constant * size : constant;
    }

    @ModifyVariable(method = "getHitResult", ordinal = 0, at = @At("STORE"))
    private static Vector3d getHitResult(Vector3d velocity, Entity entity) {
        double size = ((ISized) entity).getSizeCM();
        return size < 1.0 ? velocity.multiply(size, size, size) : velocity;
    }

    @ModifyConstant(method = "getEntityHitResult(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;)Lnet/minecraft/util/math/EntityRayTraceResult;", constant = @Constant(doubleValue = 0.30000001192092896))
    private static double getHitResult(double constant, World world, Entity entity) {
        double size = constant * ((ISized) entity).getSizeCM();
        return size < 1.0 ? size : constant;
    }
}
