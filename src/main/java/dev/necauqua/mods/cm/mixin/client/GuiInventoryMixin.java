/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiInventory.class)
public final class GuiInventoryMixin {

    // cancel out the entity size when rendering it in GUIs
    @Redirect(method = "drawEntityOnScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V"))
    private static void scale(float x, float y, float z,
            int posX, int posY, int scale, float mouseX, float mouseY,
            EntityLivingBase entity) {
        double size = ((IRenderSized) entity).getSizeCM();
        GlStateManager.scale(x / size, y / size, z / size);
    }
}
