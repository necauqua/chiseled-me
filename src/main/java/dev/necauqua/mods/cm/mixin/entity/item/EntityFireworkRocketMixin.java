/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.item;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFireworkRocket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityFireworkRocket.class)
public abstract class EntityFireworkRocketMixin extends EntityMixin {

    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 0.3))
    double onUpdate(double constant) {
        return boostedEntity != null ?
                constant * ((ISized) boostedEntity).getSizeCM() :
                constant * $cm$size;
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args,
                boostedEntity != null ?
                        ((ISized) boostedEntity).getSizeCM() :
                        $cm$size);
    }

    @Shadow
    private EntityLivingBase boostedEntity;
}
