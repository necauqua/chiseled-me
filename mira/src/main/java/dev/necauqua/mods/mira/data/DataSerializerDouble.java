/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.data;

import dev.necauqua.mods.mira.Mira;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DataSerializerEntry;

import static dev.necauqua.mods.mira.Mira.ns;

@EventBusSubscriber(modid = Mira.MODID, bus = Bus.MOD)
public final class DataSerializerDouble implements IDataSerializer<Double> {

    public static final DataSerializerDouble INSTANCE = new DataSerializerDouble();

    @SubscribeEvent
    public static void on(RegistryEvent.Register<DataSerializerEntry> e) {
        e.getRegistry().register(new DataSerializerEntry(INSTANCE).setRegistryName(ns("double")));
    }

    @Override
    public void write(PacketBuffer buf, Double value) {
        buf.writeDouble(value);
    }

    @Override
    public Double read(PacketBuffer buf) {
        return buf.readDouble();
    }

    @Override
    public Double copy(Double value) {
        return value;
    }
}
