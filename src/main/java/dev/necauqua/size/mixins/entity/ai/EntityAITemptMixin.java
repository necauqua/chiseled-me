package dev.necauqua.size.mixins.entity.ai;

import dev.necauqua.size.api.ISized;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITempt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// scale area and closest distance for entities that follow you when you hold food
@Mixin(EntityAITempt.class)
public final class EntityAITemptMixin {

    @ModifyConstant(method = "shouldExecute", constant = @Constant(doubleValue = 10.0))
    double shouldExecute(double constant) {
        return Math.max(0.5625, constant * ((ISized) temptedEntity).getSize());
        // ^ limit it to 9 pixels minimum because mob AI works in integer block coordinates, meh
    }

    @ModifyConstant(method = "shouldContinueExecuting", constant = @Constant(doubleValue = 0.010000000000000002))
    double shouldContinueExecuting(double constant) {
        double size = ((ISized) temptedEntity).getSize();
        return constant * size * size;
    }

    @ModifyConstant(method = "shouldContinueExecuting", constant = @Constant(doubleValue = 36.0))
    double shouldContinueExecutingLimited(double constant) {
        double size = ((ISized) temptedEntity).getSize();
        return Math.max(0.5625 * 0.5625, constant * size * size);
        // ^ limit it to 9 pixels minimum because mob AI works in integer block coordinates, meh
    }

    @ModifyConstant(method = "updateTask", constant = @Constant(doubleValue = 6.25))
    double updateTask(double constant) {
        double size = ((ISized) temptedEntity).getSize();
        return Math.max(0.5625 * 0.5625, constant * size * size);
    }

    @Shadow
    @Final
    private EntityCreature temptedEntity;
}
