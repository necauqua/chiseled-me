/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IWorldPlayPreciseEvent;
import dev.necauqua.mods.cm.size.IPreciseEffectPacket;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayClient.class)
public final class NetHandlerPlayClientMixin {

    @Redirect(method = "handleEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;playEvent(ILnet/minecraft/util/math/BlockPos;I)V"))
    void handleEffect(WorldClient self, int type, BlockPos pos, int data, SPacketEffect packet) {
        IPreciseEffectPacket p = (IPreciseEffectPacket) packet;
        ((IWorldPlayPreciseEvent) self).playEvent(type, pos, data, p.getSizeCM(), p.getCoordsCM());
    }
}
