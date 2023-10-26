/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin;

import dev.necauqua.mods.mira.Config;
import dev.necauqua.mods.mira.api.IWorldPreciseEvents;
import dev.necauqua.mods.mira.api.IWorldPreciseSounds;
import dev.necauqua.mods.mira.extras.IPreciseEffectPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.profiler.IProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements IWorldPreciseEvents, IWorldPreciseSounds {

    @Shadow
    @Final
    private MinecraftServer server;

    public ServerWorldMixin(ISpawnWorldInfo info, RegistryKey<World> dimension, DimensionType type,
                            Supplier<IProfiler> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed) {
        super(info, dimension, type, profiler, isClientSide, isDebug, biomeZoomSeed);
    }

    @SuppressWarnings("ConstantConditions") // interface we mixed in
    @Override
    public void levelEvent(@Nullable PlayerEntity player, int type, BlockPos blockPos, int data, double size,
                           Vector3d pos) {
        SPlaySoundEventPacket packet = new SPlaySoundEventPacket(type, blockPos, data, false);
        ((IPreciseEffectPacket) packet).populateCM(size, pos);
        server.getPlayerList().broadcast(player, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
            size > 1.0 ? 64.0 * size : 64.0, dimension(), packet);
    }

    @Override
    public void playLocalSound(Vector3d pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch,
                               boolean distanceDelay, double size) {
    }

    @SuppressWarnings("ConstantConditions") // interface we mixed in
    @Override
    public void playSound(@Nullable PlayerEntity player, Vector3d pos, SoundEvent sound, SoundCategory category,
                          float volume, float pitch, double size) {
        // equivalent to playSound, but the sound packet is populated with
        // size/precise-pos data

        PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(player, sound, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) {
            return;
        }
        sound = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        SPlaySoundEffectPacket packet = new SPlaySoundEffectPacket(sound, category, pos.x, pos.y, pos.z, volume, pitch);
        if (Config.scaleSounds.get()) {
            ((IPreciseEffectPacket) packet).populateCM(size, pos);
        }
        server.getPlayerList().broadcast(player, pos.x, pos.y, pos.z,
            volume > 1.0F ? (double) (16.0F * volume) : 16.0D, dimension(),
            packet);
    }

    @Override
    @Shadow
    public abstract List<ServerPlayerEntity> players();
}
