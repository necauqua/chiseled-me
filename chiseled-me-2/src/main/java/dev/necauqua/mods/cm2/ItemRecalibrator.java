/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */
package dev.necauqua.mods.cm2;

import dev.necauqua.mods.mira.api.IRenderSized;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.List;

import static dev.necauqua.mods.cm2.ChiseledMe2.MODID;
import static dev.necauqua.mods.cm2.ChiseledMe2.ns;
import static dev.necauqua.mods.cm2.ItemRecalibrator.RecalibrationType.*;
import static dev.necauqua.mods.mira.api.IResizingProgress.log2LerpTime;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public final class ItemRecalibrator extends Item {

    private static final boolean entityItemBBoxOffset = !ModList.get().isLoaded("itemphysic");

    public ItemRecalibrator() {
        super(new Properties().tab(ChiseledMe2.TAB).stacksTo(1));

        ItemModelsProperties.register(this, ns("recalibrator_type"), (stack, world, entity) -> getEffectFromStack(stack).type.getFactor());

        // maybe someone someday will make some cool model/texture based on that
        ItemModelsProperties.register(this, ns("recalibrator_tier"), (stack, world, entity) -> getEffectFromStack(stack).tier);

        DispenserBlock.registerBehavior(this, (source, stack) -> {
            BlockPos at = source.getPos().offset(source.getBlockState().getValue(FACING).getNormal());
            List<Entity> list = source.getLevel().getEntities(null, new AxisAlignedBB(at));
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
        return RecalibrationEffect.fromNBT(stack.getTag());
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isCrouching()) {
            ItemStack used = getEffectFromStack(stack).apply(player, stack.copy());
            return ActionResult.success(player.isCreative() ? stack : used);
        } else if (world.isClientSide()) {
            Minecraft.getInstance().setScreen(new RecalibratorScreen(stack));
            return ActionResult.pass(stack);
        }

        return ActionResult.pass(stack);

//        if (!Config.allowRecalibratingOtherEntities.get()) {
//            ItemStack used = getEffectFromStack(stack).apply(player, stack.copy());
//            return ActionResult.success(player.isCreative() ? stack : used);
//        }

//        double reach = player.getAttribute(REACH_DISTANCE.get()).getValue();
//        Vector3d start = player.getEyePosition(1.0f);
//        Vector3d dir = player.getLookAngle().scale(reach);
//        Vector3d end = start.add(dir);
//
//        Entity match = null;
//        double maxDistSq = reach * reach; // for square comparison
//
//        for (Entity entity : world.getEntities(player, new AxisAlignedBB(start.x, start.y, start.z, end.x, end.y, end.z))) {
//            if (entity.isInvisible()) {
//                continue;
//            }
//            AxisAlignedBB aabb = entity.getBoundingBox();
//            if (entityItemBBoxOffset && entity instanceof ItemEntity) {
//                double h = aabb.maxY - aabb.minY;
//                aabb = new AxisAlignedBB(aabb.minX, aabb.minY + h, aabb.minZ, aabb.maxX, aabb.maxY + h, aabb.maxZ);
//            }
//            Optional<Vector3d> result = aabb.clip(start, end);
//            if (!result.isPresent()) {
//                continue;
//            }
//            double distSq = start.distanceToSqr(result.get());
//            if (distSq < maxDistSq) {
//                maxDistSq = distSq;
//                match = entity;
//            }
//        }
//        if (match != null && (Config.allowRecalibratingOtherPlayers.get() || !(match instanceof PlayerEntity))) {
//            ItemStack used = getEffectFromStack(stack).apply(match, stack.copy());
//            return ActionResult.success(player.isCreative() ? stack : used);
//        }
//        return ActionResult.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        RecalibrationEffect effect = getEffectFromStack(stack);
        tooltip.add(effect.getDisplayString("tooltip"));
        ITextComponent uses = effect.getChargesLeft();
        if (uses != null) {
            tooltip.add(uses);
        }
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return getEffectFromStack(stack).getDisplayString("name");
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        RecalibrationEffect effect = getEffectFromStack(stack);
        return effect.type == RESET ? Rarity.UNCOMMON :
            effect.tier <= (effect.type == REDUCTION ? 8 : 2) ? Rarity.RARE : Rarity.EPIC;
    }

    @Override
    public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> items) {
        super.fillItemCategory(tab, items); // add the default item which is the reset recalibrator
        if (!allowdedIn(tab)) {
            return;
        }
        for (byte i = 1; i <= 12; i++) {
            items.add(create(REDUCTION, i));
        }
        for (byte i = 1; i <= 4; i++) {
            items.add(create(AMPLIFICATION, i));
        }
    }

    public ItemStack create(RecalibrationType type, byte tier) {
        ItemStack stack = new ItemStack(this);
        CompoundNBT nbt = new CompoundNBT();
        nbt.putByte("type", (byte) type.getFactor());
        nbt.putByte("tier", tier);
        nbt.putInt("charges", 0);
        stack.setTag(nbt);
        return stack;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getEffectFromStack(stack).showBar();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return getEffectFromStack(stack).getBar();
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

        public int getFactor() {
            return factor;
        }

        public int getMaxTier() {
            return maxTier;
        }
    }

    public static class RecalibrationEffect {

        private final RecalibrationType type;
        private final int tier;
        private final int charges;
        private final float size;
        private final float maxCharges;

        private RecalibrationEffect(RecalibrationType type, int tier, int charges) {
            this.type = type;
            this.tier = tier;
            this.charges = charges;
            size = (float) Math.pow(2.0, tier * type.getFactor());
            maxCharges = (float) Math.pow(2.0, type.getMaxTier() - tier + 4);
        }

        public static RecalibrationEffect fromNBT(@Nullable CompoundNBT nbt) {
            if (nbt != null) {
                RecalibrationType type = fromFactor(nbt.getByte("type"));
                if (type != RESET) {
                    byte tier = nbt.getByte("tier");
                    if (tier > 0 && tier <= type.getMaxTier()) {
                        int charges = nbt.getInt("charges");
                        if (charges >= 0) {
                            return new RecalibrationEffect(type, tier, charges);
                        }
                    }
                }
            }
            return new RecalibrationEffect(RESET, (byte) 0, 0);
        }

        public boolean showBar() {
            return type != RESET && charges > 0;
        }

        public double getBar() {
            return charges / maxCharges;
        }

        @Nullable
        public ITextComponent getChargesLeft() {
            return type != RESET ?
                new TranslationTextComponent("item." + MODID + ".recalibrator.charges", (int) (maxCharges - charges)) :
                null;
        }

        public ITextComponent getDisplayString(String sub) {
            int s = (int) (type == REDUCTION ? 1.0f / size : size);
            String name = type.name().toLowerCase();
            return new TranslationTextComponent("item." + MODID + ".recalibrator." + name + "." + sub, s);
        }

        public ItemStack apply(Entity entity, ItemStack stack) {
            boolean isPlayer = entity instanceof PlayerEntity;
            int i = isPlayer ? 1 : 2;
            IRenderSized sized = (IRenderSized) entity;
            double currentSize = sized.getSizeCM();
            if (size != currentSize) {
                if (!entity.level.isClientSide()) {
                    sized.setSizeCM(size, log2LerpTime(currentSize, size));
                }
                if (!isPlayer) {
                    i *= 4;
                }
            }
            if (type == RESET) {
                return stack;
            }
            if (charges < maxCharges - i) {
                CompoundNBT nbt = stack.getTag();
                if (nbt == null) {
                    nbt = new CompoundNBT();
                }
                nbt.putInt("charges", charges + i);
                stack.setTag(nbt);
            } else {
                stack.setTag(null); // set recalibrator to reset mode
                if (entity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity player = (ServerPlayerEntity) entity;
                    if (!player.isCreative()) { // because in creative the item won't be replaced
                        StatelessTrigger.RECALIBRATOR_RESET.trigger(player);
                    }
                }
            }
            return stack;
        }
    }
}
