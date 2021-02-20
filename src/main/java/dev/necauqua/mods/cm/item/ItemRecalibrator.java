/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */
package dev.necauqua.mods.cm.item;

import dev.necauqua.mods.cm.Config;
import dev.necauqua.mods.cm.SidedHandler;
import dev.necauqua.mods.cm.advancements.AdvancementTriggers;
import dev.necauqua.mods.cm.advancements.SizeTrigger;
import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static dev.necauqua.mods.cm.ChiseledMe.ns;
import static dev.necauqua.mods.cm.item.ItemRecalibrator.RecalibrationType.*;
import static dev.necauqua.mods.cm.size.ChangingSizeProcess.log2LerpTime;
import static net.minecraft.entity.player.EntityPlayer.REACH_DISTANCE;
import static net.minecraft.util.EnumActionResult.SUCCESS;

public final class ItemRecalibrator extends ItemMod {

    private static boolean entityItemBBoxOffset = true;

    public ItemRecalibrator() {
        super("recalibrator");
        setMaxStackSize(1);

        entityItemBBoxOffset = !Loader.isModLoaded("itemphysic");

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
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, (source, stack) -> {
            BlockPos at = source.getBlockPos().offset(source.getBlockState().getValue(BlockDispenser.FACING));
            List<Entity> list = source.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(at));
            if (list.isEmpty()) {
                return stack;
            }
            RecalibrationEffect effect = ItemRecalibrator.getEffectFromStack(stack);
            ItemStack worked = stack.copy();
            for (Entity entity : list) {
                worked = effect.apply(entity, worked);
            }
            return worked;
        });
    }

    public static RecalibrationEffect getEffectFromStack(ItemStack stack) {
        return RecalibrationEffect.fromNBT(stack.getTagCompound());
    }

    public ItemStack create(RecalibrationType type, byte tier) {
        ItemStack stack = new ItemStack(this);
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

        if (!Config.allowRecalibratingOtherEntities || !player.isSneaking()) {
            ItemStack used = getEffectFromStack(stack).apply(player, stack.copy());
            return new ActionResult<>(SUCCESS, player.isCreative() ? stack : used);
        }

        double reach = player.getEntityAttribute(REACH_DISTANCE).getAttributeValue();
        Vec3d start = player.getPositionEyes(1.0f);
        Vec3d dir = player.getLook(1.0f).scale(reach);
        Vec3d end = start.add(dir);

        Entity match = null;
        double maxDistSq = reach * reach; // for square comparison

        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(start.x, start.y, start.z, end.x, end.y, end.z))) {
            if (entity.isInvisible()) {
                continue;
            }
            AxisAlignedBB aabb = entity.getEntityBoundingBox();
            if (entityItemBBoxOffset && entity instanceof EntityItem) {
                double h = aabb.maxY - aabb.minY;
                aabb = new AxisAlignedBB(aabb.minX, aabb.minY + h, aabb.minZ, aabb.maxX, aabb.maxY + h, aabb.maxZ);
            }
            RayTraceResult result = aabb.calculateIntercept(start, end);
            if (result == null) {
                continue;
            }
            double distSq = start.squareDistanceTo(result.hitVec);
            if (distSq < maxDistSq) {
                maxDistSq = distSq;
                match = entity;
            }
        }
        if (match != null && (Config.allowRecalibratingOtherPlayers || !(match instanceof EntityPlayer))) {
            ItemStack used = getEffectFromStack(stack).apply(match, stack.copy());
            return new ActionResult<>(SUCCESS, player.isCreative() ? stack : used);
        }
        return new ActionResult<>(SUCCESS, stack);
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

    @SuppressWarnings("deprecation") // dont want to hard-depend on new forge (yet)
    @Override
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
            IRenderSized sized = (IRenderSized) entity;
            double currentSize = sized.getSizeCM();
            if (size != currentSize) {
                if (!entity.world.isRemote) {
                    sized.setSizeCM(size, log2LerpTime(currentSize, size));
                }
                if (isPlayer) {
                    SizeTrigger.INSTANCE.trigger((EntityPlayer) entity, currentSize, size);
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
