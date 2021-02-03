/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.item;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.api.IWorldPlayPreciseEvent;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityExpBottle.class)
public abstract class EntityExpBottleMixin extends EntityMixin {

    @Redirect(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(ILnet/minecraft/util/math/BlockPos;I)V"))
    void onImpact(World self, int type, BlockPos pos, int data, RayTraceResult result) {
        ((IWorldPlayPreciseEvent) self).playEvent(type, pos, data, $cm$size, result.hitVec);
    }

    @ModifyArg(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    Entity onImpact(Entity entity) {
        ((ISized) entity).setSizeCM($cm$size);
        return entity;
    }
}
