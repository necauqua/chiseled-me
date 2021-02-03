/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm;

import dev.necauqua.mods.cm.ChiseledMe.Init;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLInjectionData;

import java.io.File;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;

public final class Config {

    private Config() {}

    // main
    public static boolean enableSupersmalls;
    public static boolean enableBigSizes;
    public static boolean allowRecalibratingOtherEntities;
    public static boolean allowRecalibratingOtherPlayers;

    // misc
    public static boolean changeBedAABB;
    public static boolean allowSleepingWhenSmall;

    // compat
    public static boolean enableNeatIntegration;

    private static Configuration c;

    private static void load() {
        enableSupersmalls = c.getBoolean("enableSupersmalls", "main", true,
            "At these sizes (most noticeable at 1/4096) Minecraft starts to break a little so beware " +
                "of various (mostly visual, mob AI and colliding) glitches");

        enableBigSizes = c.getBoolean("enableBigs", "main", true,
            "Big sizes are OP and bugged even more then small.");

        allowRecalibratingOtherEntities = c.getBoolean("allowRecalibratingOtherEntities", "main", true,
                "Allows to disable shift-click-recalibrating arbitrary entities");

        allowRecalibratingOtherPlayers = c.getBoolean("allowRecalibratingOtherPlayers", "main", false,
                "Allows to shift-click-recalibrate other players against their will");

        changeBedAABB = c.getBoolean("bedBBox", "misc", true,
            "Override vanilla bed bounding box so if you're small enough you can walk under it");

        allowSleepingWhenSmall = c.getBoolean("allowSleepingWhenSmall", "misc", false,
                "Sleeping is not allowed when resized because the mod author was/is too lazy to fix sleeping model, " +
                        "camera and entity positioning, this config can force the mod to allow sleeping for small players, " +
                        "but everything that was mentioned will be still broken");

        enableNeatIntegration = c.getBoolean("enableNeatIntegration", "compat", true,
            "Enable or disable scaling of the mob health bars from Neat, which is a mod by Vazkii");
    }

    @Init
    private static void init() {
        File file = new File(/* .minecraft dir */(File) FMLInjectionData.data()[6], "config/" + MODID + ".cfg");
        c = new Configuration(file);
        c.load();
        load();
        if (c.hasChanged()) {
            c.save();
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(OnConfigChangedEvent eventArgs) {
        if (MODID.equals(eventArgs.getModID())) {
            load();
            if (c.hasChanged()) {
                c.save();
            }
        }
    }
}
