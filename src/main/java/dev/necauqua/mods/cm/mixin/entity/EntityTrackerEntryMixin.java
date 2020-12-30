package dev.necauqua.mods.cm.mixin.entity;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityTrackerEntry.class)
public final class EntityTrackerEntryMixin {

    @ModifyVariable(method = "isVisibleTo", at = @At("STORE"))
    int isVisibleTo(int i) {
        double size = ((ISized) trackedEntity).getSizeCM();
        return size > 1.0 ? (int) (i * size) : i;
    }

    @ModifyConstant(method = "updatePlayerList", constant = @Constant(doubleValue = 16.0))
    double updatePlayerList(double constant) {
        double size = ((ISized) trackedEntity).getSizeCM();
        return constant * size * size;
    }

    @ModifyConstant(method = "updatePlayerList", constant = @Constant(longValue = 128))
    long updatePlayerList(long constant) {
        double size = ((ISized) trackedEntity).getSizeCM();
        return (long) (constant * size * size);
    }

    @Shadow
    @Final
    private Entity trackedEntity;
}
