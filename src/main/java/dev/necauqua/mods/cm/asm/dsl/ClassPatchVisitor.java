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

package dev.necauqua.mods.cm.asm.dsl;

import dev.necauqua.mods.cm.Log;
import dev.necauqua.mods.cm.asm.dsl.ClassPatcher.FieldDesc;
import dev.necauqua.mods.cm.asm.dsl.ClassPatcher.MethodDesc;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.objectweb.asm.Opcodes.ASM5;

public final class ClassPatchVisitor extends ClassVisitor {

    private final ClassPatcher patcher;
    private final List<Modifier> modifiers = new ArrayList<>();

    private final List<MethodPatcher> missedMethods = new ArrayList<>();
    private final List<Modifier> missedModifiers = new ArrayList<>();

    @Nullable
    private String visitingMethod = null;

    public ClassPatchVisitor(ClassVisitor parent, ClassPatcher patcher) {
        super(ASM5, parent);
        this.patcher = patcher;
    }

    public List<MethodPatcher> getMissedMethods() {
        return missedMethods;
    }

    public List<Modifier> getMissedModifiers() {
        return missedModifiers;
    }

    @Nullable
    public String getVisitingMethod() {
        return visitingMethod;
    }

    public void setVisitingMethod(@Nullable String visitingMethod) {
        this.visitingMethod = visitingMethod;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        List<String> ifaces = new ArrayList<>(asList(interfaces));
        ifaces.addAll(patcher.getExtraInterfaces());
        ifaces.removeAll(patcher.getStrippedInterfaces());

        super.visit(version, access, name, signature, superName, ifaces.toArray(new String[0]));

        for (FieldDesc f : patcher.getFields()) {
            Log.debug("  - Adding field: " + f.getName() + "(" + f.getDesc() + ")");
            cv.visitField(f.getAcc(), f.getName(), f.getDesc(), f.getSign(), null)
                .visitEnd();
        }

        for (MethodDesc m : patcher.getMethods()) {
            Log.debug("  - Adding method: " + m.getName() + m.getDesc());
            MethodVisitor mv = cv.visitMethod(m.getAcc(), m.getName(), m.getDesc(), m.getSign(), m.getExceptions());
            mv.visitCode();
            m.getCode().accept(new ContextMethodVisitor(name, emptyMap(), mv, mv));
            mv.visitEnd();
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        patcher.getMethodPatchers().stream()
            .filter(mp -> !mp.didMatch())
            .forEach(missedMethods::add);
        modifiers.stream()
            .filter(m -> !m.didMatch())
            .forEach(missedModifiers::add);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        List<Modifier> modifiers = new ArrayList<>();
        List<Pair<String, Type>> locals = new ArrayList<>();

        boolean isDump = false;

        for (MethodPatcher methodPatcher : patcher.getMethodPatchers()) {
            for (Pair<String, String> md : methodPatcher.getMethodsToPatch()) {
                if (!md.getLeft().equals(name) || !md.getRight().equals(desc)) {
                    continue;
                }
                PatchContext context = new PatchContext(methodPatcher);

                methodPatcher.apply(context);

                isDump |= context.isDump();
                modifiers.addAll(context.getModifiers());
                locals.addAll(context.getLocals());
            }
        }

        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);

        if (modifiers.isEmpty()) {
            return visitor;
        }

        this.modifiers.addAll(modifiers);

        String className = patcher.getClassName();
        if (isDump) {
            visitor = MethodDumper.create(visitor, "patched", className, name + desc);
        }

        LocalVariablesSorter lvs = new LocalVariablesSorter(access, desc, visitor);
        Map<String, Integer> localIndices = locals.stream().collect(toMap(Pair::getLeft, p -> lvs.newLocal(p.getRight())));
        ContextMethodVisitor context = new ContextMethodVisitor(className, localIndices, lvs, visitor);
        ContextMethodVisitor patched = context;

        for (Modifier mod : modifiers) {
            patched = mod.apply(patched);
        }

        MethodVisitor debugInfoReader = new DebugInfoReader(patched, context, this, name + desc);
        return isDump ? MethodDumper.create(debugInfoReader, "original", className, name + desc) : debugInfoReader;
    }
}
