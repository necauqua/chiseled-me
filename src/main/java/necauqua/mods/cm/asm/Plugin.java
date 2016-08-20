/*
 * Copyright (c) 2016 Anton Bulakh
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

package necauqua.mods.cm.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import java.util.Map;

@Name("Chiseled Me ASM")
@MCVersion("1.10.2")
@SortingIndex(1001) // above 1000 so srg deobfuscation would happen EARLIER >.< (so at least descs are the same)
@TransformerExclusions("necauqua.mods.cm.asm") // ^ i feel that Lex would just annihilate me for that :D
public class Plugin implements IFMLLoadingPlugin, IClassTransformer {

    @Override
    public void injectData(Map<String, Object> data) {
        ASM.init(new Transformers(), (Boolean) data.get("runtimeDeobfuscationEnabled"));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        return ASM.doTransform(transformedName, bytes);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "necauqua.mods.cm.asm.Plugin" };
    }

    @Override
    public String getModContainerClass() { return null; }

    @Override
    public String getSetupClass() { return null; }

    @Override
    public String getAccessTransformerClass() { return null; }
}
