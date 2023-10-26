/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm2;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static dev.necauqua.mods.cm2.ChiseledMe2.MODID;
import static dev.necauqua.mods.cm2.ChiseledMe2.ns;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class StatelessTrigger extends AbstractCriterionTrigger<CriterionInstance> {

    public static final StatelessTrigger RECALIBRATOR_RESET = new StatelessTrigger(ns("recalibrator_reset"));
    public static final StatelessTrigger BLUE_STAR_DECRAFT = new StatelessTrigger(ns("blue_star_decraft"));
    private final ResourceLocation id;

    public StatelessTrigger(ResourceLocation id) {
        this.id = id;
    }

    @SubscribeEvent
    public static void on(FMLCommonSetupEvent e) {
        CriteriaTriggers.register(RECALIBRATOR_RESET);
        CriteriaTriggers.register(BLUE_STAR_DECRAFT);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public CriterionInstance createInstance(JsonObject json, EntityPredicate.AndPredicate predicate, ConditionArrayParser parser) {
        return new CriterionInstance(id, predicate) {};
    }

    public void trigger(ServerPlayerEntity player) {
        trigger(player, $ -> true);
    }
}
