package dev.necauqua.mods.cm.advancements;

import dev.necauqua.mods.cm.ChiseledMe.Init;
import net.minecraft.advancements.CriteriaTriggers;

import static dev.necauqua.mods.cm.ChiseledMe.ns;

public final class AdvancementTriggers {
    public static final StatelessTrigger RECALIBRATOR_RESET = new StatelessTrigger(ns("recalibrator_reset"));
    public static final StatelessTrigger BLUE_STAR_DECRAFT = new StatelessTrigger(ns("blue_star_decraft"));
    public static final StatelessTrigger WEIRD_BEACON_CRAFT = new StatelessTrigger(ns("weird_beacon_craft"));

    @Init
    private static void init() {
        CriteriaTriggers.register(RECALIBRATOR_RESET);
        CriteriaTriggers.register(BLUE_STAR_DECRAFT);
        CriteriaTriggers.register(WEIRD_BEACON_CRAFT);
    }
}
