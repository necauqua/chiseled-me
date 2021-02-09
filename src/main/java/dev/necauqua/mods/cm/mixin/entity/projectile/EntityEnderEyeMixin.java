/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.projectile;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderEye;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import static org.objectweb.asm.Opcodes.GETFIELD;

@Mixin(EntityEnderEye.class)
public abstract class EntityEnderEyeMixin extends EntityMixin {

    @ModifyConstant(method = "onUpdate", constant = {
            @Constant(doubleValue = 0.25),
            @Constant(doubleValue = 0.5),
            @Constant(doubleValue = 0.6),
    })
    double onUpdate(double constant) {
        return constant * $cm$size;
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 0, target = "Lnet/minecraft/entity/item/EntityEnderEye;motionX:D"))
    double onUpdateMotionX(EntityEnderEye self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 0, target = "Lnet/minecraft/entity/item/EntityEnderEye;motionY:D"))
    double onUpdateMotionY(EntityEnderEye self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 0, target = "Lnet/minecraft/entity/item/EntityEnderEye;motionZ:D"))
    double onUpdateMotionZ(EntityEnderEye self) {
        return self.motionZ * $cm$size;
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    Entity onUpdateMotionX(Entity entity) {
        ((ISized) entity).setSizeCM($cm$size);
        return entity;
    }
}
