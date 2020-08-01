package dev.necauqua.mods.cm.recipes;

import dev.necauqua.mods.cm.Log;
import dev.necauqua.mods.cm.advancements.AdvancementTriggers;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static dev.necauqua.mods.cm.ChiseledMe.ns;
import static net.minecraft.init.Blocks.*;

public final class WeirdBeaconRecipe extends ShapedRecipes {

    @ObjectHolder("chiseled_me:blue_star")
    private static Item BLUE_STAR;

    private static final ItemStack RESULT = new ItemStack(BEACON);
    private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.create();

    static {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagCompound blockTag = new NBTTagCompound();
        blockTag.setByte("chiseled_me:color", (byte) 3);
        nbt.setTag("BlockEntityData", blockTag);
        RESULT.setTagCompound(nbt);

        Ingredient glass = Ingredient.fromItem(Item.getItemFromBlock(GLASS));
        Ingredient obsidian = Ingredient.fromItem(Item.getItemFromBlock(OBSIDIAN));
        INGREDIENTS.add(glass);
        INGREDIENTS.add(glass);
        INGREDIENTS.add(glass);
        INGREDIENTS.add(glass);
        INGREDIENTS.add(Ingredient.fromItem(BLUE_STAR));
        INGREDIENTS.add(glass);
        INGREDIENTS.add(obsidian);
        INGREDIENTS.add(obsidian);
        INGREDIENTS.add(obsidian);
    }

    public WeirdBeaconRecipe() {
        super("", 3, 3, INGREDIENTS, RESULT);
        setRegistryName(ns("weird_beacon"));
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        AdvancementTriggers.WEIRD_BEACON_CRAFT.trigger(ForgeHooks.getCraftingPlayer());
        return super.getRemainingItems(inv);
    }

    @SubscribeEvent
    public static void on(RegistryEvent.Register<IRecipe> e) {
        e.getRegistry().register(new WeirdBeaconRecipe());

        // disallow the blue star in the standard beacon recipe
        // it is allowed there because its registered as a nether star in ore dictionary
        // and vanilla recipes take precedence over the custom ones
        ShapedRecipes beaconRecipe = (ShapedRecipes) CraftingManager.REGISTRY.getObject(new ResourceLocation("beacon"));
        if (beaconRecipe == null) {
            Log.warn("Did not found the vanilla beacon recipe to fix");
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
}
