/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.api;

/**
 * An interface describing something <i>sized</i>, meaning it has a size attribute of type double.
 * This is usually implemented on vanilla types (Entities, Particles etc.) by a mixin.
 * <p>
 * See {@link IRenderSized} for more description and for example usage as a soft dependency.
 * @see IRenderSized
 * @author necauqua
 */
public interface ISized {

    /**
     * @return the size multiplier of this thing
     */
    double getSizeCM();

    /**
     * Sets the size of the this thing.
     * <p>
     * <b>NO CHECKS ARE DONE FOR SIZE CORRECTNESS</b>
     * (e.g. you can set it to NaN or negative and get a bunch of unexpected behavior).
     *
     * @param size the size to be set
     */
    void setSizeCM(double size);
}
