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

import dev.necauqua.mods.cm.advancements.AdvancementTriggers;
import dev.necauqua.mods.cm.item.ItemMod;
import dev.necauqua.mods.cm.item.ItemRecalibrator;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.UUID;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.item.ItemRecalibrator.RecalibrationType.REDUCTION;

@Mod(modid = MODID,
        version = "@VERSION@",
        dependencies = "required:size@[1.12-1.0.0.0,)",
        acceptedMinecraftVersions = "@MC_VERSION_RANGE@",
        updateJSON = "https://raw.githubusercontent.com/necauqua/chiseled-me.wiki/master/updates.json",
        certificateFingerprint = "c677c954974252994736eb15e855e1e6fc5a2e62",
        useMetadata = true)
@EventBusSubscriber
public final class ChiseledMe {

    public static final String MODID = "chiseled_me";

    public static final CreativeTabs TAB = new CreativeTabs(MODID) {

        @Override
        @Nonnull
        public ItemStack getTabIconItem() {
            return RECALIBRATOR.create(REDUCTION, (byte) 1);
        }
    };

    private static final ItemRecalibrator RECALIBRATOR = new ItemRecalibrator();
    private static final ItemMod BLUE_STAR = new ItemMod("blue_star").setGlowing();

    private static final Item[] ITEMS = {
            RECALIBRATOR,
            BLUE_STAR,
            new ItemMod("pym_container"),
            new ItemMod("pym_container_x").setMaxStackSize(16),
            new ItemMod("pym_essence").setMaxStackSize(42),
            new ItemMod("pym_essence_x").setMaxStackSize(13),
            new ItemMod("pym_essence_b").setMaxStackSize(7)
    };

    @EventHandler
    public void on(FMLPreInitializationEvent e) {
        AdvancementTriggers.init();
        Config.init();
    }

    @EventHandler
    public void on(FMLFingerprintViolationEvent e) {
        Log.warn("FINGERPRINT VIOLATED: you're running some unauthorized modification of the mod, be warned. " +
                "No support will be provided for any issues encountered while using this jar.");
    }

    @SubscribeEvent
    public static void on(RegistryEvent.Register<Item> e) {
        e.getRegistry().registerAll(ITEMS);
        OreDictionary.registerOre("netherStar", BLUE_STAR);
    }

    @SubscribeEvent
    public static void on(ModelRegistryEvent e) {
        Arrays.stream(ITEMS).forEach(SidedHandler.instance::registerDefaultModel);
    }

    // everybody has some kind of this, I want it too, lol
    @SubscribeEvent
    public static void on(PlayerEvent.NameFormat e) {
        UUID id = e.getEntityPlayer().getGameProfile().getId();

        if (id.getMostSignificantBits() == 0xf98e93652c5248c5L &&
                id.getLeastSignificantBits() == 0x86476662f70b7e3dL) {
            e.setDisplayname("§o§dnecauqua§r");
        }
    }

    public static ResourceLocation ns(String id) {
        return new ResourceLocation(MODID, id);
    }
}
