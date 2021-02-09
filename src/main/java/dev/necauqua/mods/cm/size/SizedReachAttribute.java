/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.size;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

public final class SizedReachAttribute implements IAttributeInstance, ISized {

    private final IAttributeInstance peer;
    private final ISized sized;

    public SizedReachAttribute(IAttributeInstance peer, ISized sized) {
        this.peer = peer;
        this.sized = sized;
    }

    @Override
    public double getAttributeValue() { // the main deal
        return peer.getAttributeValue() * sized.getSizeCM();
    }

    @Override
    public double getSizeCM() {
        return sized.getSizeCM();
    }

    @Override
    public void setSizeCM(double size) {
        sized.setSizeCM(size);
    }

    @Override
    public IAttribute getAttribute() {
        return peer.getAttribute();
    }

    @Override
    public double getBaseValue() {
        return peer.getBaseValue();
    }

    @Override
    public void setBaseValue(double baseValue) {
        peer.setBaseValue(baseValue);
    }

    @Override
    public Collection<AttributeModifier> getModifiersByOperation(int operation) {
        return peer.getModifiersByOperation(operation);
    }

    @Override
    public Collection<AttributeModifier> getModifiers() {
        return peer.getModifiers();
    }

    @Override
    public boolean hasModifier(AttributeModifier modifier) {
        return peer.hasModifier(modifier);
    }

    @Nullable
    @Override
    public AttributeModifier getModifier(UUID uuid) {
        return peer.getModifier(uuid);
    }

    @Override
    public void applyModifier(AttributeModifier modifier) {
        peer.applyModifier(modifier);
    }

    @Override
    public void removeModifier(AttributeModifier modifier) {
        peer.removeModifier(modifier);
    }

    @Override
    public void removeModifier(UUID uuid) {
        peer.removeModifier(uuid);
    }

    @Override
    public void removeAllModifiers() {
        peer.removeAllModifiers();
    }
}
