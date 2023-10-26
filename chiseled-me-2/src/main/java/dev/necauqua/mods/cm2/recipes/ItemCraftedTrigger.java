/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm2.recipes;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static dev.necauqua.mods.cm2.ChiseledMe2.MODID;
import static dev.necauqua.mods.cm2.ChiseledMe2.ns;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class ItemCraftedTrigger extends AbstractCriterionTrigger<ItemCraftedTrigger.Instance> {

    private static final ResourceLocation ID = ns("item_crafted");
    private static final ItemCraftedTrigger INSTANCE = new ItemCraftedTrigger();

    @SubscribeEvent
    public static void on(FMLCommonSetupEvent e) {
        CriteriaTriggers.register(INSTANCE);
    }

    public static void trigger(ServerPlayerEntity player, ItemStack item) {
        INSTANCE.trigger(player, instance -> instance.item.matches(item));
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ItemCraftedTrigger.Instance createInstance(JsonObject json, AndPredicate predicate, ConditionArrayParser parser) {
        return new Instance(predicate, ItemPredicate.fromJson(json.get("predicate")));
    }

    @EventBusSubscriber(modid = MODID)
    private static final class ForgeHandler {

        @SubscribeEvent
        public static void on(ItemCraftedEvent e) {
            PlayerEntity player = e.getPlayer();
            if (player instanceof ServerPlayerEntity) {
                trigger((ServerPlayerEntity) player, e.getCrafting());
            }
        }
    }

    public static final class Instance extends CriterionInstance {

        private final ItemPredicate item;

        public Instance(AndPredicate predicate, ItemPredicate item) {
            super(ID, predicate);
            this.item = item;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject json = super.serializeToJson(serializer);
            json.add("predicate", item.serializeToJson());
            return json;
        }
    }
}
