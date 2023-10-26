/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * This interface is a part of the mixin that adds necessary modifications
 * to the vanilla sound system to allow precise world positioning
 * and 'resizing' of sounds.
 * <p>
 * It is implemented on a {@link World} class by the mixin.
 * <p>
 * The size is not part of the volume to avoid various volume clamping and is
 * just used to fool
 * the sound system into thinking the sound is further/closer to the listener
 * than it actually is,
 * based on the source size.
 */
public interface IWorldPreciseSounds {

    /**
     * Same as
     * {@link World#playLocalSound(double, double, double, SoundEvent, SoundCategory, float, float, boolean)}
     * but also carries the size of the sound source.
     * <p>
     * This one has a client-only effect, just like the vanilla method.
     */
    void playLocalSound(Vector3d pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch,
                        boolean distanceDelay, double size);

    /**
     * Same as
     * {@link World#playSound(PlayerEntity, double, double, double, SoundEvent, SoundCategory, float, float)}
     * but also carries the size of the sound source.
     * <p>
     * This one is the network-sent version, just like the vanilla method.
     */
    void playSound(@Nullable PlayerEntity player, Vector3d pos, SoundEvent soundIn, SoundCategory category,
                   float volume, float pitch, double size);
}
