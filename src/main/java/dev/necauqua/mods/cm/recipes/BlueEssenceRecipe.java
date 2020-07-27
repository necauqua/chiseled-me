package dev.necauqua.mods.cm.recipes;

import dev.necauqua.mods.cm.Config;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

import static dev.necauqua.mods.cm.ChiseledMe.ns;
import static net.minecraft.init.Items.NETHER_STAR;

public final class BlueEssenceRecipe extends ShapelessRecipes {

    @ObjectHolder("chiseled_me:blue_star")
    private static Item BLUE_STAR;

    @ObjectHolder("chiseled_me:pym_essence_b")
    private static Item ESSENCE_B;

    @ObjectHolder("chiseled_me:pym_essence_x")
    private static Item ESSENCE_X;

    public BlueEssenceRecipe() {
        super("", new ItemStack(ESSENCE_B), createIngredients());
        setRegistryName(ns("pym_essence_b"));
    }

    private static NonNullList<Ingredient> createIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.fromItem(ESSENCE_X));
        ingredients.add(Ingredient.fromItem(BLUE_STAR));
        return ingredients;
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



    @SubscribeEvent
    public static void on(RegistryEvent.Register<IRecipe> e) {
        e.getRegistry().register(Config.enableBigSizes ?
            new BlueEssenceRecipe() :
            new DumbRecipe("pym_essence_b"));
    }
}
