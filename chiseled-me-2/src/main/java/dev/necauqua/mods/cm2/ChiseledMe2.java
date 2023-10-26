/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm2;

import dev.necauqua.mods.cm2.recipes.EnhancedShapedRecipe;
import dev.necauqua.mods.cm2.recipes.EnhancedShapelessRecipe;
import net.minecraft.item.*;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

import static dev.necauqua.mods.cm2.ItemRecalibrator.RecalibrationType.REDUCTION;

@Mod(ChiseledMe2.MODID)
@EventBusSubscriber(bus = Bus.MOD)
public final class ChiseledMe2 {

    public static final String MODID = "chiseled_me_2";

    public static final ItemGroup TAB = new ItemGroup(MODID) {

        @Override
        @Nonnull
        public ItemStack makeIcon() {
            return RECALIBRATOR.get().create(REDUCTION, (byte) 1);
        }
    };

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public static final RegistryObject<ItemRecalibrator> RECALIBRATOR = ITEMS.register("recalibrator", ItemRecalibrator::new);

    public ChiseledMe2() {
        Config.init();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register("blue_star", () -> new SimpleFoiledItem(new Properties().tab(TAB).rarity(Rarity.UNCOMMON)) {
            @Override
            public ItemStack getContainerItem(ItemStack itemStack) {
                return super.getContainerItem(itemStack);
            }
        });
        ITEMS.register("pym_container", () -> new Item(new Properties().tab(TAB)));
        ITEMS.register("pym_container_x", () -> new Item(new Properties().tab(TAB)));
        ITEMS.register("pym_essence", () -> new Item(new Properties().tab(TAB)));
        ITEMS.register("pym_essence_x", () -> new Item(new Properties().tab(TAB)));
        ITEMS.register("pym_essence_b", () -> new Item(new Properties().tab(TAB)));

        RECIPE_SERIALIZERS.register("enhanced_shaped", EnhancedShapedRecipe.Serializer::new);
        RECIPE_SERIALIZERS.register("enhanced_shapeless", EnhancedShapelessRecipe.Serializer::new);

        ITEMS.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }

    public static ResourceLocation ns(String path) {
        return new ResourceLocation(MODID, path);
    }
}
