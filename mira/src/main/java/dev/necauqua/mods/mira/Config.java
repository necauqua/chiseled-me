/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import static dev.necauqua.mods.mira.Mira.MODID;
import static net.minecraftforge.fml.config.ModConfig.Type.SERVER;

@EventBusSubscriber(modid = MODID)
public final class Config {

    // main
    public static ConfigValue<Boolean> enableSupersmalls;
    public static ConfigValue<Boolean> enableBigSizes;

    // scale
    public static ConfigValue<Boolean> scaleReachSmall;
    public static ConfigValue<Boolean> scaleReachBig;
    public static ConfigValue<Boolean> scaleMassSmall;
    public static ConfigValue<Boolean> scaleMassBig;
    public static ConfigValue<Boolean> scaleDamageDealtSmall;
    public static ConfigValue<Boolean> scaleDamageDealtBig;
    public static ConfigValue<Boolean> scaleDamageReceivedSmall;
    public static ConfigValue<Boolean> scaleDamageReceivedBig;
    public static ConfigValue<Boolean> scaleFallSmall;
    public static ConfigValue<Boolean> scaleFallBig;

    public static ConfigValue<Boolean> scaleSounds;

    // limits
    public static ConfigValue<Boolean> allowSleepingWhenSmall;
    public static ConfigValue<Boolean> allowSleepingWhenBig;
    public static ConfigValue<Boolean> allowRidingSameSize;
    public static ConfigValue<Boolean> allowAnyRiding;
    public static ConfigValue<Boolean> allowAnySizes;

    // misc
    public static ConfigValue<Double> maxShadowSize;

    // compat
    public static ConfigValue<Boolean> enableNeatIntegration;

    public static void init() {
        ModLoadingContext context = ModLoadingContext.get();
        // context.registerConfig(CLIENT, defineClient());
        context.registerConfig(SERVER, defineCommon());
    }

    public static ForgeConfigSpec defineCommon() {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("main");

        enableSupersmalls = define(b, "enable_supersmalls", true,
            "At these sizes (most noticeable at 1/4096) Minecraft starts to break a little so beware of various glitches");

        enableBigSizes = define(b, "enable_bigs", true, "Big sizes are OP and bugged even more then small.");

        b.pop()
            .comment("You can exclude certain aspects of the game from being scaled by using these options", "")
            .push("scale");

        scaleReachSmall = define(b, "reach_when_small", true,
            "Make reach distance shorter when being smaller");

        scaleReachBig = define(b, "reach_when_big", true,
            "Make reach distance longer when being bigger");

        scaleMassSmall = define(b, "mass_when_small", true,
            "Make small entities push and knockback bigger entities less");

        scaleMassBig = define(b, "mass_when_big", true,
            "Make big entities push and knockback smaller entities more");

        scaleDamageDealtSmall = define(b, "damage_dealt_small", true,
            "Make small entities damage bigger entities less");

        scaleDamageDealtBig = define(b, "damage_dealt_big", true,
            "Make big entities damage smaller entities more");

        scaleDamageReceivedSmall = define(b, "damage_received_small", true,
            "Make small entities receive more damage from the bigger entities");

        scaleDamageReceivedBig = define(b, "damage_received_big", true,
            "Make big entities receive less damage from the smaller entities");

        scaleFallSmall = define(b, "fall_distance_when_big", true,
            "Scale up the fall distance for small entities, adds to realism");

        scaleFallBig = define(b, "fall_distance_when_small", true,
            "Scale down the fall distance for big entities, adds to convenience");

        scaleSounds = define(b, "scale_sounds", true,
            "Scale the sounds of entities depending on their relative size - helps a lot with realism and immersion");

        b.pop()
            .comment(
                "Options in this category allow to overcome artificial limits on broken/unsupported outcomes of adding this mod to the game")
            .push("limits");

        allowSleepingWhenSmall = define(b, "allow_sleeping_when_small", false,
            "Sleeping is not allowed when resized because the mod author was/is too lazy to fix sleeping model, " +
                "camera and entity positioning, this config can force the mod to allow sleeping for small players, "
                +
                "but everything that was mentioned will be still broken");

        allowSleepingWhenBig = define(b, "allow_sleeping_when_big", false,
            "Sleeping when big, unlike when small, actually makes no sense and thus will never be supported, but a config option is left there nonetheless");

        allowRidingSameSize = define(b, "allow_riding_same_size", false,
            "Riding is not supported (yet) by the mod, so entity riding position and movement will be bugged if you enable this");

        allowAnyRiding = define(b, "allow_any_riding", false,
            "Riding entities with different sizes will likely never be supported by the mod, but you can enable this at your own risk");

        allowAnySizes = define(b, "allow_any_sizes", false,
            "Disables number checking for the /size command, meaning you can set your size to any double precision number, including zero, negatives, NaN or infinities.\n"
                +
                "Note that this is obviously unsupported and bugged in a lot of ways, but can be used to achieve extra-small or bigger-than-16 sizes");

        maxShadowSize = defineInRange(b, "max_shadow_size", 16.0, 0.0, Double.MAX_VALUE,
            "If allow_any_sizes is set to true, the shadow size is limited by this value, " +
                "because vanilla code iterates through a lot of blocks to render the shadow and this degrades performance by a lot");

        b.pop().push("compat");

        enableNeatIntegration = define(b, "enable_neat_integration", true,
            "Enable or disable scaling of the mob health bars from Neat, which is a mod by Vazkii");

        return b.pop().build();
    }

    private static <T> ConfigValue<T> define(ForgeConfigSpec.Builder builder, String name, T defaultValue,
                                             String comment) {
        return adjust(builder, name, comment).define(name, defaultValue);
    }

    private static ConfigValue<Double> defineInRange(ForgeConfigSpec.Builder builder, String name, double defaultValue,
                                                     double min, double max, String comment) {
        return adjust(builder, name, comment).defineInRange(name, defaultValue, min, max);
    }

    private static Builder adjust(Builder builder, String name, String comment) {
        return builder
            .comment(comment)
            .translation("config." + MODID + ":" + name);
    }
}
