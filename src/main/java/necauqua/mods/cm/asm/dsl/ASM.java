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

import necauqua.mods.cm.ChiseledMe;
import necauqua.mods.cm.Log;
import necauqua.mods.cm.asm.dsl.anchors.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class ASM {

    private static boolean loadedAtAll = false;

    private static final Map<String, ClassPatcher> patchers = new HashMap<>();

    static String currentTransformer = null;

    private ASM() {}

    public static void check() {
        if (!loadedAtAll) {
            Log.error(
                "\n  ****************************************************************************************************\n" +
                    "  * For some reason coremod part of the mod was not even loaded at all!\n" +
                    "  * Something is completely wrong - corrupt jar-file, manifest etc.\n" +
                    "  * Redownload the mod, ensuring that Minecraft and Forge versions are the ones required and so on.\n" +
                    "  ****************************************************************************************************");
            throw new IllegalStateException("Coremod failed!");
        }
    }

    public static void init(Object holder) {
        loadedAtAll = true;
        for (Method m : holder.getClass().getMethods()) {
            if (m.isAnnotationPresent(Transformer.class)) {
                try {
                    currentTransformer = m.getName();
                    m.invoke(holder);
                } catch (Exception e) {
                    throw new IllegalStateException("Can't load transformer '" + m.getName() + "'!", e); // this should not happen
                }
            }
        }
        currentTransformer = null;
    }

    public static byte[] doTransform(String className, byte[] original) {
        ClassPatcher patcher = patchers.get(className);
        if (patcher == null) {
            return original;
        }
        Log.debug("Patching class: " + className);
        ClassReader reader = new ClassReader(original);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES) {

            @Override
            protected String getCommonSuperClass(String type1, String type2) { // stupid hack
                Class<?> c, d;
                ClassLoader classLoader = ChiseledMe.class.getClassLoader(); // because this one line was breaking stuff :/ fixed
                try {
                    c = Class.forName(type1.replace('/', '.'), false, classLoader);
                    d = Class.forName(type2.replace('/', '.'), false, classLoader);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.toString());
                }
                if (c.isAssignableFrom(d)) {
                    return type1;
                }
                if (d.isAssignableFrom(c)) {
                    return type2;
                }
                if (c.isInterface() || d.isInterface()) {
                    return "java/lang/Object";
                } else {
                    do {
                        c = c.getSuperclass();
                    } while (!c.isAssignableFrom(d));
                    return c.getName().replace('.', '/');
                }
            }
        };
        try {
            ClassPatchVisitor visitor = new ClassPatchVisitor(writer, patcher);
            reader.accept(visitor, ClassReader.SKIP_FRAMES);
            return writer.toByteArray();
        } catch (Exception e) {
            Log.error("Failed to patch class " + className, e);
            return original;
        }
    }

    // *** boilerplate for declarativeness *** //

    public static String srg(String mcpName) {
        return mcpName;
    }

    @SuppressWarnings("unused")
    public static String srg(String mcpName, String className) {
        return mcpName;
    }

    @SuppressWarnings("unused")
    public static String srg(String mcpName, String className, String methodDesc) {
        return mcpName;
    }

    public static ClassPatcher inClass(String className) {
        if (currentTransformer == null) {
            throw new IllegalStateException("Can't use 'inClass' outside transformer method!");
        }
        return patchers.computeIfAbsent(className, ClassPatcher::new);
    }

    public static PatchWithLocalsDsl patch() {
        return new Patch();
    }

    public static Anchor methodBegin() {
        return MethodBeginAnchor.INSTANCE;
    }

    public static Anchor insn(int opcode) {
        return new InsnAnchor(opcode);
    }

    public static Anchor ldcInsn(Object cst) {
        return new LdcInsnAnchor(cst);
    }

    public static Anchor varInsn(int opcode, int var) {
        return new VarInsnAnchor(opcode, var);
    }

    public static Anchor jumpInsn(int opcode) {
        return new JumpInsnAnchor(opcode);
    }

    public static Anchor fieldInsn(int opcode, String owner, String name, String desc) {
        return new FieldInsnAnchor(opcode, owner, name, desc);
    }

    public static Anchor methodInsn(int opcode, String owner, String name, String desc) {
        return new MethodInsnAnchor(opcode, owner, name, desc);
    }
}
