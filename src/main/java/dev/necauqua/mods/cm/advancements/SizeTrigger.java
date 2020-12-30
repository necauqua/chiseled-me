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
import com.google.gson.JsonSyntaxException;
import dev.necauqua.mods.cm.ChiseledMe.Init;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static dev.necauqua.mods.cm.ChiseledMe.ns;

public final class SizeTrigger extends AdvancementTrigger<SizeTrigger.Instance> {

    private static final ResourceLocation ID = ns("size");
    private static final Map<String, SizeMatcher> MATCHERS = new HashMap<>();

    public static final SizeTrigger INSTANCE = new SizeTrigger();

    static {
        MATCHERS.put("exact", ((fromSize, toSize, condition) -> toSize == condition));
        MATCHERS.put("crossing", ((fromSize, toSize, condition) -> fromSize < toSize ?
                fromSize <= condition && condition <= toSize :
                toSize <= condition && condition <= fromSize));
        MATCHERS.put("lt", ((fromSize, toSize, condition) -> toSize < condition));
        MATCHERS.put("gt", ((fromSize, toSize, condition) -> toSize > condition));
        MATCHERS.put("le", ((fromSize, toSize, condition) -> toSize <= condition));
        MATCHERS.put("ge", ((fromSize, toSize, condition) -> toSize >= condition));
    }

    @Init
    private static void init() {
        CriteriaTriggers.register(INSTANCE);
    }

    private SizeTrigger() {
        super(ID);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        float size = JsonUtils.getFloat(json, "size");
        String matchStr = JsonUtils.getString(json, "match", "exact");
        SizeMatcher matcher = MATCHERS.get(matchStr);
        if (matcher == null) {
            throw new JsonSyntaxException("Expected match to be one of " + String.join(", ", MATCHERS.keySet()) + ", was " + matchStr);
        }
        return new Instance(size, matcher);
    }

    public void trigger(EntityPlayer player, double fromSize, double toSize) {
        trigger(player, instance -> instance.matches(fromSize, toSize));
    }

    @FunctionalInterface
    public interface SizeMatcher {

        boolean match(double fromSize, double toSize, double condition);
    }

    public static final class Instance implements ICriterionInstance {
        private final float size;
        private final SizeMatcher matcher;

        public Instance(float size, SizeMatcher matcher) {
            this.size = size;
            this.matcher = matcher;
        }

        public boolean matches(double fromSize, double toSize) {
            return matcher.match(fromSize, toSize, size);
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
