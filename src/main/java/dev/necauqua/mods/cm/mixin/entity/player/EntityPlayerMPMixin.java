/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.player;

import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityMixin {

    @ModifyConstant(method = "handleFalling", constant = @Constant(doubleValue = 0.20000000298023224D))
    double handleFalling(double constant) {
        return constant * $cm$size;
    }

    // that getPosition fix
    @ModifyConstant(method = "getPosition", constant = @Constant(doubleValue = 0.5))
    double getPosition(double constant) {
        return constant * $cm$size;
    }
}
