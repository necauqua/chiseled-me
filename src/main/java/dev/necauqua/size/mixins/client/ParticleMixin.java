package dev.necauqua.size.mixins.client;

import dev.necauqua.size.api.IRenderSized;
import dev.necauqua.size.api.ISized;
import dev.necauqua.size.EntitySizeInteractions;
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

    public double $size$it = 1.0;
    private float $size$original_width;
    private float $size$original_height;

    @Override
    public double getSize() {
        return $size$it;
    }

    @Override
    public void setSize(double size) {
        $size$it = size;

        width = (float) ($size$original_width * size);
        height = (float) ($size$original_height * size);

        float w = width / 2.0F;
        setBoundingBox(new AxisAlignedBB(posX - w, posY, posZ - w, posX + w, posY + height, posZ + w));
    }

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 0)
    float setSizeWidth(float width) {
        $size$original_width = width;
        return (float) (width * $size$it);
    }

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 1)
    float setSizeHeight(float height) {
        $size$original_height = height;
        return (float) (height * $size$it);
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0)
    double moveX(double x) {
        return x * $size$it;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 1)
    double moveY(double y) {
        return y * $size$it;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 2)
    double moveZ(double z) {
        return z * $size$it;
    }

    @ModifyVariable(method = "renderParticle", ordinal = 10, at = @At("STORE"))
    float renderParticle(float scale) {
        return (float) (scale * $size$it);
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
            return x * $size$it;
        }

        @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 1)
        double moveY(double y) {
            return y * $size$it;
        }

        @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 2)
        double moveZ(double z) {
            return z * $size$it;
        }

        @Redirect(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticlePortal;motionX:D"))
        double onUpdateMotionX(ParticlePortal self) {
            return motionX * $size$it;
        }

        @Inject(method = "onUpdate", at = @At(value = "FIELD", opcode = PUTFIELD, shift = AFTER, target = "Lnet/minecraft/client/particle/ParticlePortal;posY:D"))
        void onUpdateMotionY(CallbackInfo ci) {
            float f = (float) particleAge / (float) particleMaxAge;
            float f1 = -f + f * f * 2.0f;
            float f2 = 1.0f - f1;
            posY = portalPosY + (motionY * f2 + (1.0f - f)) * $size$it;
        }

        @Redirect(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticlePortal;motionZ:D"))
        double onUpdateMotionZ(ParticlePortal self) {
            return motionZ * $size$it;
        }

        @Shadow
        @Final
        private double portalPosY;
    }

    @Mixin(ParticleDigging.class)
    public static abstract class ParticleDiggingMixin extends ParticleMixin {

        @ModifyVariable(method = "renderParticle", ordinal = 10, at = @At("STORE"))
        float renderParticle(float scale) {
            return (float) (scale * $size$it);
        }
    }

    @Mixin(ParticleBreaking.class)
    public static abstract class ParticleBreakingMixin extends ParticleMixin {

        @ModifyVariable(method = "renderParticle", ordinal = 10, at = @At("STORE"))
        float renderParticle(float scale) {
            return (float) (scale * $size$it);
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
            return (float) (scale * $size$it);
        }
    }

    @Mixin(ParticleEmitter.class)
    public static abstract class ParticleEmitterMixin extends ParticleMixin {

        @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;ZDDDDDD[I)V"))
        int[] spawnParticleArgs(int[] args) {
            return EntitySizeInteractions.appendSize(args, ((IRenderSized) attachedEntity).getSize());
        }

        @Shadow
        @Final
        private Entity attachedEntity;
    }

    @Mixin(ParticleItemPickup.class)
    public static abstract class ParticleItemPickupMixin extends ParticleMixin {

        @ModifyVariable(method = "<init>", at = @At("LOAD"))
        float constructor(float yOffset, World world, Entity item, Entity thrower) {
            return (float) (yOffset * ((IRenderSized) thrower).getSize());
        }
    }
}
