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

package dev.necauqua.mods.cm.asm;

import dev.necauqua.mods.cm.asm.dsl.ASM;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Name("Chiseled Me ASM")
@SortingIndex(1001) // above 1000 so notch->srg deobfuscation would happen before us
@TransformerExclusions("dev.necauqua.mods.cm")
public final class ChiseledMePlugin implements IFMLLoadingPlugin, IClassTransformer {

    /**
     * Because Forge is dumb and FMLPluginWrapper class is private
     * even though it gives you a list of those.
     */
    @SuppressWarnings("unchecked")
    private static Set<String> getLoadedCoremods(Map<String, Object> data) {
        try {
            Field field;
            Class<?> cls = Class.forName("net.minecraftforge.fml.relauncher.CoreModManager$FMLPluginWrapper");
            field = cls.getField("name");
            field.setAccessible(true);
            return ((List<Object>) data.get("coremodList")).stream()
                .map(wrapper -> {
                    try {
                        return (String) field.get(wrapper);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(toSet());
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void injectData(Map<String, Object> data) {
        Set<String> loadedCoremods = getLoadedCoremods(data);
        Boolean obfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
        ASM.init(new Transformers(obfuscated, loadedCoremods));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        return ASM.doTransform(transformedName, bytes);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"dev.necauqua.mods.cm.asm.ChiseledMePlugin"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
