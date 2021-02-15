/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NBTPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.advancements.critereon.ItemPredicates;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.ChiseledMe.ns;
import static net.minecraft.init.Items.*;

@EventBusSubscriber(modid = MODID)
public final class PotionIngredientFactory implements IIngredientFactory {

    private static final Map<String, Item> potionTypes = new HashMap<>();

    static {
        potionTypes.put("regular", POTIONITEM);
        potionTypes.put("splash", SPLASH_POTION);
        potionTypes.put("lingering", LINGERING_POTION);
    }

    private static Item getPotionItem(JsonObject json) {
        String potionType = JsonUtils.getString(json, "potion_type", "regular");
        Item item = potionTypes.get(potionType);
        if (item == null) {
            throw new JsonSyntaxException("Expected potion_type to be one of regular, splash or lingering, was " + potionType);
        }
        return item;
    }

    private static PotionType getPotion(@Nullable JsonContext context, JsonObject json) {
        String s = JsonUtils.getString(json, "potion");
        String potionName = context == null ? s : context.appendModId(s);
        PotionType potion = PotionType.getPotionTypeForName(potionName);
        if (potion == null) {
            throw new JsonSyntaxException("No potion registered for name " + potionName);
        }
        return potion;
    }

    @Override
    @Nonnull
    public Ingredient parse(JsonContext context, JsonObject json) {
        Item item = getPotionItem(json);
        PotionType potion = getPotion(context, json);
        return new Ingredient(PotionUtils.addPotionToItemStack(new ItemStack(item), potion)) {
            @Override
            public boolean apply(@Nullable ItemStack stack) {
                return stack != null && stack.getItem() == item && PotionUtils.getPotionFromItem(stack) == potion;
            }
        };
    }

    @SubscribeEvent
    public static void itemPredicateHack(RegistryEvent.Register<IRecipe> e) {
        ItemPredicates.register(ns("potion"), json -> {
            Item item = getPotionItem(json);
            PotionType potion = getPotion(null, json);
            return new ItemPredicate(
                item,
                null,
                MinMaxBounds.UNBOUNDED,
                MinMaxBounds.UNBOUNDED,
                new EnchantmentPredicate[0],
                potion,
                NBTPredicate.ANY);
        });
    }
}
