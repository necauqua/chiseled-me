/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.EntityAgeable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityAgeable.class)
public abstract class EntityAgeableMixin extends EntityMixin {

    @ModifyVariable(method = "processInteract", at = @At("STORE"))
    EntityAgeable processInteract(EntityAgeable child) {
        if (child != null) {
            ((ISized) child).setSizeCM($cm$size);
        }
        return child;
    }

    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.5))
    double onLivingUpdate(double constant) {
        return constant * $cm$size;
    }

    @ModifyArg(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] attemptTeleport(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }
}
