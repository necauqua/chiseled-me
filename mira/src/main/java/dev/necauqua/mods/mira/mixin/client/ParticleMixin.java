/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.client.particle.*;
import net.minecraft.entity.Entity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(Particle.class)
public abstract class ParticleMixin implements ISized {

    public double $cm$size = 1.0;
    @Shadow
    protected float bbWidth;
    @Shadow
    protected float bbHeight;
    @Shadow
    protected double x;
    @Shadow
    protected double y;
    @Shadow
    protected double z;
    private float $cm$original_width;
    private float $cm$original_height;

    @Override
    public double getSizeCM() {
        return $cm$size;
    }

    @Override
    public void setSizeCM(double size) {
        $cm$size = size;

        bbWidth = (float) ($cm$original_width * size);
        bbHeight = (float) ($cm$original_height * size);

        float w = bbWidth / 2.0F;
        setBoundingBox(new AxisAlignedBB(x - w, y, z - w, x + w, y + bbHeight, z + w));
    }

    @Shadow
    public abstract void setBoundingBox(AxisAlignedBB bb);

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    float setSizeWidth(float width) {
        $cm$original_width = width;
        return (float) (width * $cm$size);
    }

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    float setSizeHeight(float height) {
        $cm$original_height = height;
        return (float) (height * $cm$size);
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    double moveX(double x) {
        return x * $cm$size;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    double moveY(double y) {
        return y * $cm$size;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    double moveZ(double z) {
        return z * $cm$size;
    }

    @Mixin(TexturedParticle.class)
    public static abstract class TexturedParticleMixin extends ParticleMixin {

        @ModifyVariable(method = "render", ordinal = 4, at = @At("STORE"))
        float renderParticle(float scale) {
            return (float) (scale * $cm$size);
        }
    }

    @Mixin(CritParticle.class)
    public static abstract class CritParticleMixin extends ParticleMixin {

        // this one onUpdate in constructor runs while the size is still
        // not set (so its 1) and for one tick the particle moves as if not scaled
        @Redirect(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/CritParticle;tick()V"))
        void removeExtraOnUpdate(CritParticle self) {
        }
    }

    @Mixin(PortalParticle.class)
    public static abstract class PortalParticleMixin extends ParticleMixin {

        @Shadow
        @Final
        private double xStart;
        @Shadow
        @Final
        private double yStart;
        @Shadow
        @Final
        private double zStart;

        @Override
        @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0, argsOnly = true)
        double moveX(double x) {
            return x * $cm$size;
        }

        @Override
        @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 1, argsOnly = true)
        double moveY(double y) {
            return y * $cm$size;
        }

        @Override
        @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 2, argsOnly = true)
        double moveZ(double z) {
            return z * $cm$size;
        }

        @Inject(method = "tick", at = @At(value = "FIELD", opcode = PUTFIELD, shift = AFTER, target = "Lnet/minecraft/client/particle/PortalParticle;x:D"))
        void tickX(CallbackInfo ci) {
            x = (x - xStart) * $cm$size + xStart;
        }

        @Inject(method = "tick", at = @At(value = "FIELD", opcode = PUTFIELD, shift = AFTER, target = "Lnet/minecraft/client/particle/PortalParticle;y:D"))
        void tickY(CallbackInfo ci) {
            y = (y - yStart) * $cm$size + yStart;
        }

        @Inject(method = "tick", at = @At(value = "FIELD", opcode = PUTFIELD, shift = AFTER, target = "Lnet/minecraft/client/particle/PortalParticle;z:D"))
        void tickZ(CallbackInfo ci) {
            z = (z - zStart) * $cm$size + zStart;
        }
    }

    @Mixin(EmitterParticle.class)
    public static abstract class EmitterParticleMixin extends ParticleMixin {

        @Shadow
        @Final
        private Entity entity;

        @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/particles/IParticleData;ZDDDDDD)V"))
        IParticleData spawnParticleArgs(IParticleData particleData) {
            return ScaledParticleData.wrap(particleData, ((ISized) entity).getSizeCM());
        }
    }

    @Mixin(ItemPickupParticle.class)
    public static abstract class ItemPickupParticleMixin {

        @Shadow
        @Final
        private Entity target;

        @ModifyConstant(method = "render", constant = @Constant(doubleValue = 0.5))
        double render(double constant) {
            return constant * ((ISized) target).getSizeCM();
        }
    }
}
