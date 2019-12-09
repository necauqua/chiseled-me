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
        setTranslationKey(MODID + ":" + name);
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
