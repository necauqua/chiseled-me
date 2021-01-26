package dev.necauqua.size;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;

import static dev.necauqua.size.Size.ID;
import static net.minecraft.world.GameRules.ValueType.ANY_VALUE;
import static net.minecraft.world.GameRules.ValueType.BOOLEAN_VALUE;

@Mod(modid = ID)
public final class Size {
    public static final String ID = "size";
    public static final String ENTITY_SIZE_RULE = ID + ":defaultForEntities";
    public static final String PLAYER_SIZE_RULE = ID + ":defaultForPlayer";
    public static final String KEEP_SIZE_RULE = ID + ":keep";

    @EventHandler
    public void on(FMLFingerprintViolationEvent e) {
        LogManager.getLogger(Size.class)
                .warn("FINGERPRINT VIOLATED: you're running some unauthorized modification of the mod, be warned. " +
                        "No support will be provided for any issues encountered while using this jar.");
    }

    @EventHandler
    public void on(FMLPreInitializationEvent e) {
        Config.load(e.getSuggestedConfigurationFile());
        Network.init();
        SizeAdvancementTrigger.init();
    }

    @EventHandler
    public void on(FMLServerStartingEvent e) {
        e.registerServerCommand(new SizeofCommand());
        GameRules gameRules = e.getServer().getWorld(0).getGameRules();
        gameRules.addGameRule(ENTITY_SIZE_RULE, "1", ANY_VALUE);
        gameRules.addGameRule(PLAYER_SIZE_RULE, "1", ANY_VALUE);
        gameRules.addGameRule(KEEP_SIZE_RULE, "false", BOOLEAN_VALUE);
    }

    @EventHandler
    public void on(FMLInitializationEvent e) {
        try {
            // imagine making an IMC call for this or something, right?
            Class.forName("micdoodle8.mods.galacticraft.core.GalacticraftCore")
                    .getField("isHeightConflictingModInstalled")
                    .set(null, true);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    public static ResourceLocation ns(String id) {
        return new ResourceLocation(ID, id);
    }
}
