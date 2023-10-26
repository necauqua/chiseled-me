package dev.necauqua.mods.mira.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.necauqua.mods.mira.api.IRenderSized;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {

    @ModifyVariable(method = "render*", at = @At("STORE"), ordinal = 0)
    double renderCapeDx(double dx, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, float animPos, float animSpeed, float partialTicks) {
        return dx / ((IRenderSized) player).getSizeCM(partialTicks);
    }

    @ModifyVariable(method = "render*", at = @At("STORE"), ordinal = 1)
    double renderCapeDy(double dy, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, float animPos, float animSpeed, float partialTicks) {
        return dy / ((IRenderSized) player).getSizeCM(partialTicks);
    }

    @ModifyVariable(method = "render*", at = @At("STORE"), ordinal = 2)
    double renderCapeDz(double dz, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, float animPos, float animSpeed, float partialTicks) {
        return dz / ((IRenderSized) player).getSizeCM(partialTicks);
    }
}
