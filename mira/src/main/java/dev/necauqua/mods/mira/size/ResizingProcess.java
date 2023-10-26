/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.size;

import dev.necauqua.mods.mira.api.IResizingProgress;

public final class ResizingProcess implements IResizingProgress {

    public final double startSize;
    public final double targetSize;

    public double prevTickSize;
    public int currentTime;
    public int interval;

    public ResizingProcess(double startSize, double targetSize, int interval) {
        this.startSize = prevTickSize = startSize;
        this.targetSize = targetSize;
        this.interval = interval;
    }

    @Override
    public double getStartSize() {
        return startSize;
    }

    @Override
    public double getTargetSize() {
        return targetSize;
    }

    @Override
    public double getPrevTickSize() {
        return prevTickSize;
    }

    @Override
    public int getCurrentTime() {
        return currentTime;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public String toString() {
        return "ResizingProcess{" +
            "fromSize=" + startSize +
            ", toSize=" + targetSize +
            ", animationTicks=" + interval +
            '}';
    }
}
