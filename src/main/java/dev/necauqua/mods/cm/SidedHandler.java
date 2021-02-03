/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm;

import dev.necauqua.mods.cm.item.ItemRecalibrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Objects;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;

public abstract class SidedHandler {

    @SidedProxy(modId = MODID)
    public static SidedHandler instance;

    public void registerDefaultModel(Item item) {}

    @Nullable
    public World getClientWorld() {
        return null;
    }

    @Nullable
    public EntityPlayer getClientPlayer() {
        return null;
    }

    public String getLocalization(String key, Object... format) {
        return "";
    }

    public void scheduleClientMainLoopTask(Runnable task) {
    }

    @SideOnly(Side.CLIENT)
    public static final class ClientProxy extends SidedHandler {

        private static final Minecraft mc = Minecraft.getMinecraft();

        @Override
        public void registerDefaultModel(Item item) {
            ResourceLocation registryName = Objects.requireNonNull(item.getRegistryName());
            ModelResourceLocation mrl = new ModelResourceLocation(registryName, "inventory");
            ModelLoader.setCustomModelResourceLocation(item, 0, mrl);

            // I really hope that in next versions the advancement icons would support NBT,
            // so leaving this as a dirty hack (see also property overrides in ItemRecalibrator)
            if (item instanceof ItemRecalibrator) {
                for (int i = 1; i <= 16; i++) {
                    ModelLoader.setCustomModelResourceLocation(item, i, mrl);
                }
            }
        }

        @Override
        public String getLocalization(String key, Object... parameters) {
            return I18n.format(key, parameters);
        }

        @Nullable
        @Override
        public EntityPlayer getClientPlayer() {
            return mc.player;
        }

        @Nullable
        @Override
        public World getClientWorld() {
            return mc.world;
        }

        @Override
        public void scheduleClientMainLoopTask(Runnable task) {
            mc.addScheduledTask(task);
        }
    }

    @SideOnly(Side.SERVER)
    public static final class ServerProxy extends SidedHandler {}
}
