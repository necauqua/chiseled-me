package dev.necauqua.mods.cm.size;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.DataSerializerEntry;

import static dev.necauqua.mods.cm.ChiseledMe.ns;

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
