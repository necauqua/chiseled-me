/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.api.IWorldPreciseEvents;
import dev.necauqua.mods.mira.api.IWorldPreciseSounds;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static dev.necauqua.mods.mira.size.MixinHelpers.getViewerSize;
import static dev.necauqua.mods.mira.size.MixinHelpers.renderingNearPatch;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements IWorldPreciseEvents, IWorldPreciseSounds {

    private double $cm$size = 1.0;

    private Vector3d $cm$pos;
    @Shadow
    private boolean needsUpdate;

    // see GameRendererMixin, used for multipass fix of near clipping & depth buffer
    // issues
    @ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"))
    int renderLevelDontClear(int clearBits) {
        return renderingNearPatch ? GL_DEPTH_BUFFER_BIT : clearBits;
    }

    // don't double-render (ugly overlaps) debug frustums etc
    @Inject(method = "renderDebug", at = @At("HEAD"), cancellable = true)
    void renderDebug(ActiveRenderInfo camera, CallbackInfo ci) {
        if (renderingNearPatch) {
            ci.cancel();
        }
    }

    // force it to not update (and cache) the limited set of chunks that are
    // rendered during the near patch pass
    // ^ woohoo, fix of the century
    @ModifyArg(method = "renderLevel", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;setupRender(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/culling/ClippingHelper;ZIZ)V"))
    boolean renderLevelDontUpdateChunks(boolean dontUpdateChunks) {
        return dontUpdateChunks || renderingNearPatch;
    }

    // scale particle render distance
    @ModifyConstant(method = "addParticleInternal(Lnet/minecraft/particles/IParticleData;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;", constant = @Constant(doubleValue = 1024.0))
    double addParticleInternal(double constant, IParticleData particleData) {
        return constant * ScaledParticleData.getSize(particleData) * getViewerSize();
    }

    @Override
    public void levelEvent(@Nullable PlayerEntity player, int type, BlockPos blockPos, int data, double size,
                           Vector3d pos) {
        $cm$size = size;
        $cm$pos = pos;
        levelEvent(player, type, blockPos, data);
        $cm$pos = null;
        $cm$size = 1.0;
    }

    @Shadow
    public abstract void levelEvent(@Nullable PlayerEntity player, int type, BlockPos blockPos, int data);

    @Redirect(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playLocalSound(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FFZ)V"))
    void playLocalSound(ClientWorld self, BlockPos blockPos, SoundEvent sound, SoundCategory category, float volume,
                        float pitch, boolean distanceDelay) {
        Vector3d pos = $cm$pos;
        if (pos == null) {
            self.playLocalSound(blockPos, sound, category, volume, pitch, distanceDelay);
        } else {
            self.playLocalSound(pos.x, pos.y, pos.z, sound, category, volume, pitch, distanceDelay);
        }
    }

    @Redirect(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    void addParticle(WorldRenderer self, IParticleData particleData, double x, double y, double z, double xd, double yd,
                     double zd, @Nullable PlayerEntity player, int event, BlockPos blockPos) {
        Vector3d pos = $cm$pos;
        if (pos == null) {
            addParticle(particleData, x, y, z, xd, yd, zd);
            return;
        }
        addParticle(
            ScaledParticleData.wrap(particleData, $cm$size),
            pos.x + (x - blockPos.getX() - 0.5) * $cm$size,
            pos.y + (y - blockPos.getY() - 0.5) * $cm$size,
            pos.z + (z - blockPos.getZ() - 0.5) * $cm$size,
            xd, yd, zd);
    }

    @Shadow
    protected abstract <T extends IParticleData> void addParticle(T particleData, double x, double y, double z,
                                                                  double xd, double yd, double zd);

    @Redirect(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;addParticleInternal(Lnet/minecraft/particles/IParticleData;ZDDDDDD)Lnet/minecraft/client/particle/Particle;"))
    Particle addParticleInternal(WorldRenderer self, IParticleData particleData, boolean ignoreRange, double x,
                                 double y, double z, double xd, double yd, double zd, @Nullable PlayerEntity player, int event,
                                 BlockPos blockPos) {
        Vector3d pos = $cm$pos;
        if (pos == null) {
            return addParticleInternal(particleData, ignoreRange, x, y, z, xd, yd, zd);
        }
        return addParticleInternal(
            ScaledParticleData.wrap(particleData, $cm$size),
            ignoreRange,
            pos.x + (x - blockPos.getX() - 0.5) * $cm$size,
            pos.y + (y - blockPos.getY() - 0.5) * $cm$size,
            pos.z + (z - blockPos.getZ() - 0.5) * $cm$size,
            xd, yd, zd);
    }

    @Shadow
    @Nullable
    protected abstract Particle addParticleInternal(IParticleData particleData, boolean ignoreRange, double x, double y,
                                                    double z, double xd, double yd, double zd);
}
