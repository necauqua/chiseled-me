/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.IWorldPlayPreciseEvent;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;

// just copying the WorldMixin interface implementation here
// because apparently it fails on some servers for absolutely unknown reasons
@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends WorldMixin implements IWorldPlayPreciseEvent {

    @SuppressWarnings("ConstantConditions") // that null player lol
    @Override
    public void playEvent(int type, BlockPos blockPos, int data, double size, Vec3d pos) {
        // same as playEvent(null, type, pos, data), just rerouting to the method with extra args
        try {
            for (IWorldEventListener listener : eventListeners) {
                if (listener instanceof IWorldPlayPreciseEvent) {
                    ((IWorldPlayPreciseEvent) listener).playEvent(type, blockPos, data, size, pos);
                }
            }
        } catch (Throwable e) {
            CrashReport crashreport3 = CrashReport.makeCrashReport(e, "Playing level event");
            CrashReportCategory crashreportcategory3 = crashreport3.makeCategory("Level event being played");
            crashreportcategory3.addCrashSection("Block coordinates", CrashReportCategory.getCoordinateInfo(blockPos));
            crashreportcategory3.addCrashSection("Event source", null);
            crashreportcategory3.addCrashSection("Event type", type);
            crashreportcategory3.addCrashSection("Event data", data);
            throw new ReportedException(crashreport3);
        }
    }
}
