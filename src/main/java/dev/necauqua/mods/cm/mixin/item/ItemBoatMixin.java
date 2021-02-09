/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.item;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBoat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBoat.class)
public final class ItemBoatMixin {

    private static double $cm$hack = 1.0;

    @Inject(method = "onItemRightClick", at = @At("HEAD"))
    void onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        $cm$hack = ((ISized) player).getSizeCM();
    }

    @Redirect(method = "onItemRightClick", at = @At(value = "NEW", target = "net/minecraft/entity/item/EntityBoat"))
    EntityBoat onItemRightClick(World worldIn, double x, double y, double z) {
        EntityBoat entityBoat = new EntityBoat(worldIn, x, y, z);
        //noinspection ConstantConditions intellij stfu
        ((ISized) entityBoat).setSizeCM($cm$hack);
        return entityBoat;
    }

    @ModifyConstant(method = "onItemRightClick", constant = {
            @Constant(doubleValue = 5.0),
            @Constant(doubleValue = 1.0, ordinal = 3),
            @Constant(doubleValue = -0.12),
            @Constant(doubleValue = -0.1),
    })
    double isInLava(double constant) {
        return constant * $cm$hack;
    }
}
