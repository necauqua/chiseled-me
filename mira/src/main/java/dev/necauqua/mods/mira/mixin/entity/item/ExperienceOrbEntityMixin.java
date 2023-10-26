/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.item;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.item.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin {

    @ModifyConstant(method = "tick", constant = {
        @Constant(doubleValue = 8.0),
        @Constant(doubleValue = 1.0, ordinal = 1)
    })
    double tick(double constant) {
        double size = ((ISized) this).getSizeCM();
        return constant * size;
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 64.0))
    double tickSq(double constant) {
        double size = ((ISized) this).getSizeCM();
        return constant * size * size;
    }
}
