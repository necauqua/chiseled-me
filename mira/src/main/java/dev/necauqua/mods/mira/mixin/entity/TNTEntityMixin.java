package dev.necauqua.mods.mira.mixin.entity;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.particles.IParticleData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TNTEntity.class)
public abstract class TNTEntityMixin extends Entity {

    public TNTEntityMixin(EntityType<?> entityType, World level) {
        super(entityType, level);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData tick(IParticleData particleData) {
        return ScaledParticleData.wrap(particleData, ((ISized) this).getSizeCM());
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 0.5))
    double tick(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    @Inject(method = "onSyncedDataUpdated", at = @At("HEAD"))
    void onSyncedDataUpdated(DataParameter<?> dataParameter, CallbackInfo ci) {
        super.onSyncedDataUpdated(dataParameter);
    }
}
