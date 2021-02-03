/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity;

import dev.necauqua.mods.cm.Network;
import dev.necauqua.mods.cm.api.IRenderSized;
import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.size.ChangingSizeProcess;
import dev.necauqua.mods.cm.size.DataSerializerDouble;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mixin(Entity.class)
public abstract class EntityMixin implements IRenderSized {

    private static final DataParameter<Double> $CM$SIZE = EntityDataManager.createKey(Entity.class, DataSerializerDouble.INSTANCE);

    private static final String SIZE_NBT_TAG = "chiseled_me:size";

    public double $cm$size = 1.0;
    private float $cm$originalWidth = 0.0F;
    private float $cm$originalHeight = 0.0F;

    @Nullable
    private ChangingSizeProcess $cm$process = null;

    @Override
    public double getSizeCM() {
        return $cm$size;
    }

    @Override
    public double getSizeCM(float partialTick) {
        return $cm$process != null ?
                $cm$process.prevTickSize + ($cm$size - $cm$process.prevTickSize) * partialTick :
                $cm$size;
    }

    @Override
    public void setSizeCM(double size, int lerpTime) {
        dismountRidingEntity();
        removePassengers();

        // cannot reference EntityPlayer from here for cryptic reasons
        EntitySizeInteractions.wakeUp((Entity) (Object) this);

        if (lerpTime == 0) {
            setRawSizeCM(size);
        } else {
            $cm$process = new ChangingSizeProcess($cm$size, size, lerpTime);
            if (!world.isRemote) {
                Network.sync((Entity) (Object) this, size, lerpTime);
            }
        }

        Entity[] parts = getParts();
        if (parts != null) {
            for (Entity part : parts) {
                ((IRenderSized) part).setSizeCM(size, lerpTime);
            }
        }
    }

    @Override
    public boolean isResizingCM() {
        return $cm$process != null;
    }

    @Override
    public void setRawSizeCM(double size) {
        $cm$size = size;
        if ($cm$process == null) { // only force-set it when animation is done
            dataManager.set($CM$SIZE, size);
        }

        if ($cm$originalWidth == 0.0f) {
            $cm$originalWidth = width;
        }
        if ($cm$originalHeight == 0.0f) {
            $cm$originalHeight = height;
        }

        float w = (width = (float) ($cm$originalWidth * size)) / 2.0F;
        float h = height = (float) ($cm$originalHeight * size);

        AxisAlignedBB aabb = getEntityBoundingBox();
        double x = (aabb.minX + aabb.maxX) / 2.0;
        double z = (aabb.minZ + aabb.maxZ) / 2.0;
        setEntityBoundingBox(new AxisAlignedBB(x - w, aabb.minY, z - w, x + w, aabb.minY + h, z + w));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "<init>", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/network/datasync/EntityDataManager;register(Lnet/minecraft/network/datasync/DataParameter;Ljava/lang/Object;)V"))
    void registerSize(EntityDataManager dataManager, DataParameter key, Object value, World world) {
        dataManager.register($CM$SIZE, $cm$size);
        dataManager.register(key, value);
    }

    @Inject(method = "notifyDataManagerChange", at = @At("HEAD"))
    void notifyDataManagerChange(DataParameter<?> key, CallbackInfo ci) {
        if ($CM$SIZE.equals(key)) {
            setSizeCM(dataManager.get($CM$SIZE), 0);
        }
    }

    @Inject(method = "onEntityUpdate", at = @At("HEAD"))
    void onEntityUpdate(CallbackInfo ci) {
        ChangingSizeProcess p = $cm$process;
        if (p == null) {
            return;
        }
        if (p.lerpedTicks++ < p.lerpTime) {
            p.prevTickSize = $cm$size;
            setRawSizeCM(p.fromSize + (p.toSize - p.fromSize) / p.lerpTime * p.lerpedTicks);
        } else {
            $cm$process = null;
            setRawSizeCM(p.toSize);
        }
    }

    // region collision

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 0)
    float setSizeWidth(float width) {
        $cm$originalWidth = width;
        return (float) (width * $cm$size);
    }

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 1)
    float setSizeHeight(float height) {
        $cm$originalHeight = height;
        return (float) (height * $cm$size);
    }

    @ModifyConstant(method = "isEntityInsideOpaqueBlock", constant = @Constant(floatValue = 0.1f))
    float isEntityInsideOpaqueBlock(float constant) {
        return (float) (constant * $cm$size);
    }

    @ModifyConstant(method = "doBlockCollisions", constant = @Constant(doubleValue = 0.001))
    double doBlockCollisions(double constant) {
        return constant * $cm$size;
    }

    @ModifyConstant(method = "isInLava", constant = {
            @Constant(doubleValue = -0.10000000149011612),
            @Constant(doubleValue = -0.4000000059604645),
    })
    double isInLava(double constant) {
        return constant * $cm$size;
    }

    @ModifyConstant(method = "isOverWater", constant = {
            @Constant(doubleValue = -20.0),
            @Constant(doubleValue = 0.001),
    })
    double isOverWater(double constant) {
        return constant * $cm$size;
    }
    // endregion

    // region movement

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0)
    double moveX(double x, MoverType type) {
        return type == MoverType.SELF ? x * $cm$size : x;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 1)
    double moveY(double y, MoverType type) {
        return type == MoverType.SELF ? y * $cm$size : y;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 2)
    double moveZ(double z, MoverType type) {
        return type == MoverType.SELF ? z * $cm$size : z;
    }

    @ModifyConstant(method = "move", constant = {
            @Constant(doubleValue = 0.5),  //
            @Constant(doubleValue = -0.5), // shifting on edges of aabb's
            @Constant(doubleValue = 0.20000000298023224), // block step collision
            @Constant(doubleValue = 0.001), // flammable collision
    })
    double moveConstantsMul(double constant) {
        return constant * $cm$size;
    }

    @ModifyConstant(method = "move", constant = @Constant(doubleValue = 0.6))
    double moveStepSound(double constant) {
        return constant / $cm$size;
    }

    @ModifyConstant(method = "move", constant = @Constant(floatValue = 0.35f))
    float moveSwimSound(float constant) {
        return (float) (constant / $cm$size);
    }

    @Redirect(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;stepHeight:F"))
    float moveStepHeight(Entity self) {
        return (float) (self.stepHeight * $cm$size);
    }

    @ModifyConstant(method = "handleWaterMovement", constant = {
            @Constant(doubleValue = -0.4000000059604645),
            @Constant(doubleValue = 0.001),
    })
    double handleWaterMovement(double constant) {
        return constant * $cm$size;
    }

    @ModifyVariable(method = "updateFallState", at = @At("HEAD"), ordinal = 0)
    double updateFallState(double y) {
        return y / $cm$size;
    }

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    void applyEntityCollision(Entity self, double x, double y, double z, Entity other) {
        double coeff = ((ISized) other).getSizeCM() / ((ISized) self).getSizeCM();
        self.addVelocity(x * coeff, y * coeff, z * coeff);
    }

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    void applyEntityCollision2(Entity self, double x, double y, double z, Entity other) {
        double coeff = ((ISized) self).getSizeCM() / ((ISized) other).getSizeCM();
        self.addVelocity(x * coeff, y * coeff, z * coeff);
    }

    // endregion

    // region dropping items

    @ModifyVariable(method = "entityDropItem", at = @At("LOAD"))
    float entityDropItem(float offsetY) {
        return (float) (offsetY * $cm$size);
    }

    @ModifyVariable(method = "entityDropItem", at = @At("STORE"))
    EntityItem entityDropItem(EntityItem entityItem) {
        ((IRenderSized) entityItem).setSizeCM($cm$size);
        return entityItem;
    }

    // endregion

    // region NBT

    @Inject(method = "readFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"))
    void readFromNBT(NBTTagCompound nbt, CallbackInfo ci) {

        // fix for when mods just set width/height in constructor
        $cm$originalWidth = width;
        $cm$originalHeight = height;
        setRawSizeCM(1.0);

        double size = nbt.getDouble(SIZE_NBT_TAG);
        if (size != 0.0) {
            setSizeCM(size);
        }
    }

    @Inject(method = "writeToNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeEntityToNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"))
    void writeToNBT(NBTTagCompound nbt, CallbackInfoReturnable<NBTTagCompound> cir) {
        nbt.setDouble(SIZE_NBT_TAG, $cm$process != null ? $cm$process.toSize : $cm$size);
    }

    // endregion

    // region toString

    @ModifyConstant(method = "toString", remap = false, constant = @Constant(ordinal = 0))
    String toStringArgs(String constant) {
        return constant.substring(0, constant.length() - 1) + ", cm:size=%.4f" + constant.substring(constant.length() - 1);
    }

    @ModifyArg(method = "toString", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
    Object[] toStringArgs(Object[] args) {
        Object[] modified = Arrays.copyOf(args, args.length + 1);
        modified[args.length] = $cm$size;
        return modified;
    }

    // endregion

    // region particles

    @ModifyArg(method = "doWaterSplashEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] doWaterSplashEffect(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @ModifyConstant(method = "createRunningParticles", constant = {
            @Constant(doubleValue = 0.1),
            @Constant(doubleValue = 0.20000000298023224),
    })
    double createRunningParticles(double constant) {
        return constant * $cm$size;
    }

    @ModifyArg(method = "createRunningParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] createRunningParticles(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    // endregion

    // getPosition fix for commands or something
    @ModifyConstant(method = "getPosition", constant = @Constant(doubleValue = 0.5))
    double getPosition(double constant) {
        return constant * $cm$size;
    }

    @Shadow
    public World world;

    @Shadow
    protected EntityDataManager dataManager;

    @Shadow
    public float width;

    @Shadow
    public float height;

    // motionXYZ are used in EntityArrowMixin

    @Shadow
    public double motionX;

    @Shadow
    public double motionY;

    @Shadow
    public double motionZ;

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow
    public abstract void setEntityBoundingBox(AxisAlignedBB bb);

    @Shadow
    public abstract void dismountRidingEntity();

    @Shadow
    public abstract void removePassengers();

    @Shadow
    @Nullable
    public abstract Entity[] getParts();

    @Shadow
    public abstract String getName();
}
