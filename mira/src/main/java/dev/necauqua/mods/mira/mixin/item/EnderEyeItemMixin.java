/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.item;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.EyeOfEnderEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public abstract class EnderEyeItemMixin {

    private double $cm$size = 1.0;

    @Inject(method = "use", at = @At("HEAD"))
    void use(World entity, PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        $cm$size = ((ISized) player).getSizeCM();
    }

    @Redirect(method = "use", at = @At(value = "NEW", target = "net/minecraft/entity/projectile/EyeOfEnderEntity"))
    EyeOfEnderEntity use(World world, double x, double y, double z) {
        EyeOfEnderEntity entity = new EyeOfEnderEntity(world, x, y, z);
        // noinspection ConstantConditions intellij stfu
        ((ISized) entity).setSizeCM($cm$size);
        return entity;
    }

    // @Redirect(method = "use", at = @At(value = "INVOKE", target =
    // "Lnet/minecraft/world/World;playEvent(Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/math/BlockPos;I)V"))
    // void playEvent(World self, EntityPlayer player, int type, BlockPos pos, int
    // data, World world, EntityPlayer player2) {
    // double size = ((ISized) player2).getSizeCM();
    // ((IWorldPreciseEvents) self).playEvent(player, type, pos, data, size,
    // player2.getPositionEyes(1.0f));
    // }

    // @Redirect(method = "use", at = @At(value = "INVOKE", target =
    // "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"))
    // void playSound(World self, EntityPlayer player, double x, double y, double z,
    // SoundEvent sound, SoundCategory category, float volume, float pitch, World
    // world, EntityPlayer player2) {
    // double size = ((ISized) player2).getSizeCM();
    // ((IWorldPreciseSounds) self).playSound(player, player2.getPositionEyes(1.0f),
    // sound, category, volume, pitch, size);
    // }
}
