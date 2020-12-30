package dev.necauqua.mods.cm.mixin.entity.ai;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityAINearestAttackableTarget.class)
public abstract class EntityAINearestAttackableTargetMixin extends EntityAITargetMixin {

    @ModifyConstant(method = "getTargetableArea", constant = @Constant(doubleValue = 4.0))
    double getTargetableArea(double constant) {
        return constant * ((ISized) taskOwner).getSizeCM();
    }
}
