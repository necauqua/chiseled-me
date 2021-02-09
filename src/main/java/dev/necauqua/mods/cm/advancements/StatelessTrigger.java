/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public final class StatelessTrigger extends AdvancementTrigger<StatelessTrigger.Instance> {
    public StatelessTrigger(ResourceLocation id) {
        super(id);
    }

    @Override
    public StatelessTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(id);
    }

    public void trigger(EntityPlayer player) {
        trigger(player, $ -> true);
    }

    public static final class Instance implements ICriterionInstance {
        private final ResourceLocation id;

        public Instance(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }
    }
}
