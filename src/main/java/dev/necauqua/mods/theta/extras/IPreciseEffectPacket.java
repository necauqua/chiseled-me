/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.theta.extras;

import net.minecraft.util.math.vector.Vector3d;

public interface IPreciseEffectPacket {

    double getSizeCM();

    Vector3d getCoordsCM();

    void populateCM(double size, Vector3d pos);
}
