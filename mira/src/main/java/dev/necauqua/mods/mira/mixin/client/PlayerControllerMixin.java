/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.size.MixinHelpers;
import net.minecraft.client.multiplayer.PlayerController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerController.class)
public abstract class PlayerControllerMixin {

    @ModifyConstant(method = "getPickRange", constant = @Constant(floatValue = 0.5F))
    float getBlockReachDistance(float constant) {
        return (float) (constant * MixinHelpers.getViewerSize());
    }
}
