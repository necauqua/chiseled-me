package dev.necauqua.mods.cm.size;

import dev.necauqua.mods.cm.asm.CalledFromASM;

import static java.lang.Math.*;

@CalledFromASM
public final class ChangingSizeProcess {
    private static final double TWO_OVER_LOG_TWO = 2.0 / log(2);

    public final double fromSize;
    public final double toSize;

    public double prevTickSize;
    public int interpTicks;
    public int interpInterval;

    public ChangingSizeProcess(double fromSize, double toSize) {
        this.fromSize = fromSize;
        this.toSize = toSize;

        prevTickSize = fromSize;
        interpTicks = 0;
        interpInterval = calculateInterpolationInterval(fromSize, toSize);
    }

    @Override
    public String toString() {
        return "ChangingSizeProcess{" +
            "fromSize=" + fromSize +
            ", toSize=" + toSize +
            '}';
    }

    public static int calculateInterpolationInterval(double fromSize, double toSize) {
        return (int) (abs(log(fromSize) - log(toSize)) * TWO_OVER_LOG_TWO);
    }
}
