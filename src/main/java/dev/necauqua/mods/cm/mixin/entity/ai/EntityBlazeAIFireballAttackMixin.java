package dev.necauqua.mods.cm.mixin.entity.ai;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityBlaze;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.entity.monster.EntityBlaze$AIFireballAttack")
public final class EntityBlazeAIFireballAttackMixin {

    // fireball vertical offset
    @ModifyConstant(method = "updateTask()V", constant = @Constant(doubleValue = 0.5))
    double updateTask(double constant) {
        return constant * ((ISized) blaze).getSizeCM();
    }

    // melee attack, lol
    @ModifyConstant(method = "updateTask()V", constant = @Constant(doubleValue = 4.0))
    double updateTaskSq(double constant) {
        assert blaze.getAttackTarget() != null;
        return constant * ((ISized) blaze).getSizeCM() * ((ISized) blaze.getAttackTarget()).getSizeCM();
    }

    @ModifyConstant(method = "getFollowDistance()D", constant = @Constant(doubleValue = 16.0))
    double getFollowDistance(double constant) {
        return constant * ((ISized) blaze).getSizeCM();
    }

    @Redirect(method = "getFollowDistance()D", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;getAttributeValue()D"))
    double getFollowDistance(IAttributeInstance self) {
        return self.getAttributeValue() * ((ISized) blaze).getSizeCM();
    }

    @Shadow
    @Final
    private EntityBlaze blaze;
}
