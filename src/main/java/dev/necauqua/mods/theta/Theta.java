package dev.necauqua.mods.theta;

import dev.necauqua.mods.theta.data.DoubleValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import static dev.necauqua.mods.theta.Theta.MODID;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(MODID)
public final class Theta {

    public static final String MODID = "theta";

    public static final RuleKey<BooleanValue> KEEP_SIZE_RULE = GameRules.register(MODID + ":keep", Category.MOBS, BooleanValue.create(false));
    public static final RuleKey<DoubleValue> PLAYER_SIZE_RULE = GameRules.register(MODID + ":playerDefault", Category.PLAYER, DoubleValue.create(1.0));
    public static final RuleKey<DoubleValue> ENTITY_SIZE_RULE = GameRules.register(MODID + ":entityDefault", Category.MOBS, DoubleValue.create(1.0));

    public static final double LOWER_LIMIT = 0.000244140625; // = 1/16/16/16 = 1/4096, lower - and we start geting issues with 32-bit floats
    public static final double UPPER_LIMIT = 16.0;

    public Theta() {
        Config.init();
    }

    public static ResourceLocation ns(String path) {
        return new ResourceLocation(MODID, path);
    }
}
