/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.size;

import dev.necauqua.mods.mira.Config;
import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public final class SizedReachAttribute extends ModifiableAttributeInstance implements ISized {

    private final ModifiableAttributeInstance peer;
    private final ISized sized;

    public SizedReachAttribute(ModifiableAttributeInstance peer, ISized sized) {
        super(peer.getAttribute(), $ -> peer.setDirty());
        this.peer = peer;
        this.sized = sized;
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
    public Attribute getAttribute() {
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
    public Set<AttributeModifier> getModifiers(Operation operation) {
        return peer.getModifiers(operation);
    }

    @Override
    public Set<AttributeModifier> getModifiers() {
        return peer.getModifiers();
    }

    @Nullable
    @Override
    public AttributeModifier getModifier(UUID uuid) {
        return peer.getModifier(uuid);
    }

    @Override
    public boolean hasModifier(AttributeModifier modifier) {
        return peer.hasModifier(modifier);
    }

    @Override
    public void addTransientModifier(AttributeModifier modifier) {
        peer.addTransientModifier(modifier);
    }

    @Override
    public void addPermanentModifier(AttributeModifier modifier) {
        peer.addPermanentModifier(modifier);
    }

    @Override
    public void setDirty() {
        peer.setDirty();
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
    public boolean removePermanentModifier(UUID uuid) {
        return peer.removePermanentModifier(uuid);
    }

    @Override
    public void removeModifiers() {
        peer.removeModifiers();
    }

    @Override
    public double getValue() { // the main deal
        double real = peer.getValue();
        if (real < 1.0 && Config.scaleReachSmall.get()) {
            return real * sized.getSizeCM();
        }
        if (real > 1.0 && Config.scaleReachBig.get()) {
            return real * sized.getSizeCM();
        }
        return real;
    }

    @Override
    public void replaceFrom(ModifiableAttributeInstance attribute) {
        peer.replaceFrom(attribute);
    }

    @Override
    public CompoundNBT save() {
        return peer.save();
    }

    @Override
    public void load(CompoundNBT nbt) {
        peer.load(nbt);
    }
}
