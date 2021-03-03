package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import static org.objectweb.asm.Opcodes.PUTFIELD;

@Mixin(Explosion.class)
public final class ExplosionMixin implements ISized {

    // don't reuse the vanilla explosion size
    // for more precision and to fix various offsets too
    private double $cm$size = 1.0;

    @ModifyConstant(method = "doExplosionA", constant = {
            @Constant(floatValue = 0.3f, ordinal = 0),
            @Constant(floatValue = 0.22500001f),
    })
    float doExplosionA(float constant) {
        return (float) (constant * $cm$size);
    }

    @Redirect(method = "doExplosionA", at = @At(value = "FIELD", target = "Lnet/minecraft/world/Explosion;size:F"))
    float doExplosionA(Explosion self) {
        return (float) (size * $cm$size);
    }

    @ModifyConstant(method = "doExplosionA", constant = {
            @Constant(doubleValue = 0.30000001192092896D),
            @Constant(doubleValue = 1.0, ordinal = 0),
            @Constant(doubleValue = 1.0, ordinal = 1),
            @Constant(doubleValue = 1.0, ordinal = 2),
            @Constant(doubleValue = 1.0, ordinal = 3),
            @Constant(doubleValue = 1.0, ordinal = 4),
            @Constant(doubleValue = 1.0, ordinal = 5),
    })
    double doExplosionA(double constant) {
        return (float) (constant * $cm$size);
    }

    @Redirect(method = "doExplosionA", at = @At(value = "FIELD", opcode = PUTFIELD, target = "Lnet/minecraft/entity/Entity;motionX:D"))
    void doExplosionAmotionX(Entity entity, double value) {
        entity.motionX += (value - entity.motionX) * $cm$size / ((ISized) entity).getSizeCM();
    }

    @Redirect(method = "doExplosionA", at = @At(value = "FIELD", opcode = PUTFIELD, target = "Lnet/minecraft/entity/Entity;motionY:D"))
    void doExplosionAmotionY(Entity entity, double value) {
        entity.motionY += (value - entity.motionY) * $cm$size / ((ISized) entity).getSizeCM();
    }

    @Redirect(method = "doExplosionA", at = @At(value = "FIELD", opcode = PUTFIELD, target = "Lnet/minecraft/entity/Entity;motionZ:D"))
    void doExplosionAmotionZ(Entity entity, double value) {
        entity.motionZ += (value - entity.motionZ) * $cm$size / ((ISized) entity).getSizeCM();
    }

    @ModifyArg(method = "doExplosionB", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] doExplosionB(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @Override
    public double getSizeCM() {
        return $cm$size;
    }

    @Override
    public void setSizeCM(double size) {
        $cm$size = size;
    }

    @Shadow
    @Final
    private float size;
}
