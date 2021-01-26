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

package dev.necauqua.size.api;

import net.minecraft.entity.Entity;

/**
 * An extension of {@link ISized} that supports resizing animation,
 * and provides methods for size interpolation.
 * <p>
 * The {@link Entity} class is modified by the coremod
 * to implement this interface.
 * <p>
 * This API allows to arbitrarily get and set entity sizes.
 * <p>
 * You should not shadow the API or the whole implementation in your mod.
 * If you hard-depend on it you can just compile your mod with a compilation
 * dependency.
 * Example use as soft dependency (meaning your mod uses the API if it is there,
 * but can work without it):
 *
 * <pre><code>
 * public static double getSize(Entity entity) {
 *     if (Loader.isModLoaded("size")) {
 *         return getSizeImpl(entity);
 *     }
 *     return 1.0;
 * }
 *
 * {@literal @}Optional.Method(modid = "size")
 * private static double getSizeImpl(Entity entity) {
 *     return ((ISizedEntity) entity).getSize();
 * }
 * </code></pre>
 * <p>
 * Same with other methods defined in this and other interfaces.
 * <p>
 * You need to put this glue code somewhere in a util class
 * (or as a set of extension methods if using Kotlin).
 *
 * @see ISized
 * @author necauqua
 */
public interface IRenderSized extends ISized {

    /**
     * @param partialTick the interpolation factor
     * @return the interpolated ("smooth") multiplier of the size when it is resizing, can be used for rendering
     */
    double getSize(float partialTick);

    /**
     * An implementation of {@link ISized#setSize} that immediately sets
     * the size by calling {@link #setSize(double, int) setSizeCM(size, 0)}.
     */
    default void setSize(double size) {
        setSize(size, 0);
    }

    /**
     * Properly sets the entity size with network sync,
     * should be called only on the server (with the classic <code>!world.isRemote</code> check).
     * <p>
     * <b>NO CHECKS ARE DONE FOR SIZE CORRECTNESS</b>.
     * <p>
     * So if you set your size to an invalid number (zero, negative, NaN, etc.)
     * the result is undefined.
     * <p>
     * However, othter appropriate actions are done, such as dismounting,
     * getting up from the bed, and calling this method for
     * {@link Entity#getParts() entity subparts} recursively.
     * <p>
     * The size is set immediately if the <code>lerpTime</code> is zero,
     * otherwise the process of linear smooth size change is started, lasting for
     * <code>lerpTime</code> ticks.
     *
     * @param size     the size to be set
     * @param lerpTime the time required for the size to be changed from current to required
     */
    void setSize(double size, int lerpTime);

    /**
     * <p>Directly sets the size for this entity instance.
     * <p>No network sync, no lerping and no extra resize actions (such as dismounting).</p>
     * <p>Sets the field (to be returned by {@link #getSize()}) and updates the hitbox, thats it.</p>
     * <p>Used by other entity size setters, exposed in public API to allow precise size control
     * with no overhead from the default method.</p>
     *
     * @param size the size to be set immediately
     */
    void setRawSize(double size);

    /**
     * Returns <code>true</code> for the duration of the resizing animation
     * caused by {@link #setSize(double, int)}.
     *
     * @return <code>true</code> if the entity is currently being resized
     */
    int getResizingTicks();
}
