/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IRenderSized;
import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(Render.class)
public abstract class RenderMixin {

    protected double $cm$sizeHack = 1.0;

    // scale distance to camera here to make the shadow transparency *exactly* the same
    @ModifyVariable(method = "doRenderShadowAndFire", ordinal = 3, at = @At("STORE"))
    double doRenderShadowAndFire(double d0, Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        return d0 / ($cm$sizeHack = ((IRenderSized) entity).getSizeCM(partialTicks));
    }

    // scale shadow size
    @Redirect(method = "renderShadow", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/Render;shadowSize:F"))
    float renderShadow(Render<?> self, Entity entity, double x, double y, double z, float shadowAlpha, float partialTicks) {
        return (float) (shadowSize * $cm$sizeHack); // well reuse the hack, whatever
    }

    // transparency
    @ModifyConstant(method = "renderShadowSingle", constant = @Constant(doubleValue = 2.0, ordinal = 0))
    double renderShadowSingleConstant(double constant) {
        // hack field exists because we have no reference to entity that casted the shadow here
        return constant * $cm$sizeHack;
    }

    // vertical shadow offset
    @ModifyConstant(method = "renderShadowSingle", constant = @Constant(doubleValue = 0.015625))
    double renderShadowSingleConstantOnlySmall(double constant) {
        // hack field exists because we have no reference to entity that casted the shadow here
        return $cm$sizeHack < 1.0 ? // only for smalls because you actually become smaller than the shadow height lol
                constant * $cm$sizeHack :
                constant; // but don't move it extra up when being big
    }

    // undo the height scaling for renderLabel because we scale the whole model with the GL model matrix in RenderManagerMixin
    @SuppressWarnings("UnresolvedMixinReference") // the parameter is generic so the plugin gets confused
    @Redirect(method = "renderLivingLabel", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;height:F"))
    float renderLivingLabel(Entity self) {
        return (float) (self.height / ((IRenderSized) self).getSizeCM());
    }

    // extend the render bbox to include shadow height (vanilla fix even I guess)
    @ModifyVariable(method = "shouldRender", at = @At(value = "STORE", ordinal = 0))
    AxisAlignedBB shouldRender(AxisAlignedBB aabb, Entity entity) {
        return aabb.expand(0.0, -((ISized) entity).getSizeCM(), 0.0);
    }

    @Shadow
    protected float shadowSize;
}
