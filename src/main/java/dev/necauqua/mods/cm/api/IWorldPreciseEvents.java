/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * This interface is a part of the mixin that adds necessary modifications
 * to the vanilla event (some network-sent particles and sounds) system to allow precise world positioning
 * and resizing of events.
 * <p>
 * It is implemented on a {@link World} class by the mixin.
 */
public interface IWorldPreciseEvents {

    /**
     * Same as {@link World#playEvent(int, BlockPos, int)} but carries additional data
     * (which is then sent by the network) which is used in altered client code
     * accorddingly - to use a precise (and not block-indexed) position and to resize the effect.
     */
    void playEvent(EntityPlayer player, int type, BlockPos blockPos, int data, double size, Vec3d pos);
}
