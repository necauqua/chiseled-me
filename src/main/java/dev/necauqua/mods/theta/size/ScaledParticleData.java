package dev.necauqua.mods.theta.size;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.necauqua.mods.theta.api.ISized;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Locale;

import static dev.necauqua.mods.theta.Theta.MODID;
import static dev.necauqua.mods.theta.Theta.ns;

@EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Bus.MOD)
public final class ScaledParticleData implements IParticleData {

    @SubscribeEvent
    public static void on(RegistryEvent.Register<ParticleType<?>> e) {
        e.getRegistry().registerAll(TYPE, OVERRIDING_TYPE);
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Bus.MOD)
    @OnlyIn(Dist.CLIENT)
    private static final class ClientsideRegister {

        @SubscribeEvent
        public static void on(FMLClientSetupEvent e) {
            ParticleManager manager = e.getMinecraftSupplier().get().particleEngine;
            IParticleFactory<ScaledParticleData> factory = (data, world, x, y, z, xd, yd, zd) -> {
                Particle particle = manager.makeParticle(data.getParticleData(), x, y, z, xd, yd, zd);
                if (particle != null) {
                    ((ISized) particle).setSizeCM(data.getSize());
                }
                return particle;
            };
            manager.register(TYPE, factory);
            manager.register(OVERRIDING_TYPE, factory);
        }
    }

    public static final Codec<ScaledParticleData> CODEC = RecordCodecBuilder.create(builder ->
        builder.group(
            ParticleTypes.CODEC.fieldOf("p").forGetter(instance -> instance.particleData),
            Codec.DOUBLE.fieldOf("s").forGetter(instance -> instance.size)
        ).apply(builder, ScaledParticleData::new));

    @SuppressWarnings({"deprecation", "unchecked"})
    public static final IParticleData.IDeserializer<ScaledParticleData> DESERIALIZER = new IParticleData.IDeserializer<ScaledParticleData>() {
        @Override
        public ScaledParticleData fromCommand(ParticleType<ScaledParticleData> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            ParticleType<IParticleData> innerType = (ParticleType<IParticleData>) Registry.PARTICLE_TYPE.get(new ResourceLocation(reader.readString()));
            if (innerType == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
            }
            IParticleData particle = innerType.getDeserializer().fromCommand(innerType, reader);
            reader.expect(' ');
            return new ScaledParticleData(particle, reader.readDouble());
        }

        @Override
        public ScaledParticleData fromNetwork(ParticleType<ScaledParticleData> type, PacketBuffer buffer) {
            ParticleType<IParticleData> innerType = (ParticleType<IParticleData>) Registry.PARTICLE_TYPE.get(new ResourceLocation(buffer.readUtf()));
            if (innerType == null) {
                throw new IllegalStateException("bad particle type lol");
            }
            return new ScaledParticleData(innerType.getDeserializer().fromNetwork(innerType, buffer), buffer.readDouble());
        }
    };

    private static final ParticleType<ScaledParticleData> TYPE = new ParticleType<ScaledParticleData>(false, DESERIALIZER) {
        @Override
        public Codec<ScaledParticleData> codec() {
            return CODEC;
        }
    };

    private static final ParticleType<ScaledParticleData> OVERRIDING_TYPE = new ParticleType<ScaledParticleData>(false, DESERIALIZER) {
        @Override
        public Codec<ScaledParticleData> codec() {
            return CODEC;
        }
    };

    static {
        TYPE.setRegistryName(ns("scaled_particle"));
        OVERRIDING_TYPE.setRegistryName(ns("scaled_overriding_particle"));
    }

    private final IParticleData particleData;
    private final double size;

    private ScaledParticleData(IParticleData particleData, double size) {
        this.particleData = particleData;
        this.size = size;
    }

    public static ScaledParticleData wrap(IParticleData particleData, double size) {
        return new ScaledParticleData(particleData, size);
    }

    public static double getSize(IParticleData particleData) {
        return particleData instanceof ScaledParticleData ? ((ScaledParticleData) particleData).size : 1.0;
    }

    public IParticleData getParticleData() {
        return particleData;
    }

    public double getSize() {
        return size;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void writeToNetwork(PacketBuffer buffer) {
        ResourceLocation key = Registry.PARTICLE_TYPE.getKey(particleData.getType());
        buffer.writeUtf(key != null ? key.toString() : "");
        particleData.writeToNetwork(buffer);
        buffer.writeDouble(size);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %s %f", Registry.PARTICLE_TYPE.getKey(getType()), particleData.writeToString(), size);
    }

    @Override
    public ParticleType<ScaledParticleData> getType() {
        return particleData.getType().getOverrideLimiter() ? OVERRIDING_TYPE : TYPE;
    }
}
