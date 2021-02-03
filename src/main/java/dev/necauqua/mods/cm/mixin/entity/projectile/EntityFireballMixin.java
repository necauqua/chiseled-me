/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.projectile;

import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.projectile.EntityFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import static org.objectweb.asm.Opcodes.GETFIELD;

@Mixin(EntityFireball.class)
public abstract class EntityFireballMixin extends EntityMixin {

    @ModifyConstant(method = "onUpdate", constant = {
            @Constant(doubleValue = 0.25),
            @Constant(doubleValue = 0.5),
    })
    double getUpdate(double constant) {
        return constant * $cm$size;
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 0, target = "Lnet/minecraft/entity/projectile/EntityFireball;motionX:D"))
    double onUpdateMotionX(EntityFireball self) {
        return self.motionX * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 0, target = "Lnet/minecraft/entity/projectile/EntityFireball;motionY:D"))
    double onUpdateMotionY(EntityFireball self) {
        return self.motionY * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 0, target = "Lnet/minecraft/entity/projectile/EntityFireball;motionZ:D"))
    double onUpdateMotionZ(EntityFireball self) {
        return self.motionZ * $cm$size;
    }
}
