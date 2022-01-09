/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.theta.api;

import static java.lang.Math.*;

public interface IResizingProgress {

    double TWO_OVER_LOG_TWO = 2.0 / log(2);

    double getStartSize();

    double getTargetSize();

    double getPrevTickSize();

    int getCurrentTime();

    int getInterval();

    static int log2LerpTime(double fromSize, double toSize) {
        return (int) (abs(log(fromSize) - log(toSize)) * TWO_OVER_LOG_TWO);
    }
}
