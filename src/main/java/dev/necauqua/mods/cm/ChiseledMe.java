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
import dev.necauqua.mods.cm.asm.ChiseledMePlugin;
import dev.necauqua.mods.cm.item.ItemMod;
import dev.necauqua.mods.cm.item.ItemRecalibrator;
import dev.necauqua.mods.cm.size.EntitySizeManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.item.ItemRecalibrator.RecalibrationType.REDUCTION;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toSet;

@Mod(modid = MODID,
    version = "@VERSION@",
//    dependencies = "required:forge@[@FORGE_VERSION@,);",
    acceptedMinecraftVersions = "@MC_VERSION_RANGE@",
    updateJSON = "https://raw.githubusercontent.com/necauqua/chiseled-me/master/updates.json",
    certificateFingerprint = "c677c954974252994736eb15e855e1e6fc5a2e62",
    useMetadata = true)
public final class ChiseledMe implements ChiseledMeInterface {

    public static final String MODID = "chiseled_me";

    public static final CreativeTabs TAB = new CreativeTabs(MODID) {

        @Override
        @Nonnull
        public ItemStack createIcon() {
            return ItemRecalibrator.create(REDUCTION, (byte) 1);
        }
    };

    public static final ItemRecalibrator RECALIBRATOR = new ItemRecalibrator();
    public static final Item[] ITEMS = {
        RECALIBRATOR,
        new ItemMod("blue_star").setGlowing(),
        new ItemMod("pym_container"),
        new ItemMod("pym_container_x").setMaxStackSize(16),
        new ItemMod("pym_essence").setMaxStackSize(42),
        new ItemMod("pym_essence_x").setMaxStackSize(13),
        new ItemMod("pym_essence_b").setMaxStackSize(7)
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        populateApi(this);

        addHooks(preInits, e.getAsmData(), OnPreInit.class);
        addHooks(inits, e.getAsmData(), OnInit.class);
        addHooks(postInits, e.getAsmData(), OnPostInit.class);

        preInits.forEach(Runnable::run);

        // kinda could've been the default
        // possibly with separate annotation just for main event bus, idk
        e.getAsmData().getAll(SubscribeEvent.class.getName())
            .stream()
            .filter(s -> s.getClassName().startsWith("dev.necauqua.mods.cm"))
            .collect(toSet())
            .forEach(asmData -> {
                try {
                    MinecraftForge.EVENT_BUS.register(Class.forName(asmData.getClassName()));
                } catch (ClassNotFoundException ex) {
                    throw new AssertionError("This should not happen", ex);
                }
            });
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        inits.forEach(Runnable::run);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        postInits.forEach(Runnable::run);
    }

    @EventHandler
    public void onViolatedFingerprint(FMLFingerprintViolationEvent e) {
        Log.warn("FINGERPRINT VIOLATED: you're running some unauthorized modification of the mod, be warned");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().registerAll(ITEMS);

        // heh
        OreDictionary.registerOre("netherStar", ITEMS[1]);
    }

    @SubscribeEvent
    public static void registerItemModels(ModelRegistryEvent event) {
        Arrays.stream(ITEMS).forEach(SidedHandler.instance::registerDefaultModel);
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent e) {
        e.registerServerCommand(new SizeofCommand());
    }

    @Override
    public float getSizeOf(Entity entity) {
        return (float) EntitySizeManager.getSize(entity);
    }

    @Override
    public float getRenderSizeOf(Entity entity, float partialTick) {
        return (float) EntitySizeManager.getRenderSize(entity, partialTick);
    }

    @Override
    public void setSizeOf(Entity entity, float size, boolean interp) {
        EntitySizeManager.setSizeAndSync(entity, size, interp);
    }

    public static ResourceLocation ns(String id) {
        return new ResourceLocation(MODID, id);
    }

    private static void populateApi(ChiseledMeInterface api) {
        try {
            EnumHelper.setFailsafeFieldValue(ChiseledMeAPI.class.getField("interaction"), null, api);
        } catch (Exception e) {
            throw new AssertionError("This should not happen", e);
        }
    }

    private static final List<Runnable> preInits = new ArrayList<>();
    private static final List<Runnable> inits = new ArrayList<>();
    private static final List<Runnable> postInits = new ArrayList<>();

    private static void addHooks(List<Runnable> list, ASMDataTable asmDataTable, Class<? extends Annotation> annotationType) {
        asmDataTable.getAll(annotationType.getName())
            .forEach(asmData ->
                list.add(() -> {
                    try {
                        String cls = asmData.getClassName();
                        if (!cls.startsWith("dev.necauqua.mods.cm")) {
                            // should not happen as nobody should use my annotations I guess lol
                            return;
                        }
                        String objectName = asmData.getObjectName();
                        Class.forName(cls)
                            .getMethod(objectName.substring(0, objectName.indexOf('(')))
                            .invoke(null);
                    } catch (InvocationTargetException e) {
                        rethrow(e.getCause());
                        e.printStackTrace();
                    } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ex) {
                        throw new AssertionError("This should not happen", ex);
                    }
                }));
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void rethrow(Throwable exception) throws E {
        throw (E) exception;
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface OnPreInit {}

    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface OnInit {}

    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface OnPostInit {}
}
