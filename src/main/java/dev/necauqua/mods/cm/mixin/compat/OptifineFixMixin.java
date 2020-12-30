package dev.necauqua.mods.cm.mixin.compat;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.objectweb.asm.Opcodes.GETSTATIC;

@Pseudo
@Mixin(targets = "net.optifine.DynamicLights", remap = false)
public final class OptifineFixMixin {

    @Dynamic // shut up the plugin
    @Redirect(method = "getItemStack", at = @At(value = "FIELD", opcode = GETSTATIC, target = "Lnet/optifine/DynamicLights;PARAMETER_ITEM_STACK:Lnet/minecraft/network/datasync/DataParameter;"))
    private static DataParameter<ItemStack> getItemStack() {
        return EntityItem.ITEM;
    }
}
