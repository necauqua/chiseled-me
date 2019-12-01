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
package dev.necauqua.mods.cm.api;

import net.minecraft.entity.Entity;

/**
 * Example use as soft dependency (if you bundle this API with your mod then
 * you're doing it WRONG):
 *
 * <pre><code>
 * public float getSize(Entity entity) {
 *     if (Loader.isModLoaded("chiseled_me")) {
 *         return getSizeImpl(entity);
 *     }
 *     return 1.0f;
 * }
 *
 * {@literal @}Optional.Method(modid = "chiseled_me")
 * private float getSizeImpl(Entity entity) {
 *     return ChiseledMeAPI.interaction.getSizeOf(entity);
 * }
 * </code></pre>
 * <p>
 * Put this code somewhere where you use it.
 **/

public final class ChiseledMeAPI {

    /**
     * If the mod is loaded, after pre-initialization stage this will be
     * populated (with reflection hacks) with proper implementation to use.
     * <p>
     * Also it is a mod instance.
     **/
    public static final ChiseledMeInterface interaction = new ChiseledMeInterface() { // this is a stub (for those doing it the wrong way)

        @Override
        public float getSizeOf(Entity entity) {
            return 1.0f;
        }

        @Override
        public float getRenderSizeOf(Entity entity, float partialTick) {
            return 1.0f;
        }

        @Override
        public void setSizeOf(Entity entity, float size, boolean interp) {
            // NOOP
        }
    };
}
