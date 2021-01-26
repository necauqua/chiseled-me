package dev.necauqua.size.mixins.entity;

import dev.necauqua.size.Network;
import dev.necauqua.size.api.IRenderSized;
import dev.necauqua.size.api.ISized;
import dev.necauqua.size.ChangingSizeProcess;
import dev.necauqua.size.DataSerializerDouble;
import dev.necauqua.size.EntitySizeInteractions;
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

    private static final DataParameter<Double> $SIZE$IT = EntityDataManager.createKey(Entity.class, DataSerializerDouble.INSTANCE);

    public double $size$it = 1.0;
    private float $size$originalWidth = 0.8F;
    private float $size$originalHeight = 1.8F;

    @Nullable
    private ChangingSizeProcess $size$process = null;

    @Override
    public double getSize() {
        return $size$it;
    }

    @Override
    public double getSize(float partialTick) {
        return $size$process != null ?
                $size$process.prevTickSize + ($size$it - $size$process.prevTickSize) * partialTick :
                $size$it;
    }

    @Override
    public void setSize(double size, int lerpTime) {
        dismountRidingEntity();
        removePassengers();

        EntitySizeInteractions.onEntitySetSize((Entity) (Object) this, $size$it, size);

        if (lerpTime == 0) {
            setRawSize(size);
        } else {
            $size$process = new ChangingSizeProcess($size$it, size, lerpTime);
            if (!world.isRemote) {
                Network.sync((Entity) (Object) this, size, lerpTime);
            }
        }

        Entity[] parts = getParts();
        if (parts != null) {
            for (Entity part : parts) {
                ((IRenderSized) part).setSize(size, lerpTime);
            }
        }
    }

    @Override
    public int getResizingTicks() {
        ChangingSizeProcess p = this.$size$process;
        return p != null ? p.lerpTime - p.lerpedTicks : 0;
    }

    @Override
    public void setRawSize(double size) {
        $size$it = size;
        if ($size$process == null) { // only force-set it when animation is done
            dataManager.set($SIZE$IT, size);
        }

        float w = (width = (float) ($size$originalWidth * size)) / 2.0F;
        float h = height = (float) ($size$originalHeight * size);

        AxisAlignedBB aabb = getEntityBoundingBox();
        double x = (aabb.minX + aabb.maxX) / 2.0;
        double z = (aabb.minZ + aabb.maxZ) / 2.0;
        setEntityBoundingBox(new AxisAlignedBB(x - w, aabb.minY, z - w, x + w, aabb.minY + h, z + w));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "<init>", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/network/datasync/EntityDataManager;register(Lnet/minecraft/network/datasync/DataParameter;Ljava/lang/Object;)V"))
    void registerSize(EntityDataManager dataManager, DataParameter key, Object value, World world) {
        dataManager.register($SIZE$IT, $size$it);
        dataManager.register(key, value);
    }

    @Inject(method = "notifyDataManagerChange", at = @At("HEAD"))
    void notifyDataManagerChange(DataParameter<?> key, CallbackInfo ci) {
        if ($SIZE$IT.equals(key)) {
            setSize(dataManager.get($SIZE$IT), 0);
        }
    }

    @Inject(method = "onEntityUpdate", at = @At("HEAD"))
    void onEntityUpdate(CallbackInfo ci) {
        ChangingSizeProcess p = $size$process;
        if (p == null) {
            return;
        }
        if (++p.lerpedTicks < p.lerpTime) {
            p.prevTickSize = $size$it;
            setRawSize(p.fromSize + (p.toSize - p.fromSize) / p.lerpTime * p.lerpedTicks);
        } else {
            $size$process = null;
            setRawSize(p.toSize);
        }
    }

    // region collision

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 0)
    float setSizeWidth(float width) {
        $size$originalWidth = width;
        return (float) (width * $size$it);
    }

    @ModifyVariable(method = "setSize", at = @At("HEAD"), ordinal = 1)
    float setSizeHeight(float height) {
        $size$originalHeight = height;
        return (float) (height * $size$it);
    }

    @ModifyConstant(method = "isEntityInsideOpaqueBlock", constant = @Constant(floatValue = 0.1f))
    float isEntityInsideOpaqueBlock(float constant) {
        return (float) (constant * $size$it);
    }

    @ModifyConstant(method = "doBlockCollisions", constant = @Constant(doubleValue = 0.001))
    double doBlockCollisions(double constant) {
        return constant * $size$it;
    }

    @ModifyConstant(method = "isInLava", constant = {
            @Constant(doubleValue = -0.10000000149011612),
            @Constant(doubleValue = -0.4000000059604645),
    })
    double isInLava(double constant) {
        return constant * $size$it;
    }

    @ModifyConstant(method = "isOverWater", constant = {
            @Constant(doubleValue = -20.0),
            @Constant(doubleValue = 0.001),
    })
    double isOverWater(double constant) {
        return constant * $size$it;
    }
    // endregion

    // region movement

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0)
    double moveX(double x, MoverType type) {
        return type == MoverType.SELF ? x * $size$it : x;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 1)
    double moveY(double y, MoverType type) {
        return type == MoverType.SELF ? y * $size$it : y;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 2)
    double moveZ(double z, MoverType type) {
        return type == MoverType.SELF ? z * $size$it : z;
    }

    @ModifyConstant(method = "move", constant = {
            @Constant(doubleValue = 0.5),  //
            @Constant(doubleValue = -0.5), // shifting on edges of aabb's
            @Constant(doubleValue = 0.20000000298023224), // block step collision
            @Constant(doubleValue = 0.001), // flammable collision
    })
    double moveConstantsMul(double constant) {
        return constant * $size$it;
    }

    @ModifyConstant(method = "move", constant = @Constant(doubleValue = 0.6))
    double moveStepSound(double constant) {
        return constant / $size$it;
    }

    @ModifyConstant(method = "move", constant = @Constant(floatValue = 0.35f))
    float moveSwimSound(float constant) {
        return (float) (constant / $size$it);
    }

    @Redirect(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;stepHeight:F"))
    float moveStepHeight(Entity self) {
        return (float) (self.stepHeight * $size$it);
    }

    @ModifyConstant(method = "handleWaterMovement", constant = {
            @Constant(doubleValue = -0.4000000059604645),
            @Constant(doubleValue = 0.001),
    })
    double handleWaterMovement(double constant) {
        return constant * $size$it;
    }

    @ModifyVariable(method = "updateFallState", at = @At("HEAD"), ordinal = 0)
    double updateFallState(double y) {
        return y / $size$it;
    }

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    void applyEntityCollision(Entity self, double x, double y, double z, Entity other) {
        double coeff = ((ISized) other).getSize() / ((ISized) self).getSize();
        self.addVelocity(x * coeff, y * coeff, z * coeff);
    }

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    void applyEntityCollision2(Entity self, double x, double y, double z, Entity other) {
        double coeff = ((ISized) self).getSize() / ((ISized) other).getSize();
        self.addVelocity(x * coeff, y * coeff, z * coeff);
    }

    // endregion

    // region dropping items

    @ModifyVariable(method = "entityDropItem", at = @At("LOAD"))
    float entityDropItem(float offsetY) {
        return (float) (offsetY * $size$it);
    }

    @ModifyVariable(method = "entityDropItem", at = @At("STORE"))
    EntityItem entityDropItem(EntityItem entityItem) {
        ((IRenderSized) entityItem).setSize($size$it);
        return entityItem;
    }

    // endregion

    // region NBT

    @Inject(method = "readFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"))
    void readFromNBT(NBTTagCompound nbt, CallbackInfo ci) {
        double size = nbt.getDouble("size:it");
        if (size != 0.0) {
            setSize(size);
        } else { // compat for old cm saves
            size = nbt.getDouble("chiseled_me:size");
            if (size != 0.0) {
                setSize(size);
            }
        }
    }

    @Inject(method = "writeToNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeEntityToNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"))
    void writeToNBT(NBTTagCompound nbt, CallbackInfoReturnable<NBTTagCompound> cir) {
        nbt.setDouble("size:it", $size$process != null ? $size$process.toSize : $size$it);
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
        modified[args.length] = $size$it;
        return modified;
    }

    // endregion

    // region particles

    @ModifyArg(method = "doWaterSplashEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] doWaterSplashEffect(int[] args) {
        return EntitySizeInteractions.appendSize(args, $size$it);
    }

    @ModifyConstant(method = "createRunningParticles", constant = {
            @Constant(doubleValue = 0.1),
            @Constant(doubleValue = 0.20000000298023224),
    })
    double createRunningParticles(double constant) {
        return constant * $size$it;
    }

    @ModifyArg(method = "createRunningParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] createRunningParticles(int[] args) {
        return EntitySizeInteractions.appendSize(args, $size$it);
    }

    // endregion

    // getPosition fix for commands or something
    @ModifyConstant(method = "getPosition", constant = @Constant(doubleValue = 0.5))
    double getPosition(double constant) {
        return constant * $size$it;
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
}
