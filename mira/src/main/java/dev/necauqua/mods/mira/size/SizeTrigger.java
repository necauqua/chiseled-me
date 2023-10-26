/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.size;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.necauqua.mods.mira.size.SizeTrigger.Instance;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static dev.necauqua.mods.mira.Mira.MODID;
import static dev.necauqua.mods.mira.Mira.ns;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class SizeTrigger extends AbstractCriterionTrigger<Instance> {

    private static final ResourceLocation ID = ns("size");
    private static final BiMap<String, SizeMatcher> MATCHERS = HashBiMap.create();

    private static final SizeTrigger INSTANCE = new SizeTrigger();

    static {
        MATCHERS.put("exact", ((fromSize, toSize, condition) -> toSize == condition));
        MATCHERS.put("crossing", ((fromSize, toSize, condition) -> fromSize < toSize ?
            fromSize <= condition && condition <= toSize : toSize <= condition && condition <= fromSize));
        MATCHERS.put("lt", ((fromSize, toSize, condition) -> toSize < condition));
        MATCHERS.put("gt", ((fromSize, toSize, condition) -> toSize > condition));
        MATCHERS.put("le", ((fromSize, toSize, condition) -> toSize <= condition));
        MATCHERS.put("ge", ((fromSize, toSize, condition) -> toSize >= condition));
    }

    private SizeTrigger() {
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent e) {
        CriteriaTriggers.register(INSTANCE);
    }

    public static void trigger(ServerPlayerEntity player, double fromSize, double toSize) {
        INSTANCE.trigger(player, instance -> instance.matcher.match(fromSize, toSize, instance.size));
    }

    @Override
    protected Instance createInstance(JsonObject json, AndPredicate predicate, ConditionArrayParser parser) {
        float size = JSONUtils.getAsFloat(json, "size");
        String matchStr = JSONUtils.getAsString(json, "match", "exact");
        SizeMatcher matcher = MATCHERS.get(matchStr);
        if (matcher == null) {
            throw new JsonSyntaxException("Expected match to be one of " + String.join(", ", MATCHERS.keySet()) + ", was " + matchStr);
        }
        return new Instance(size, matcher, predicate);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @FunctionalInterface
    public interface SizeMatcher {

        boolean match(double fromSize, double toSize, double condition);
    }

    public static final class Instance extends CriterionInstance {

        private final float size;
        private final SizeMatcher matcher;

        public Instance(float size, SizeMatcher matcher, AndPredicate predicate) {
            super(ID, predicate);
            this.size = size;
            this.matcher = matcher;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject json = super.serializeToJson(serializer);
            json.addProperty("size", size);
            String matcher = MATCHERS.inverse().get(this.matcher);
            if (matcher == null) {
                throw new IllegalStateException("Trying to serialize an unregistered matcher");
            }
            json.addProperty("match", matcher);
            return json;
        }

        @Override
        public String toString() {
            return "SizeTrigger{size=" + size + ", matcher=" + MATCHERS.inverse().get(matcher) + '}';
        }
    }
}
