/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin;

import dev.necauqua.mods.mira.extras.IEntityExtras;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(World.class)
public abstract class WorldMixin {

    // to not depend on whether entity calls super in it's onUpdate
    @Inject(method = "guardEntityTick", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = Shift.AFTER))
    void updateEntityWithOptionalForce(Consumer<Entity> consumer, Entity entity, CallbackInfo ci) {
        ((IEntityExtras) entity).onUpdateCM();
    }
}
