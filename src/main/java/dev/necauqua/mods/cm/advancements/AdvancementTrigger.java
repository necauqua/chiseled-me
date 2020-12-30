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

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Predicate;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

public abstract class AdvancementTrigger<T extends ICriterionInstance> implements ICriterionTrigger<T> {
    protected final ResourceLocation id;
    private final Map<PlayerAdvancements, Set<Listener<T>>> listeners = new WeakHashMap<>();

    public AdvancementTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addListener(PlayerAdvancements advancements, Listener<T> listener) {
        listeners.computeIfAbsent(advancements, $ -> new HashSet<>()).add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements advancements, Listener<T> listener) {
        Set<Listener<T>> listenerSet = listeners.get(advancements);
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

    protected void trigger(EntityPlayer player, Predicate<T> filter) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        PlayerAdvancements advancements = ((EntityPlayerMP) player).getAdvancements();
        listeners.getOrDefault(advancements, emptySet()).stream()
            .filter(listener -> filter.test(listener.getCriterionInstance()))
            .collect(toList()) // intermediate collection because grantCondition might call removeListener and cause CMEs
            .forEach(listener -> listener.grantCriterion(advancements));
    }
}
