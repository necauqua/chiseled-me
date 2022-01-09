/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm2;

import net.minecraft.item.*;
import net.minecraft.item.Item.Properties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.annotation.Nonnull;

import static dev.necauqua.mods.cm2.ItemRecalibrator.RecalibrationType.REDUCTION;

@Mod(ChiseledMe.MODID)
@EventBusSubscriber(bus = Bus.MOD)
public final class ChiseledMe {

    public static final String MODID = "chiseled_me";

    public static final ItemGroup TAB = new ItemGroup(MODID) {

        @Override
        @Nonnull
        public ItemStack makeIcon() {
            return ItemRecalibrator.INSTANCE.create(REDUCTION, (byte) 1);
        }
    };

    public ChiseledMe() {
        Config.init();
    }

    @SubscribeEvent
    public static void on(RegistryEvent.Register<Item> e) {
        e.getRegistry().registerAll(
            new SimpleFoiledItem(new Properties().tab(TAB).rarity(Rarity.UNCOMMON)) {
                @Override
                public ItemStack getContainerItem(ItemStack itemStack) {
                    return super.getContainerItem(itemStack);
                }
            }.setRegistryName(ns("blue_star")),
            new Item(new Properties().tab(TAB)).setRegistryName(ns("pym_container")),
            new Item(new Properties().tab(TAB).stacksTo(16)).setRegistryName(ns("pym_container_x")),
            new Item(new Properties().tab(TAB).stacksTo(42)).setRegistryName(ns("pym_essence")),
            new Item(new Properties().tab(TAB).stacksTo(13)).setRegistryName(ns("pym_essence_x")),
            new Item(new Properties().tab(TAB).stacksTo(7)).setRegistryName(ns("pym_essence_b"))
        );
    }

    public static ResourceLocation ns(String path) {
        return new ResourceLocation(MODID, path);
    }
}
