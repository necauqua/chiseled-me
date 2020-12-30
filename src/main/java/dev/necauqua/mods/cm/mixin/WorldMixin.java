package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.IWorldPlayPreciseEvent;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(World.class)
public abstract class WorldMixin implements IWorldPlayPreciseEvent {

    // replace the initial value of `raytraceresult2` so it is not null
    // so the game would stop spamming `Null returned as 'hitResult', this shouldn't happen!` errors
    @ModifyVariable(
            method = "rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;",
            at = @At(value = "STORE", ordinal = 1))
    RayTraceResult rayTraceBlocks(RayTraceResult result, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        int x = MathHelper.floor(vec31.x);
        int y = MathHelper.floor(vec31.y);
        int z = MathHelper.floor(vec31.z);
        return new RayTraceResult(RayTraceResult.Type.MISS, vec31, EnumFacing.DOWN, new BlockPos(x, y, z));
    }

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

    @Shadow
    protected List<IWorldEventListener> eventListeners;
}
