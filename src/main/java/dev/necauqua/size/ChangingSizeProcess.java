package dev.necauqua.size;

public final class ChangingSizeProcess {
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
}
