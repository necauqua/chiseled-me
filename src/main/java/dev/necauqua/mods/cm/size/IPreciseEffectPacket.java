package dev.necauqua.mods.cm.size;

import net.minecraft.util.math.Vec3d;

public interface IPreciseEffectPacket {

    double getSizeCM();

    Vec3d getCoordsCM();

    void populateCM(double size, Vec3d pos);
}
