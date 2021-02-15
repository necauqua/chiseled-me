/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.recipes;

import dev.necauqua.mods.cm.advancements.AdvancementTriggers;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.ChiseledMe.ns;
import static net.minecraft.init.Blocks.LAPIS_BLOCK;
import static net.minecraft.init.Items.NETHER_STAR;

@EventBusSubscriber(modid = MODID)
public final class BlueStarDecraftRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @ObjectHolder("chiseled_me:blue_star")
    private static Item BLUE_STAR;

    public BlueStarDecraftRecipe() {
        setRegistryName(ns("blue_star_decraft"));
    }

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

    @SubscribeEvent
    public static void on(RegistryEvent.Register<IRecipe> e) {
        e.getRegistry().register(new BlueStarDecraftRecipe());
    }
}
