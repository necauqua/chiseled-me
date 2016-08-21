/*
 * Copyright (c) 2016 Anton Bulakh
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

package necauqua.mods.cm;

import necauqua.mods.cm.api.ChiseledMeAPI;
import necauqua.mods.cm.api.ChiseledMeInterface;
import necauqua.mods.cm.asm.ASM;
import necauqua.mods.cm.cmd.GetSizeCommand;
import necauqua.mods.cm.cmd.SetSizeCommand;
import necauqua.mods.cm.item.CraftItem;
import necauqua.mods.cm.item.ItemMod;
import necauqua.mods.cm.item.ItemRecalibrator;
import necauqua.mods.cm.item.ItemRecalibrator.RecalibrationEffect;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import javax.annotation.Nonnull;

@Mod(modid = "chiseled_me", version = "@VERSION@")
public final class ChiseledMe implements ChiseledMeInterface {

    public static final CreativeTabs TAB = new CreativeTabs("chiseled_me") {

        private ItemStack icon;

        @Override
        @Nonnull
        public Item getTabIconItem() {
            return RECALIBRATOR; // whyyyyy is this abstract, MC's codebase is even worse than mine
        }

        @Override
        @Nonnull
        public ItemStack getIconItemStack() {
            if(icon == null) {
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
        ChiseledMeAPI.interaction = this;
        Config.init(e.getModConfigurationDirectory());
        Network.init();
        EntitySizeManager.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        RandomUtils.forEachStaticField(ItemMod.class, ItemMod::init);
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
    public void setSizeOf(Entity entity, float size, boolean interp) {
        EntitySizeManager.setSize(entity, size, interp);
    }
}
