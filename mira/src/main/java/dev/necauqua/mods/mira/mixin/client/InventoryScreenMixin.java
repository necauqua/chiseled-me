/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    // cancel out the entity size when rendering it in GUIs
    @Redirect(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;scale(FFF)V"))
    private static void scale(MatrixStack instance, float x, float y, float z, int guiX, int guiY, int scale, float yaw,
                              float pitch, LivingEntity entity) {
        double size = ((ISized) entity).getSizeCM();
        instance.scale((float) (x / size), (float) (y / size), (float) (z / size));
    }
}
