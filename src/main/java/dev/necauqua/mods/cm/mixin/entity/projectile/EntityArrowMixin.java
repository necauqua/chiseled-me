/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.projectile;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(EntityArrow.class)
public abstract class EntityArrowMixin extends EntityMixin {

    private double $cm$hack = 1.0;

    @ModifyConstant(method = "onHit", constant = @Constant(doubleValue = 0.05000000074505806))
    double onHit(double constant) {
        return constant * $cm$size;
    }

    @Inject(method = "onHit", at = @At(value = "FIELD", ordinal = 1, opcode = PUTFIELD, shift = AFTER, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionZ:D"))
    void onHit(RayTraceResult raytraceResultIn, CallbackInfo ci) {
        motionX /= $cm$size;
        motionY /= $cm$size;
        motionZ /= $cm$size;
    }

    // crit particles motion and collision expansion
    @ModifyConstant(method = "onUpdate", constant = {
            @Constant(doubleValue = 0.2),
            @Constant(doubleValue = 0.05),
    })
    double onUpdate(double constant) {
        return constant * $cm$size;
    }

    // crit particles position
    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 4.0))
    double onUpdateDiv(double constant) {
        return constant / $cm$size;
    }

    @Redirect(method = "findEntityOnPath", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/AxisAlignedBB;expand(DDD)Lnet/minecraft/util/math/AxisAlignedBB;"))
    AxisAlignedBB findEntityOnPath(AxisAlignedBB aabb, double x, double y, double z) {
        return aabb.expand(x * $cm$size, y * $cm$size, z * $cm$size);
    }

//    @ModifyConstant(method = "findEntityOnPath", constant = @Constant(doubleValue = 1.0))
//    double findEntityOnPath(double constant) {
//        return constant * $cm$size;
//    }

    // same as in EntityThrowable#onUpdate
    @Redirect(method = "findEntityOnPath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEntityBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;"))
    AxisAlignedBB findEntityOnPath(Entity entity) {
        $cm$hack = ((ISized) entity).getSizeCM();
        return entity.getEntityBoundingBox();
    }

    @ModifyConstant(method = "findEntityOnPath", constant = @Constant(doubleValue = 0.30000001192092896))
    double findEntityOnPath2(double constant) {
        double size = $cm$hack;
        $cm$hack = 1.0;
        return constant * size;
    }

    // no multiple targets for redirects???

    // region onUpdate motionX redirects
    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 4, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionX:D"))
    double onUpdateMotionX4(EntityArrow self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 5, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionX:D"))
    double onUpdateMotionX5(EntityArrow self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 7, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionX:D"))
    double onUpdateMotionX7(EntityArrow self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 8, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionX:D"))
    double onUpdateMotionX8(EntityArrow self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 13, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionX:D"))
    double onUpdateMotionX13(EntityArrow self) {
        return self.motionX * $cm$size;
    }
    // endregion

    // region onUpdate motionZ redirects
    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 4, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionZ:D"))
    double onUpdateMotionZ4(EntityArrow self) {
        return self.motionZ * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 5, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionZ:D"))
    double onUpdateMotionZ5(EntityArrow self) {
        return self.motionZ * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 7, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionZ:D"))
    double onUpdateMotionZ7(EntityArrow self) {
        return self.motionZ * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 8, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionZ:D"))
    double onUpdateMotionZ8(EntityArrow self) {
        return self.motionZ * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 13, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionZ:D"))
    double onUpdateMotionZ13(EntityArrow self) {
        return self.motionZ * $cm$size;
    }
    // endregion

    // region onUpdate motionY redirects
    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 2, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionY:D"))
    double onUpdateMotionY2(EntityArrow self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 3, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionY:D"))
    double onUpdateMotionY3(EntityArrow self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 5, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionY:D"))
    double onUpdateMotionY5(EntityArrow self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 6, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionY:D"))
    double onUpdateMotionY6(EntityArrow self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 9, target = "Lnet/minecraft/entity/projectile/EntityArrow;motionY:D"))
    double onUpdateMotionY9(EntityArrow self) {
        return self.motionY * $cm$size;
    }
    // endregion
}
