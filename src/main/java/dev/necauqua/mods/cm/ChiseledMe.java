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

import dev.necauqua.mods.cm.item.ItemMod;
import dev.necauqua.mods.cm.item.ItemRecalibrator;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.item.ItemRecalibrator.RecalibrationType.REDUCTION;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toSet;
import static net.minecraft.world.GameRules.ValueType.ANY_VALUE;
import static net.minecraft.world.GameRules.ValueType.BOOLEAN_VALUE;

@Mod(modid = MODID,
        version = "@VERSION@",
        acceptedMinecraftVersions = "@MC_VERSION_RANGE@",
        updateJSON = "https://raw.githubusercontent.com/wiki/necauqua/chiseled-me/updates.json",
        certificateFingerprint = "c677c954974252994736eb15e855e1e6fc5a2e62",
        useMetadata = true)
public final class ChiseledMe {

    public static final String MODID = "chiseled_me";

    public static final float LOWER_LIMIT = 0.000244140625f; // = 1/16/16/16 = 1/4096
    public static final float UPPER_LIMIT = 16.0f;
    public static final String ENTITY_SIZE_RULE = MODID + ":defaultEntitySize";
    public static final String PLAYER_SIZE_RULE = MODID + ":defaultPlayerSize";
    public static final String KEEP_SIZE_RULE = MODID + ":keepSize";

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
        try {
            for (ASMData asmData : e.getAsmData().getAll(Init.class.getName())) {
                String objectName = asmData.getObjectName();
                String name = objectName.substring(0, objectName.indexOf('('));
                Method method = Class.forName(asmData.getClassName()).getDeclaredMethod(name);
                Log.debug("Found init method " + method);
                method.setAccessible(true);
                method.invoke(null);
            }
        } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e1) {
            throw new AssertionError("This should not happen", e1);
        } catch (InvocationTargetException e1) {
            rethrow(e1.getCause());
        }

        // kinda could've been the default
        // possibly with separate annotation just for main event bus, idk
        try {
            for (String s : e.getAsmData().getAll(SubscribeEvent.class.getName())
                    .stream()
                    .map(ASMData::getClassName)
                    .filter(cls -> cls.startsWith("dev.necauqua.mods.cm") && !cls.contains("mixin"))
                    .collect(toSet())) {
                MinecraftForge.EVENT_BUS.register(Class.forName(s));
            }
        } catch (ClassNotFoundException ex) {
            throw new AssertionError("This should not happen", ex);
        }
    }

    @EventHandler
    public void on(FMLInitializationEvent e) {
        try {
            Class.forName("micdoodle8.mods.galacticraft.core.GalacticraftCore")
                    .getField("isHeightConflictingModInstalled")
                    .set(null, true); // IMC (or something) IS FOR LOOOOSEEERS

            // (if you are reading this source and can't get the sarcasm, I am not stating that
            // IMC is for losers and thats why I am mixing into their init, I am stating that they
            // must think that IMC is for losers as they don't have any, and their management of
            // `isHeightConflictingModInstalled` field is based on hardcode-detecting a couple of
            // popular mods that they know have such issues)
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
            // no gc
        }
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

    @EventHandler
    public void on(FMLServerStartingEvent e) {
        e.registerServerCommand(new SizeofCommand());
        GameRules gameRules = e.getServer().getWorld(0).getGameRules();
        gameRules.addGameRule(ENTITY_SIZE_RULE, "1", ANY_VALUE);
        gameRules.addGameRule(PLAYER_SIZE_RULE, "1", ANY_VALUE);
        gameRules.addGameRule(KEEP_SIZE_RULE, "false", BOOLEAN_VALUE);
    }

    public static ResourceLocation ns(String id) {
        return new ResourceLocation(MODID, id);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void rethrow(Throwable exception) throws E {
        throw (E) exception;
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface Init {}
}
