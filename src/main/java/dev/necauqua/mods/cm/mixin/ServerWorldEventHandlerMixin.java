/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.IWorldPlayPreciseEvent;
import dev.necauqua.mods.cm.size.IPreciseEffectPacket;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerWorldEventHandler.class)
public final class ServerWorldEventHandlerMixin implements IWorldPlayPreciseEvent {

    @SuppressWarnings("ConstantConditions") // mixed in interface
    @Override
    public void playEvent(int type, BlockPos blockPos, int data, double size, Vec3d pos) {
        SPacketEffect packet = new SPacketEffect(type, blockPos, data, false);
        ((IPreciseEffectPacket) packet).populateCM(size, pos);
        mcServer.getPlayerList()
                .sendToAllNearExcept(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 64.0D * size, world.provider.getDimension(), packet);
    }

    @Shadow
    @Final
    private MinecraftServer mcServer;

    @Shadow
    @Final
    private WorldServer world;
}
