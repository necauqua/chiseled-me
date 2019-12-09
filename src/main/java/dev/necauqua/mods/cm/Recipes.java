/*
 * Copyright (c) 2016-2019 Anton Bulakh <necauqua@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.necauqua.mods.cm;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.necauqua.mods.cm.ChiseledMe.OnInit;
import dev.necauqua.mods.cm.advancements.AdvancementTriggers;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

import static dev.necauqua.mods.cm.ChiseledMe.ns;
import static net.minecraft.init.Blocks.LAPIS_BLOCK;
import static net.minecraft.init.Items.*;

public final class Recipes {

    @ObjectHolder("chiseled_me:blue_star")
    private static Item BLUE_STAR = null;

    @ObjectHolder("chiseled_me:pym_essence_b")
    private static Item ESSENCE_B = null;

    @ObjectHolder("chiseled_me:pym_essence_x")
    private static Item ESSENCE_X = null;

    private Recipes() {}

    @SuppressWarnings("unused") // referred from json
    public static final class SupersmallsEnabledCondition implements IConditionFactory {

        @Override
        public BooleanSupplier parse(JsonContext context, JsonObject json) {
            return () -> Config.enableSupersmalls;
        }
    }

    @SuppressWarnings("unused") // referred from json
    public static final class BigEnabledCondition implements IConditionFactory {

        @Override
        public BooleanSupplier parse(JsonContext context, JsonObject json) {
            return () -> Config.enableBigSizes;
        }
    }

    @SuppressWarnings("unused") // referred from json
    public static final class PotionIngredientFactory implements IIngredientFactory {

        private static final Map<String, Item> potionTypes = new HashMap<>();

        static {
            potionTypes.put("regular", POTIONITEM);
            potionTypes.put("splash", SPLASH_POTION);
            potionTypes.put("lingering", LINGERING_POTION);
        }

        @Override
        @Nonnull
        public Ingredient parse(JsonContext context, JsonObject json) {
            String potionType = JsonUtils.getString(json, "potion_type", "regular");
            Item item = potionTypes.get(potionType);
            if (item == null) {
                throw new JsonSyntaxException("Expected potion_type to be one of regular, splash or lingering, was " + potionType);
            }
            String potionName = context.appendModId(JsonUtils.getString(json, "potion"));
            PotionType potion = PotionType.getPotionTypeForName(potionName);
            if (potion == null) {
                throw new JsonSyntaxException("No potion registered for name " + potionName);
            }
            return new Ingredient(PotionUtils.addPotionToItemStack(new ItemStack(item), potion)) {
                @Override
                public boolean apply(@Nullable ItemStack stack) {
                    return stack != null && stack.getItem() == item && PotionUtils.getPotionFromItem(stack) == potion;
                }
            };
        }
    }

    @OnInit
    public static void beaconRecipeHack() {
        // disallow the blue star in the standard beacon recipe
        // it is allowed there because its registered as a nether star in ore dictionary
        // and vanilla recipes take precedence over the custom ones

        ShapedRecipes beaconRecipe = (ShapedRecipes) CraftingManager.REGISTRY.getObject(new ResourceLocation("beacon"));
        if (beaconRecipe == null) {
            Log.warn("Failed to fix the beacon recipe");
            return;
        }
        NonNullList<Ingredient> ingredients = beaconRecipe.getIngredients();
        Ingredient ore = ingredients.get(4);
        Ingredient fixed = new Ingredient(ore.getMatchingStacks()) {
            @Override
            public ItemStack[] getMatchingStacks() {
                return ore.getMatchingStacks();
            }

            @Override
            @SideOnly(Side.CLIENT)
            public IntList getValidItemStacksPacked() {
                return ore.getValidItemStacksPacked();
            }

            @Override
            public boolean apply(@Nullable ItemStack stack) {
                return (stack == null || stack.getItem() != BLUE_STAR) && ore.apply(stack);
            }
        };
        ingredients.set(4, fixed);
    }

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> e) {
        OreDictionary.registerOre("netherStar", BLUE_STAR);

        e.getRegistry().register(new BlueStarDecraftRecipe().setRegistryName(ns("blue_star_decraft")));

        if (Config.enableBigSizes) {
            e.getRegistry().register(new BlueEssenceRecipe().setRegistryName(ns("pym_essence_b")));
        }
    }

    private static class BlueEssenceRecipe extends ShapelessRecipes {

        private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.create();

        static {
            INGREDIENTS.add(Ingredient.fromItem(ESSENCE_X));
            INGREDIENTS.add(Ingredient.fromItem(BLUE_STAR));
        }

        private BlueEssenceRecipe() {
            super("", new ItemStack(ESSENCE_B), INGREDIENTS);
        }

        @Override
        @Nonnull
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            NonNullList<ItemStack> remaining = super.getRemainingItems(inv);
            IntStream.range(0, inv.getSizeInventory())
                .filter(i -> inv.getStackInSlot(i).getItem() == BLUE_STAR)
                .forEach(i -> remaining.set(i, new ItemStack(NETHER_STAR)));
            return remaining;
        }
    }

    private static class BlueStarDecraftRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

        @Override
        public boolean canFit(int width, int height) {
            return width * height >= 1;
        }

        @Override
        public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
            boolean once = false;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.getItem() == BLUE_STAR) {
                    once = true;
                } else if (!stack.isEmpty()) {
                    return false;
                }
            }
            return once;
        }

        @Nonnull
        @Override
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
            long count = IntStream.range(0, inv.getSizeInventory()).filter(i -> !inv.getStackInSlot(i).isEmpty()).count();
            return new ItemStack(LAPIS_BLOCK, (int) count);
        }

        @Nonnull
        @Override
        public ItemStack getRecipeOutput() {
            return new ItemStack(LAPIS_BLOCK);
        }

        @Override
        @Nonnull
        public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
            NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
            IntStream.range(0, inv.getSizeInventory())
                .filter(i -> inv.getStackInSlot(i).getItem() == BLUE_STAR)
                .forEach(i -> ret.set(i, new ItemStack(NETHER_STAR)));

            AdvancementTriggers.BLUE_STAR_DECRAFT.trigger(ForgeHooks.getCraftingPlayer());

            return ret;
        }

        @Override
        public NonNullList<Ingredient> getIngredients() {
            NonNullList<Ingredient> list = NonNullList.create();
            list.add(Ingredient.fromItem(BLUE_STAR));
            return list;
        }
    }
}
