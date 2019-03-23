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

import necauqua.mods.cm.Log;
import necauqua.mods.cm.asm.dsl.ClassPatcher.FieldDesc;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClassPatchVisitor extends ClassVisitor {

    private final ClassPatcher patcher;

    public ClassPatchVisitor(ClassVisitor parent, ClassPatcher patcher) {
        super(Opcodes.ASM5, parent);
        this.patcher = patcher;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        for (FieldDesc f : patcher.getFields()) {
            Log.debug("  - Adding field: " + f.getName() + "(" + f.getDesc() + ")");
            cv.visitField(f.getAcc(), f.getName(), f.getDesc(), f.getSign(), null)
                .visitEnd();
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        List<Modifier> modifiers = new ArrayList<>();
        Map<String, Type> locals = new HashMap<>();

        for (MethodPatcher methodPatcher : patcher.getMethodPatchers()) {
            for (Pair<String, String> md : methodPatcher.getMethodsToPatch()) {
                if (!md.getLeft().equals(name) || !md.getRight().equals(desc)) {
                    continue;
                }
                Patch patch = (Patch) methodPatcher.getThePatch(); // meh
                modifiers.addAll(patch.getModifiers());
                locals.putAll(patch.getLocals());
            }
        }

        MethodVisitor visititor = super.visitMethod(access, name, desc, signature, exceptions);

        if (!modifiers.isEmpty()) {
            ContextMethodVisitor visitor = new ContextMethodVisitor(patcher.getClassName(), locals, access, desc, visititor);
            for (Modifier mod : modifiers) {
                visitor = mod.apply(visitor);
            }
            return visitor;
        }
        return visititor;
    }
}
