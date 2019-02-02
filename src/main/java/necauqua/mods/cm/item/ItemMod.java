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

import necauqua.mods.cm.ChiseledMe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMod extends Item {

    protected final String name;

    private final ResourceLocation defaultModel;

    public ItemMod(String name) {
        this.name = name;
        setRegistryName("chiseled_me", name);
        setUnlocalizedName("chiseled_me:" + name);
        setCreativeTab(ChiseledMe.TAB);
        defaultModel = new ResourceLocation("chiseled_me", name);
    }

    public void init() {
        GameRegistry.register(this);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            clientRegister();
        }
    }

    @SideOnly(Side.CLIENT)
    protected ResourceLocation getModelResource(ItemStack stack) {
        return defaultModel;
    }

    protected String[] getModelVariants() {
        return new String[0];
    }

    @SideOnly(Side.CLIENT)
    private void clientRegister() {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(this, s -> new ModelResourceLocation(getModelResource(s), "inventory"));
        String[] vs = getModelVariants();
        ResourceLocation[] variants = new ResourceLocation[vs.length + 1];
        variants[0] = defaultModel;
        for (int i = 0; i < vs.length; i++) {
            variants[i + 1] = new ResourceLocation("chiseled_me", vs[i]);
        }
        ModelBakery.registerItemVariants(this, variants);
    }
}
