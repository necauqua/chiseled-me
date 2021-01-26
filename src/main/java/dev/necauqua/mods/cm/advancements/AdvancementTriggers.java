package dev.necauqua.mods.cm.advancements;

import net.minecraft.advancements.CriteriaTriggers;

import static dev.necauqua.mods.cm.ChiseledMe.ns;

public final class AdvancementTriggers {
    public static final StatelessTrigger RECALIBRATOR_RESET = new StatelessTrigger(ns("recalibrator_reset"));
    public static final StatelessTrigger BLUE_STAR_DECRAFT = new StatelessTrigger(ns("blue_star_decraft"));
    public static final StatelessTrigger WEIRD_BEACON_CRAFT = new StatelessTrigger(ns("weird_beacon_craft"));

    public static void init() {
        CriteriaTriggers.register(RECALIBRATOR_RESET);
        CriteriaTriggers.register(BLUE_STAR_DECRAFT);
        CriteriaTriggers.register(WEIRD_BEACON_CRAFT);
    }
}
