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

import dev.necauqua.mods.cm.item.ItemRecalibrator;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.IntStream;

import static dev.necauqua.mods.cm.item.ItemRecalibrator.RecalibrationEffect.AMPLIFICATION;
import static dev.necauqua.mods.cm.item.ItemRecalibrator.RecalibrationEffect.REDUCTION;
import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Items.*;
import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPED;
import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS;

public final class Recipes {

    private Recipes() {
    }

    public static void init() {
        OreDictionary.registerOre("netherStar", ChiseledMe.BLUE_STAR);
        GameRegistry.addShapelessRecipe(new ItemStack(ChiseledMe.BLUE_STAR), NETHER_STAR, LAPIS_BLOCK);

        GameRegistry.addRecipe(new BlueStarDecraftRecipe());
        RecipeSorter.register("chiseled_me:blue_star_decraft", BlueStarDecraftRecipe.class, SHAPELESS, "after:minecraft:shapeless");

        GameRegistry.addRecipe(new OverridenBeaconRecipe());
        RecipeSorter.register("chiseled_me:overriden_beacon", OverridenBeaconRecipe.class, SHAPED, "after:minecraft:shaped before:forge:shapedore");

        GameRegistry.addRecipe(new PymContainerRecipe());
        RecipeSorter.register("chiseled_me:container", PymContainerRecipe.class, SHAPED, "after:minecraft:shaped before:minecraft:shapeless");

        GameRegistry.addShapelessRecipe(
            new ItemStack(ChiseledMe.PYM_ESSENSE),
            ChiseledMe.PYM_CONTAINER,
            REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK,
            REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK
        );
        for (int i = 1; i <= 8; i++) {
            Object[] params = new Object[i + 1];
            params[0] = DRAGON_BREATH;
            for (int j = 1; j <= i; j++) {
                params[j] = ChiseledMe.PYM_ESSENSE;
            }
            GameRegistry.addShapelessRecipe(ItemRecalibrator.create(REDUCTION, (byte) i), params);
        }
        if (Config.enableSupersmalls || Config.enableBigSizes) {
            GameRegistry.addShapedRecipe(
                new ItemStack(ChiseledMe.PYM_CONTAINER_X),
                "xyx",
                "yzy",
                "xyx",
                'x', IRON_BLOCK, 'y', DIAMOND_BLOCK, 'z', ChiseledMe.PYM_CONTAINER // this is so original i cant even..
            );
            GameRegistry.addShapelessRecipe(new ItemStack(ChiseledMe.PYM_ESSENSE_X), ChiseledMe.PYM_CONTAINER_X, NETHER_STAR, REDSTONE_BLOCK);
        }
        if (Config.enableSupersmalls) {
            for (int i = 1; i <= 4; i++) {
                Object[] params = new Object[i + 1];
                params[0] = DRAGON_BREATH;
                for (int j = 1; j <= i; j++) {
                    params[j] = ChiseledMe.PYM_ESSENSE_X;
                }
                GameRegistry.addShapelessRecipe(ItemRecalibrator.create(REDUCTION, (byte) (i + 8)), params);
            }
        }
        if (Config.enableBigSizes) {
            GameRegistry.addRecipe(new BlueEssenseRecipe());
            RecipeSorter.register("chiseled_me:blue_essense", BlueEssenseRecipe.class, SHAPELESS, "after:minecraft:shapeless");
            for (int i = 1; i <= 4; i++) {
                Object[] params = new Object[i + 1];
                params[0] = DRAGON_BREATH;
                for (int j = 1; j <= i; j++) {
                    params[j] = ChiseledMe.PYM_ESSENSE_B;
                }
                GameRegistry.addShapelessRecipe(ItemRecalibrator.create(AMPLIFICATION, (byte) i), params);
            }
        }
    }

    private static class OverridenBeaconRecipe extends ShapedRecipes {

        public OverridenBeaconRecipe() {
            super(3, 3, new ItemStack[]{
                new ItemStack(GLASS), new ItemStack(GLASS), new ItemStack(GLASS),
                new ItemStack(GLASS), new ItemStack(ChiseledMe.BLUE_STAR), new ItemStack(GLASS),
                new ItemStack(OBSIDIAN), new ItemStack(OBSIDIAN), new ItemStack(OBSIDIAN)
            }, createBlueBeacon());
        }

        @Override
        @Nonnull
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            ForgeHooks.getCraftingPlayer().addStat(Achievements.WEIRD_BEACON); // meh
            return super.getRemainingItems(inv);
        }

        private static ItemStack createBlueBeacon() {
            ItemStack beacon = new ItemStack(BEACON);
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("chiseled_me:color", (byte) 3);
            nbt.setTag("BlockEntityTag", tag);
            beacon.setTagCompound(nbt);
            return beacon;
        }
    }

    private static class PymContainerRecipe extends ShapedRecipes { // wait why recipes do not work with nbt??

        private static PotionType awkward = PotionType.getPotionTypeForName("minecraft:awkward");

        private PymContainerRecipe() {
            super(3, 3, new ItemStack[]{
                new ItemStack(IRON_INGOT), new ItemStack(DIAMOND), new ItemStack(IRON_INGOT),
                new ItemStack(DIAMOND), new ItemStack(POTIONITEM), new ItemStack(DIAMOND),
                new ItemStack(IRON_INGOT), new ItemStack(DIAMOND), new ItemStack(IRON_INGOT)
            }, new ItemStack(ChiseledMe.PYM_CONTAINER));
        }

        @Override
        public boolean matches(@Nonnull InventoryCrafting inv, World world) {
            if (super.matches(inv, world)) {
                ItemStack stack = inv.getStackInSlot(4);
                return PotionUtils.getPotionFromItem(stack) == awkward;
            }
            return false;
        }
    }

    private static class BlueEssenseRecipe extends ShapelessRecipes {

        private BlueEssenseRecipe() {
            super(new ItemStack(ChiseledMe.PYM_ESSENSE_B), Arrays.asList(new ItemStack(ChiseledMe.PYM_ESSENSE_X), new ItemStack(ChiseledMe.BLUE_STAR)));
        }

        @Override
        @Nonnull
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            NonNullList<ItemStack> remaining = super.getRemainingItems(inv);
            IntStream.range(0, inv.getSizeInventory())
                .filter(i -> inv.getStackInSlot(i).getItem() == ChiseledMe.BLUE_STAR)
                .forEach(i -> remaining.set(i, new ItemStack(NETHER_STAR)));
            return remaining;
        }
    }

    private static class BlueStarDecraftRecipe implements IRecipe {

        @Override
        public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
            boolean once = false;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.getItem() == ChiseledMe.BLUE_STAR) {
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

        @Override
        public int getRecipeSize() {
            return 9;
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
                .filter(i -> inv.getStackInSlot(i).getItem() == ChiseledMe.BLUE_STAR)
                .forEach(i -> ret.set(i, new ItemStack(NETHER_STAR)));
            ForgeHooks.getCraftingPlayer().addStat(Achievements.SURPRISE); // meh x2
            return ret;
        }
    }
}
