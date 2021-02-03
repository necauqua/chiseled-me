package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IRenderSized;
import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.client.particle.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(Particle.class)
public abstract class ParticleMixin implements ISized {

    public double $cm$size = 1.0;
    private float $cm$original_width;
    private float $cm$original_height;

    @Override
    public double getSizeCM() {
        return $cm$size;
    }

    @Override
    public void setSizeCM(double size) {
        $cm$size = size;

        width = (float) ($cm$original_width * size);
        height = (float) ($cm$original_height * size);

        float w = width / 2.0F;
        setBoundingBox(new AxisAlignedBB(posX - w, posY, posZ - w, posX + w, posY + height, posZ + w));
    }

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 0)
    float setSizeWidth(float width) {
        $cm$original_width = width;
        return (float) (width * $cm$size);
    }

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 1)
    float setSizeHeight(float height) {
        $cm$original_height = height;
        return (float) (height * $cm$size);
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0)
    double moveX(double x) {
        return x * $cm$size;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 1)
    double moveY(double y) {
        return y * $cm$size;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 2)
    double moveZ(double z) {
        return z * $cm$size;
    }

    @ModifyVariable(method = "renderParticle", ordinal = 10, at = @At("STORE"))
    float renderParticle(float scale) {
        return (float) (scale * $cm$size);
    }

    @Shadow
    protected float width;
    @Shadow
    protected float height;

    @Shadow
    protected double posX;
    @Shadow
    protected double posY;
    @Shadow
    protected double posZ;

    @Shadow
    protected double motionX;
    @Shadow
    protected double motionY;
    @Shadow
    protected double motionZ;

    @Shadow
    protected int particleAge;

    @Shadow
    protected int particleMaxAge;

    @Shadow
    public abstract void setBoundingBox(AxisAlignedBB bb);

    @Mixin(ParticleCrit.class)
    public static abstract class ParticleCritMixin extends ParticleMixin implements ISized {

        // this one onUpdate in constructor runs while the size is still
        // not set (so its 1) and for one tick the particle moves as if not scaled
        @Redirect(method = "<init>(Lnet/minecraft/world/World;DDDDDDF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleCrit;onUpdate()V"))
        void removeExtraOnUpdate(ParticleCrit self) {
        }
    }

    @Mixin(ParticlePortal.class)
    public static abstract class ParticlePortalMixin extends ParticleMixin {

        @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0)
        double moveX(double x) {
            return x * $cm$size;
        }

        @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 1)
        double moveY(double y) {
            return y * $cm$size;
        }

        @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 2)
        double moveZ(double z) {
            return z * $cm$size;
        }

        @Redirect(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticlePortal;motionX:D"))
        double onUpdateMotionX(ParticlePortal self) {
            return motionX * $cm$size;
        }

        @Inject(method = "onUpdate", at = @At(value = "FIELD", opcode = PUTFIELD, shift = AFTER, target = "Lnet/minecraft/client/particle/ParticlePortal;posY:D"))
        void onUpdateMotionY(CallbackInfo ci) {
            float f = (float) particleAge / (float) particleMaxAge;
            float f1 = -f + f * f * 2.0f;
            float f2 = 1.0f - f1;
            posY = portalPosY + (motionY * f2 + (1.0f - f)) * $cm$size;
        }

        @Redirect(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticlePortal;motionZ:D"))
        double onUpdateMotionZ(ParticlePortal self) {
            return motionZ * $cm$size;
        }

        @Shadow
        @Final
        private double portalPosY;
    }

    @Mixin(ParticleDigging.class)
    public static abstract class ParticleDiggingMixin extends ParticleMixin {

        @ModifyVariable(method = "renderParticle", ordinal = 10, at = @At("STORE"))
        float renderParticle(float scale) {
            return (float) (scale * $cm$size);
        }
    }

    @Mixin(ParticleBreaking.class)
    public static abstract class ParticleBreakingMixin extends ParticleMixin {

        @ModifyVariable(method = "renderParticle", ordinal = 10, at = @At("STORE"))
        float renderParticle(float scale) {
            return (float) (scale * $cm$size);
        }
    }

    @Mixin(ParticleBreaking.Factory.class)
    public static abstract class ParticleBreakingFactoryMixin {

        @ModifyConstant(method = "createParticle", constant = @Constant(intValue = 1, ordinal = 0))
        int createParticle(int constant, int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters) {
            return parameters.length == 3 ?
                    Integer.MAX_VALUE : // meh
                    constant;
        }
    }

    @Mixin(ParticleSweepAttack.class)
    public static abstract class ParticleSweepAttackMixin extends ParticleMixin {

        @ModifyVariable(method = "renderParticle", ordinal = 10, at = @At("STORE"))
        float renderParticle(float scale) {
            return (float) (scale * $cm$size);
        }
    }

    @Mixin(ParticleEmitter.class)
    public static abstract class ParticleEmitterMixin extends ParticleMixin {

        @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;ZDDDDDD[I)V"))
        int[] spawnParticleArgs(int[] args) {
            return EntitySizeInteractions.appendSize(args, ((IRenderSized) attachedEntity).getSizeCM());
        }

        @Shadow
        @Final
        private Entity attachedEntity;
    }

    @Mixin(ParticleItemPickup.class)
    public static abstract class ParticleItemPickupMixin extends ParticleMixin {

        @ModifyVariable(method = "<init>", at = @At("LOAD"))
        float constructor(float yOffset, World world, Entity item, Entity thrower) {
            return (float) (yOffset * ((IRenderSized) thrower).getSizeCM());
        }
    }
}
