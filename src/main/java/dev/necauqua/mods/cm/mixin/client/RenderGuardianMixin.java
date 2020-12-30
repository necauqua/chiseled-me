package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.client.renderer.entity.RenderGuardian;
import net.minecraft.entity.monster.EntityGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderGuardian.class)
public final class RenderGuardianMixin {

    // undo the scaling of the guardian beam
    @ModifyVariable(method = "doRender", ordinal = 3, at = @At(value = "LOAD", ordinal = 1))
    double doRender(double d, EntityGuardian entity, double x, double y, double z, float entityYaw, float partialTicks) {
        return d / ((IRenderSized) entity).getSizeCM(partialTicks);
    }
}
