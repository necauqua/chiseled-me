package dev.necauqua.size;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@EventBusSubscriber(modid = Size.ID)
public final class Config {

    private Config() {}

    // compat
    public static boolean enableNeatIntegration;
    public static double maxSize = 16.0;

    private static Configuration c;

    private static void load() {
        maxSize = getDouble("maxSize", "main", 16.0, 1.0, Double.MAX_VALUE,
                "This config allows you to increase the default max size limit of the /sizeof command if you really want to break the game with it.");

        enableNeatIntegration = c.getBoolean("enableNeatIntegration", "compat", true,
                "Enable or disable scaling of the mob health bars from Neat, which is a mod by Vazkii");
    }

    private static double getDouble(String name, String category, double defaultValue, double minValue, double maxValue, String comment) {
        Property prop = c.get(category, name, Double.toString(defaultValue), name);
        prop.setLanguageKey(name);
        prop.setComment(comment + " [range: " + minValue + " ~ " + maxValue + ", default: " + defaultValue + "]");
        prop.setMinValue(minValue);
        prop.setMaxValue(maxValue);
        try {
            return Math.min(Math.max(Double.parseDouble(prop.getString()), minValue), maxValue);
        } catch (Exception e) {
            FMLLog.log.error("Failed to get float for {}/{}", name, category, e);
        }
        return defaultValue;
    }

    public static void load(File configFile) {
        c = new Configuration(configFile);
        c.load();
        load();
        if (c.hasChanged()) {
            c.save();
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (!Size.ID.equals(eventArgs.getModID())) {
            return;
        }
        load();
        if (c.hasChanged()) {
            c.save();
        }
    }
}
