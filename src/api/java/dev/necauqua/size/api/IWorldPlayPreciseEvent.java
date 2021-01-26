package dev.necauqua.size.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * This interface is a part of the mixin that overrides vanilla event system
 * for sending particles and sounds (sounds not yet implemented) to the client
 * to allow precise world positioning and resizing of those events.
 * <p>
 * It is implemented on a {@link World} class by the mixin.
 */
public interface IWorldPlayPreciseEvent {

    /**
     * Same as {@link World#playEvent(int, BlockPos, int)} but allows to attach additional data
     * which is then used in altered client code accorddingly - to use a precise
     * (and not block-indexed as in vanilla) position and to resize the effect.
     * <p>
     * This is used by the implementation itself to 'fix' vanilla entities and similar,
     * exposed as public API for mods to potentially use.
     */
    void playEvent(int type, BlockPos blockPos, int data, double size, Vec3d pos);
}
