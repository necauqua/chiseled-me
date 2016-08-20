/*
 * Copyright (c) 2016 Anton Bulakh
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

package necauqua.mods.cm.asm;

import com.google.common.base.Joiner;
import necauqua.mods.cm.ChiseledMe;
import necauqua.mods.cm.Log;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.util.Printer;
import scala.Tuple4;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/*
  If you strip some shit related to mc/forge weird obfuscation
  and network sidednes this class turns out to be pretty cool after all
  Even not applied patches and stuff pretty well crash the game and tell wth has happened
*/
public final class ASM {

    public static final Element methodBegin = new MethodBeginElement();

    public static boolean loaded = false;
    private static boolean obfuscated = true;

    private static final Map<String, ClassPatcher> patchers = new HashMap<>();
    private static String currentTransformer = null;

    private ASM() {}

    public static String resolve(String mcpName, String srgName) {
        return obfuscated ? srgName : mcpName;
    }

    public static void check() {
        if(!loaded) {
            throw new IllegalStateException(
                "For some reason coremod part of the mod was not even loaded!" +
                " Something is completely wrong - corrupt jar-file, manifest etc." +
                " Re-Download the mod, ensuring that Minecraft and Forge versions are the ones required and so on.");
        }
    }

    public static void init(Object holder, boolean obf) {
        obfuscated = obf;
        loaded = true;
        Log.debug("Obfuscated: " + obf + ". Names will be resolved to " + (obf ? "srg" : "mcp") + " names");
        for(Method m : holder.getClass().getMethods()) {
            if(m.isAnnotationPresent(Transformer.class)) {
                try {
                    currentTransformer = m.getName();
                    m.invoke(holder);
                }catch(Exception e) {
                    throw new IllegalStateException("Can't load transformer '" + m.getName() + "'!", e); // this should not happen
                }
            }
        }
        currentTransformer = null;
    }

    public static byte[] doTransform(String className, byte[] original) {
        ClassPatcher patcher = patchers.get(className);
        if(patcher != null) {
            Log.debug("Patching class: " + className);
            ClassReader reader = new ClassReader(original);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES) {

                @Override
                protected String getCommonSuperClass(String type1, String type2) { // HAX defined
                    Class<?> c, d;
                    ClassLoader classLoader = ChiseledMe.class.getClassLoader(); // this one line was breaking stuff :/ fixed
                    try {
                        c = Class.forName(type1.replace('/', '.'), false, classLoader);
                        d = Class.forName(type2.replace('/', '.'), false, classLoader);
                    }catch(Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e.toString());
                    }
                    if(c.isAssignableFrom(d)) {
                        return type1;
                    }
                    if(d.isAssignableFrom(c)) {
                        return type2;
                    }
                    if(c.isInterface() || d.isInterface()) {
                        return "java/lang/Object";
                    }else {
                        do {
                            c = c.getSuperclass();
                        }while (!c.isAssignableFrom(d));
                        return c.getName().replace('.', '/');
                    }
                }
            };
            ClassPatchVisitor visitor = new ClassPatchVisitor(writer, patcher);
            try {
                reader.accept(visitor, ClassReader.SKIP_FRAMES);
            }catch(Exception e) {
                Log.error("Couldn't accept patch visitor!", e);
            }

            Set<String> ts = new HashSet<>();
            visitor.unusedPatches.forEach(patch -> {
                if(patch.optional) {
                    Log.debug("One of patches from " + className + " wasn't applied but ignoring because it's marked as optional (eg. @SideOnly)");
                }else {
                    ts.add(patch.transformer);
                }
            });
            if(!ts.isEmpty()) {
                throw new IllegalStateException("Transformers {" + Joiner.on(", ").join(ts) + "} were not applied!");
            }

            visitor.allModifiers.forEach((method, mods) ->
                mods.forEach(mod -> {
                    if(mod.code == null || !mod.code.succededOnce()) {
                        throw new IllegalStateException("Modifier " + mod + " from " + method + " was not applied even once!");
                    }
                })
            );
            try {
                return writer.toByteArray();
            }catch(Exception e) {
                Log.error("Couldn't write patched class!", e);
                return original;
            }
        }
        return original;
    }

    private static class ClassPatchVisitor extends ClassVisitor {

        public final Map<String, List<Modifier>> allModifiers = new HashMap<>();
        public final List<MethodPatcher> unusedPatches;
        private final ClassPatcher patcher;

        public ClassPatchVisitor(ClassVisitor parent, ClassPatcher patcher) {
            super(Opcodes.ASM5, parent);
            this.patcher = patcher;
            unusedPatches = new ArrayList<>(patcher.patches);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            patcher.fields.forEach(f -> {
                Log.debug("  - Adding field: " + f._2() + "(" + f._3() + ")");
                cv.visitField(f._1(), f._2(), f._3(), f._4(), null).visitEnd();
            });
        }
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            Set<String> transformers = new HashSet<>(); // used for logging
            List<Modifier> modifiers = new ArrayList<>();
            Map<String, Type> locals = new HashMap<>();
     outer: for(MethodPatcher patch : patcher.patches) {
                for(Pair<String, String> md : patch.methods) {
                    if(name.equals(md.getLeft()) && desc.equals(md.getRight())) {
                        transformers.add(patch.transformer);
                        modifiers.addAll(patch.patch.modifiers);
                        locals.putAll(patch.patch.locals);
                        unusedPatches.remove(patch);
                        continue outer;
                    }
                }
            }
            MethodVisitor parent = super.visitMethod(access, name, desc, signature, exceptions);
            if(!modifiers.isEmpty()) {
                Log.debug(" - Patching method with {" + Joiner.on(", ").join(transformers) + "}: " + name + desc);
                allModifiers.put(name + desc, modifiers);
                SpecialMethodVisitor visitor = new SpecialMethodVisitor(locals, access, desc, parent);
                for(Modifier mod : modifiers) {
                    Log.trace("   * Applying " + mod);
                    visitor = mod.apply(visitor);
                }
                return visitor;
            }
            return parent;
        }
    }

    // *** boilerplate for declarativeness in 3.. 2.. 1 *** //

    public static ClassPatcher inClass(String cls) {
        if(currentTransformer == null) {
            throw new IllegalStateException("Can't use 'inClass' outside transformer method!");
        }
        ClassPatcher patcher = patchers.get(cls);
        if(patcher == null) {
            patcher = new ClassPatcher();
            patchers.put(cls, patcher);
        }
        return patcher;
    }

    public static Element insn(int opcode) {
        return new InsnElement(opcode);
    }

    public static Element ldcInsn(Object cst) {
        return new LdcInsnElement(cst);
    }

    public static Element varInsn(int opcode, int var) {
        return new VarInsnElement(opcode, var);
    }

    public static Element jumpInsn(int opcode) {
        return new JumpInsnElement(opcode);
    }

    public static Element fieldInsn(int opcode, String owner, String mcpName, String srgName, String desc) {
        return new FieldInsnElement(opcode, owner, resolve(mcpName, srgName), desc);
    }

    public static Element methodInsn(int opcode, String owner, String mcpName, String srgName, String desc) {
        return new MethodInsnElement(opcode, owner, resolve(mcpName, srgName), desc);
    }

    public static PatchWithLocals patch() {
        return new PatchWithLocals();
    }

    public static class ClassPatcher {

        private final List<MethodPatcher> patches = new ArrayList<>();
        private final List<Tuple4<Integer, String, String, String>> fields = new ArrayList<>(); // don't want POJO's, just f it, scala tuples ftw

        private ClassPatcher() {}

        public ClassPatcher addField(int acc, String name, String desc, String sign) {
            fields.add(new Tuple4<>(acc, name, desc, sign));
            return this;
        }

        public ClassPatcher addField(int acc, String name, String desc) {
            return addField(acc, name, desc, null);
        }

        public MethodPatcher patchConstructor(String desc) { // jff lol
            return patchMethod("<init>", "<init>", desc);
        }

        public MethodPatcher patchMethod(String mcpName, String srgName, String desc) {
            MethodPatcher patch = new MethodPatcher(this, currentTransformer, mcpName, srgName, desc, false);
            patches.add(patch);
            return patch;
        }

        public MethodPatcher patchMethodOptionally(String mcpName, String srgName, String desc) {
            MethodPatcher patch = new MethodPatcher(this, currentTransformer, mcpName, srgName, desc, true);
            patches.add(patch);
            return patch;
        }
    }

    public static class MethodPatcher {

        public final String transformer; // used only for logging

        private final List<Pair<String, String>> methods = new ArrayList<>();
        private final ClassPatcher parent;
        private final boolean optional;

        private Patch patch = new PatchWithLocals(); // empty stub

        private MethodPatcher(ClassPatcher parent, String transformer, String mcpName, String srgName, String desc, boolean optional) {
            this.parent = parent;
            this.transformer = transformer;
            this.optional = optional;
            methods.add(Pair.of(resolve(mcpName, srgName), desc));
        }

        public MethodPatcher and(String mcpName, String srgName, String desc) {
            methods.add(Pair.of(resolve(mcpName, srgName), desc));
            return this;
        }

        public ClassPatcher with(Patch patch) {
            this.patch = patch;
            return parent;
        }
    }

    public static abstract class Patch {

        protected final List<Modifier> modifiers = new ArrayList<>();
        protected final Map<String, Type> locals = new HashMap<>();

        private Patch() {}

        public Modifier insertBefore(Element element) {
            Modifier mod = new Modifier(Modifier.INSERT_BEFORE, this, element);
            modifiers.add(mod);
            return mod;
        }

        public Modifier insertAfter(Element element) {
            Modifier mod = new Modifier(Modifier.INSERT_AFTER, this, element);
            modifiers.add(mod);
            return mod;
        }

        public Modifier replace(Element element) {
            Modifier mod = new Modifier(Modifier.REPLACE, this, element);
            modifiers.add(mod);
            return mod;
        }

        @SuppressWarnings("unused") // looks like i never used this, but for sake of cleaner API i leave this here
        public Modifier insertBeforeAll(Element element) {
            return insertBefore(element).nth(-1);
        }

        public Modifier insertAfterAll(Element element) {
            return insertAfter(element).nth(-1);
        }

        public Modifier replaceAll(Element element) {
            return replace(element).nth(-1);
        }
    }

    public static class PatchWithLocals extends Patch { // so addLocal can be called only before any modifiers were added

        public PatchWithLocals addLocal(String assocName, Type type) {
            locals.put(assocName, type);
            return this;
        }
    }

    public static class Modifier {

        public static final int REPLACE = 0;
        public static final int INSERT_BEFORE = 1;
        public static final int INSERT_AFTER = 2;

        private static final String[] TYPE_NAMES = { "replace", "insert before", "insert after" };

        private final int type;
        private final Patch parent;
        private final Element element;

        private CheckedHook code = null;
        private int at = 1;

        private Modifier(int type, Patch parent, Element element) {
            this.type = type;
            this.parent = parent;
            this.element = element;
        }

        public Patch code(AsmMethodHook code) {
            this.code = new CheckedHook(code);
            return parent;
        }

        public Modifier nth(int n) {
            at = n;
            return this;
        }

        private SpecialMethodVisitor apply(SpecialMethodVisitor parent) {
            parent.pass = 1;
            if(code != null) {
                return element.apply(parent, code, type, at);
            }else {
                throw new IllegalStateException("Modifier has no 'code' block!"); // should never happen but whatever
            }
        }

        @Override
        public String toString() {
            return "<" + TYPE_NAMES[type] + (at  == -1 ? " all" : "") + " " + element + (at > 1 ? " at " + at : "") + ">";
        }
    }

    public static class SpecialMethodVisitor extends MethodVisitor { // dont ever ask me how this class works with locals. Such hax

        private final Map<String, Integer> locals;
        private final LocalVariablesSorter lvs;
        private final MethodVisitor rootMV;

        public int pass = 1; // used in `code` lambdas here and there

        public SpecialMethodVisitor(Map<String, Type> locals, int access, String desc, MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
            this.locals = new HashMap<>();
            if(!locals.isEmpty()) {
                lvs = new LocalVariablesSorter(access, desc, mv);
                locals.forEach((k, v) -> this.locals.put(k, lvs.newLocal(v)));
            }else {
                lvs = null;
            }
            rootMV = mv;
        }

        public SpecialMethodVisitor(SpecialMethodVisitor mv) {
            super(Opcodes.ASM5, mv);
            locals = mv.locals;
            rootMV = mv.rootMV;
            lvs = null;
        }

        // Additional methods:

        public void visitHook(AsmMethodHook hook) {
            hook.accept(this);
        }

        public void visitMethodInsn(int opcode, String owner, String mcpName, String srgName, String desc) {
            visitMethodInsn(opcode, owner, mcpName, srgName, desc, false);
        }

        public void visitMethodInsn(int opcode, String owner, String mcpName, String srgName, String desc, boolean isInterface) {
            visitMethodInsn(opcode, owner, resolve(mcpName, srgName), desc, isInterface);
        }

        public void visitFieldInsn(int opcode, String owner, String mcpName, String srgName, String desc) {
            visitFieldInsn(opcode, owner, resolve(mcpName, srgName), desc);
        }

        public void visitVarInsn(int opcode, String assocName) {
            Integer var = locals.get(assocName);
            if(var != null) {
                rootMV.visitVarInsn(opcode, var);
            }else {
                throw new IllegalArgumentException("Local with assoc name '" + assocName + "' was never created!");
            }
        }

        @SuppressWarnings("unused") // i dont even surely know what this is, but as above for cleaner API i leave this here
        public void visitIincInsn(String assocName, int increment) {
            Integer var = locals.get(assocName);
            if(var != null) {
                rootMV.visitIincInsn(var, increment);
            }else {
                throw new IllegalArgumentException("Local with assoc name '" + assocName + "' was never created!");
            }
        }

        // Overrides for root LVS:

        @Override
        public void visitVarInsn(int opcode, int var) {
            if(lvs != null) {
                lvs.visitVarInsn(opcode, var);
            }else {
                super.visitVarInsn(opcode, var);
            }
        }

        @Override
        public void visitIincInsn(int var, int increment) {
            if(lvs != null) {
                lvs.visitIincInsn(var, increment);
            }else {
                super.visitIincInsn(var, increment);
            }
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            if(lvs != null) {
                lvs.visitMaxs(maxStack, maxLocals);
            }else {
                super.visitMaxs(maxStack, maxLocals);
            }
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            if(lvs != null) {
                lvs.visitLocalVariable(name, desc, signature, start, end, index);
            }else {
                super.visitLocalVariable(name, desc, signature, start, end, index);
            }
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
            if(lvs != null) {
                return lvs.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
            }else {
                return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
            }
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            if(lvs != null) {
                lvs.visitFrame(type, nLocal, local, nStack, stack);
            }else {
                super.visitFrame(type, nLocal, local, nStack, stack);
            }
        }
    }

    @FunctionalInterface
    interface AsmMethodHook extends Consumer<SpecialMethodVisitor> {}

    private static class CheckedHook {

        public final AsmMethodHook hook;
        private boolean once = false;

        public CheckedHook(AsmMethodHook hook) {
            this.hook = hook;
        }

        public void accept(SpecialMethodVisitor smv) {
            hook.accept(smv);
            once = true;
        }

        public boolean succededOnce() {
            return once;
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Transformer {}

    private static abstract class Element {

        abstract SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook code, int type, int at);
    }

    public static class MethodBeginElement extends Element {

        private MethodBeginElement() {}

        @Override
        SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook hook, int type, int at) {
            return new SpecialMethodVisitor(parent) {
                @Override
                public void visitCode() {
                    if(type == Modifier.INSERT_AFTER) {
                        mv.visitCode();
                    }
                    hook.accept(parent);
                    if(type == Modifier.INSERT_BEFORE) {
                        mv.visitCode();
                    }
                }
            };
        }

        @Override
        public String toString() {
            return "method begin";
        }
    }

    public static class InsnElement extends Element {

        private final int opcode;

        private InsnElement(int opcode) {
            this.opcode = opcode;
        }

        @Override
        SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook code, int type, int at) {
            return new SpecialMethodVisitor(parent) {
                private int n = 0;
                @Override
                public void visitInsn(int opcode) {
                    if(type == Modifier.INSERT_AFTER) {
                        mv.visitInsn(opcode);
                    }
                    if(opcode == InsnElement.this.opcode && (at == ++n || at == -1)) {
                        parent.pass = n;
                        code.accept(parent);
                    }else if(type == Modifier.REPLACE) {
                        mv.visitInsn(opcode);
                    }
                    if(type == Modifier.INSERT_BEFORE) {
                        mv.visitInsn(opcode);
                    }
                }
            };
        }

        @Override
        public String toString() {
            return Printer.OPCODES[opcode];
        }
    }

    public static class LdcInsnElement extends Element {

        private final Object cst;

        private LdcInsnElement(Object cst) {
            this.cst = cst;
        }

        @Override
        SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook hook, int type, int at) {
            return new SpecialMethodVisitor(parent) {
                private int n = 0;
                @Override
                public void visitLdcInsn(Object cst) {
                    if(type == Modifier.INSERT_AFTER) {
                        mv.visitLdcInsn(cst);
                    }
                    if(cst.equals(LdcInsnElement.this.cst) && (at == ++n || at == -1)) {
                        parent.pass = n;
                        hook.accept(parent);
                    }else if(type == Modifier.REPLACE) {
                        mv.visitLdcInsn(cst);
                    }
                    if(type == Modifier.INSERT_BEFORE) {
                        mv.visitLdcInsn(cst);
                    }
                }
            };
        }

        @Override
        public String toString() {
            return "LDC " + cst;
        }
    }

    public static class VarInsnElement extends Element {

        private final int opcode;
        private final int var;

        private VarInsnElement(int opcode, int var) {
            this.opcode = opcode;
            this.var = var;
        }

        @Override
        SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook hook, int type, int at) {
            return new SpecialMethodVisitor(parent) {
                private int n = 0;
                @Override
                public void visitVarInsn(int opcode, int var) {
                    if(type == Modifier.INSERT_AFTER) {
                        mv.visitVarInsn(opcode, var);
                    }
                    if(opcode == VarInsnElement.this.opcode && var == VarInsnElement.this.var && (at == ++n || at == -1)) {
                        parent.pass = n;
                        hook.accept(parent);
                    }else if(type == Modifier.REPLACE) {
                        mv.visitVarInsn(opcode, var);
                    }
                    if(type == Modifier.INSERT_BEFORE) {
                        mv.visitVarInsn(opcode, var);
                    }
                }
            };
        }

        @Override
        public String toString() {
            return Printer.OPCODES[opcode] + " " + var;
        }
    }

    public static class JumpInsnElement extends Element {

        private final int opcode;

        private JumpInsnElement(int opcode) {
            this.opcode = opcode;
        }

        @Override
        SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook hook, int type, int at) {
            return new SpecialMethodVisitor(parent) {
                private int n = 0;
                @Override
                public void visitJumpInsn(int opcode, Label label) {
                    if(type == Modifier.INSERT_AFTER) {
                        mv.visitJumpInsn(opcode, label);
                    }
                    if(opcode == JumpInsnElement.this.opcode && (at == ++n || at == -1)) {
                        parent.pass = n;
                        hook.accept(parent);
                    }else if(type == Modifier.REPLACE) {
                        mv.visitJumpInsn(opcode, label);
                    }
                    if(type == Modifier.INSERT_BEFORE) {
                        mv.visitJumpInsn(opcode, label);
                    }
                }
            };
        }

        @Override
        public String toString() {
            return Printer.OPCODES[opcode];
        }
    }

    public static class FieldInsnElement extends Element {

        private final int opcode;
        private final String owner;
        private final String name;
        private final String desc;

        private FieldInsnElement(int opcode, String owner, String name, String desc) {
            this.opcode = opcode;
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }

        @Override
        SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook hook, int type, int at) {
            return new SpecialMethodVisitor(parent) {
                private int n = 0;
                @Override
                public void visitFieldInsn(int op, String _owner, String _name, String _desc) {
                    if(type == Modifier.INSERT_AFTER) {
                        mv.visitFieldInsn(op, _owner, _name, _desc);
                    }
                    if(op == opcode && _owner.equals(owner) && _name.equals(name) && _desc.equals(desc) && (at == ++n || at == -1)) {
                        parent.pass = n;
                        hook.accept(parent);
                    }else if(type == Modifier.REPLACE) {
                        mv.visitFieldInsn(op, _owner, _name, _desc);
                    }
                    if(type == Modifier.INSERT_BEFORE) {
                        mv.visitFieldInsn(op, _owner, _name, _desc);
                    }
                }
            };
        }

        @Override
        public String toString() {
            return Printer.OPCODES[opcode] + " " + owner + "." + name + " " + desc;
        }
    }

    public static class MethodInsnElement extends Element {

        private final int opcode;
        private final String owner;
        private final String name;
        private final String desc;

        private MethodInsnElement(int opcode, String owner, String name, String desc) {
            this.opcode = opcode;
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }

        @Override
        SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook hook, int type, int at) {
            return new SpecialMethodVisitor(parent) {
                private int n = 0;
                @Override
                public void visitMethodInsn(int op, String _owner, String _name, String _desc, boolean isInterface) {
                    if(type == Modifier.INSERT_AFTER) {
                        mv.visitMethodInsn(op, _owner, _name, _desc, isInterface);
                    }
                    if(op == opcode && _owner.equals(owner) && _name.equals(name) && _desc.equals(desc) && (at == ++n || at == -1)) {
                        parent.pass = n;
                        hook.accept(parent);
                    }else if(type == Modifier.REPLACE) {
                        mv.visitMethodInsn(op, _owner, _name, _desc, isInterface);
                    }
                    if(type == Modifier.INSERT_BEFORE) {
                        mv.visitMethodInsn(op, _owner, _name, _desc, isInterface);
                    }
                }
            };
        }

        @Override
        public String toString() {
            return Printer.OPCODES[opcode] + " " + owner + "." + name + desc;
        }
    }
}
