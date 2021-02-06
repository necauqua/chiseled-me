/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.item;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEnderEye.class)
public final class ItemEnderEyeMixin {

    @Redirect(method = "onItemRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    boolean onItemRightClick(World self, Entity entity, World world, EntityPlayer player) {
        ((ISized) entity).setSizeCM(((ISized) player).getSizeCM());
        return self.spawnEntity(entity);
    }
}
