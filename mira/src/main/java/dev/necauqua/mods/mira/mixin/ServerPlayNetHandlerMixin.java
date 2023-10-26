/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CUseEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    // entity interaction limit - mostly applicable for big entities interacting
    @ModifyConstant(method = "handleInteract", constant = @Constant(doubleValue = 36.0))
    double handleInteractScaleDist(double constant, CUseEntityPacket packet) {
        Entity entity = packet.getTarget(player.getLevel());
        if (entity == null) {
            return constant;
        }
        return constant * ((ISized) player).getSizeCM() * ((ISized) entity).getSizeCM();
    }

    @ModifyConstant(method = "noBlocksAround", constant = {
        @Constant(doubleValue = 0.0625, ordinal = 0),
        @Constant(doubleValue = -0.55),
    })
    double noBlocksAroundScaleOffsets(double constant, Entity entity) {
        return constant * ((ISized) entity).getSizeCM();
    }

    @ModifyConstant(method = "handleMovePlayer", constant = {
        @Constant(doubleValue = -0.5), // some sort of vertical movement checker
        @Constant(doubleValue = 0.5), //
        @Constant(doubleValue = -0.03125), // floating checker
    })
    double processPlayer(double constant) {
        return constant * ((ISized) player).getSizeCM();
    }

    @ModifyConstant(method = "handleMovePlayer", constant = @Constant(doubleValue = 0.0625))
    double processPlayerMovementCheck(double constant) {
        double size = ((ISized) player).getSizeCM();
        // idk a dumb patch for that weird mod
        return size > 1.0 ? Double.MAX_VALUE : // ok, no idea how to properly scale it, just disable the check for big
            // sizes
            constant;
    }
}
