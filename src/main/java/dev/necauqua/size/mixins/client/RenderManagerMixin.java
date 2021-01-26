package dev.necauqua.size.mixins.client;

import dev.necauqua.size.api.IRenderSized;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public final class RenderManagerMixin {

    private double $size$itHack = 1.0; // idk just a useless optimization I guess

    @Inject(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    void renderEntityPre(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo ci) {
        double size = $size$itHack = ((IRenderSized) entity).getSize(partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.scale(size, size, size);
    }

    @Inject(method = "renderEntity", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    void renderEntityPost(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @ModifyArg(method = "renderEntity", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    double renderEntityX(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        return x / $size$itHack;
    }

    @ModifyArg(method = "renderEntity", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    double renderEntityY(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        return y / $size$itHack;
    }

    @ModifyArg(method = "renderEntity", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"))
    double renderEntityZ(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        return z / $size$itHack;
    }

    @ModifyConstant(method = "renderDebugBoundingBox", constant = {
            @Constant(doubleValue = 0.009999999776482582),
            @Constant(doubleValue = 2.0),
    })
    double renderDebugBoundingBox(double constant, Entity entity) {
        return constant * ((IRenderSized) entity).getSize();
    }
}
