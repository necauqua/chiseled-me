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

    // main
    public static boolean enableSupersmalls;
    public static boolean enableBigSizes;
    public static boolean allowRecalibratingOtherEntities;
    public static boolean allowRecalibratingOtherPlayers;

    // scale
    public static boolean scaleReachSmall;
    public static boolean scaleReachBig;
    public static boolean scaleMassSmall;
    public static boolean scaleMassBig;
    public static boolean scaleDamageDealtSmall;
    public static boolean scaleDamageDealtBig;
    public static boolean scaleDamageReceivedSmall;
    public static boolean scaleDamageReceivedBig;
    public static boolean scaleFallSmall;
    public static boolean scaleFallBig;

    // limits
    public static boolean allowSleepingWhenSmall;
    public static boolean allowSleepingWhenBig;
    public static boolean allowRidingSameSize;
    public static boolean allowAnyRiding;
    public static boolean allowAnySizes;

    // misc
    public static boolean changeBedAABB;

    // compat
    public static boolean enableNeatIntegration;

    private static Configuration c;

    private Config() {}

    private static void load() {

        c.addCustomCategoryComment("main", "None of the configs are synchronized between client and server in 1.12\n" +
                "Make sure this config on your client is the same as on the server that you are connecting to, or it will cause unpleasant desyncs");

        // main

        enableSupersmalls = c.getBoolean("enableSupersmalls", "main", true,
            "At these sizes (most noticeable at 1/4096) Minecraft starts to break a little so beware " +
                "of various (mostly visual, mob AI and colliding) glitches");

        enableBigSizes = c.getBoolean("enableBigs", "main", true,
            "Big sizes are OP and bugged even more then small.");

        allowRecalibratingOtherEntities = c.getBoolean("allowRecalibratingOtherEntities", "main", true,
                "Allows to disable shift-click-recalibrating arbitrary entities");

        allowRecalibratingOtherPlayers = c.getBoolean("allowRecalibratingOtherPlayers", "main", false,
                "Allows to shift-click-recalibrate other players against their will (please just use dispensers instead of this config)");

        // scale
        c.addCustomCategoryComment("scale", "You can exclude certain aspects of the game from being scaled by using options from this category");

        scaleReachSmall = c.getBoolean("reachWhenSmall", "scale", true,
                "Make reach distance shorter when being smaller");

        scaleReachBig = c.getBoolean("reachWhenBig", "scale", true,
                "Make reach distance longer when being bigger");

        scaleMassSmall = c.getBoolean("massWhenSmall", "scale", true,
                "Make small entities push and knockback bigger entities less");

        scaleMassBig = c.getBoolean("massWhenBig", "scale", true,
                "Make big entities push and knockback smaller entities more");

        scaleDamageDealtSmall = c.getBoolean("damageDealtSmall", "scale", true,
                "Make small entities damage bigger entities less");

        scaleDamageDealtBig = c.getBoolean("damageDealtBig", "scale", true,
                "Make big entities damage smaller entities more");

        scaleDamageReceivedSmall = c.getBoolean("damageReceivedSmall", "scale", true,
                "Make small entities receive more damage from the bigger entities");

        scaleDamageReceivedBig = c.getBoolean("damageReceivedBig", "scale", true,
                "Make big entities receive less damage from the smaller entities");

        scaleFallSmall = c.getBoolean("fallDistanceWhenBig", "scale", true,
                "Scale up the fall distance for small entities, adds to realism");

        scaleFallBig = c.getBoolean("fallDistanceWhenSmall", "scale", true,
                "Scale down the fall distance for big entities, adds to convenience");

        // limits
        c.addCustomCategoryComment("limits", "Options in this category allow to overcome artificial limits on broken/unsupported outcomes of adding this mod to the game");

        allowSleepingWhenSmall = c.getBoolean("allowSleepingWhenSmall", "limits", false,
                "Sleeping is not allowed when resized because the mod author was/is too lazy to fix sleeping model, " +
                        "camera and entity positioning, this config can force the mod to allow sleeping for small players, " +
                        "but everything that was mentioned will be still broken");

        allowSleepingWhenBig = c.getBoolean("allowSleepingWhenBig", "limits", false,
                "Sleeping when big, unlike when small, actually makes no sense and thus will never be supported, but a config option is left there nonetheless");

        allowRidingSameSize = c.getBoolean("allowRidingSameSize", "limits", false,
                "Riding is not supported (yet) by the mod, so entity riding position and movement will be bugged if you enable this");

        allowAnyRiding = c.getBoolean("allowAnyRiding", "limits", false,
                "Riding entities with different sizes will likely never be supported by the mod, but you can enable this at your own risk");

        allowAnySizes = c.getBoolean("allowAnySizes", "limits", false,
                "Disables number checking for the /sizeof command, meaning you can set your size to any double precision number, including zero, negatives, NaN or infinities.\n" +
                        "Note that this is obviously unsupported and bugged in a lot of ways, but can be used to achieve extra-small or bigger-than-16 sizes");

        // misc

        changeBedAABB = c.getBoolean("bedBBox", "misc", true,
            "Override vanilla bed bounding box so if you're small enough you can walk under it");

        // compat

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
