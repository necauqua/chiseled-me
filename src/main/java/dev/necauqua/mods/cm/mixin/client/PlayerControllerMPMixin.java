/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerControllerMP.class)
public final class PlayerControllerMPMixin {

    @ModifyConstant(method = "getBlockReachDistance", constant = @Constant(floatValue = 0.5F))
    float getBlockReachDistance(float constant) {
        return (float) (constant * ((IRenderSized) mc.player).getSizeCM());
    }

    @Shadow
    @Final
    private Minecraft mc;
}
