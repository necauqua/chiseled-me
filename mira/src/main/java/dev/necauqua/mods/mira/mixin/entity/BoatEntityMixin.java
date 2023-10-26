/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.item.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin {

    // fix aabb expansion in boat extra push behavior
    @ModifyConstant(method = "tick", constant = {
        @Constant(doubleValue = 0.20000000298023224),
        @Constant(doubleValue = -0.009999999776482582),
    })
    double tick(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    // riding, yey
    @ModifyConstant(method = "getPassengersRidingOffset", constant = @Constant(doubleValue = -0.1))
    double getPassengersRidingOffset(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }
}
