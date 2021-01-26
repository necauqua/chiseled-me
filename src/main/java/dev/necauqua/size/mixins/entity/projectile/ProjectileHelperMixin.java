package dev.necauqua.size.mixins.entity.projectile;

import dev.necauqua.size.api.ISized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ProjectileHelper.class)
public final class ProjectileHelperMixin {

    private static double $size$hack = 1.0;

    @ModifyConstant(method = "forwardsRaycast", constant = @Constant(doubleValue = 1.0))
    private static double forwardsRaycast(double constant, Entity projectile) {
        return constant * ((ISized) projectile).getSize();
    }

    @ModifyVariable(method = "forwardsRaycast", ordinal = 3, at = @At("STORE"))
    private static double forwardsRaycastX(double variable, Entity projectile) {
        return variable * ((ISized) projectile).getSize();
    }

    @ModifyVariable(method = "forwardsRaycast", ordinal = 4, at = @At("STORE"))
    private static double forwardsRaycastY(double variable, Entity projectile) {
        return variable * ((ISized) projectile).getSize();
    }

    @ModifyVariable(method = "forwardsRaycast", ordinal = 5, at = @At("STORE"))
    private static double forwardsRaycastZ(double variable, Entity projectile) {
        return variable * ((ISized) projectile).getSize();
    }

    // same as in EntityThrowableAndArrowMixin#onUpdate too
    @Redirect(method = "forwardsRaycast", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/entity/Entity;getEntityBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;"))
    private static AxisAlignedBB forwardsRaycast(Entity entity) {
        $size$hack = ((ISized) entity).getSize();
        return entity.getEntityBoundingBox();
    }

    @ModifyConstant(method = "forwardsRaycast", constant = @Constant(doubleValue = 0.30000001192092896))
    private static double forwardsRaycast(double constant) {
        double size = $size$hack;
        $size$hack = 1.0;
        return constant * size;
    }

    @ModifyVariable(method = "rotateTowardsMovement", ordinal = 0, at = @At("STORE"))
    private static double rotateTowardsMovementX(double variable, Entity projectile) {
        return variable * ((ISized) projectile).getSize();
    }

    @ModifyVariable(method = "rotateTowardsMovement", ordinal = 1, at = @At("STORE"))
    private static double rotateTowardsMovementY(double variable, Entity projectile) {
        return variable * ((ISized) projectile).getSize();
    }

    @ModifyVariable(method = "rotateTowardsMovement", ordinal = 2, at = @At("STORE"))
    private static double rotateTowardsMovementZ(double variable, Entity projectile) {
        return variable * ((ISized) projectile).getSize();
    }
}
