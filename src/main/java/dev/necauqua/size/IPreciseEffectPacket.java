package dev.necauqua.size;

import net.minecraft.util.math.Vec3d;

public interface IPreciseEffectPacket {

    double $size$getSize();

    Vec3d $size$getCoords();

    void $size$populate(double size, Vec3d pos);
}
