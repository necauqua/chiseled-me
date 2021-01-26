package dev.necauqua.size.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static dev.necauqua.size.EntitySizeInteractions.getAverageSize;

@Mixin(EntityCreature.class)
public abstract class EntityCreatureMixin extends EntityMixin {

    // leash distances

    @Redirect(method = "updateLeashedState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityCreature;getDistance(Lnet/minecraft/entity/Entity;)F"))
    float blockUsingShield(EntityCreature self, Entity entity) {
        double size = getAverageSize(self, entity);
        maximumHomeDistance = (float) (5.0 * size); // also fix this, whatever it is
        return (float) (self.getDistance(entity) / size);
    }

    @Shadow
    private float maximumHomeDistance;
}
