/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.IWorldPreciseEvents;
import dev.necauqua.mods.cm.api.IWorldPreciseSounds;
import dev.necauqua.mods.cm.size.IPreciseEffectPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(ServerWorldEventHandler.class)
public final class ServerWorldEventHandlerMixin implements IWorldPreciseEvents, IWorldPreciseSounds {

    @SuppressWarnings("ConstantConditions") // mixed in interface
    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPos, int data, double size, Vec3d pos) {
        SPacketEffect packet = new SPacketEffect(type, blockPos, data, false);
        ((IPreciseEffectPacket) packet).populateCM(size, pos);
        mcServer.getPlayerList()
                .sendToAllNearExcept(player, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 64.0D * size, world.provider.getDimension(), packet);
    }

    @Override
    public void playSound(Vec3d pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay, double size) {}

    @SuppressWarnings("ConstantConditions") // mixed in interface
    @Override
    public void playSound(@Nullable EntityPlayer player, Vec3d pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, double size) {
        SPacketSoundEffect packet = new SPacketSoundEffect(soundIn, category, pos.x, pos.y, pos.z, volume, pitch);
        ((IPreciseEffectPacket) packet).populateCM(size, pos);
        mcServer.getPlayerList()
                .sendToAllNearExcept(player, pos.x, pos.y, pos.z, (volume > 1.0f ? (16.0 * volume) : 16.0) * size, world.provider.getDimension(), packet);
    }

    @Shadow
    @Final
    private MinecraftServer mcServer;

    @Shadow
    @Final
    private WorldServer world;
}
