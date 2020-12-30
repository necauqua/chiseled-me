package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemMonsterPlacer.class)
public final class ItemMonsterPlacerMixin {

    private static final String SPIGOT_SPAWN_CREATURE = "spawnCreature(Lnet/minecraft/world/World;Lnet/minecraft/util/ResourceLocation;DDDLorg/bukkit/event/entity/CreatureSpawnEvent$SpawnReason;)Lnet/minecraft/entity/Entity;";

    private static double $cm$hack = 1.0f;
    private static EnumFacing $cm$facingHack;

    @ModifyVariable(method = "onItemUse", ordinal = 1, at = @At("STORE"))
    BlockPos onItemUseNoBlockPosOffset(BlockPos blockPos, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        $cm$hack = ((IRenderSized) player).getSizeCM();
        $cm$facingHack = facing;
        return pos;
    }

    @ModifyConstant(method = "onItemUse", constant = @Constant(doubleValue = 0.5, ordinal = 0))
    double onItemUseX(double constant, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return hitX;
    }

    @ModifyVariable(method = "onItemUse", ordinal = 0, at = @At("STORE"))
    double onItemUseY(double d0, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return hitY;
    }

    @ModifyConstant(method = "onItemUse", constant = @Constant(doubleValue = 0.5, ordinal = 1))
    double onItemUseZ(double constant, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return hitZ;
    }

    @Inject(method = "onItemRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemMonsterPlacer;spawnCreature(Lnet/minecraft/world/World;Lnet/minecraft/util/ResourceLocation;DDD)Lnet/minecraft/entity/Entity;"))
    void onItemRightClick(World world, EntityPlayer player, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        // idk it seems like onItemRightClick is never called
        $cm$hack = ((IRenderSized) player).getSizeCM();
    }

    @Group(name = "spawnCreatureLocation", min = 1, max = 1)
    @Redirect(method = "spawnCreature", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setLocationAndAngles(DDDFF)V"), expect = 0, require = 0)
    private static void spawnCreatureLocation(Entity self, double x, double y, double z, float yaw, float pitch) {
        ((IRenderSized) self).setSizeCM($cm$hack);
        $cm$hack = 1.0f;
        if ($cm$facingHack == null) {
            self.setLocationAndAngles(x, y, z, yaw, pitch);
            return;
        }
        self.setLocationAndAngles(
                x + $cm$facingHack.getFrontOffsetX() * self.width / 2.0,
                $cm$facingHack == EnumFacing.DOWN ? y - self.height : y,
                z + $cm$facingHack.getFrontOffsetZ() * self.width / 2.0,
                yaw, pitch);
        $cm$facingHack = null;
    }

    @SuppressWarnings("target")
    @Dynamic("Spigot redirect")
    @Group(name = "spawnCreatureLocation", min = 1, max = 1)
    @Redirect(method = SPIGOT_SPAWN_CREATURE, remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setLocationAndAngles(DDDFF)V"), expect = 0, require = 0)
    private static void spawnCreatureLocationSpigot(Entity self, double x, double y, double z, float yaw, float pitch) {
        ((IRenderSized) self).setSizeCM($cm$hack);
        $cm$hack = 1.0f;
        if ($cm$facingHack == null) {
            self.setLocationAndAngles(x, y, z, yaw, pitch);
            return;
        }
        self.setLocationAndAngles(
                x + $cm$facingHack.getFrontOffsetX() * self.width / 2.0,
                $cm$facingHack == EnumFacing.DOWN ? y - self.height : y,
                z + $cm$facingHack.getFrontOffsetZ() * self.width / 2.0,
                yaw, pitch);
        $cm$facingHack = null;
    }

    @Group(name = "spawnCreatureForge", min = 1, max = 1)
    @Redirect(method = "spawnCreature", at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraftforge/event/ForgeEventFactory;doSpecialSpawn(Lnet/minecraft/entity/EntityLiving;Lnet/minecraft/world/World;FFFLnet/minecraft/tileentity/MobSpawnerBaseLogic;)Z"), expect = 0, require = 0)
    private static boolean spawnCreatureForgeLocation(EntityLiving entity, World world, float x, float y, float z, MobSpawnerBaseLogic spawner) {
        if ($cm$facingHack == null) {
            return ForgeEventFactory.doSpecialSpawn(entity, world, x, y, z, spawner);
        }
        boolean res = ForgeEventFactory.doSpecialSpawn(entity, world,
                x + $cm$facingHack.getFrontOffsetX() * entity.width / 2.0f,
                $cm$facingHack == EnumFacing.DOWN ? y - entity.height : y,
                z + $cm$facingHack.getFrontOffsetZ() * entity.width / 2.0f,
                spawner);
        $cm$facingHack = null;
        return res;
    }

    @SuppressWarnings("target")
    @Dynamic("Spigot redirect")
    @Group(name = "spawnCreatureForge", min = 1, max = 1)
    @Redirect(method = SPIGOT_SPAWN_CREATURE, remap = false, at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraftforge/event/ForgeEventFactory;doSpecialSpawn(Lnet/minecraft/entity/EntityLiving;Lnet/minecraft/world/World;FFFLnet/minecraft/tileentity/MobSpawnerBaseLogic;)Z"), expect = 0, require = 0)
    private static boolean spawnCreatureForgeLocationSpigot(EntityLiving entity, World world, float x, float y, float z, MobSpawnerBaseLogic spawner) {
        if ($cm$facingHack == null) {
            return ForgeEventFactory.doSpecialSpawn(entity, world, x, y, z, spawner);
        }
        boolean res = ForgeEventFactory.doSpecialSpawn(entity, world,
                x + $cm$facingHack.getFrontOffsetX() * entity.width / 2.0f,
                $cm$facingHack == EnumFacing.DOWN ? y - entity.height : y,
                z + $cm$facingHack.getFrontOffsetZ() * entity.width / 2.0f,
                spawner);
        $cm$facingHack = null;
        return res;
    }
}
