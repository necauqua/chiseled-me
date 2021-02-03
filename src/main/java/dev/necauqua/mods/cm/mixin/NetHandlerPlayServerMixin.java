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
            @Constant(doubleValue = 0.0625), // fix for small aabbs
            @Constant(doubleValue = -0.03125), // floating checker
            @Constant(doubleValue = -0.55), // some kind of levitation potion effect checker
    })
    double processPlayer(double constant) {
        return constant * ((ISized) player).getSizeCM();
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

    @Shadow
    public EntityPlayerMP player;
}
