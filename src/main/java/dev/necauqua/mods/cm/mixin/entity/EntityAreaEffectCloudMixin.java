/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity;

import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.EntityAreaEffectCloud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityAreaEffectCloud.class)
public abstract class EntityAreaEffectCloudMixin extends EntityMixin {

    @ModifyVariable(method = "onUpdate", ordinal = 3, at = @At("STORE"))
    float onUpdate(float constant) {
        return (float) (constant * $cm$size);
    }

    @ModifyConstant(method = "onUpdate", constant = @Constant(floatValue = 0.2f))
    float onUpdateConstant(float constant) {
        return (float) (constant * $cm$size);
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnAlwaysVisibleParticle(IDDDDDD[I)V"))
    int[] onUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }
}
