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

package dev.necauqua.mods.cm.asm;

import dev.necauqua.mods.cm.asm.dsl.Hook;
import dev.necauqua.mods.cm.asm.dsl.Patch;
import dev.necauqua.mods.cm.asm.dsl.Transformer;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.util.function.IntPredicate;

import static dev.necauqua.mods.cm.asm.dsl.ASM.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.DOUBLE_TYPE;
import static org.objectweb.asm.Type.FLOAT_TYPE;

// intellij sees to many random duplicates in the hooks
@SuppressWarnings("Duplicates")
public final class Transformers {
    private static final String SIZE_FIELD = "$cm_size";
    private static final String PROCESS_FIELD = "$cm_process";

    private static final String O_WIDTH_FIELD = "$cm_original_width";
    private static final String O_HEIGHT_FIELD = "$cm_original_height";

    private static final Hook thePlayer =
        mv -> mv.visitFieldInsn(GETFIELD,
            "net/minecraft/client/Minecraft",
            srg("player", "Minecraft"),
            "Lnet/minecraft/client/entity/EntityPlayerSP;");

    private static final Hook scale =
        mv -> mv.visitMethodInsn(INVOKESTATIC,
            "net/minecraft/client/renderer/GlStateManager",
            srg("scale", "GlStateManager", "(FFF)V"),
            "(FFF)V",
            false);

    private static final Hook cutBiggerThanOne = cut(FCMPL, IFLE);
    private static final Hook cutSmallerThanOne = cut(FCMPG, IFGE);
    private static final String SIZE_NBT_TAG = "chiseled_me:size";
    private static final String PROCESS_CLASS = "dev/necauqua/mods/cm/size/ChangingSizeProcess";
    private static final String PROCESS_TYPE = "L" + PROCESS_CLASS + ";";

    private static Hook cut(int compareInsn, int jumpInsn) {
        return mv -> {
            mv.visitInsn(DUP);
            mv.visitInsn(FCONST_1);
            mv.visitInsn(compareInsn);
            Label skipOne = new Label();
            mv.visitJumpInsn(jumpInsn, skipOne);
            mv.visitInsn(POP);
            mv.visitInsn(FCONST_1);
            mv.visitLabel(skipOne);
        };
    }

    @Transformer
    public void entityFields() {
        inClass("net/minecraft/entity/Entity")

            .addField(ACC_PUBLIC, SIZE_FIELD, "D")
            .addField(ACC_PUBLIC, PROCESS_FIELD, PROCESS_TYPE)
            .addField(ACC_PUBLIC, O_WIDTH_FIELD, "F")
            .addField(ACC_PUBLIC, O_HEIGHT_FIELD, "F")

            .addInterface("dev/necauqua/mods/cm/api/ISizedEntity")

            .patchConstructor("(Lnet/minecraft/world/World;)V")
            .with(p -> p.insertAfter(methodBegin(), mv -> {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(DUP);
                mv.visitInsn(DCONST_1);
                mv.visitFieldInsn(PUTFIELD, SIZE_FIELD, "D");
            }))

            .patchMethod(srg("onEntityUpdate", "Entity"), "()V")
            .with(p -> {
                p.addLocal("process", Type.getType(PROCESS_TYPE));
                p.insertAfter(methodBegin(), mv -> {

                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, PROCESS_FIELD, PROCESS_TYPE);
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ASTORE, "process");

                    Label skip = new Label();
                    mv.visitJumpInsn(IFNULL, skip);

                    mv.visitVarInsn(ALOAD, "process");
                    mv.visitInsn(DUP);
                    mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "interpTicks", "I");
                    mv.visitInsn(DUP_X1);
                    mv.visitInsn(ICONST_1);
                    mv.visitInsn(IADD);
                    mv.visitFieldInsn(PUTFIELD, PROCESS_CLASS, "interpTicks", "I");
                    mv.visitVarInsn(ALOAD, "process");
                    mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "interpInterval", "I");

                    mv.ifJump(IF_ICMPGE, () -> {
                        mv.visitVarInsn(ALOAD, "process");
                        mv.visitInsn(DUP);
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "interpTicks", "I");
                        mv.visitInsn(ICONST_1);
                        mv.visitInsn(IADD);
                        mv.visitFieldInsn(PUTFIELD, PROCESS_CLASS, "interpTicks", "I");
                        mv.visitVarInsn(ALOAD, "process");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                        mv.visitFieldInsn(PUTFIELD, PROCESS_CLASS, "prevTickSize", "D");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, "process");
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "fromSize", "D");
                        mv.visitVarInsn(ALOAD, "process");
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "toSize", "D");
                        mv.visitVarInsn(ALOAD, "process");
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "fromSize", "D");
                        mv.visitInsn(DSUB);
                        mv.visitVarInsn(ALOAD, "process");
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "interpInterval", "I");
                        mv.visitInsn(I2D);
                        mv.visitInsn(DDIV);
                        mv.visitVarInsn(ALOAD, "process");
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "interpTicks", "I");
                        mv.visitInsn(I2D);
                        mv.visitInsn(DMUL);
                        mv.visitInsn(DADD);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "setEntitySize", "(D)V");
                        mv.visitJumpInsn(GOTO, skip);
                    });
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, "process");
                    mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "toSize", "D");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "setEntitySize", "(D)V");
                    mv.visitInsn(ACONST_NULL);
                    mv.visitFieldInsn(PUTFIELD, PROCESS_FIELD, PROCESS_TYPE);

                    mv.visitLabel(skip);
                });
            })

            .addMethod(ACC_PUBLIC, "getEntitySize", "()D", mv -> {
                Label start = new Label();
                mv.visitLabel(start);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                mv.visitInsn(DRETURN);

                Label end = new Label();
                mv.visitLabel(end);

                mv.visitLocalVariable("this", "Lnet/minecraft/entity/Entity;", null, start, end, 0);

                mv.visitMaxs(2, 1);
            })

            .addMethod(ACC_PUBLIC, "getEntitySize", "(F)D", mv -> {
                Label start = new Label();
                mv.visitLabel(start);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, PROCESS_FIELD, PROCESS_TYPE);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, 2);

                mv.ifJump(IFNONNULL, () -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                }, () -> {
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "prevTickSize", "D");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "prevTickSize", "D");
                    mv.visitInsn(DSUB);
                    mv.visitVarInsn(FLOAD, 1);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                    mv.visitInsn(DADD);
                });
                mv.visitInsn(DRETURN);

                Label end = new Label();
                mv.visitLabel(end);

                mv.visitLocalVariable("this", "Lnet/minecraft/entity/Entity;", null, start, end, 0);
                mv.visitLocalVariable("partialTick", "F", null, start, end, 1);
                mv.visitLocalVariable("process", PROCESS_TYPE, null, start, end, 2);

                mv.visitMaxs(6, 3);
            })

            .addMethod(ACC_PUBLIC, "setEntitySize", "(DZ)V", mv -> {
                Label start = new Label();
                mv.visitLabel(start);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ILOAD, 3);

                mv.ifJump(IFEQ,
                    () -> {
                        mv.visitTypeInsn(NEW, PROCESS_CLASS);
                        mv.visitInsn(DUP);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                        mv.visitVarInsn(DLOAD, 1);
                        mv.visitMethodInsn(INVOKESPECIAL, PROCESS_CLASS, "<init>", "(DD)V", false);
                        mv.visitFieldInsn(PUTFIELD, PROCESS_FIELD, PROCESS_TYPE);
                    }, () -> {
                        mv.visitVarInsn(DLOAD, 1);
                        mv.visitFieldInsn(PUTFIELD, SIZE_FIELD, "D");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(DUP);
                        mv.visitFieldInsn(GETFIELD, O_WIDTH_FIELD, "F");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, O_HEIGHT_FIELD, "F");
                        mv.visitMethodInsn(INVOKEVIRTUAL, srg("setSize", "Entity"), "(FF)V");
                    });

                mv.visitInsn(RETURN);

                Label end = new Label();
                mv.visitLabel(end);

                mv.visitLocalVariable("this", "Ldev/necauqua/mods/cm/Test;", null, start, end, 0);
                mv.visitLocalVariable("size", "D", null, start, end, 1);
                mv.visitLocalVariable("interpolate", "Z", null, start, end, 3);

                mv.visitMaxs(8, 4);
            })

            .patchMethod(srg("readFromNBT", "Entity"), "(Lnet/minecraft/nbt/NBTTagCompound;)V")
            .with(p -> {
                p.addLocal("size", DOUBLE_TYPE);
                p.insertBefore(varInsn(ALOAD, 1), mv -> {
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitLdcInsn(SIZE_NBT_TAG);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", srg("getDouble", "NBTTagCompound"), "(Ljava/lang/String;)D", false);
                    mv.visitInsn(DUP2);
                    mv.visitVarInsn(DSTORE, "size");
                    mv.visitInsn(DCONST_0);
                    mv.visitInsn(DCMPL);
                    mv.ifJump(IFEQ, () -> {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(DLOAD, "size");
                        mv.visitFieldInsn(PUTFIELD, SIZE_FIELD, "D");
                    });
                });
            })

            .patchMethod(srg("writeToNBT", "Entity"), "(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;")
            .with(p -> {
                p.addLocal(PROCESS_FIELD, Type.getType(PROCESS_TYPE));
                p.insertBefore(varInsn(ALOAD, 1), mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, PROCESS_FIELD, PROCESS_TYPE);
                    mv.visitVarInsn(ASTORE, PROCESS_FIELD);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitLdcInsn(SIZE_NBT_TAG);
                    mv.visitVarInsn(ALOAD, PROCESS_FIELD);
                    mv.ifJump(IFNULL,
                        () -> {
                            mv.visitVarInsn(ALOAD, PROCESS_FIELD);
                            mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "toSize", "D");
                        }, () -> {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                        });
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", srg("setDouble", "NBTTagCompound"), "(Ljava/lang/String;D)V", false);
                });
            })

            .patchMethod("toString", "()Ljava/lang/String;")
            .with(p -> {
                p.replace(intInsn(BIPUSH, 7), mv -> mv.visitIntInsn(BIPUSH, 8));
                p.replace(ldcInsn(), mv -> mv.visitLdcInsn("%s[\'%s\'/%d, l=\'%s\', x=%.2f, y=%.2f, z=%.2f, cm:size=%.4f]"));
                p.insertAfter(insn(AASTORE), mv -> {
                    mv.visitInsn(DUP);
                    mv.visitIntInsn(BIPUSH, 7);
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                    mv.visitInsn(AASTORE);
                });
            });
    }

    private static final String BOBBING_FIELD = "$cm_bobbing_dirty_state";

    @Transformer
    public void cameraView() {
        inClass("net/minecraft/client/renderer/EntityRenderer")
            .addField(ACC_PRIVATE, BOBBING_FIELD, "Z")
            .patchMethod(srg("orientCamera"), "(F)V") // camera height & shift
            .with(p -> {
                p.addLocal("size", FLOAT_TYPE);
                p.insertBefore(varInsn(FSTORE, 3), mv -> { // local float f
                    // f = entity.getEyeHeight() / getSize(entity) * getRenderSize(entity)
                    // ^ because getEyeHeight is patched and returns height multiplied
                    // by non-render size

                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitVarInsn(FLOAD, 1); // local float partialTicks
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEntitySize", "(F)D", false);
                    mv.visitInsn(D2F);
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(FSTORE, "size");
                    mv.visitInsn(FMUL);
                });
                p.insertBefore(varInsn(DSTORE, 12), mv -> { // local double d3
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                });
                p.insertAfterAll(ldcInsn(0.1f), mv -> { // f5 fix
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                });
                p.insertAfter(ldcInsn(0.05f), mv -> { // forward cam shift fix
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                });
            })
            .patchMethod(srg("setupCameraTransform"), "(FI)V")
            .with(p -> {
                p.addLocal("size", FLOAT_TYPE);
                p.insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, srg("mc", "EntityRenderer"), "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitVarInsn(FLOAD, 1); // local float partialTick
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/entity/EntityPlayerSP", "getEntitySize", "(F)D", false);
                    mv.visitInsn(D2F);
                    mv.visitVarInsn(FSTORE, "size");
                });
                p.insertAfter(insn(I2F), mv -> { // far plane decrease (closer fog)
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitHook(cutBiggerThanOne);
                    mv.visitInsn(F2D);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                });
                p.insertAfter(ldcInsn(0.05f), mv -> { // another clipping fix
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                });
                p.insertAround(methodInsn(INVOKESPECIAL, "net/minecraft/client/renderer/EntityRenderer", srg("applyBobbing"), "(F)V"),
                    mv -> {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(ICONST_1);
                        mv.visitFieldInsn(PUTFIELD, BOBBING_FIELD, "Z");
                    },
                    mv -> {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(ICONST_0);
                        mv.visitFieldInsn(PUTFIELD, BOBBING_FIELD, "Z");
                    });
            })
            .patchMethod(srg("applyBobbing"), "(F)V") // bobbing fix (-_-)
            .with(p -> {
                p.addLocal("size", FLOAT_TYPE);
                p.insertBefore(varInsn(ASTORE, 2), mv -> { // local EntityPlayer entityplayer
                    mv.visitInsn(DUP);
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/player/EntityPlayer", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitVarInsn(FSTORE, "size");
                });
                p.insertAfterAll(varInsn(FLOAD, 5), mv -> { // local float f2
                    if (mv.getPass() > 2) { // only first two
                        return;
                    }
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, BOBBING_FIELD, "Z");
                    mv.ifJump(IFEQ, () -> {
                        mv.visitVarInsn(FLOAD, "size");
                        mv.visitInsn(FMUL);
                    });
                });
            })
            .patchMethod(srg("renderWorldPass"), "(IFJ)V") // these three are clipping
            .and(srg("renderCloudsCheck"), "(Lnet/minecraft/client/renderer/RenderGlobal;FIDDD)V")
            .and(srg("renderHand", "EntityRenderer", "(FI)V"), "(FI)V")
            .with(p -> {
                p.addLocal("size", FLOAT_TYPE);
                p.insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, srg("mc", "EntityRenderer"), "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/client/entity/EntityPlayerSP", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitHook(cutBiggerThanOne);
                    mv.visitVarInsn(FSTORE, "size");
                });
                p.insertAfterAll(ldcInsn(0.05f), mv -> {
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                });
            });
    }

    @Transformer
    public void entityRender() {
        inClass("net/minecraft/client/renderer/entity/RenderManager")
            .patchMethod(srg("doRenderEntity"), "(Lnet/minecraft/entity/Entity;DDDFFZ)V")
            .with(p -> {
                p.addLocal("size", DOUBLE_TYPE);
                p.insertBefore(varInsn(ALOAD, 11), 2, mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 9); // param float partialTicks
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEntitySize", "(F)D", false);
                    mv.visitInsn(D2F);
                    mv.visitInsn(DUP);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                });
                p.insertAfter(varInsn(DLOAD, 2), mv -> { // param double x
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                });
                p.insertAfter(varInsn(DLOAD, 4), mv -> { // param double y
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                });
                p.insertAfter(varInsn(DLOAD, 6), mv -> { // param double z
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                });
                p.insertBefore(jumpInsn(GOTO), mv -> {
                    mv.visitInsn(FCONST_1);
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                });
            })
            .patchMethod(srg("renderDebugBoundingBox"), "(Lnet/minecraft/entity/Entity;DDDFF)V")
            .with(p -> {
                p.addLocal("size", DOUBLE_TYPE);
                p.insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitVarInsn(DSTORE, "size");
                });
                p.insertAfterAll(ldcInsn(2.0), mv -> { // length of blue 'eye sight' vector
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                });
                p.insertAfterAll(ldcInsn(0.009999999776482582), mv -> { // height of red 'eye heigth' box
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                });
            });
        inClass("net/minecraft/client/renderer/entity/Render")
            .patchMethod(srg("renderShadow", "Render"), "(Lnet/minecraft/entity/Entity;DDDFF)V")
            .with(p -> {
                p.addLocal("size", FLOAT_TYPE);
                p.insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 1); // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 9); // param float partialTicks
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEntitySize", "(F)D", false);
                    mv.visitInsn(D2F);
                    mv.visitVarInsn(FSTORE, "size");
                });
                p.insertBefore(varInsn(FSTORE, 11), mv -> { // local float f
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                });
                p.insertBefore(varInsn(DSTORE, 27), mv -> { // local double d3
                    // code below is this: d3 -= size < 1.0f ? 0.015625f * (1.0f - size) : 0.0f
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FCONST_1);
                    mv.visitInsn(FCMPG);
                    Label skipHook = new Label();
                    mv.visitJumpInsn(IFGE, skipHook);
                    mv.visitLdcInsn(0.015625f);
                    mv.visitInsn(FCONST_1);
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FSUB);
                    mv.visitInsn(FMUL);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DSUB);
                    mv.visitLabel(skipHook);
                });
                p.insertAfter(varInsn(FLOAD, 8), mv -> { // local float shadowAlpha
                    //  replace shadowAlpha in renderShadowSingle call with
                    //  shadowAlpha - (y - (blockpos.getY() + d3)) / 2.0 * (1.0f / size - 1.0f)
                    mv.visitVarInsn(DLOAD, 4);
                    mv.visitVarInsn(ALOAD, 34);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", srg("getY", "Vec3i"), "()I", false);
                    mv.visitInsn(I2D);
                    mv.visitVarInsn(DLOAD, 27);
                    mv.visitInsn(DADD);
                    mv.visitInsn(DSUB);
                    mv.visitLdcInsn(2.0);
                    mv.visitInsn(DDIV);
                    mv.visitInsn(D2F);

                    mv.visitInsn(FCONST_1);
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FDIV);
                    mv.visitInsn(FCONST_1);
                    mv.visitInsn(FSUB);
                    mv.visitInsn(FMUL);

                    mv.visitInsn(FSUB);
                });
            })
            .patchMethod(srg("renderLivingLabel"), "(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V")
            .with(p -> {
                p.addLocal("off", FLOAT_TYPE);
                p.replace(varInsn(FLOAD, 16), mv -> {
                    mv.visitVarInsn(FLOAD, 16); // local float f2

                    mv.visitVarInsn(ALOAD, 1); // param Entity entityIn
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", srg("isSneaking", "Entity"), "()Z", false);

                    mv.ifJump(IFEQ,
                        () -> mv.visitLdcInsn(0.25f),
                        () -> mv.visitLdcInsn(0.5f));
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(FSTORE, "off");

                    mv.visitInsn(FSUB);

                    mv.visitVarInsn(ALOAD, 1); // param Entity entityIn
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                    mv.visitVarInsn(FLOAD, "off");
                    mv.visitInsn(FADD);
                });
            });
        Patch renderDistPatch = p -> p.insertBefore(varInsn(DSTORE, 3), 3, mv -> {
            mv.visitVarInsn(ALOAD, 0); // Entity this
            mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
            mv.visitInsn(D2F);
            mv.visitHook(cutBiggerThanOne);
            mv.visitInsn(F2D);
            mv.visitInsn(DDIV);
        });
        inClass("net/minecraft/entity/Entity")
            .patchMethodOptionally(srg("isInRangeToRenderDist", "EntityOtherPlayerMP"), "(D)Z")
            .with(renderDistPatch);
        inClass("net/minecraft/client/entity/EntityOtherPlayerMP") // because stupid EntityOtherPlayerMP
            .patchMethod(srg("isInRangeToRenderDist", "EntityOtherPlayerMP"), "(D)Z")
            .with(renderDistPatch);
        inClass("net/minecraft/client/gui/inventory/GuiInventory")
            .patchMethod(srg("drawEntityOnScreen"), "(IIIFFLnet/minecraft/entity/EntityLivingBase;)V")
            .with(p -> p
                .insertAfter(varInsn(FSTORE, 10), mv -> { // local float f4
                    mv.visitInsn(FCONST_1);
                    mv.visitVarInsn(ALOAD, 5);  // param EntityLivingBase ent
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/EntityLivingBase", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                }));
    }

    @Transformer
    public void entityMotion() {
        inClass("net/minecraft/entity/Entity")
            .patchMethod(srg("move", "Entity"), "(Lnet/minecraft/entity/MoverType;DDD)V")
            .with(p -> {
                p.addLocal("size", DOUBLE_TYPE);
                p.insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0);  // Entity this
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitVarInsn(DSTORE, "size");

                    mv.visitVarInsn(ALOAD, 1); // MoverType mover type
                    mv.visitFieldInsn(GETSTATIC, "net/minecraft/entity/MoverType", srg("SELF"), "Lnet/minecraft/entity/MoverType;");
                    mv.ifJump(IF_ACMPNE, () -> {
                        mv.visitVarInsn(DLOAD, 2); // param double x
                        mv.visitVarInsn(DLOAD, "size");
                        mv.visitInsn(DMUL);
                        mv.visitVarInsn(DSTORE, 2); // param double x
                        mv.visitVarInsn(DLOAD, 4); // param double y
                        mv.visitVarInsn(DLOAD, "size");
                        mv.visitInsn(DMUL);
                        mv.visitVarInsn(DSTORE, 4); // param double y
                        mv.visitVarInsn(DLOAD, 6); // param double z
                        mv.visitVarInsn(DLOAD, "size");
                        mv.visitInsn(DMUL);
                        mv.visitVarInsn(DSTORE, 6); // param double z
                    });
                });
                Hook mulBySize = mv -> {
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                };
                p.insertAfterAll(ldcInsn(0.05), mulBySize);  //
                p.insertAfterAll(ldcInsn(-0.05), mulBySize); // shifting on edges of aabb's
                p.insertBefore(varInsn(DSTORE, 4), 4, mulBySize); // stepHeight
                p.insertAfterAll(ldcInsn(0.6), mv -> { // div distanceWalked(Modified) for the step sounds
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                });
                p.insertAfterAll(ldcInsn(0.35f), mv -> { // div local f for the swim sounds
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                });
                p.insertAfter(ldcInsn(0.20000000298023224), mulBySize); // block step collision
            })
            .patchMethod(srg("createRunningParticles"), "()V")
            .with(p -> p
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitLdcInsn(0.25);
                    mv.visitInsn(DCMPG);
                    mv.ifJump(IFGT, () -> mv.visitInsn(RETURN));
                }));
        Patch libmSwingAnimation = p ->
            p.insertAfter(ldcInsn(4.0f), mv -> { // fix for limb swing animation
                mv.visitVarInsn(ALOAD, 0);  // EntityLivingBase this
                mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                mv.visitInsn(D2F);
                mv.visitInsn(FDIV);
            });
        inClass("net/minecraft/entity/EntityLivingBase")
            .patchMethod(srg("moveEntityWithHeading", "EntityLivingBase"), "(FF)V")
            .with(libmSwingAnimation);
        inClass("net/minecraft/client/entity/EntityOtherPlayerMP") // because stupid EntityOtherPlayerMP x 2
            .patchMethod(srg("onUpdate", "EntityOtherPlayerMP"), "()V")
            .with(libmSwingAnimation);
        inClass("net/minecraft/client/entity/EntityPlayerSP")
            .patchMethod(srg("updateAutoJump"), "(FF)V")
            .with(p -> {
                p.addLocal("size", FLOAT_TYPE);
                p.insertAfter(ldcInsn(0.001f), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayerSP this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(FSTORE, "size");
                    mv.visitInsn(FMUL);
                });
                Hook mulBySize = mv -> {
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                };
                p.insertAfter(ldcInsn(0.001f), 2, mulBySize);
                p.insertAfter(ldcInsn(-0.15f), mulBySize);
                p.insertAfter(ldcInsn(7.0f), 2, mulBySize);
                p.insertAfter(ldcInsn(0.75f), mulBySize);
                p.insertAfter(ldcInsn(1.2f), mulBySize);
                p.insertAfter(ldcInsn(0.5f), 2, mulBySize);
                p.replace(insn(ICONST_1), 2, mv -> mv.visitInsn(ICONST_0));
                p.insertAfterAll(ldcInsn(0.5099999904632568D), mv -> {
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                });
            });
    }

    @Transformer
    public void serverMotionFixes() {
        inClass("net/minecraft/client/entity/EntityPlayerSP")
            .patchMethod(srg("onUpdateWalkingPlayer"), "()V")
            .with(p -> p
                .insertAfter(ldcInsn(0.0009), mv -> {
                    mv.visitVarInsn(ALOAD, 0);  // EntityPlayerSP this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DMUL);
                    mv.visitInsn(DMUL);
                }));
        inClass("net/minecraft/entity/EntityTrackerEntry")
            .patchMethod(srg("updatePlayerList", "EntityTrackerEntry"), "(Ljava/util/List;)V")
            .with(p -> p
                .replace(ldcInsn(128L), mv -> {
                    mv.visitLdcInsn(128.0f);
                    mv.visitVarInsn(ALOAD, 0); // EntityTrackerEntry this
                    mv.visitFieldInsn(GETFIELD, srg("trackedEntity"), "Lnet/minecraft/entity/Entity;");
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(DUP);  //
                    mv.visitInsn(FMUL); // bytecode squaring <3
                    mv.visitInsn(FMUL);
                    mv.visitInsn(F2L);
                }));
        inClass("net/minecraft/network/NetHandlerPlayServer")
            .patchMethod(srg("processPlayer", "NetHandlerPlayServer"), "(Lnet/minecraft/network/play/client/CPacketPlayer;)V")
            .with(p -> {
                p.addLocal("size", DOUBLE_TYPE);
                p.insertBefore(varInsn(ALOAD, 2), mv -> { // setup
                    mv.visitVarInsn(ALOAD, 0); // NetHandlerPlayServer this
                    mv.visitFieldInsn(GETFIELD, srg("player", "NetHandlerPlayServer"), "Lnet/minecraft/entity/player/EntityPlayerMP;");
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/player/EntityPlayer", SIZE_FIELD, "D");
                    mv.visitVarInsn(DSTORE, "size");
                });
                p.insertAfterAll(ldcInsn(0.0625), mv -> { // fix for small aabbs
                    if (mv.getPass() != 2) { // dont change movement correctness checker (just so you can be relatively fast when small?)
                        mv.visitVarInsn(DLOAD, "size");
                        mv.visitInsn(DMUL);
                    }
                });
                p.insertAfter(ldcInsn(-0.03125D), mv -> { // floating checker
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                });
                p.insertAfter(ldcInsn(-0.55D), mv -> { // some kind of levitation potion effect checker
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                });
            });
        inClass("net/minecraft/entity/player/EntityPlayerMP")
            .patchMethod(srg("handleFalling"), "(DZ)V")
            .with(p ->
                p.insertAfter(ldcInsn(0.20000000298023224D), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayerMP this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(DMUL);
                }));
    }

    @Transformer
    public void entityCollisions() {
        Hook mulBySize = mv -> {
            mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
            mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
            mv.visitInsn(DMUL);
        };

        inClass("net/minecraft/entity/player/EntityPlayer")
            .patchMethod(srg("onLivingUpdate", "EntityPlayer"), "()V") // fixes collideEntityWithPlayer aabb expansion
            .with(p -> {
                p.replaceAll(insn(DCONST_1), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                });
                p.insertAfter(ldcInsn(0.5), mulBySize);
            });

        Patch ignoreFirstCheck = p -> p.replace(insn(ICONST_1), mv -> mv.visitInsn(ICONST_0));

        inClass("net/minecraft/entity/EntityAgeable")
            .patchMethod(srg("setSize", "EntityAgeable"), "(FF)V")
            .with(ignoreFirstCheck);
        inClass("net/minecraft/entity/monster/EntityZombie")
            .patchMethod(srg("setSize", "EntityZombie"), "(FF)V")
            .with(ignoreFirstCheck);

        inClass("net/minecraft/entity/Entity")
            .patchMethod(srg("setSize", "Entity"), "(FF)V")
            .with(p ->
                p.insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(FLOAD, 1);
                    mv.visitFieldInsn(PUTFIELD, O_WIDTH_FIELD, "F");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(FLOAD, 2);
                    mv.visitFieldInsn(PUTFIELD, O_HEIGHT_FIELD, "F");

                    mv.visitVarInsn(FLOAD, 1);
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                    mv.visitVarInsn(FSTORE, 1);

                    mv.visitVarInsn(FLOAD, 2);
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                    mv.visitVarInsn(FSTORE, 2);
                }))

            .patchMethod(srg("isEntityInsideOpaqueBlock", "Entity"), "()Z")
            .with(p -> p.insertAfter(ldcInsn(0.1f), mv -> {
                mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                mv.visitInsn(D2F);
                mv.visitInsn(FMUL);
            }))
            .patchMethod(srg("doBlockCollisions"), "()V")
            .with(p -> {
                Label skipBlockCollision = new Label();
                p.insertBefore(varInsn(ALOAD, 8), mv -> { // could've hooked in BlockPortal#onEntityCollidedWithBlock but this is more flexible
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitVarInsn(ALOAD, 8); // local IBlockState iblockstate
                    mv.visitVarInsn(ALOAD, 4); // local BlockPos blockpos$pooledmutableblockpos2
                    mv.visitMethodInsn(INVOKESTATIC, "dev/necauqua/mods/cm/Hooks", "cancelBlockCollision", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;)Z", false);
                    mv.visitJumpInsn(IFNE, skipBlockCollision);
                });
                p.insertAfter(methodInsn(INVOKEVIRTUAL, "net/minecraft/block/Block", srg("onEntityCollidedWithBlock", "Block"), "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V"),
                    mv -> mv.visitLabel(skipBlockCollision));
                p.insertAfterAll(ldcInsn(0.001), mulBySize);
            })
            .patchMethod(srg("handleWaterMovement", "Entity"), "()Z")
            .with(p -> {
                p.insertAfter(ldcInsn(-0.4000000059604645), mulBySize); // vertical AABB extension
                p.insertAfter(ldcInsn(0.001), mulBySize); // AABB shrink
            });
        inClass("net/minecraft/client/renderer/ItemRenderer")
            .patchMethod(srg("renderOverlays"), "(F)V")
            .with(p -> p.insertAfter(ldcInsn(0.1f), mv -> {
                mv.visitVarInsn(ALOAD, 4); // EntityPlayer entityplayer
                mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                mv.visitInsn(D2F);
                mv.visitInsn(FMUL);
            }));
    }

    @Transformer
    public void reachDistance() {
        inClass("net/minecraft/client/renderer/EntityRenderer")
            .patchMethod(srg("getMouseOver"), "(F)V")
            .with(p -> {
                p.addLocal("size", DOUBLE_TYPE);
                p.insertBefore(varInsn(DSTORE, 3), mv -> {
                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitVarInsn(FLOAD, 1); // local float partialTicks
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEntitySize", "(F)D", false);
                    mv.visitInsn(DUP2);
                    mv.visitVarInsn(DSTORE, "size");
                    mv.visitInsn(DMUL);
                });
                Hook mulBySize = mv -> {
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                };
                p.insertAfter(ldcInsn(6.0), mulBySize);
                p.insertAfterAll(ldcInsn(3.0), mulBySize);
            });

        inClass("net/minecraft/client/multiplayer/PlayerControllerMP") // on client
            .patchMethod(srg("getBlockReachDistance", "PlayerControllerMP"), "()F")
            .with(p -> p
                .insertBefore(insn(FRETURN), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // PlayerControllerMP this
                    mv.visitFieldInsn(GETFIELD, srg("mc", "PlayerControllerMP"), "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitHook(cutSmallerThanOne);
                    mv.visitInsn(FMUL);
                }));
        inClass("net/minecraft/server/management/PlayerInteractionManager") // on server - only increase reach to match client
            .patchMethod("getBlockReachDistance", "()D") // lol
            .with(p ->
                p.insertBefore(insn(DRETURN), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // PlayerInteractionManager this
                    mv.visitFieldInsn(GETFIELD, srg("player", "PlayerInteractionManager"), "Lnet/minecraft/entity/player/EntityPlayerMP;");
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitHook(cutSmallerThanOne);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net/minecraft/network/NetHandlerPlayServer") // entity server reach
            .patchMethod(srg("processUseEntity", "NetHandlerPlayServer"), "(Lnet/minecraft/network/play/client/CPacketUseEntity;)V")
            .with(p ->
                p.insertAfter(varInsn(DLOAD, 5), mv -> { // local double d0
                    mv.visitVarInsn(ALOAD, 3); // local Entity entity
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitHook(cutSmallerThanOne);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DMUL);
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net/minecraft/world/World") // when reach distance <= 1 this one null return screws up raytracing a bit so here's a fix
            .patchMethod(srg("rayTraceBlocks", "World", "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;"),
                "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;")
            .with(p -> p.replace(insn(ACONST_NULL), mv -> {
                mv.visitTypeInsn(NEW, "net/minecraft/util/math/RayTraceResult");
                mv.visitInsn(DUP);
                mv.visitFieldInsn(GETSTATIC, "net/minecraft/util/math/RayTraceResult$Type", srg("MISS"),
                    "Lnet/minecraft/util/math/RayTraceResult$Type;");
                mv.visitVarInsn(ALOAD, 1); // param vec31
                mv.visitInsn(ACONST_NULL);
                mv.visitTypeInsn(CHECKCAST, "net/minecraft/util/EnumFacing");
                mv.visitTypeInsn(NEW, "net/minecraft/util/math/BlockPos");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 1); // param vec31
                mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/util/math/BlockPos", "<init>",
                    "(Lnet/minecraft/util/math/Vec3d;)V", false);
                mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/util/math/RayTraceResult", "<init>",
                    "(Lnet/minecraft/util/math/RayTraceResult$Type;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/BlockPos;)V", false);
            }));
    }

    @Transformer
    public void eyeHeight() {
        Patch mulBySize = p -> p
            .insertBefore(insn(FRETURN), mv -> {
                mv.visitVarInsn(ALOAD, 0); // Entity this
                mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                mv.visitInsn(D2F);
                mv.visitInsn(FMUL);
            });
        inClass("net/minecraft/entity/player/EntityPlayer")
            .patchMethod(srg("getEyeHeight", "EntityPlayer"), "()F")
            .with(mulBySize);

        // those patches are for shooting mobs with hardcoded height, there is no way to make it universal currently
        inClass("net/minecraft/entity/monster/AbstractSkeleton")
            .patchMethod(srg("getEyeHeight", "AbstractSkeleton"), "()F")
            .with(mulBySize);
        inClass("net/minecraft/entity/monster/EntitySnowman")
            .patchMethod(srg("getEyeHeight", "EntitySnowman"), "()F")
            .with(mulBySize);
        inClass("net/minecraft/entity/monster/EntityGhast")
            .patchMethod(srg("getEyeHeight", "EntityGhast"), "()F")
            .with(mulBySize);
        inClass("net/minecraft/entity/monster/EntityWitch")
            .patchMethod(srg("getEyeHeight", "EntityWitch"), "()F")
            .with(mulBySize);
    }

    @Transformer
    public void itemFixes() {
        inClass("net/minecraft/entity/item/EntityItem")
            .patchMethod(srg("searchForOtherItemsNearby"), "()V")
            .with(p ->
                p.insertAfterAll(ldcInsn(0.5), mv -> { // items stacking with each other
                    mv.visitVarInsn(ALOAD, 0); // EntityItem this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(DMUL);
                }));
        inClass("net/minecraft/client/particle/ParticleItemPickup")
            .patchConstructor("(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;F)V")
            .with(p -> p
                .insertAfter(varInsn(FLOAD, 4), mv -> { // first person pickup render
                    mv.visitVarInsn(ALOAD, 3); // param Entity target
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                }));
        inClass("net/minecraft/entity/player/EntityPlayer")
            .patchMethod(srg("dropItem", "EntityPlayer", "(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;"),
                "(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;")
            .with(p -> p
                .insertAfter(ldcInsn(0.30000001192092896), mv -> { // player's item drop hardcoded height
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(DMUL);
                }));
        inClass("net/minecraft/entity/Entity")
            .patchMethod(srg("entityDropItem", "Entity"), "(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/item/EntityItem;")
            .with(p -> {
                p.insertAfter(varInsn(FLOAD, 2), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                });
                p.insertBefore(varInsn(ASTORE, 3), mv -> { // EntityItem entityitem
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(ICONST_0);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/item/EntityItem", "setEntitySize", "(DZ)V", false);
                });
            });
    }

    @Transformer
    public void beaconBaseColor() { // this is the coolest thing i did with asm so far :V
        String WHITE = srg("WHITE", "EnumDyeColor");
        inClass("net/minecraft/tileentity/TileEntityBeacon")
            .addField(ACC_PRIVATE, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;")
            .patchConstructor("()V")
            .with(p -> p
                .insertAfter(varInsn(ALOAD, 0), 2, mv -> {
                    mv.visitInsn(DUP);
                    mv.visitFieldInsn(GETSTATIC, "net/minecraft/item/EnumDyeColor", WHITE, "Lnet/minecraft/item/EnumDyeColor;");
                    mv.visitFieldInsn(PUTFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                }))
            .patchMethod(srg("updateSegmentColors"), "()V")
            .with(p -> p
                .replace(fieldInsn(GETSTATIC, "net/minecraft/item/EnumDyeColor", WHITE, "Lnet/minecraft/item/EnumDyeColor;"), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                    mv.visitFieldInsn(GETFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                }))
            .patchMethod(srg("readFromNBT", "TileEntityBeacon"), "(Lnet/minecraft/nbt/NBTTagCompound;)V")
            .with(p -> p
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                    mv.visitVarInsn(ALOAD, 1); // param NBTTagCompound compound
                    mv.visitLdcInsn("chiseled_me:color");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", srg("getByte", "NBTTagCompound"), "(Ljava/lang/String;)B", false);
                    mv.visitMethodInsn(INVOKESTATIC, "net/minecraft/item/EnumDyeColor", srg("byMetadata", "EnumDyeColor"), "(I)Lnet/minecraft/item/EnumDyeColor;", false);
                    mv.visitFieldInsn(PUTFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                }))
            .patchMethod(srg("writeToNBT", "TileEntityBeacon"), "(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;")
            .with(p -> p
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 1); // param NBTTagCompound compound
                    mv.visitLdcInsn("chiseled_me:color");
                    mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                    mv.visitFieldInsn(GETFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/item/EnumDyeColor", srg("getMetadata", "EnumDyeColor"), "()I", false);
                    mv.visitInsn(I2B);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", srg("setByte", "NBTTagCompound"), "(Ljava/lang/String;B)V", false);
                }));
    }

    private Patch motionPatchFor(IntPredicate filterXZ, IntPredicate filterY) {
        return p -> {
            Hook mulBySize = mv -> {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                mv.visitInsn(DMUL);
            };
            Hook filtered = mulBySize.filter(filterXZ);
            Hook filteredY = mulBySize.filter(filterY);
            String className = p.getClassName().replace('.', '/');
            p.insertAfterAll(fieldInsn(GETFIELD, className, srg("motionX", "Entity"), "D"), filtered);
            p.insertAfterAll(fieldInsn(GETFIELD, className, srg("motionY", "Entity"), "D"), filteredY);
            p.insertAfterAll(fieldInsn(GETFIELD, className, srg("motionZ", "Entity"), "D"), filtered);
            p.insertAfterAll(ldcInsn(0.25D), mulBySize);
        };
    }

    @Transformer
    public void throwableEntityMotion() {
        Hook mulBySize = mv -> {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
            mv.visitInsn(DMUL);
        };
        Patch heightPatch = p ->
            p.insertAfter(ldcInsn(0.10000000149011612D), mv -> {
                mv.visitVarInsn(ALOAD, 2);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                mv.visitInsn(DMUL);
            });
        Patch collisionPatch = p -> {
            p.replace(insn(DCONST_1), mv -> {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
            });
            p.insertAfterAll(ldcInsn(0.30000001192092896D), mulBySize);
        };
        Patch sizeFieldPatch = p -> p.insertAfterAll(ldcInsn(0.25D), mulBySize);
        inClass("net/minecraft/entity/projectile/EntityThrowable")
            .patchConstructor("(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)V")
            .with(heightPatch)
            .patchMethod(srg("onUpdate", "EntityThrowable"), "()V")
            .with(sizeFieldPatch
                .and(collisionPatch)
                .and(motionPatchFor(
                    pass -> (pass >= 2 && pass <= 5) || pass == 9 || pass == 10,
                    pass -> (pass >= 2 && pass <= 5) || pass == 7 || pass == 8)));

        inClass("net/minecraft/entity/projectile/EntityArrow")
            .patchConstructor("(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)V")
            .with(heightPatch)
            .patchMethod(srg("onUpdate", "EntityArrow"), "()V")
            .with(sizeFieldPatch
                .and(motionPatchFor(
                    pass -> (pass >= 5 && pass <= 9) || pass == 13 || pass == 14,
                    pass -> (pass >= 3 && pass <= 7) || pass == 9 || pass == 10))
                .and(p ->
                    p.insertAfter(methodInsn(INVOKEVIRTUAL, "net/minecraft/entity/projectile/EntityArrow", srg("getIsCritical"), "()Z"),
                        mv -> mv.ifJump(IFEQ,
                            () -> {
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                                mv.visitLdcInsn(0.25);
                                mv.visitInsn(DCMPG);
                                mv.ifJump(IFGT,
                                    () -> mv.visitInsn(ICONST_0),
                                    () -> mv.visitInsn(ICONST_1));
                            },
                            () -> mv.visitInsn(ICONST_0)))))
            .patchMethod(srg("findEntityOnPath"), "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/entity/Entity;")
            .with(collisionPatch)
            .patchMethod(srg("onHit", "EntityArrow"), "(Lnet/minecraft/util/math/RayTraceResult;)V")
            .with(p -> {
//                p.insertAfter(methodBegin(), mv -> {
//                    mv.visitVarInsn(ALOAD, 1); // param RayTraceResult raytraceResultIn
//                    mv.visitFieldInsn(GETFIELD, "net/minecraft/util/math/RayTraceResult", srg("typeOfHit"), "Lnet/minecraft/util/math/RayTraceResult$Type;");
//                    mv.visitFieldInsn(GETSTATIC, "net/minecraft/util/math/RayTraceResult$Type", srg("MISS"), "Lnet/minecraft/util/math/RayTraceResult$Type;");
//                    mv.ifJump(IF_ACMPNE, () -> mv.visitInsn(RETURN));
//                });
                p.insertAfterAll(ldcInsn(0.05000000074505806D), mulBySize);
            });
    }
}
