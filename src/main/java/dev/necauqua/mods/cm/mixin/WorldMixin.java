/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.IWorldPreciseEvents;
import dev.necauqua.mods.cm.api.IWorldPreciseSounds;
import dev.necauqua.mods.cm.size.IEntityExtras;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(World.class)
public abstract class WorldMixin implements IWorldPreciseEvents, IWorldPreciseSounds {

    // replace the initial value of `raytraceresult2` so it is not null
    // so the game would stop spamming `Null returned as 'hitResult', this shouldn't happen!` errors
    @ModifyVariable(
            method = "rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;",
            at = @At(value = "STORE", ordinal = 1))
    RayTraceResult rayTraceBlocks(RayTraceResult result, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        int x = MathHelper.floor(vec31.x);
        int y = MathHelper.floor(vec31.y);
        int z = MathHelper.floor(vec31.z);
        return new RayTraceResult(RayTraceResult.Type.MISS, vec32, EnumFacing.DOWN, new BlockPos(x, y, z));
    }

    @SuppressWarnings("ConstantConditions") // that null player lol
    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPos, int data, double size, Vec3d pos) {
        // same as playEvent(null, type, pos, data), just rerouting to the method with extra args
        try {
            for (IWorldEventListener listener : eventListeners) {
                if (listener instanceof IWorldPreciseEvents) {
                    ((IWorldPreciseEvents) listener).playEvent(null, type, blockPos, data, size, pos);
                }
            }
        } catch (Throwable e) {
            CrashReport report = CrashReport.makeCrashReport(e, "Playing level event");
            CrashReportCategory crashreportcategory3 = report.makeCategory("Level event being played");
            crashreportcategory3.addCrashSection("Block coordinates", CrashReportCategory.getCoordinateInfo(blockPos));
            crashreportcategory3.addCrashSection("Event source", null);
            crashreportcategory3.addCrashSection("Event type", type);
            crashreportcategory3.addCrashSection("Event data", data);
            throw new ReportedException(report);
        }
    }

    @Override
    public void playSound(Vec3d pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay, double size) {}

    @Override
    public void playSound(@Nullable EntityPlayer player, Vec3d pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, double size) {
        PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(player, soundIn, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) {
            return;
        }
        soundIn = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        pitch = event.getPitch();

        for (IWorldEventListener listener : eventListeners) {
            if (listener instanceof IWorldPreciseEvents) {
                ((IWorldPreciseSounds) listener).playSound(player, pos, soundIn, category, volume, pitch, size);
            }
        }
    }

    // to not depend of whether entity calls super in it's onUpdate
    @Inject(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    void updateEntityWithOptionalForce(Entity entity, boolean forceUpdate, CallbackInfo ci) {
        ((IEntityExtras) entity).onUpdateCM();
    }

    @Shadow
    protected List<IWorldEventListener> eventListeners;
}
