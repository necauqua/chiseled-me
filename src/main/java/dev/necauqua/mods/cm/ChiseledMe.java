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

import dev.necauqua.mods.cm.api.ChiseledMeAPI;
import dev.necauqua.mods.cm.api.ChiseledMeInterface;
import dev.necauqua.mods.cm.asm.dsl.ASM;
import dev.necauqua.mods.cm.cmd.GetSizeCommand;
import dev.necauqua.mods.cm.cmd.SetSizeCommand;
import dev.necauqua.mods.cm.item.CraftItem;
import dev.necauqua.mods.cm.item.ItemMod;
import dev.necauqua.mods.cm.item.ItemRecalibrator;
import dev.necauqua.mods.cm.item.ItemRecalibrator.RecalibrationEffect;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import javax.annotation.Nonnull;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;

@Mod(modid = MODID, version = "@VERSION@", updateJSON = "@UPDATE_URL@")
public final class ChiseledMe implements ChiseledMeInterface {

    public static final String MODID = "chiseled_me";

    public static final CreativeTabs TAB = new CreativeTabs(MODID) {

        private ItemStack icon;

        @Override
        @Nonnull
        public Item getTabIconItem() {
            return RECALIBRATOR; // whyyyyy is this abstract, MC's codebase is even worse than mine
        }

        @Override
        @Nonnull
        public ItemStack getIconItemStack() {
            if (icon == null) {
                icon = ItemRecalibrator.create(RecalibrationEffect.REDUCTION, (byte) 1);
            }
            return icon;
        }
    };

    public static final Item BLUE_STAR = new CraftItem("blue_star").bindAchievement(12).setGlowing();
    public static final Item PYM_CONTAINER = new CraftItem("pym_container").bindAchievement(0);
    public static final Item PYM_CONTAINER_X = new CraftItem("pym_container_x").bindAchievement(8).setMaxStackSize(16);
    public static final Item PYM_ESSENSE = new CraftItem("pym_essense").bindAchievement(1).setMaxStackSize(42);
    public static final Item PYM_ESSENSE_X = new CraftItem("pym_essense_x").bindAchievement(9).setMaxStackSize(13);
    public static final Item PYM_ESSENSE_B = new CraftItem("pym_essense_b").bindAchievement(15).setMaxStackSize(7);

    public static final ItemRecalibrator RECALIBRATOR = new ItemRecalibrator();

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        ASM.check();
        populateApi(this);
        Config.init(e.getModConfigurationDirectory());
        Network.init();
        EntitySizeManager.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        RandomUtils.forEachStaticField(getClass(), ItemMod.class, ItemMod::init);
        Handlers.init();
        Achievements.init();
        Recipes.init();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent e) {
        e.registerServerCommand(new GetSizeCommand());
        e.registerServerCommand(new SetSizeCommand());
    }

    @Override
    public float getSizeOf(Entity entity) {
        return EntitySizeManager.getSize(entity);
    }

    @Override
    public float getRenderSizeOf(Entity entity, float partialTick) {
        return EntitySizeManager.getRenderSize(entity, partialTick);
    }

    @Override
    public void setSizeOf(Entity entity, float size, boolean interp) {
        EntitySizeManager.setSize(entity, size, interp);
    }

    private static void populateApi(ChiseledMeInterface api) {
        try {
            EnumHelper.setFailsafeFieldValue(ChiseledMeAPI.class.getField("interaction"), null, api);
        } catch (Exception e) {
            throw new AssertionError("This should not happen", e);
        }
    }
}
