/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.ai;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.passive.EntityAnimal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

// disallow mating between differently sized entities
@Mixin(EntityAIMate.class)
public final class EntityAIMateMixin {

    @Inject(method = "shouldExecute", at = @At("HEAD"), cancellable = true)
    void shouldExecute(CallbackInfoReturnable<Boolean> cir) {
        if (targetMate != null && ((ISized) animal).getSizeCM() != ((ISized) targetMate).getSizeCM()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldContinueExecuting", at = @At("HEAD"), cancellable = true)
    void shouldContinueExecuting(CallbackInfoReturnable<Boolean> cir) {
        if (targetMate != null && ((ISized) animal).getSizeCM() != ((ISized) targetMate).getSizeCM()) {
            cir.setReturnValue(false);
        }
    }

    @ModifyConstant(method = "getNearbyMate", constant = @Constant(doubleValue = 8.0))
    double getNearbyMate(double constant) {
        return constant * ((ISized) animal).getSizeCM();
    }

    @Shadow
    @Final
    private EntityAnimal animal;

    @Shadow
    @Nullable
    private EntityAnimal targetMate;
}
