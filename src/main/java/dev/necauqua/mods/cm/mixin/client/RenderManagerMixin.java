/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public final class RenderManagerMixin {

    private double $cm$sizeHack = 1.0; // idk just a useless optimization I guess

    @Inject(method = "renderEntity", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    void renderEntityPre(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo ci) {
        double size = $cm$sizeHack = ((IRenderSized) entity).getSizeCM(partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.scale(size, size, size);
    }

    @Inject(method = "renderEntity", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    void renderEntityPost(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @ModifyArg(method = "renderEntity", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    double renderEntityX(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        return x / $cm$sizeHack;
    }

    @ModifyArg(method = "renderEntity", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    double renderEntityY(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        return y / $cm$sizeHack;
    }

    @ModifyArg(method = "renderEntity", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    double renderEntityZ(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        return z / $cm$sizeHack;
    }

    @ModifyConstant(method = "renderDebugBoundingBox", constant = {
            @Constant(doubleValue = 0.009999999776482582),
            @Constant(doubleValue = 2.0),
    })
    double renderDebugBoundingBox(double constant, Entity entity) {
        return constant * ((IRenderSized) entity).getSizeCM();
    }

    @Redirect(method = "renderMultipass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;renderMultipass(Lnet/minecraft/entity/Entity;DDDFF)V"))
    void renderMultipass(Render<Entity> self, Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        double size = ((IRenderSized) entity).getSizeCM(partialTicks);
        if (size == 1.0) {
            self.renderMultipass(entity, x, y, z, yaw, partialTicks);
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale(size, size, size);
        self.renderMultipass(entity, x / size, y / size, z / size, yaw, partialTicks);
        GlStateManager.popMatrix();
    }
}
