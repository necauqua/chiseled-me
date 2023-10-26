/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm2.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.necauqua.mods.cm2.ChiseledMe2.MODID;

@Mixin(BeaconTileEntity.class)
public final class BeaconTileEntityMixin {

    private static final String COLOR_TAG = MODID + ":color";

    private DyeColor $cm$baseColor = DyeColor.WHITE;

    // this redirect is pretty cringe, but it works
    // and the entire algorithm we're trying to adjust is pretty messy
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBeaconColorMultiplier(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)[F"))
    private float[] tickGetBeaconColorMultiplier(BlockState instance, IWorldReader reader, BlockPos pos, BlockPos beaconPos) {
        return pos.equals(beaconPos) ?
            $cm$baseColor.getTextureDiffuseColors() :
            instance.getBeaconColorMultiplier(reader, pos, beaconPos);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void load(BlockState state, CompoundNBT nbt, CallbackInfo ci) {
        $cm$baseColor = DyeColor.byName(nbt.getString(COLOR_TAG), $cm$baseColor);
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void save(CompoundNBT nbt, CallbackInfoReturnable<CompoundNBT> cir) {
        nbt.putString(COLOR_TAG, $cm$baseColor.getName());
    }
}
