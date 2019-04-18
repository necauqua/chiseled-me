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

import dev.necauqua.mods.cm.ChiseledMe;
import dev.necauqua.mods.cm.Log;
import dev.necauqua.mods.cm.asm.dsl.anchors.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public final class ASM {

    private static boolean loadedAtAll = false;

    private static final Map<String, ClassPatcher> patchers = new HashMap<>();

    static String currentTransformer = null;

    private ASM() {}

    public static void check() {
        if (!loadedAtAll) {
            Log.error("\n" +
                    "  ****************************************************************************************************\n" +
                    "  * For some reason coremod part of the mod was not even loaded at all!\n" +
                    "  * Something is completely wrong - corrupt jar-file, manifest etc.\n" +
                    "  * Redownload the mod, ensuring that Minecraft and Forge versions are the ones required and so on.\n" +
                    "  ****************************************************************************************************");
            throw new IllegalStateException("Coremod was not loaded!");
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
        ClassPatchVisitor visitor = new ClassPatchVisitor(writer, patcher);
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        byte[] modified = writer.toByteArray();
        // we dont collect all misses and then fail with all of them printed because
        // a class could be loaded at any time, so we can't know when to finally check
        checkMisses(className, visitor);
        return modified;
    }

    private static void checkMisses(String className, ClassPatchVisitor visitor) {
        String shortName = className.substring(className.lastIndexOf('.') + 1);
        StringBuilder message = new StringBuilder();

        // absolutely functional filters btw, logging is a 'light' side effect, hehe
        List<MethodPatcher> missedMethods = visitor.getMissedMethods().stream()
                .filter(mp -> {
                    if (mp.isOptional()) {
                        Log.debug("Missed optional method(s) " + mp.getMethodNames());
                        return false;
                    }
                    return true;
                })
                .collect(toList());

        if (!missedMethods.isEmpty()) {
            message.append("Some methods were not found in class ").append(shortName).append(":\n");
            missedMethods.stream()
                    .collect(groupingBy(MethodPatcher::getTransformerName))
                    .forEach((transformer, mps) -> {
                        message.append("  transformer '").append(transformer).append("':\n");
                        mps.forEach(mp -> message.append("    method(s) ").append(mp.getMethodNames()).append('\n'));
                    });
        }

        List<Modifier> missedModifiers = visitor.getMissedModifiers().stream()
                .filter(m -> {
                    if (m.getParent().isOptional()) {
                        Log.debug("Missed optional modifier " + m);
                        return false;
                    }
                    return true;
                })
                .collect(toList());
        if (!missedModifiers.isEmpty()) {
            message.append("Some patches were not applied to class ").append(shortName).append(":\n");
            missedModifiers.stream()
                    .collect(groupingBy(m -> m.getParent().getTransformerName()))
                    .forEach((transformer, transformerMisses) -> {
                        message.append("  transformer '").append(transformer).append("':\n");
                        transformerMisses.stream()
                                .collect(groupingBy(modifier -> modifier.getParent().getMethodNames()))
                                .forEach((method, methodMisses) -> {
                                    message.append("    method ").append(method).append(":\n");
                                    methodMisses.forEach(m -> message.append("      - ").append(m).append('\n'));
                                });
                    });
        }
        if (message.length() > 0) {
            throw new IllegalStateException("Coremod failed!\n" + message);
        }
    }

    // *** boilerplate for declarativeness *** //

    public static String srg(String mcpName) {
        return mcpName;
    }

    /**
     * Calls to this method are transformed with a smart Gradle task
     */
    @SuppressWarnings("unused")
    public static String srg(String mcpName, String className) {
        return mcpName;
    }

    /**
     * Calls to this method are transformed with a smart Gradle task
     */
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
