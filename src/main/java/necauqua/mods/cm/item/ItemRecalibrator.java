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

package necauqua.mods.cm.item;

import necauqua.mods.cm.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static necauqua.mods.cm.ChiseledMe.MODID;
import static necauqua.mods.cm.item.ItemRecalibrator.RecalibrationEffect.*;

public class ItemRecalibrator extends ItemMod {

    public ItemRecalibrator() {
        super("recalibrator");
        setMaxStackSize(1);
    }

    public static RecalibrationEffect getEffectFromStack(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null && nbt.hasKey("type", 1) && nbt.hasKey("tier", 1) && nbt.hasKey("charges", 3)) {
            return new RecalibrationEffect(nbt.getByte("type"), nbt.getByte("tier"), nbt.getInteger("charges"));
        }
        return new RecalibrationEffect(RESET, (byte) 0, 0);
    }

    public static ItemStack create(byte type, byte tier) {
        ItemStack stack = new ItemStack(ChiseledMe.RECALIBRATOR);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("type", type);
        nbt.setByte("tier", tier);
        nbt.setInteger("charges", 0);
        stack.setTagCompound(nbt);
        return stack;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        ItemStack ret = stack;
        double dist = Config.recalibratorEntityReachDist;
        if (dist > 0.0 && player.isSneaking()) {
            Vec3d start = player.getPositionVector().addVector(0.0, player.getEyeHeight(), 0.0);
            Vec3d end = start.add(player.getLook(1.0F).scale(dist));
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
            if (target != null && !target.isInvisibleToPlayer(player) && !(target instanceof EntityPlayer)) {
                ret = getEffectFromStack(stack).apply(target, stack.copy());
            }
        } else {
            ret = getEffectFromStack(stack).apply(player, stack.copy());
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.isCreative() ?
            stack :
            ret);
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT) // why this is not client-only by default?
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        return getEffectFromStack(stack).getDisplayString("name");
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
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
            effect.tier <= (effect.type == REDUCTION ?
                8 :
                2) ?
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
    public void getSubItems(@Nonnull Item item, @Nullable CreativeTabs tab, List<ItemStack> subItems) {
        super.getSubItems(item, tab, subItems); // reset one
        for (byte i = 1; i <= 12; i++) {
            subItems.add(create(REDUCTION, i));
        }
        for (byte i = 1; i <= 4; i++) {
            subItems.add(create(AMPLIFICATION, i));
        }
    }

    @Override
    protected ResourceLocation getModelResource(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        byte type = nbt != null ? nbt.getByte("type") : 0;
        return new ResourceLocation(MODID, "recalibrator" +
            (type == -1 ? "_reduction" : type == 1 ? "_amplification" : ""));
    }

    @Override
    protected String[] getModelVariants() {
        return new String[]{"recalibrator_reduction", "recalibrator_amplification"};
    }

    public static class RecalibrationEffect {

        public static final byte REDUCTION = -1;
        public static final byte RESET = 0;
        public static final byte AMPLIFICATION = 1;

        private final byte type;
        private final byte tier;
        private final int charges;

        private final float size;
        private final float maxCharges;

        public RecalibrationEffect(byte type, byte tier, int charges) {
            this.type = type;
            this.tier = tier;
            this.charges = charges;
            int maxTier = (byte) (type == REDUCTION ?
                12 :
                type == AMPLIFICATION ?
                    4 :
                    0);
            size = tier <= maxTier ?
                (float) Math.pow(2.0, tier * type) :
                1.0F;
            maxCharges = (float) Math.pow(2.0, maxTier - tier);
        }

        public boolean showBar() {
            return type != RESET && charges > 0;
        }

        public double getBar() {
            return charges / maxCharges;
        }

        @SideOnly(Side.CLIENT)
        public String getChargesLeft() {
            return type == RESET ?
                null :
                I18n.format("item.chiseled_me:recalibrator.charges", (int) (maxCharges - charges));
        }

        @SideOnly(Side.CLIENT)
        public String getDisplayString(String sub) {
            int s = (int) (type == REDUCTION ?
                1.0F / size :
                size);
            String name = type == REDUCTION ?
                "reduction" :
                type == AMPLIFICATION ?
                    "amplification" :
                    "reset";
            return I18n.format("item.chiseled_me:recalibrator." + name + "." + sub, s);
        }

        public ItemStack apply(Entity entity, ItemStack stack) {
            boolean isPlayer = entity instanceof EntityPlayer;
            int i = isPlayer ?
                1 :
                2;
            if (size != EntitySizeManager.getSize(entity)) {
                if (!entity.world.isRemote) {
                    EntitySizeManager.setSize(entity, size, true);
                    Network.sendSetSizeToClients(entity, size, true); // notify all clients 'bout this
                }
                if (isPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    if (type == REDUCTION) {
                        switch (tier) {
                            case 1:
                                player.addStat(Achievements.CABLEWORK);
                                break;
                            case 2:
                                player.addStat(Achievements.BIG_STAIRS);
                                break;
                            case 3:
                                player.addStat(Achievements.MOUSE_HOLES);
                                break;
                            case 4:
                                player.addStat(Achievements.C_AND_B_GALORE);
                                break;
                            case 8:
                                player.addStat(Achievements.C_AND_B_SQUARED);
                                break;
                            case 9:
                                player.addStat(Achievements.SUPERSMALLS);
                                break;
                            case 12:
                                player.addStat(Achievements.THE_LIMIT);
                                break;
                        }
                    } else if (type == AMPLIFICATION) {
                        switch (tier) {
                            case 1:
                                player.addStat(Achievements.DOUBLE);
                                break;
                            case 2:
                                player.addStat(Achievements.QUADRUPLE);
                                break;
                            case 3:
                                player.addStat(Achievements.OCTUPLE);
                                break;
                            case 4:
                                player.addStat(Achievements.SEXDECUPLE);
                                break;
                        }
                    }
                }
            } else {
                i *= 4;
            }
            if (type != RESET) {
                if (charges < maxCharges - i) {
                    NBTTagCompound nbt = stack.getTagCompound();
                    if (nbt == null) {
                        nbt = new NBTTagCompound();
                    }
                    nbt.setInteger("charges", charges + i);
                    stack.setTagCompound(nbt);
                } else {
                    stack.setTagCompound(null); // set recalibrator to reset mode
                    if (isPlayer) {
                        ((EntityPlayer) entity).addStat(Achievements.RESET);
                    }
                }
            }
            return stack;
        }
    }
}
