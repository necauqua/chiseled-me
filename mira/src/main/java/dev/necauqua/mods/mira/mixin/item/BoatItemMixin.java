/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.item;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatItem.class)
public abstract class BoatItemMixin {

    private double $cm$size = 1.0;

    @Inject(method = "use", at = @At("HEAD"))
    void use(World entity, PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        $cm$size = ((ISized) player).getSizeCM();
    }

    @Redirect(method = "use", at = @At(value = "NEW", target = "net/minecraft/entity/item/BoatEntity"))
    BoatEntity use(World world, double x, double y, double z) {
        BoatEntity entity = new BoatEntity(world, x, y, z);
        // noinspection ConstantConditions intellij stfu
        ((ISized) entity).setSizeCM($cm$size);
        return entity;
    }

    @ModifyConstant(method = "use", constant = {
        @Constant(doubleValue = 5.0),
        @Constant(doubleValue = 1.0),
        @Constant(doubleValue = -0.1),
    })
    double use(double constant, World world, PlayerEntity player) {
        return constant * ((ISized) player).getSizeCM();
    }
}
