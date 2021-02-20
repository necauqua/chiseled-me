/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.projectile;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.api.IWorldPreciseEvents;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityPotion.class)
public abstract class EntityPotionMixin extends EntityMixin {

    @Redirect(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(ILnet/minecraft/util/math/BlockPos;I)V"))
    void onImpact(World self, int type, BlockPos pos, int data, RayTraceResult result) {
        ((IWorldPreciseEvents) self).playEvent(null, type, pos, data, $cm$size, result.hitVec);
    }

    @ModifyVariable(method = "makeAreaOfEffectCloud", at = @At("STORE"))
    EntityAreaEffectCloud makeAreaOfEffectCloud(EntityAreaEffectCloud entity) {
        ((ISized) entity).setSizeCM($cm$size);
        return entity;
    }

    @ModifyConstant(method = "applySplash", constant = {
            @Constant(doubleValue = 2.0),
            @Constant(doubleValue = 4.0),
    })
    double applySplash(double constant) {
        return constant * $cm$size;
    }

    @ModifyConstant(method = "applyWater", constant = {
            @Constant(doubleValue = 2.0),
            @Constant(doubleValue = 4.0),
    })
    double applyWater(double constant) {
        return constant * $cm$size;
    }
}
