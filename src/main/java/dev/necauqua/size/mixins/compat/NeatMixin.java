package dev.necauqua.size.mixins.compat;

import dev.necauqua.size.Config;
import dev.necauqua.size.api.IRenderSized;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;

import java.lang.reflect.Field;

import static dev.necauqua.size.EntitySizeInteractions.getViewerSize;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;

// JESUS I DONT WANT TO COMPILE-DEPEND ON MODS WITH NO MAVEN
// (so it's buildable by CI and/or other people and shit) SO BAD
//
// I AM SORRY I SIN SO MUCH WITH THOSE DOGSHIT WORKAROUNDS
//
// and yeah there are no accessors or shadows or anything for statics in Mixin
@Pseudo
@Mixin(targets = "vazkii.neat.HealthBarRenderer", remap = false)
public final class NeatMixin {

    private static Field $size$heightAboveField;
    private static Field $size$backgroundHeightField;
    private static Field $size$maxDistanceField;
    private static double $size$heightAbove = 0.6;
    private static int $size$backgroundHeight = 6;
    private static int $size$maxDistance = 24;

    static {
        try {
            Class<?> configCls = Class.forName("vazkii.neat.NeatConfig");
            $size$heightAboveField = configCls.getField("heightAbove");
            $size$backgroundHeightField = configCls.getField("backgroundHeight");
            $size$maxDistanceField = configCls.getField("maxDistance");
            MinecraftForge.EVENT_BUS.register(Class.forName("vazkii.neat.HealthBarRenderer").newInstance());
            $size$heightAbove = $size$heightAboveField.getDouble(null);
            $size$backgroundHeight = $size$backgroundHeightField.getInt(null);
            $size$maxDistance = $size$maxDistanceField.getInt(null);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
//    @Unique well default unique behaviour of not adding the method (instead of name-mangling) is meh
    public void $size$onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
        if ("neat".equals(e.getModID())) {
            try {
                $size$heightAbove = $size$heightAboveField.getDouble(null);
                $size$backgroundHeight = $size$backgroundHeightField.getInt(null);
                $size$maxDistance = $size$maxDistanceField.getInt(null);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    @Dynamic
    @ModifyConstant(method = "renderHealthBar", constant = @Constant(floatValue = 0.026666672f))
    float renderHealthBarScale(float constant, EntityLivingBase entity, float partialTicks) {
        // we only disable actual scale change in the config
        // below patches for smooth eye height changing are still applied
        return Config.enableNeatIntegration ?
                (float) (constant * ((IRenderSized) entity).getSize(partialTicks)) :
                constant;
    }

    @Group(name = "manualRemapOmg", min = 1, max = 1)
    @Dynamic
    @Redirect(method = "renderHealthBar", at = @At(value = "FIELD", opcode = GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;height:F"), require = 0, expect = 0)
    float renderHealthBarEyeHeight(EntityLivingBase self, EntityLivingBase entity, float partialTicks) {
        IRenderSized sized = (IRenderSized) self;
        return (float) (self.height / sized.getSize() * sized.getSize(partialTicks));
    }

    @Group(name = "manualRemapOmg", min = 1, max = 1)
    @Dynamic
    @Redirect(method = "renderHealthBar", at = @At(value = "FIELD", opcode = GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;field_70131_O:F"), require = 0, expect = 0)
    float renderHealthBarEyeHeightObf(EntityLivingBase self, EntityLivingBase entity, float partialTicks) {
        IRenderSized sized = (IRenderSized) self;
        return (float) (self.height / sized.getSize() * sized.getSize(partialTicks));
    }

    // yes I cannot access the field that I redirect and that spiraled to all of the above
    // tbf I *can* with reflection, but this is called every frame hello
    @Dynamic
    @Redirect(method = "renderHealthBar", at = @At(value = "FIELD", opcode = GETSTATIC, target = "Lvazkii/neat/NeatConfig;heightAbove:D"))
    double renderHealthBarOffset(EntityLivingBase entity, float partialTicks) {
        double size = ((IRenderSized) entity).getSize(partialTicks);
        double scaledBgHeight = // literally figured out 'the (ok enough) formula' by trial and error wtf
                size < 1.0 && Config.enableNeatIntegration ?
                        $size$backgroundHeight * 0.026666672 * size :
                        $size$backgroundHeight * 0.026666672;
        return ($size$heightAbove - scaledBgHeight) * size + scaledBgHeight;
    }

    @Dynamic
    @Redirect(method = "renderHealthBar", at = @At(value = "FIELD", opcode = GETSTATIC, target = "Lvazkii/neat/NeatConfig;maxDistance:I"))
    int renderHealthBarMaxDistance(EntityLivingBase entity, float partialTicks) {
        double viewerSize = getViewerSize(partialTicks);
        double size = ((IRenderSized) entity).getSize(partialTicks);
        // this can be replaced with a redirect of entity.getDistance to avoid precision loss,
        // but I don't want to do another manual remap idk
        return Math.max(1, MathHelper.ceil($size$maxDistance * Math.sqrt(viewerSize * size))); // oh well that mod does not use square dist comparisons
    }
}
