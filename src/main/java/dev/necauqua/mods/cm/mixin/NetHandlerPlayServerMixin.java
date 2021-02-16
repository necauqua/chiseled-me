/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayServer.class)
public final class NetHandlerPlayServerMixin {

    @ModifyConstant(method = "processPlayer", constant = {
            @Constant(doubleValue = 0.0625, ordinal = 0),
            @Constant(doubleValue = 0.0625, ordinal = 2),
            @Constant(doubleValue = 0.0625, ordinal = 3), // fix for small aabbs
            @Constant(doubleValue = -0.5), // some sort of vertical movement checker
            @Constant(doubleValue = 0.5),  //
            @Constant(doubleValue = -0.03125), // floating checker
            @Constant(doubleValue = -0.55),    //
    })
    double processPlayer(double constant) {
        return constant * ((ISized) player).getSizeCM();
    }

    @ModifyConstant(method = "processPlayer", constant = @Constant(doubleValue = 0.0625, ordinal = 1))
    double processPlayerMovementCheck(double constant) {
        return ((ISized) player).getSizeCM() > 1.0 ?
                Double.MAX_VALUE : // ok, no idea how to properly scale it, just disable the check for big sizes
                constant;
    }

    // entity server reach
    @Redirect(method = "processUseEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;getDistanceSq(Lnet/minecraft/entity/Entity;)D"))
    double processUseEntity(EntityPlayerMP self, Entity argument) {
        return self.getDistanceSq(argument) / (((ISized) self).getSizeCM() * ((ISized) argument).getSizeCM());
    }

    // fix dumb server digging check y-offset
    @ModifyConstant(method = "processPlayerDigging", constant = @Constant(doubleValue = 1.5))
    double processPlayerDigging(double constant) {
        return player.getEyeHeight();
    }

    // copy stuff for vehicle movement just in case

    @ModifyConstant(method = "processVehicleMove", constant = {
            @Constant(doubleValue = 0.0625, ordinal = 0),
            @Constant(doubleValue = 0.0625, ordinal = 2),
            @Constant(doubleValue = 0.0625, ordinal = 3), // fix for small aabbs
            @Constant(doubleValue = -0.5), // some sort of vertical movement checker
            @Constant(doubleValue = 0.5),  //
            @Constant(doubleValue = -0.03125), // floating checker
            @Constant(doubleValue = -0.55),    //
            @Constant(doubleValue = 1.0E-6D), // some vehicle-specific vertical offset
            @Constant(doubleValue = 100.0D), // vehicle-specific speed check
    })
    double processVehicleMove(double constant) {
        return constant * ((ISized) lowestRiddenEnt).getSizeCM();
    }

    @ModifyConstant(method = "processVehicleMove", constant = @Constant(doubleValue = 0.0625, ordinal = 1))
    double processVehicleMoveMovementCheck(double constant) {
        return ((ISized) player).getSizeCM() > 1.0 ?
                Double.MAX_VALUE : // ok, no idea how to properly scale it, just disable the check for big sizes
                constant;
    }

    @Shadow
    public EntityPlayerMP player;

    @Shadow
    private Entity lowestRiddenEnt;
}
