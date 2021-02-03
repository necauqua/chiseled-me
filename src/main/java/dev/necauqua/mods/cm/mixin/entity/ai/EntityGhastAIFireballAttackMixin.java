/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.ai;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.monster.EntityGhast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.entity.monster.EntityGhast$AIFireballAttack")
public final class EntityGhastAIFireballAttackMixin {

    // fireball offsets
    @ModifyConstant(method = "updateTask()V", constant = {
            @Constant(doubleValue = 0.5),
            @Constant(doubleValue = 4.0),
    })
    double updateTask(double constant) {
        return constant * ((ISized) parentEntity).getSizeCM();
    }

    @ModifyConstant(method = "updateTask()V", constant = @Constant(doubleValue = 4096.0))
    double updateTaskSq(double constant) {
        assert parentEntity.getAttackTarget() != null;
        return constant * ((ISized) parentEntity).getSizeCM() * ((ISized) parentEntity.getAttackTarget()).getSizeCM();
    }

    @Shadow
    @Final
    private EntityGhast parentEntity;
}
