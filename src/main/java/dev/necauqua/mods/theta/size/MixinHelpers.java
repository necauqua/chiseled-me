/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.theta.size;

import dev.necauqua.mods.theta.api.IRenderSized;
import dev.necauqua.mods.theta.api.ISized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class MixinHelpers {

    public static boolean renderingNearPatch = false;

    private MixinHelpers() {}

    // apparently you cannot reference PlayerEntity from Entity mixin idk
    public static void onSetSize(Entity entity, double prevSize, double size) {
        if (entity instanceof PlayerEntity) {
            if (((PlayerEntity) entity).isSleeping()) {
                ((PlayerEntity) entity).stopSleeping();
            }
            if (entity instanceof ServerPlayerEntity) {
                SizeTrigger.trigger((ServerPlayerEntity) entity, prevSize, size);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void setCameraHeight(Entity entity, float prevCameraHeight, float cameraHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.cameraEntity != entity) {
            return;
        }
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        camera.eyeHeightOld = prevCameraHeight;
        camera.eyeHeight = cameraHeight;
    }

    @OnlyIn(Dist.CLIENT)
    public static double getViewerSize() {
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        return viewer != null ?
            ((ISized) viewer).getSizeCM() :
            1.0;
    }

    @OnlyIn(Dist.CLIENT)
    public static double getViewerSize(float partialTicks) {
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        return viewer != null ?
            ((IRenderSized) viewer).getSizeCM(partialTicks) :
            1.0;
    }
}
