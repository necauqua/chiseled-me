/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.ai;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.entity.monster.EntitySpider$AISpiderAttack")
public final class EntitySpiderAISpiderAttack extends EntityAIAttackMelee {

    public EntitySpiderAISpiderAttack(EntityCreature creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
    }

    @ModifyConstant(method = "getAttackReachSqr(Lnet/minecraft/entity/EntityLivingBase;)D", constant = @Constant(floatValue = 4.0f), require = 0, expect = 0)
    float getAttackReachSqr(float constant) {
        double size = ((ISized) attacker).getSizeCM();
        return (float) (constant * size * size);
    }
}
