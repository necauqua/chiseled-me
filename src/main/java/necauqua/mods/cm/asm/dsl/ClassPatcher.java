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

import java.util.ArrayList;
import java.util.List;

public final class ClassPatcher implements ClassPatcherDsl {

    private final String className;

    private final List<FieldDesc> fields = new ArrayList<>();
    private final List<MethodPatcher> methodPatchers = new ArrayList<>();

    public ClassPatcher(String className) {
        this.className = className;
    }

    public List<MethodPatcher> getMethodPatchers() {
        return methodPatchers;
    }

    public List<FieldDesc> getFields() {
        return fields;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public ClassPatcherDsl addField(int acc, String name, String desc, String sign) {
        fields.add(new FieldDesc(acc, name, desc, sign));
        return this;
    }

    @Override
    public ClassPatcherDsl addField(int acc, String name, String desc) {
        return addField(acc, name, desc, null);
    }

    @Override
    public MethodPatcherDsl patchConstructor(String desc) {
        return patchMethod("<init>", desc);
    }

    @Override
    public MethodPatcherDsl patchMethod(String name, String desc) {
        MethodPatcher patch = new MethodPatcher(this, ASM.currentTransformer, name, desc, false);
        methodPatchers.add(patch);
        return patch;
    }

    @Override
    public MethodPatcherDsl patchMethodOptionally(String name, String desc) {
        MethodPatcher patch = new MethodPatcher(this, ASM.currentTransformer, name, desc, true);
        methodPatchers.add(patch);
        return patch;
    }

    public static final class FieldDesc {

        private final int acc;
        private final String name;
        private final String desc;
        private final String sign;

        public FieldDesc(int acc, String name, String desc, String sign) {
            this.acc = acc;
            this.name = name;
            this.desc = desc;
            this.sign = sign;
        }

        public int getAcc() {
            return acc;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String getSign() {
            return sign;
        }
    }
}
