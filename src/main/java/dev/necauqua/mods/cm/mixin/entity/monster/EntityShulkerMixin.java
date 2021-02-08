/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.monster;

import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.monster.EntityShulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityShulker.class)
public abstract class EntityShulkerMixin extends EntityMixin {

    // omfg
    @ModifyConstant(method = "onUpdate", constant = {
            @Constant(doubleValue = 1.0),
            @Constant(doubleValue = 0.5, ordinal = 6),
            @Constant(doubleValue = 0.5, ordinal = 7),
            @Constant(doubleValue = 0.5, ordinal = 8),
            @Constant(doubleValue = 0.5, ordinal = 9),
            @Constant(doubleValue = 0.5, ordinal = 10),
            @Constant(doubleValue = 0.5, ordinal = 11),
            @Constant(doubleValue = 0.5, ordinal = 12),
            @Constant(doubleValue = 0.5, ordinal = 13),
            @Constant(doubleValue = 0.5, ordinal = 14),
            @Constant(doubleValue = 0.5, ordinal = 15),
            @Constant(doubleValue = 0.5, ordinal = 16),
            @Constant(doubleValue = 0.5, ordinal = 17),
            @Constant(doubleValue = 0.5, ordinal = 18),
            @Constant(doubleValue = 0.5, ordinal = 19),
            @Constant(doubleValue = 0.5, ordinal = 20),
            @Constant(doubleValue = 0.5, ordinal = 21),
            @Constant(doubleValue = 0.5, ordinal = 22),
            @Constant(doubleValue = 0.5, ordinal = 23),
            @Constant(doubleValue = 0.5, ordinal = 24),
            @Constant(doubleValue = 0.5, ordinal = 25),
            @Constant(doubleValue = 0.5, ordinal = 26),
            @Constant(doubleValue = 0.5, ordinal = 27),
            @Constant(doubleValue = 0.5, ordinal = 28),
            @Constant(doubleValue = 0.5, ordinal = 29),
    })
    double onUpdate(double constant) {
        return constant * $cm$size;
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    void onUpdate(Entity entity, MoverType type, double x, double y, double z) {
        double size = $cm$size;
        entity.move(type, x * size, y * size, z * size);
    }
}
