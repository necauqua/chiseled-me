/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.size;

import static java.lang.Math.abs;
import static java.lang.Math.log;

public final class ChangingSizeProcess {
    private static final double TWO_OVER_LOG_TWO = 2.0 / log(2);

    public final double fromSize;
    public final double toSize;

    public double prevTickSize;
    public int lerpTime;
    public int lerpedTicks;

    public ChangingSizeProcess(double fromSize, double toSize, int lerpTime) {
        this.fromSize = prevTickSize = fromSize;
        this.toSize = toSize;
        this.lerpTime = lerpTime;
    }

    @Override
    public String toString() {
        return "ChangingSizeProcess{" +
                "fromSize=" + fromSize +
                ", toSize=" + toSize +
                ", lerpTime=" + lerpTime +
                '}';
    }

    public static int log2LerpTime(double fromSize, double toSize) {
        return (int) (abs(log(fromSize) - log(toSize)) * TWO_OVER_LOG_TWO);
    }
}
