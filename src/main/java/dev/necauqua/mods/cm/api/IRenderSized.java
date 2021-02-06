/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.api;

import net.minecraft.entity.Entity;

/**
 * An extension of {@link ISized} that supports resizing animation,
 * and provides methods for size interpolation.
 * <p>
 * The {@link Entity} class is modified by the coremod
 * to implement this interface if it was loaded successfully.
 * <p>
 * This API allows to arbitrarily get and set entity sizes.
 * <p>
 * Example use as soft dependency
 * (if you bundle this API with your mod then you're doing it WRONG):
 *
 * <pre><code>
 * public static double getSize(Entity entity) {
 *     if (Loader.isModLoaded("chiseled_me")) {
 *         return getSizeImpl(entity);
 *     }
 *     return 1.0;
 * }
 *
 * {@literal @}Optional.Method(modid = "chiseled_me")
 * private static double getSizeImpl(Entity entity) {
 *     return ((ISizedEntity) entity).getSizeCM();
 * }
 * </code></pre>
 * <p>
 * Same with other methods defined in this interface.
 * <p>
 * Put this code somewhere where you use it.
 * @see ISized
 * @author necauqua
 */
public interface IRenderSized extends ISized {

    /**
     * @param partialTick the interpolation factor
     * @return the interpolated ("smooth") multiplier of the size when it is resizing, can be used for rendering
     */
    double getSizeCM(float partialTick);

    /**
     * A shortcut for {@link #setSizeCM(double, int) setEntitySize(size, 0)}.
     */
    default void setSizeCM(double size) {
        setSizeCM(size, 0);
    }

    /**
     * Returns <code>true</code> for the duration of the resizing animation
     * caused by {@link #setSizeCM(double, int)}.
     *
     * @return <code>true</code> if the entity is currently being resized
     */
    boolean isResizingCM();

    /**
     * Progresses the resizing animation by one ticks if it is in the process.
     * <p>
     * It is called from the modified minecraft code on each entity update, you generally should not call this yourself.
     * <p>
     * This is done to not be dependent on whether the entity subclass calls super onUpdate or not,
     * even in vanilla minecarts don't do that.
     */
    void updateCM();

    /**
     * Properly sets the entity size with network sync,
     * and thus should be called only on the server.
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
     * otherwise the process of smooth size change is started, lasting for
     * <code>lerpTime</code> ticks.
     * <p>
     * The formula for default recalibrator lerp time is:
     * <p>
     * <code>int lerpTime = (int) (abs(log(fromSize) - log(toSize)) * TWO_OVER_LOG_TWO)</code>
     *
     * @param size     the size to be set
     * @param lerpTime the time required for the size to be changed from current to required
     */
    void setSizeCM(double size, int lerpTime);

    /**
     * <p>Directly sets the size for this entity instance, similar to the original {@link ISized#setSizeCM} contract.</p>
     * <p>No network sync, no lerping and no extra resize checks/actions (such as dismounting).</p>
     * <p>Sets the field (to be returned by {@link #getSizeCM()}) and updates the hitbox, thats it.</p>
     * <p>Used by other entity size setters, exposed in public API to allow precise size control
     * with no overhead from the default method.</p>
     *
     * @param size the size to be set immediately.
     */
    void setRawSizeCM(double size);
}
