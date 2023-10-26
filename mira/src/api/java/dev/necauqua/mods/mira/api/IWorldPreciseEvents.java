/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * This interface is a part of the mixin that adds necessary modifications
 * to the vanilla event (some network-sent particles and sounds) system to allow
 * precise world positioning
 * and resizing of events.
 * <p>
 * It is implemented on a {@link World} class by the mixin.
 */
public interface IWorldPreciseEvents {

    /**
     * Same as {@link IWorld#levelEvent(int, BlockPos, int)} but carries additional
     * data
     * (which is then sent by the network) which is used in altered client code
     * accorddingly - to use a precise (and not block-indexed) position and to
     * resize the effect.
     */
    void levelEvent(@Nullable PlayerEntity player, int type, BlockPos blockPos, int data, double size, Vector3d pos);
}
