/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({Entity.class, RemoteClientPlayerEntity.class})
public abstract class EntityClientMixin {

    @ModifyVariable(method = "shouldRenderAtSqrDistance", ordinal = 1, at = @At(value = "STORE", ordinal = 0))
    double shouldRenderAtSqrDistance(double averageEdgeLength) {
        double size = ((ISized) this).getSizeCM();
        return size >= 1.0 ? // so big ones should use the default algo which should see that the hitbox is
            // giant
            averageEdgeLength : averageEdgeLength / size; // but small ones fool the algo, so it renders even one
        // pixel of them
    }
}
