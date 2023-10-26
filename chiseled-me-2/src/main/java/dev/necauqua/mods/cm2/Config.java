/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm2;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import static dev.necauqua.mods.cm2.ChiseledMe2.MODID;
import static net.minecraftforge.fml.config.ModConfig.Type.SERVER;

@EventBusSubscriber(modid = MODID)
public final class Config {

    // main
    public static ConfigValue<Boolean> allowRecalibratingOtherEntities;
    public static ConfigValue<Boolean> allowRecalibratingOtherPlayers;

    public static void init() {
        ModLoadingContext context = ModLoadingContext.get();
        // context.registerConfig(CLIENT, defineClient());
        context.registerConfig(SERVER, defineCommon());
    }

    public static ForgeConfigSpec defineCommon() {
        Builder b = new Builder();

        allowRecalibratingOtherEntities = define(b, "allow_recalibrating_other_entities", true,
            "Allows to disable shift-click-recalibrating arbitrary entities");

        allowRecalibratingOtherPlayers = define(b, "allow_recalibrating_other_players", false,
            "Allows to shift-click-recalibrate other players against their will (please just use dispensers instead of this config)");

        return b.build();
    }

    private static <T> ConfigValue<T> define(Builder builder, String name, T defaultValue, String comment) {
        return adjust(builder, name, comment).define(name, defaultValue);
    }

    private static Builder adjust(Builder builder, String name, String comment) {
        return builder
            .comment(comment)
            .translation("config." + MODID + ":" + name);
    }
}
