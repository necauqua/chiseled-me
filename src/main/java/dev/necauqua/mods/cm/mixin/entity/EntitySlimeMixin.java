/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity;

import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.monster.EntitySlime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntitySlime.class)
public abstract class EntitySlimeMixin extends EntityMixin {

    @ModifyVariable(method = "onUpdate", ordinal = 1, at = @At("STORE"))
    float spawnParticles(float f1) {
        return (float) (f1 * $cm$size);
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] spawnParticles(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }
}
