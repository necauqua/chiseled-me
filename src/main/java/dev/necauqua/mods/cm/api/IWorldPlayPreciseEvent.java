package dev.necauqua.mods.cm.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * This interface is a part of the mixin that overrides vanilla even system
 * (at the moment only for particles, sounds are planned)
 * to allow precise world positioning and resizing of events.
 * <p>
 * It is implemented on a {@link World} class by the mixin.
 */
public interface IWorldPlayPreciseEvent {

    /**
     * Same as {@link World#playEvent(int, BlockPos, int)} but carries additional data
     * (which is then sent by the network) which is used in altered client code
     * accorddingly - to use a precise (and not block-indexed) position and to resize the effect.
     */
    void playEvent(int type, BlockPos blockPos, int data, double size, Vec3d pos);
}
