package dev.necauqua.mods.cm.mixin.entity;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({Entity.class, EntityOtherPlayerMP.class})
public abstract class EntityMixinClient implements ISized {

    @ModifyVariable(method = "isInRangeToRenderDist", ordinal = 1, at = @At(value = "STORE", ordinal = 0))
    double isInRangeToRenderDist(double averageEdgeLength) {
        double size = getSizeCM();
        return size >= 1.0 ? // so big ones should use the default algo which should see the giant hitbox
                averageEdgeLength :
                averageEdgeLength / size; // but small ones fool the algo so it renders even one pixel of them
    }
}
