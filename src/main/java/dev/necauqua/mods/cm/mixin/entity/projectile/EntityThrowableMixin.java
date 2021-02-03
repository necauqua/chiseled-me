/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.projectile;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.objectweb.asm.Opcodes.GETFIELD;

@Mixin(EntityThrowable.class)
public abstract class EntityThrowableMixin extends EntityMixin {

    private double $cm$hack = 1.0;

    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 1.0))
    double onUpdate(double constant) {
        return constant * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/AxisAlignedBB;expand(DDD)Lnet/minecraft/util/math/AxisAlignedBB;"))
    AxisAlignedBB onUpdate(AxisAlignedBB aabb, double x, double y, double z) {
        return aabb.expand(x * $cm$size, y * $cm$size, z * $cm$size);
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEntityBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;"))
    AxisAlignedBB onUpdate(Entity entity) {
        $cm$hack = ((ISized) entity).getSizeCM();
        return entity.getEntityBoundingBox();
    }

    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 0.30000001192092896))
    double onUpdate2(double constant) {
        double size = $cm$hack;
        $cm$hack = 1.0;
        return constant * size;
    }

    // region onUpdate motionX redirects
    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 1, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionX:D"))
    double onUpdateMotionX1(EntityThrowable self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 2, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionX:D"))
    double onUpdateMotionX2(EntityThrowable self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 3, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionX:D"))
    double onUpdateMotionX3(EntityThrowable self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 4, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionX:D"))
    double onUpdateMotionX4(EntityThrowable self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 8, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionX:D"))
    double onUpdateMotionX8(EntityThrowable self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 9, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionX:D"))
    double onUpdateMotionX9(EntityThrowable self) {
        return self.motionX * $cm$size;
    }
    // endregion

    // region onUpdate motionZ redirects
    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 1, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionZ:D"))
    double onUpdateMotionZ1(EntityThrowable self) {
        return self.motionZ * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 2, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionZ:D"))
    double onUpdateMotionZ2(EntityThrowable self) {
        return self.motionZ * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 3, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionZ:D"))
    double onUpdateMotionZ3(EntityThrowable self) {
        return self.motionZ * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 4, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionZ:D"))
    double onUpdateMotionZ4(EntityThrowable self) {
        return self.motionZ * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 8, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionZ:D"))
    double onUpdateMotionZ8(EntityThrowable self) {
        return self.motionZ * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 9, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionZ:D"))
    double onUpdateMotionZ9(EntityThrowable self) {
        return self.motionZ * $cm$size;
    }
    // endregion

    // region onUpdate motionY redirects
    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 1, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionY:D"))
    double onUpdateMotionY1(EntityThrowable self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 2, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionY:D"))
    double onUpdateMotionY2(EntityThrowable self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 3, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionY:D"))
    double onUpdateMotionY3(EntityThrowable self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 4, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionY:D"))
    double onUpdateMotionY4(EntityThrowable self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 6, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionY:D"))
    double onUpdateMotionY6(EntityThrowable self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 7, target = "Lnet/minecraft/entity/projectile/EntityThrowable;motionY:D"))
    double onUpdateMotionY7(EntityThrowable self) {
        return self.motionY * $cm$size;
    }
    // endregion
}
