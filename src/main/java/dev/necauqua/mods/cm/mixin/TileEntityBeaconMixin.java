/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityBeacon.class)
public final class TileEntityBeaconMixin {

    private EnumDyeColor $cm$baseColor = EnumDyeColor.WHITE;

    @Group(name = "beaconColor", min = 1, max = 1)
    @Redirect(method = "updateSegmentColors", at = @At(value = "FIELD", target = "Lnet/minecraft/item/EnumDyeColor;WHITE:Lnet/minecraft/item/EnumDyeColor;"), expect = 0)
    EnumDyeColor beaconColor() {
        return $cm$baseColor;
    }

    @Group(name = "beaconColor", min = 1, max = 1)
    @Dynamic
    @Redirect(method = "updateGlassLayers", remap = false, at = @At(value = "FIELD", target = "Lnet/minecraft/item/EnumDyeColor;WHITE:Lnet/minecraft/item/EnumDyeColor;"), expect = 0)
    EnumDyeColor beaconColorBetterFps() {
        return $cm$baseColor;
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    void readFromNBT(NBTTagCompound nbt, CallbackInfo ci) {
        $cm$baseColor = EnumDyeColor.byMetadata(nbt.getByte("chiseled_me:color"));
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    void writeToNBT(NBTTagCompound nbt, CallbackInfoReturnable<NBTTagCompound> cir) {
        nbt.setByte("chiseled_me:color", (byte) $cm$baseColor.getMetadata());
    }
}
