/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.extras;

import net.minecraft.util.math.vector.Vector3d;

public interface IPreciseEffectPacket {

    double getSizeCM();

    Vector3d getCoordsCM();

    void populateCM(double size, Vector3d pos);
}
