/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.theta.data;

import com.google.gson.JsonObject;
import dev.necauqua.mods.theta.Config;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import static dev.necauqua.mods.theta.Theta.MODID;
import static dev.necauqua.mods.theta.Theta.ns;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class ConfigCraftingCondition implements ICondition {

    @SubscribeEvent
    public static void on(Register<IRecipeSerializer<?>> e) {
        CraftingHelper.register(new ConfigCraftingCondition.Serializer(Config.enableBigSizes, ns("big_enabled")));
        CraftingHelper.register(new ConfigCraftingCondition.Serializer(Config.enableSupersmalls, ns("supersmall_enabled")));
    }

    private final ConfigValue<Boolean> config;
    private final ResourceLocation name;

    public ConfigCraftingCondition(ConfigValue<Boolean> config, ResourceLocation name) {
        this.config = config;
        this.name = name;
    }

    @Override
    public ResourceLocation getID() {
        return name;
    }

    @Override
    public boolean test() {
        return config.get();
    }

    @Override
    public String toString() {
        return name.getPath();
    }

    public static class Serializer implements IConditionSerializer<ConfigCraftingCondition> {

        private final ConfigCraftingCondition instance;

        public Serializer(ConfigValue<Boolean> config, ResourceLocation name) {
            instance = new ConfigCraftingCondition(config, name);
        }

        @Override
        public void write(JsonObject json, ConfigCraftingCondition value) {}

        @Override
        public ConfigCraftingCondition read(JsonObject json) {
            return instance;
        }

        @Override
        public ResourceLocation getID() {
            return instance.getID();
        }
    }
}
