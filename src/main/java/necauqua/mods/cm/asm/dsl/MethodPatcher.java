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

package necauqua.mods.cm.asm.dsl;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MethodPatcher implements MethodPatcherDsl {

    private final ClassPatcher parent;
    private final String transformerName;
    private final boolean optional;

    private final List<Pair<String, String>> methodsToPatch = new ArrayList<>();
    private Patch patch = p -> {}; // empty stub

    public MethodPatcher(ClassPatcher parent, String transformerName, String name, String desc, boolean optional) {
        this.parent = parent;
        this.transformerName = transformerName;
        this.optional = optional;
        methodsToPatch.add(Pair.of(name, desc));
    }

    public String getTransformerName() {
        return transformerName;
    }

    public List<Pair<String, String>> getMethodsToPatch() {
        return methodsToPatch;
    }

    public Patch getPatch() {
        return patch;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public MethodPatcherDsl and(String name, String desc) {
        methodsToPatch.add(Pair.of(name, desc));
        return this;
    }

    @Override
    public ClassPatcherDsl with(Patch patch) {
        this.patch = patch;
        return parent;
    }

    public String getMethodNames() {
        if (methodsToPatch.size() == 1) {
            Pair<String, String> method = methodsToPatch.get(0);
            return method.getLeft() + method.getRight();
        }
        return methodsToPatch.stream()
            .map(method -> method.getLeft() + method.getRight())
            .collect(Collectors.joining(", ", "[ ", " ]"));
    }
}
