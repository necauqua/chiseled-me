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

import dev.necauqua.mods.cm.Achievements;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class CraftItem extends ItemMod {

    private int boundAchievementId = -1;
    private boolean isGlowing = false;

    public CraftItem(String name) {
        super(name);
    }

    // this method uses int id because circular static class loading results in
    // Achievements class loading before ChiseledMe class and thus
    // items (and so their icons in achievements) are null
    public CraftItem bindAchievement(int id) {
        boundAchievementId = id;
        return this;
    }

    public CraftItem setGlowing() {
        isGlowing = true;
        return this;
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        if (boundAchievementId != -1) {
            player.addStat(Achievements.get(boundAchievementId));
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return isGlowing || super.hasEffect(stack);
    }
}
