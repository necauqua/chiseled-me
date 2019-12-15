package dev.necauqua.mods.cm.advancements;

import dev.necauqua.mods.cm.ChiseledMe.OnInit;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;

import static dev.necauqua.mods.cm.ChiseledMe.ns;
import static dev.necauqua.mods.cm.asm.dsl.ASM.srg;

public final class AdvancementTriggers {
    public static final SizeTrigger SIZE = new SizeTrigger();
    public static final StatelessTrigger RECALIBRATOR_RESET = new StatelessTrigger(ns("recalibrator_reset"));
    public static final StatelessTrigger BLUE_STAR_DECRAFT = new StatelessTrigger(ns("blue_star_decraft"));
    public static final StatelessTrigger WEIRD_BEACON_CRAFT = new StatelessTrigger(ns("weird_beacon_craft"));

    private static final Method registerCriterion;
    static {
        registerCriterion = ReflectionHelper.findMethod(CriteriaTriggers.class, "register", srg("register", "CriteriaTriggers"), ICriterionTrigger.class);
        registerCriterion.setAccessible(true);
    }

    private static void register(ICriterionTrigger<?> trigger) {
        try {
            registerCriterion.invoke(null, trigger);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register trigger " + trigger.getId() + "!", e);
        }
    }

    @OnInit
    public static void init() {
        register(SIZE);
        register(RECALIBRATOR_RESET);
        register(BLUE_STAR_DECRAFT);
        register(WEIRD_BEACON_CRAFT);
    }
}
