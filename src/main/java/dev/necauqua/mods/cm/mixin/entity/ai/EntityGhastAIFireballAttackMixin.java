/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.ai;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.api.IWorldPreciseEvents;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

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

    // sound
    @Redirect(method = "updateTask()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/math/BlockPos;I)V"))
    void playEvent(World self, EntityPlayer player, int type, BlockPos pos, int data) {
        double size = ((ISized) parentEntity).getSizeCM();
        ((IWorldPreciseEvents) self).playEvent(null, type, pos, data, size, parentEntity.getPositionEyes(1.0f));
    }

    @Shadow
    @Final
    private EntityGhast parentEntity;
}
