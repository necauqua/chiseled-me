/*
 * Copyright (c) 2016-2019 Anton Bulakh <necauqua@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.necauqua.mods.cm.item;

import dev.necauqua.mods.cm.Config;
import dev.necauqua.mods.cm.SidedHandler;
import dev.necauqua.mods.cm.advancements.AdvancementTriggers;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static dev.necauqua.mods.cm.ChiseledMe.RECALIBRATOR;
import static dev.necauqua.mods.cm.ChiseledMe.ns;
import static dev.necauqua.mods.cm.item.ItemRecalibrator.RecalibrationType.*;
import static dev.necauqua.mods.cm.size.EntitySizeManager.getSize;
import static dev.necauqua.mods.cm.size.EntitySizeManager.setSizeAndSync;

public final class ItemRecalibrator extends ItemMod {

    public static final IBehaviorDispenseItem DISPENSER_BEHAVIOR = (source, stack) -> {
        BlockPos at = source.getBlockPos().offset(source.getBlockState().getValue(BlockDispenser.FACING));
        List<Entity> list = source.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(at));
        if (list.isEmpty()) {
            return stack;
        }
        RecalibrationEffect effect = getEffectFromStack(stack);
        ItemStack worked = stack.copy();
        for (Entity entity : list) {
            worked = effect.apply(entity, worked);
        }
        return worked;
    };

    public ItemRecalibrator() {
        super("recalibrator");
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, DISPENSER_BEHAVIOR);
        setMaxStackSize(1);

        addPropertyOverride(ns("recalibrator_type"),
            (stack, worldIn, entityIn) -> {
                int nbtFactor = getEffectFromStack(stack).type.getFactor();
                if (nbtFactor != 0) {
                    return nbtFactor;
                }
                // this is so stupid, need that for advancement icons
                int meta = stack.getMetadata();
                return meta == 0 ? 0 : meta <= 12 ? -1 : 1;
            });

        // maybe someone someday will make some cool model/texture based on that
        addPropertyOverride(ns("recalibrator_tier"),
            (stack, worldIn, entityIn) -> {
                int nbtTier = getEffectFromStack(stack).tier;
                if (nbtTier != 0) {
                    return nbtTier;
                }
                // advancements should do it correctly here too
                int meta = stack.getMetadata();
                return meta == 0 ? 0 : meta <= 12 ? meta : meta - 12;
            });
    }

    public static RecalibrationEffect getEffectFromStack(ItemStack stack) {
        return RecalibrationEffect.fromNBT(stack.getTagCompound());
    }

    public static ItemStack create(RecalibrationType type, byte tier) {
        ItemStack stack = new ItemStack(RECALIBRATOR);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("type", (byte) type.getFactor());
        nbt.setByte("tier", tier);
        nbt.setInteger("charges", 0);
        stack.setTagCompound(nbt);
        return stack;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ItemStack ret = stack;
        double dist = Config.recalibratorEntityReachDist;
        if (dist > 0.0 && player.isSneaking()) {
            Vec3d start = player.getPositionVector().add(0.0, player.getEyeHeight(), 0.0);
            Vec3d end = start.add(player.getLook(1.0f).scale(dist));
            AxisAlignedBB range = new AxisAlignedBB(player.posX - dist, player.posY - dist, player.posZ - dist, player.posX + dist, player.posY + dist, player.posZ + dist);
            Entity target = null;
            for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(player, range)) {
                AxisAlignedBB aabb = entity.getEntityBoundingBox();
                if (Config.recalibratorItemEntityBBoxOffset && entity instanceof EntityItem) {
                    double h = aabb.maxY - aabb.minY;
                    aabb = new AxisAlignedBB(aabb.minX, aabb.minY + h, aabb.minZ, aabb.maxX, aabb.maxY + h, aabb.maxZ);
                }
                RayTraceResult result = aabb.calculateIntercept(start, end);
                if (result != null) {
                    double d = start.distanceTo(entity.getPositionVector());
                    if (d < dist) {
                        dist = d;
                        target = entity;
                    }
                }
            }
            if (target != null && !target.isInvisible() && !(target instanceof EntityPlayer)) {
                ret = getEffectFromStack(stack).apply(target, stack.copy());
            }
        } else {
            ret = getEffectFromStack(stack).apply(player, stack.copy());
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.isCreative() ? stack : ret);
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        return getEffectFromStack(stack).getDisplayString("name");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        RecalibrationEffect effect = getEffectFromStack(stack);
        tooltip.add(effect.getDisplayString("tooltip"));
        String uses = effect.getChargesLeft();
        if (uses != null) {
            tooltip.add(uses);
        }
    }

    @Override
    @Nonnull
    public EnumRarity getRarity(ItemStack stack) {
        RecalibrationEffect effect = getEffectFromStack(stack);
        return effect.type == RESET ?
            EnumRarity.UNCOMMON :
            effect.tier <= (effect.type == REDUCTION ? 8 : 2) ?
                EnumRarity.RARE :
                EnumRarity.EPIC;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getEffectFromStack(stack).showBar();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return getEffectFromStack(stack).getBar();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        super.getSubItems(tab, items); // reset one
        if (isInCreativeTab(tab)) {
            for (byte i = 1; i <= 12; i++) {
                items.add(create(REDUCTION, i));
            }
            for (byte i = 1; i <= 4; i++) {
                items.add(create(AMPLIFICATION, i));
            }
        }
    }

    public enum RecalibrationType {
        REDUCTION(-1, 12),
        RESET(0, 0),
        AMPLIFICATION(1, 4);

        private final int factor;
        private final int maxTier;

        RecalibrationType(int factor, int maxTier) {
            this.factor = factor;
            this.maxTier = maxTier;
        }

        public int getFactor() {
            return factor;
        }

        public int getMaxTier() {
            return maxTier;
        }

        static RecalibrationType fromFactor(int factor) {
            switch (factor) {
                case -1:
                    return REDUCTION;
                case 1:
                    return AMPLIFICATION;
                default:
                    return RESET;
            }
        }
    }

    public static class RecalibrationEffect {

        private final RecalibrationType type;
        private final int tier;
        private final int charges;
        private final float size;
        private final float maxCharges;

        public static RecalibrationEffect fromNBT(@Nullable NBTTagCompound nbt) {
            if (nbt != null) {
                RecalibrationType type = fromFactor(nbt.getByte("type"));
                if (type != RESET) {
                    byte tier = nbt.getByte("tier");
                    if (tier > 0 && tier <= type.getMaxTier()) {
                        int charges = nbt.getInteger("charges");
                        if (charges >= 0) {
                            return new RecalibrationEffect(type, tier, charges);
                        }
                    }
                }
            }
            return new RecalibrationEffect(RESET, (byte) 0, 0);
        }

        private RecalibrationEffect(RecalibrationType type, int tier, int charges) {
            this.type = type;
            this.tier = tier;
            this.charges = charges;
            size = (float) Math.pow(2.0, tier * type.getFactor());
            maxCharges = (float) Math.pow(2.0, type.getMaxTier() - tier + 4);
        }

        public boolean showBar() {
            return type != RESET && charges > 0;
        }

        public double getBar() {
            return charges / maxCharges;
        }

        public String getChargesLeft() {
            if (type == RESET) {
                return null;
            }
            return SidedHandler.instance.getLocalization("item.chiseled_me:recalibrator.charges", (int) (maxCharges - charges));
        }

        public String getDisplayString(String sub) {
            int s = (int) (type == REDUCTION ? 1.0f / size : size);
            String name = type.name().toLowerCase();
            return SidedHandler.instance.getLocalization("item.chiseled_me:recalibrator." + name + "." + sub, s);
        }

        public ItemStack apply(Entity entity, ItemStack stack) {
            boolean isPlayer = entity instanceof EntityPlayer;
            int i = isPlayer ? 1 : 2;
            double currentSize = getSize(entity);
            if (size != currentSize) {
                if (!entity.world.isRemote) {
                    setSizeAndSync(entity, size, true);
                }
                if (isPlayer) {
                    AdvancementTriggers.SIZE.trigger((EntityPlayer) entity, currentSize, size);
                } else {
                    i *= 4;
                }
            }
            if (type == RESET) {
                return stack;
            }
            if (charges < maxCharges - i) {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                }
                nbt.setInteger("charges", charges + i);
                stack.setTagCompound(nbt);
            } else {
                stack.setTagCompound(null); // set recalibrator to reset mode
                if (entity instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) entity;
                    if (!player.isCreative()) { // because in creative the item wont be replaced
                        AdvancementTriggers.RECALIBRATOR_RESET.trigger(player);
                    }
                }
            }
            return stack;
        }
    }
}
