/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.item;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    // group range is [1;2] because either the first modifier is applied twice to
    // vanilla constants
    // or the second applies once to the spigot localvar

    // @Group(name = "mergeWithNeighbours", min = 1, max = 2)
    @ModifyConstant(method = "mergeWithNeighbours", constant = @Constant(doubleValue = 0.5), require = 0, expect = 0)
    double mergeWithNeighbours(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    // @Group(name = "mergeWithNeighbours", min = 1, max = 2)
    // @ModifyVariable(method = "mergeWithNeighbours", ordinal = 0, at = @At(value =
    // "STORE", ordinal = 0), require = 0, expect = 0)
    // double mergeWithNeighboursSpigot(double variable) {
    // return variable * $cm$size;
    // }
}
