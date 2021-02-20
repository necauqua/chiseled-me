/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.item;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.api.IWorldPreciseEvents;
import dev.necauqua.mods.cm.api.IWorldPreciseSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
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

    @Redirect(method = "onItemRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/math/BlockPos;I)V"))
    void playEvent(World self, EntityPlayer player, int type, BlockPos pos, int data, World world, EntityPlayer player2) {
        double size = ((ISized) player2).getSizeCM();
        ((IWorldPreciseEvents) self).playEvent(player, type, pos, data, size, player2.getPositionEyes(1.0f));
    }

    @Redirect(method = "onItemRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"))
    void playSound(World self, EntityPlayer player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, World world, EntityPlayer player2) {
        double size = ((ISized) player2).getSizeCM();
        ((IWorldPreciseSounds) self).playSound(player, player2.getPositionEyes(1.0f), sound, category, volume, pitch, size);
    }
}
