package dev.necauqua.mods.cm2;

import net.minecraft.item.ItemStack;

public final class RecalibratorConfig {

    private final ItemStack stack;

    public RecalibratorConfig(ItemStack stack) {
        this.stack = stack;
    }

    public Tier getTier() {
        switch (stack.getOrCreateTag().getString("tier")) {
            case "red":
                return Tier.RED;
            case "blue":
                return Tier.BLUE;
            default:
                return Tier.BASE;
        }
    }

    public double getSizeA() {
        double sizeA = stack.getOrCreateTag().getDouble("sizeA");
        return sizeA != 0.0 ? sizeA : 1.0;
    }

    public void setSizeA(double sizeA) {
        stack.getOrCreateTag().putDouble("sizeA", sizeA);
    }

    public double getSizeB() {
        double sizeB = stack.getOrCreateTag().getDouble("sizeB");
        return sizeB != 0.0 ? sizeB : 1.0;
    }

    public void setSizeB(double sizeB) {
        stack.getOrCreateTag().putDouble("sizeB", sizeB);
    }

    public enum Tier {
        BASE,
        RED,
        BLUE,
    }
}
