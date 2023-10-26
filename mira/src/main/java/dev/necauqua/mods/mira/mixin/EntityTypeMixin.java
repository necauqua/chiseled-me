package dev.necauqua.mods.mira.mixin;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.extras.IPreciseEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements IPreciseEntityType {

    @Nullable
    private BlockRayTraceResult $cm$target = null;
    private double $cm$size = 1.0;
    private boolean $cm$inside = false;

    // all of that is for spawn eggs, see SpawnEggItemMixin

    @Override
    public Entity spawnSized(ServerWorld level, @Nullable ItemStack stack, @Nullable PlayerEntity player, BlockPos blockPos, SpawnReason reason, double size, BlockRayTraceResult target, boolean inside) {
        $cm$size = size;
        $cm$inside = inside;
        $cm$target = target;
        Entity entity = spawn(level, stack, player, blockPos, reason, false, false);
        $cm$target = null;
        $cm$inside = false;
        $cm$size = 1.0;
        return entity;
    }

    @Shadow
    @Nullable
    public abstract Entity spawn(ServerWorld level, @Nullable ItemStack stack, @Nullable PlayerEntity player, BlockPos blockPos, SpawnReason reason, boolean offset, boolean verticalCheck);

    @Redirect(method = "create(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/nbt/CompoundNBT;Lnet/minecraft/util/text/ITextComponent;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveTo(DDDFF)V"))
    void moveTo(Entity self, double x, double y, double z, float yRot, float xRot) {
        ((ISized) self).setSizeCM($cm$size);
        if ($cm$target == null) {
            self.moveTo(x, y, z, yRot, xRot);
            return;
        }
        Vector3d pos = $cm$target.getLocation();
        Direction dir = $cm$target.getDirection();
        double d = $cm$inside ? -2.0 : 2.0;
        self.moveTo(
            (pos.x() + dir.getStepX() * self.getBbWidth() / d),
            (dir == Direction.DOWN && !$cm$inside ? pos.y() - self.getBbHeight() : pos.y()),
            (pos.z() + dir.getStepZ() * self.getBbWidth() / d),
            yRot, xRot
        );
        $cm$target = null;
    }
}
