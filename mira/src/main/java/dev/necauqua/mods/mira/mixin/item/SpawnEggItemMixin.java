/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.item;

import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.extras.IPreciseEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {

    @Nullable
    private Entity $cm$entity = null;
    @Nullable
    private BlockRayTraceResult $cm$target = null;

    @ModifyVariable(method = "use", at = @At("STORE"))
    BlockRayTraceResult use(BlockRayTraceResult blockRayTraceResult) {
        $cm$target = blockRayTraceResult;
        return blockRayTraceResult;
    }

    @Redirect(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;spawn(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;"))
    Entity useOn(EntityType<?> self, ServerWorld level, @Nullable ItemStack stack, @Nullable PlayerEntity player,
                 BlockPos blockPos, SpawnReason reason, boolean applyOffsets, boolean verticalCollisionCheck,
                 ItemUseContext context) {
        if (player == null) {
            return self.spawn(level, stack, null, blockPos, reason, applyOffsets, verticalCollisionCheck);
        }
        return ((IPreciseEntityType) self).spawnSized(level, stack, player, blockPos, reason,
            ((ISized) player).getSizeCM(), context.getHitResult(), false);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;spawn(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;"))
    Entity use(EntityType<?> self, ServerWorld level, @Nullable ItemStack stack, @Nullable PlayerEntity player,
               BlockPos blockPos, SpawnReason reason, boolean applyOffsets, boolean verticalCollisionCheck) {
        if (player == null || $cm$target == null) {
            return self.spawn(level, stack, null, blockPos, reason, applyOffsets, verticalCollisionCheck);
        }
        return ((IPreciseEntityType) self).spawnSized(level, stack, player, blockPos, reason,
            ((ISized) player).getSizeCM(), $cm$target, true);
    }

    @Inject(method = "spawnOffspringFromSpawnEgg", at = @At("HEAD"))
    void spawnOffspringFromSpawnEgg(PlayerEntity player, MobEntity entity, EntityType<?> entityType, ServerWorld world,
                                    Vector3d pos, ItemStack stack, CallbackInfoReturnable<Optional<MobEntity>> cir) {
        $cm$entity = entity;
    }

    @ModifyArg(method = "spawnOffspringFromSpawnEgg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;addFreshEntityWithPassengers(Lnet/minecraft/entity/Entity;)V"))
    Entity spawnOffspringFromSpawnEgg(Entity entity) {
        if ($cm$entity != null) {
            ((ISized) entity).setSizeCM(((ISized) $cm$entity).getSizeCM());
        }
        return entity;
    }
}
