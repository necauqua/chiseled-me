/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.recipes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.necauqua.mods.cm.Log;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.ChiseledMe.ns;

@EventBusSubscriber(modid = MODID)
public final class DumbRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @SubscribeEvent
    public static void dumpRecipesHack(RegistryEvent.Register<IRecipe> e) {
        ModContainer mod = Loader.instance().activeModContainer();
        if (mod == null) {
            throw new IllegalStateException("Active mod container is null, should be chiseled_me");
        }

        JsonContext ctx = new JsonContext(MODID);

        Gson recipesLikeGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        // @see CraftingHelper#loadRecipes(ModContainer)
        CraftingHelper.findFiles(mod, "assets/" + MODID + "/recipes", null,
            (root, file) -> {
                String relative = root.relativize(file).toString();
                if (!"json".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_")) {
                    return true;
                }

                String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");

                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    JsonObject json = JsonUtils.fromJson(recipesLikeGson, reader, JsonObject.class);
                    if (json == null) {
                        return true;
                    }
                    if (!CraftingHelper.processConditions(JsonUtils.getJsonArray(json, "conditions"), ctx)) {
                        ForgeRegistries.RECIPES.register(new DumbRecipe(name));
                        Log.trace("Registered a dumb recipe " + name);
                    }
                } catch (JsonParseException | IOException ex) {
                    return false;
                }
                return true;
            },
            true, true
        );
    }

    public DumbRecipe(String registryName) {
        setRegistryName(ns(registryName));
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
}
