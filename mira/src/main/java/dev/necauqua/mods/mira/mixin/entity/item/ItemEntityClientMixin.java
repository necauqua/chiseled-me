/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.item;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityClientMixin {

    // ItemPickupParticle copies the item entity for some reason lol
    // with this custom clientside copy method
    @Inject(method = "copy", at = @At("RETURN"))
    void copy(CallbackInfoReturnable<ItemEntity> cir) {
        ((ISized) cir.getReturnValue()).setSizeCM(((ISized) this).getSizeCM());
    }
}
