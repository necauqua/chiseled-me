/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.item;

import dev.necauqua.mods.cm.ChiseledMe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.ChiseledMe.ns;

public class ItemMod extends Item {
    private boolean isGlowing = false;

    public ItemMod(String name) {
        setRegistryName(ns(name));
        setUnlocalizedName(MODID + ":" + name);
        setCreativeTab(ChiseledMe.TAB);
    }

    public ItemMod setGlowing() {
        isGlowing = true;
        return this;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return isGlowing || super.hasEffect(stack);
    }
}
