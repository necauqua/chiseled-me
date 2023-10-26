package dev.necauqua.mods.mira;

import dev.necauqua.mods.mira.data.DoubleValue;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static dev.necauqua.mods.mira.Mira.MODID;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(MODID)
public final class Mira {

    public static final String MODID = "mira";

    public static final double LOWER_LIMIT = 0.000244140625; // = 1/16/16/16 = 1/4096, lower - and we start geting issues with 32-bit floats
    public static final double UPPER_LIMIT = 16.0;

    public static final RuleKey<BooleanValue> KEEP_SIZE_RULE = GameRules.register(MODID + ":keepSize", Category.MOBS, BooleanValue.create(false));

    public static final RuleKey<DoubleValue> PLAYER_SIZE_RULE = GameRules.register(MODID + ":playerDefault", Category.PLAYER, DoubleValue.create(1.0));

    public static final RuleKey<DoubleValue> ENTITY_SIZE_RULE = GameRules.register(MODID + ":entityDefault", Category.MOBS, DoubleValue.create(1.0));

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);

    public Mira() {
        Config.init();

        PARTICLE_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static ResourceLocation ns(String path) {
        return new ResourceLocation(MODID, path);
    }
}
