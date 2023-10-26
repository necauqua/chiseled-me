/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity;

import dev.necauqua.mods.mira.Config;
import dev.necauqua.mods.mira.Network;
import dev.necauqua.mods.mira.api.*;
import dev.necauqua.mods.mira.data.DataSerializerDouble;
import dev.necauqua.mods.mira.extras.IEntityExtras;
import dev.necauqua.mods.mira.size.MixinHelpers;
import dev.necauqua.mods.mira.size.ResizingProcess;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent.Size;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mixin(Entity.class)
public abstract class EntityMixin implements IRenderSized, IEntityExtras {

    private static final DataParameter<Double> $CM$SIZE = EntityDataManager.defineId(Entity.class,
        DataSerializerDouble.INSTANCE);
    private static final String SIZE_NBT_TAG = "mira:size";
    private static final String OLD_SIZE_NBT_TAG = "chiseled_me:size";

    public double $cm$size = 1.0;
    @Shadow
    public World level;
    @Final
    @Shadow
    protected EntityDataManager entityData;
    @Shadow
    protected boolean onGround;
    @Nullable
    private ResizingProcess $cm$process = null;
    @Shadow
    private EntitySize dimensions;
    @Shadow
    private float eyeHeight;

    @Override
    public double getSizeCM() {
        return $cm$size;
    }

    @Override
    public double getSizeCM(float partialTick) {
        return $cm$process != null ? $cm$process.prevTickSize + ($cm$size - $cm$process.prevTickSize) * partialTick
            : $cm$size;
    }

    @SuppressWarnings("ConstantConditions") // we *are* the entity class one the mixin is applied, lul
    @Override
    public void setSizeCM(double size, int animationTicks) {
        EntitySizeEvent event = new EntitySizeEvent((Entity) (Object) this, $cm$size, size, animationTicks);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        size = event.getSize();
        animationTicks = event.getAnimationTicks();

        stopRiding();
        ejectPassengers();

        // cannot reference EntityPlayer from here for cryptic reasons
        MixinHelpers.onSetSize((Entity) (Object) this, $cm$size, size);

        if (animationTicks == 0) {
            setRawSizeCM(size);
        } else {
            $cm$process = new ResizingProcess($cm$size, size, animationTicks);
            Network.sync((Entity) (Object) this, size, animationTicks);
        }

        Entity[] parts = ((IForgeEntity) this).getParts();
        if (parts != null) {
            for (Entity part : parts) {
                ((IRenderSized) part).setSizeCM(size, animationTicks);
            }
        }
    }

    // region collision

    @Override
    @Nullable
    public IResizingProgress getResizingCM() {
        return $cm$process;
    }

    @SuppressWarnings("ConstantConditions") // same
    @Override
    public void setRawSizeCM(double size) {
        // this.dimensions but unscaled
        EntitySize oldDimensions = new EntitySize((float) (dimensions.width / $cm$size),
            (float) (dimensions.height / $cm$size), dimensions.fixed);

        $cm$size = size;
        if ($cm$process == null) { // only force-set it when animation is done
            entityData.set($CM$SIZE, size);
        }

        // inlined `refreshDimensions` methods with adjustments to avoid weird position
        // changes

        Pose pose = getPose();
        // this.getDimensions is always unscaled
        EntitySize newDimensions = getDimensions(pose);
        Size sizeEvent = ForgeEventFactory.getEntitySizeForge((Entity) (Object) this, pose, oldDimensions,
            newDimensions, getEyeHeight(pose, newDimensions));
        // and obviously the event result is unscaled (by us) as well
        newDimensions = sizeEvent.getNewSize();

        float prevEyeHeight = eyeHeight;
        // set this.eyeHeight and this.size to be scaled (by us) again
        eyeHeight = (float) (sizeEvent.getNewEyeHeight() * size);
        if (level.isClientSide) {
            MixinHelpers.setCameraHeight((Entity) (Object) this, prevEyeHeight, eyeHeight);
        }

        float w = (float) (newDimensions.width * size);
        float h = (float) (newDimensions.height * size);
        dimensions = new EntitySize(w, h, dimensions.fixed);

        // custom aabb update which does not have weird logic causing weird offsets

        AxisAlignedBB aabb = getBoundingBox();
        double x = (aabb.minX + aabb.maxX) / 2.0;
        double z = (aabb.minZ + aabb.maxZ) / 2.0;
        w /= 2.0F;
        setBoundingBox(new AxisAlignedBB(x - w, aabb.minY, z - w, x + w, aabb.minY + h, z + w));
    }

    @Override
    public void onUpdateCM() {
        ResizingProcess p = $cm$process;
        if (p == null) {
            return;
        }
        if (p.currentTime++ < p.interval) {
            p.prevTickSize = $cm$size;
            setRawSizeCM(p.startSize + (p.targetSize - p.startSize) / p.interval * p.currentTime);
        } else {
            $cm$process = null;
            setRawSizeCM(p.targetSize);
        }
    }

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract EntitySize getDimensions(Pose poseIn);

    @Shadow
    protected abstract float getEyeHeight(Pose poseIn, EntitySize sizeIn);

    @Shadow
    public abstract AxisAlignedBB getBoundingBox();

    @Shadow
    public abstract void setBoundingBox(AxisAlignedBB bb);

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "<init>", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/network/datasync/EntityDataManager;define(Lnet/minecraft/network/datasync/DataParameter;Ljava/lang/Object;)V"))
    void registerSize(EntityDataManager dataManager, DataParameter key, Object value) {
        dataManager.define($CM$SIZE, $cm$size);
        dataManager.define(key, value);
    }

    // endregion

    // region movement

    @Inject(method = "onSyncedDataUpdated", at = @At("HEAD"))
    void onSyncedDataUpdated(DataParameter<?> key, CallbackInfo ci) {
        if ($CM$SIZE.equals(key)) {
            setSizeCM(entityData.get($CM$SIZE), 0);
        }
    }

    @Shadow
    public abstract void stopRiding();

    @Shadow
    public abstract void ejectPassengers();

    @ModifyVariable(method = "refreshDimensions", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    EntitySize refreshDimensionsUnScaleSize(EntitySize dimension) {
        // unscale this.dimension before the event
        return new EntitySize((float) (dimension.width / $cm$size), (float) (dimension.height / $cm$size),
            dimension.fixed);
    }

    @ModifyVariable(method = "refreshDimensions", at = @At(value = "STORE", ordinal = 1), ordinal = 1)
    EntitySize refreshDimensionsScaleSize(EntitySize size) {
        return new EntitySize((float) (size.width * $cm$size), (float) (size.height * $cm$size), size.fixed);
    }

    @Redirect(method = "refreshDimensions", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;eyeHeight:F", opcode = Opcodes.PUTFIELD))
    void refreshDimensionsScaleEyeHeight(Entity self, float eyeHeight) {
        this.eyeHeight = (float) (eyeHeight * $cm$size);
    }

    @ModifyConstant(method = "isInWall", constant = @Constant(doubleValue = 0.10000000149011612D))
    double isInWall(double constant) {
        return constant * $cm$size;
    }

    @ModifyConstant(method = "checkInsideBlocks", constant = @Constant(doubleValue = 0.001))
    double checkInsideBlocks(double constant) {
        return constant * $cm$size;
    }

    @ModifyVariable(method = "getBoundingBoxForPose", at = @At("STORE"))
    float getBoundingBoxForPoseScaleWidth(float f) {
        return (float) (f * ((ISized) this).getSizeCM());
    }

    @Redirect(method = "getBoundingBoxForPose", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntitySize;height:F"))
    float getBoundingBoxForPoseScaleWidth(EntitySize instance) {
        return (float) (instance.height * ((ISized) this).getSizeCM());
    }

    @ModifyConstant(method = "collide", constant = @Constant(doubleValue = 1.0E-7))
    double collideScaleBBoxDeflation(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    @ModifyConstant(method = "getBlockPosBelowThatAffectsMyMovement", constant = @Constant(doubleValue = 0.5000001))
    double getBlockPosBelowThatAffectsMyMovementScaleOffset(double constant) {
        return constant * ((ISized) this).getSizeCM();
    }

    // endregion

    // region NBT

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    Vector3d move(Vector3d offset, MoverType type) {
        return type == MoverType.SELF || type == MoverType.PLAYER ? offset.multiply($cm$size, $cm$size, $cm$size)
            : offset;
    }

    @ModifyConstant(method = "move", constant = {
        @Constant(doubleValue = 1.0E-7D), // small movement limit?. fixed being stuck when supersmall
        @Constant(doubleValue = 0.5), //
        @Constant(doubleValue = -0.5), // shifting on edges of aabb's
        @Constant(doubleValue = 0.20000000298023224), // block step collision
        @Constant(doubleValue = 0.001), // flammable collision
    })
    double moveConstantsMul(double constant) {
        return constant * $cm$size;
    }

    // endregion

    // region toString

    @ModifyConstant(method = "move", constant = @Constant(doubleValue = 0.6))
    double moveWalkDist(double constant) {
        return constant / $cm$size;
    }

    @ModifyConstant(method = "move", constant = @Constant(floatValue = 0.35f))
    float moveSwimSound(float constant) {
        return (float) (constant / $cm$size);
    }

    // endregion

    // region particles

    @Redirect(method = "collide(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;maxUpStep:F", ordinal = 1))
    float collideStepHeight1(Entity self) {
        return (float) (self.maxUpStep * $cm$size);
    }

    @Redirect(method = "collide(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;maxUpStep:F", ordinal = 2))
    float collideStepHeight2(Entity self) {
        return (float) (self.maxUpStep * $cm$size);
    }

    @Redirect(method = "collide(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;maxUpStep:F", ordinal = 3))
    float collideStepHeight3(Entity self) {
        return (float) (self.maxUpStep * $cm$size);
    }

    @ModifyVariable(method = "updateFluidHeightAndDoFluidPushing", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    double updateFluidHeightAndDoFluidPushingMotionScale(double motionScale) {
        return motionScale * $cm$size;
    }

    @ModifyConstant(method = "updateFluidHeightAndDoFluidPushing", constant = @Constant(doubleValue = 0.001))
    double updateFluidHeightAndDoFluidPushingConstant(double constant) {
        return constant * $cm$size;
    }

    // endregion

    @ModifyVariable(method = "checkFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    double checkFallDamage(double y) {
        return !Config.scaleFallSmall.get() && $cm$size < 1.0 || !Config.scaleFallBig.get() && $cm$size > 1.0 ? y
            : y / $cm$size;
    }

    @Redirect(method = "push(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/entity/Entity;push(DDD)V"))
    void push(Entity self, double x, double y, double z, Entity other) {
        if ($cm$size < 1.0 && Config.scaleMassSmall.get() || $cm$size > 1.0 && Config.scaleMassBig.get()) {
            double coeff = ((ISized) other).getSizeCM() / $cm$size;
            self.push(x * coeff, y * coeff, z * coeff);
        } else {
            self.push(x, y, z);
        }
    }

    @Redirect(method = "push(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/entity/Entity;push(DDD)V"))
    void push2(Entity self, double x, double y, double z, Entity other) {
        double otherSize = ((ISized) other).getSizeCM();
        if (otherSize < 1.0 && Config.scaleMassSmall.get() || otherSize > 1.0 && Config.scaleMassBig.get()) {
            double coeff = $cm$size / otherSize;
            self.push(x * coeff, y * coeff, z * coeff);
        } else {
            self.push(x, y, z);
        }
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundNBT;)V"))
    void load(CompoundNBT nbt, CallbackInfo ci) {
        // fixup for upgrading from 1.12 if that even makes sense
        if (nbt.contains(OLD_SIZE_NBT_TAG, NBT.TAG_ANY_NUMERIC)) {
            setSizeCM(nbt.getDouble(OLD_SIZE_NBT_TAG));
            return;
        }
        if (nbt.contains(SIZE_NBT_TAG, NBT.TAG_ANY_NUMERIC)) {
            setSizeCM(nbt.getDouble(SIZE_NBT_TAG));
        }
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundNBT;)V"))
    void saveWithoutId(CompoundNBT nbt, CallbackInfoReturnable<CompoundNBT> cir) {
        nbt.putDouble(SIZE_NBT_TAG, $cm$process != null ? $cm$process.targetSize : $cm$size);
    }

    @ModifyConstant(method = "toString", remap = false, constant = @Constant(ordinal = 0))
    String toStringArgs(String constant) {
        return constant.substring(0, constant.length() - 1) + ", size=%.4f" + constant.substring(constant.length() - 1);
    }

    @ModifyArg(method = "toString", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
    Object[] toStringArgs(Object[] args) {
        Object[] modified = Arrays.copyOf(args, args.length + 1);
        modified[args.length] = $cm$size;
        return modified;
    }

    @ModifyConstant(method = "doWaterSplashEffect", constant = {
        @Constant(floatValue = 1.0f, ordinal = 5),
        @Constant(floatValue = 1.0f, ordinal = 7)
    })
    float doWaterSplashEffect(float constant) {
        return (float) (constant * $cm$size);
    }

    @ModifyArg(method = "doWaterSplashEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData doWaterSplashEffect(IParticleData args) {
        return ScaledParticleData.wrap(args, $cm$size);
    }

    @Inject(method = "canSpawnSprintParticle", at = @At("HEAD"), cancellable = true)
    void spawnSprintParticle(CallbackInfoReturnable<Boolean> cir) {
        if (!onGround) {
            cir.setReturnValue(false);
        }
    }

    @ModifyConstant(method = "spawnSprintParticle", constant = {
        @Constant(doubleValue = 0.1),
        @Constant(doubleValue = 0.20000000298023224),
    })
    double spawnSprintParticle(double constant) {
        return constant * $cm$size;
    }

    @ModifyArg(method = "spawnSprintParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    IParticleData spawnSprintParticle(IParticleData args) {
        return ScaledParticleData.wrap(args, $cm$size);
    }

    // more vanilla fixes for the surprising cases like entities being tiny lol
    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    void playStepSound(CallbackInfo ci) {
        if (!onGround) {
            ci.cancel();
        }
    }

    @Redirect(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"))
    void playSound(World self, @Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound,
                   SoundCategory category, float volume, float pitch) {
        ((IWorldPreciseSounds) self).playSound(null, new Vector3d(x, y, z), sound, category, volume, pitch, $cm$size);
    }
}
