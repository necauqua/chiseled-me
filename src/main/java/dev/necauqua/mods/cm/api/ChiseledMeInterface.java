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

public interface ChiseledMeInterface {

    /**
     * Returns an entity size that was set before with {@linkplain #setSizeOf} or 1.<br>
     * Returned float is relative to original size of the entity (meaning that default size is 1 and eg 1/16th is
     * 0.0625).<br>
     * <p>
     * Note that this also returns intermediate tick sizes if entity's size is changing right now
     * (interp param in {@linkplain #setSizeOf}).
     *
     * @param entity any minecraft entity which size might have been changed before
     * @return relative size of given entity
     **/
    float getSizeOf(Entity entity);

    /**
     * Same as {@link #getSizeOf} except that it also interpolates between
     * previous tick size and current tick size when size changes.
     * <p>
     * <code>getRenderSizeOf(entity, 1.0F)</code> is equivalent to <code>getSizeOf(entity)</code>
     *
     * @param entity      any minecraft entity which size might have been changed before
     * @param partialTick interpolation parameter in range between 0 and 1
     * @return interpolated relative size of given entity
     */
    float getRenderSizeOf(Entity entity, float partialTick);

    /**
     * Sets the size of an entity to a given float.<br>
     * While its completely ok to set size at any number in given boundaries
     * it is recommended to stick with negative and positive powers of two
     * (not nessesary, but it determines smooth change speed using log2).<br>
     * <p>
     * Lower limit exists because of float presicion, because if you go smaller minecraft
     * logic starts to freak out much more then warned in congigs.
     *
     * @param entity modified minecraft entity
     * @param size   size to be set in between 0.000244140625 (1/4092 or 1/16/16/16) and 16
     * @param interp will size change be smooth or not
     **/
    void setSizeOf(Entity entity, float size, boolean interp);
}
