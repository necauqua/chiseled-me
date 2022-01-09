/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.theta.data;

import dev.necauqua.mods.theta.Theta;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DataSerializerEntry;

@EventBusSubscriber(modid = Theta.MODID, bus = Bus.MOD)
public final class DataSerializerDouble implements IDataSerializer<Double> {

    public static final DataSerializerDouble INSTANCE = new DataSerializerDouble();

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

    @SubscribeEvent
    public static void on(RegistryEvent.Register<DataSerializerEntry> e) {
        e.getRegistry().register(new DataSerializerEntry(INSTANCE).setRegistryName(Theta.ns("double")));
    }
}
