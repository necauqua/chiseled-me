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

package dev.necauqua.mods.cm.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.*;

import static java.util.Collections.emptySet;

public final class StatelessTrigger implements ICriterionTrigger<ICriterionInstance> {

    private final Map<PlayerAdvancements, Set<Listener<ICriterionInstance>>> listeners = new WeakHashMap<>();
    private final ResourceLocation id;

    public StatelessTrigger(ResourceLocation id) {
        this.id = id;
    }

    public void trigger(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        PlayerAdvancements advancements = ((EntityPlayerMP) player).getAdvancements();
        // copy the list because grantCondition might call removeListener and cause CMEs
        for (Listener<ICriterionInstance> listener : new ArrayList<>(listeners.getOrDefault(advancements, emptySet()))) {
            listener.grantCriterion(advancements);
        }
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public ICriterionInstance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return () -> id;
    }

    @Override
    public void addListener(PlayerAdvancements advancements, Listener<ICriterionInstance> listener) {
        listeners.computeIfAbsent(advancements, $ -> new HashSet<>()).add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements advancements, Listener<ICriterionInstance> listener) {
        Set<Listener<ICriterionInstance>> listenerSet = listeners.get(advancements);
        if (listenerSet == null) {
            return;
        }
        listenerSet.remove(listener);
        if (listenerSet.isEmpty()) {
            listeners.remove(advancements);
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements advancements) {
        listeners.remove(advancements);
    }
}
