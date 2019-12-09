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
import dev.necauqua.mods.cm.advancements.StatelessTrigger.StatelessInstance;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public final class StatelessTrigger extends AdvancementTrigger<StatelessInstance> {
    public StatelessTrigger(ResourceLocation id) {
        super(id);
    }

    @Override
    public StatelessInstance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new StatelessInstance(id);
    }

    public void trigger(EntityPlayer player) {
        trigger(player, $ -> true);
    }

    public static final class StatelessInstance implements ICriterionInstance {
        private final ResourceLocation id;

        public StatelessInstance(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }
    }
}
