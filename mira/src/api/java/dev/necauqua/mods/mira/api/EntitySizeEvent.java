package dev.necauqua.mods.mira.api;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public final class EntitySizeEvent extends EntityEvent {

    private final double prevSize;
    private double size;
    private int animationTicks;

    public EntitySizeEvent(Entity entity, double prevSize, double size, int animationTicks) {
        super(entity);
        this.prevSize = prevSize;
        this.size = size;
        this.animationTicks = animationTicks;
    }

    public double getPrevSize() {
        return prevSize;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public int getAnimationTicks() {
        return animationTicks;
    }

    public void setAnimationTicks(int animationTicks) {
        this.animationTicks = animationTicks;
    }
}
