/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.IWorldPreciseEvents;
import dev.necauqua.mods.cm.api.IWorldPreciseSounds;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

// just copying the WorldMixin interface implementation here
// because apparently it fails on some servers for absolutely unknown reasons
@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends WorldMixin implements IWorldPreciseEvents, IWorldPreciseSounds {

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
}
