/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.size;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.DataSerializerEntry;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.ChiseledMe.ns;

@EventBusSubscriber(modid = MODID)
public final class DataSerializerDouble implements DataSerializer<Double> {

    public static final DataSerializerDouble INSTANCE = new DataSerializerDouble();

    public void write(PacketBuffer buf, Double value) {
        buf.writeDouble(value);
    }

    public Double read(PacketBuffer buf) {
        return buf.readDouble();
    }

    public DataParameter<Double> createKey(int id) {
        return new DataParameter<>(id, this);
    }

    public Double copyValue(Double value) {
        return value;
    }

    @SubscribeEvent
    public static void on(RegistryEvent.Register<DataSerializerEntry> e) {
        e.getRegistry().register(new DataSerializerEntry(INSTANCE).setRegistryName(ns("double")));
    }
}
