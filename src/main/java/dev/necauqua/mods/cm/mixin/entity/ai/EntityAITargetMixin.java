/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.ai;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityAITarget.class)
public abstract class EntityAITargetMixin {

    @ModifyConstant(method = "getTargetDistance", constant = @Constant(doubleValue = 16.0))
    double getTargetDistance(double constant) {
        return constant * ((ISized) taskOwner).getSizeCM();
    }

    @Redirect(method = "getTargetDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;getAttributeValue()D"))
    double getTargetDistance(IAttributeInstance self) {
        double size = ((ISized) taskOwner).getSizeCM();
        EntityLivingBase target = taskOwner.getAttackTarget();
        return self.getAttributeValue() * (target != null ? Math.sqrt(size * ((ISized) target).getSizeCM()) : size);
    }

    @Shadow
    @Final
    protected EntityCreature taskOwner;
}
