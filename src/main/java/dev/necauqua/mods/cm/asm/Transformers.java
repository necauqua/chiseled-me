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
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.Label;

import java.util.Set;
import java.util.function.IntPredicate;

import static dev.necauqua.mods.cm.asm.dsl.ASM.*;
import static org.objectweb.asm.Opcodes.*;

// intellij sees too many random duplicates in the hooks
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

    private static final Hook sizeOfThis = mv -> {
        mv.visitVarInsn(ALOAD, 0);  // Entity this
        mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
    };

    private static final Hook cutBiggerThanOne = cut(FCMPL, IFLE);
    private static final Hook cutSmallerThanOne = cut(FCMPG, IFGE);
    private static final String SIZE_NBT_TAG = "chiseled_me:size";
    private static final String PROCESS_CLASS = "dev/necauqua/mods/cm/size/ChangingSizeProcess";
    private static final String PROCESS_TYPE = "L" + PROCESS_CLASS + ";";

    private final boolean obfuscated;
    private final Set<String> loadedCoremods;

    public Transformers(boolean obfuscated, Set<String> loadedCoremods) {
        this.obfuscated = obfuscated;
        this.loadedCoremods = loadedCoremods;
    }

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
        inClass("net.minecraft.entity.Entity")

            .addField(ACC_PUBLIC, SIZE_FIELD, "D")
            .addField(ACC_PUBLIC, PROCESS_FIELD, PROCESS_TYPE)
            .addField(ACC_PUBLIC, O_WIDTH_FIELD, "F")
            .addField(ACC_PUBLIC, O_HEIGHT_FIELD, "F")

            .addInterface("dev.necauqua.mods.cm.api.ISizedEntity")

            .patchConstructor("(Lnet/minecraft/world/World;)V")
            .with(p -> p.insertAfter(methodBegin(), mv -> {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(DUP);
                mv.visitInsn(DCONST_1);
                mv.visitFieldInsn(PUTFIELD, SIZE_FIELD, "D");
            }))

            .patchMethod(srg("onEntityUpdate", "Entity"), "()V")
            .with(p ->
                p.insertAfter(methodBegin(), mv -> {
                    Hook processField = mv2 -> {
                        mv2.visitVarInsn(ALOAD, 0);
                        mv2.visitFieldInsn(GETFIELD, PROCESS_FIELD, PROCESS_TYPE);
                    };

                    // if (this.PROCESS_FIELD == null) { skip everything }
                    mv.visitHook(processField);
                    Label skip = new Label();
                    mv.visitJumpInsn(IFNULL, skip);

                    // _p.interpTicks++;
                    mv.visitHook(processField);
                    mv.visitInsn(DUP);
                    mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "interpTicks", "I");
                    mv.visitInsn(DUP_X1);
                    mv.visitInsn(ICONST_1);
                    mv.visitInsn(IADD);
                    mv.visitFieldInsn(PUTFIELD, PROCESS_CLASS, "interpTicks", "I");
                    mv.visitHook(processField);
                    mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "interpInterval", "I");

                    // if (_p.interpTicks < _p.interpInterval) {
                    mv.ifJump(IF_ICMPGE, () -> {
                        // _p.prevTickSize = this.SIZE_FIELD
                        mv.visitHook(processField);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                        mv.visitFieldInsn(PUTFIELD, PROCESS_CLASS, "prevTickSize", "D");
                        // this.setEntitySize(_p.fromSize + (_p.toSize - _p.fromSize) / (double) _p.interpInterval * (double) _.interpTicks)
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitHook(processField);
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "fromSize", "D");
                        mv.visitHook(processField);
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "toSize", "D");
                        mv.visitHook(processField);
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "fromSize", "D");
                        mv.visitInsn(DSUB);
                        mv.visitHook(processField);
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "interpInterval", "I");
                        mv.visitInsn(I2D);
                        mv.visitInsn(DDIV);
                        mv.visitHook(processField);
                        mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "interpTicks", "I");
                        mv.visitInsn(I2D);
                        mv.visitInsn(DMUL);
                        mv.visitInsn(DADD);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "setEntitySize", "(D)V");
                        mv.visitJumpInsn(GOTO, skip);
                    });
                    // } else {
                    //    this.setEntitySize(_p.toSize)
                    //    this.PROCESS_FIELD = null
                    // }
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInsn(DUP);
                    mv.visitHook(processField);
                    mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "toSize", "D");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "setEntitySize", "(D)V");
                    mv.visitInsn(ACONST_NULL);
                    mv.visitFieldInsn(PUTFIELD, PROCESS_FIELD, PROCESS_TYPE);

                    mv.visitLabel(skip);
                }))

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

                mv.visitVarInsn(ILOAD, 3);

                mv.ifJump(IFEQ,
                    () -> {
                        // this.PROCESS_FIELD = new PROCESS_CLASS(this.SIZE_FIELD, size);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitTypeInsn(NEW, PROCESS_CLASS);
                        mv.visitInsn(DUP);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                        mv.visitVarInsn(DLOAD, 1);
                        mv.visitMethodInsn(INVOKESPECIAL, PROCESS_CLASS, "<init>", "(DD)V", false);
                        mv.visitFieldInsn(PUTFIELD, PROCESS_FIELD, PROCESS_TYPE);
                    }, () -> {
                        // this.SIZE_FIELD = size;
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(DLOAD, 1);
                        mv.visitFieldInsn(PUTFIELD, SIZE_FIELD, "D");

                        // this.width = this.O_WIDTH_FIELD * (float) size;
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(DUP);
                        mv.visitFieldInsn(GETFIELD, O_WIDTH_FIELD, "F");
                        mv.visitVarInsn(DLOAD, 1);
                        mv.visitInsn(D2F);
                        mv.visitInsn(FMUL);
                        mv.visitFieldInsn(PUTFIELD, srg("width", "Entity"), "F");

                        // this.height = this.O_HEIGHT_FIELD * (float) size;
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(DUP);
                        mv.visitFieldInsn(GETFIELD, O_HEIGHT_FIELD, "F");
                        mv.visitVarInsn(DLOAD, 1);
                        mv.visitInsn(D2F);
                        mv.visitInsn(FMUL);
                        mv.visitFieldInsn(PUTFIELD, srg("height", "Entity"), "F");

                        // this.setPosition(this.posX, this.posY, this.posZ);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(DUP);
                        mv.visitFieldInsn(GETFIELD, srg("posX", "Entity"), "D");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, srg("posY", "Entity"), "D");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, srg("posZ", "Entity"), "D");
                        mv.visitMethodInsn(INVOKEVIRTUAL, srg("setPosition", "Entity"), "(DDD)V");
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
            .with(p ->
                p.insertBefore(varInsn(ALOAD, 1), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitVarInsn(ALOAD, 1); // NBTTagCompound compound
                    mv.visitLdcInsn(SIZE_NBT_TAG);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", srg("getDouble", "NBTTagCompound"), "(Ljava/lang/String;)D", false);
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DCONST_0);
                    mv.visitInsn(DCMPL);
                    mv.ifJump(IFEQ, () -> {
                        mv.visitInsn(ICONST_0);
                        // entity, size, false
                        mv.visitMethodInsn(INVOKEVIRTUAL, "setEntitySize", "(DZ)V");
                    }, () -> {
                        mv.visitInsn(POP2);
                        mv.visitInsn(POP);
                    });
                }))

            .patchMethod(srg("writeToNBT", "Entity"), "(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;")
            .with(p ->
                p.insertBefore(varInsn(ALOAD, 1), mv -> {
                    mv.visitVarInsn(ALOAD, 1); // NBTTagCompound compound
                    mv.visitLdcInsn(SIZE_NBT_TAG);

                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, PROCESS_FIELD, PROCESS_TYPE);

                    mv.ifJump(IFNULL,
                        () -> {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, PROCESS_FIELD, PROCESS_TYPE);
                            mv.visitFieldInsn(GETFIELD, PROCESS_CLASS, "toSize", "D");
                        }, () -> {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                        });
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", srg("setDouble", "NBTTagCompound"), "(Ljava/lang/String;D)V", false);
                }))

            .patchMethod("toString", "()Ljava/lang/String;")
            .with(p -> {
                p.replace(intInsn(BIPUSH, 7), mv -> mv.visitIntInsn(BIPUSH, 8));
                p.replace(ldcInsn(), mv -> mv.visitLdcInsn("%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f, cm:size=%.4f]"));
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
        inClass("net.minecraft.client.renderer.EntityRenderer")
            .addField(ACC_PRIVATE, BOBBING_FIELD, "Z")
            .patchMethod(srg("orientCamera"), "(F)V") // camera height & shift
            .with(p -> {
                Hook getEntitySize = mv -> {
                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitVarInsn(FLOAD, 1); // local float partialTicks
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEntitySize", "(F)D", false);
                };
                p.insertBefore(varInsn(FSTORE, 3), mv -> { // local float f
                    // f = entity.getEyeHeight() / getSize(entity) * getRenderSize(entity)
                    // ^ because getEyeHeight is patched and returns height multiplied
                    // by non-render size

                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                });
                p.insertBefore(varInsn(DSTORE, obfuscated ? 12 : 10), mv -> { // local double d3
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(DMUL);
                });
                p.insertAfterAll(ldcInsn(0.1f), mv -> { // f5 fix
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                });
                p.insertAfter(ldcInsn(0.05f), mv -> { // forward cam shift fix
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                });
            })
            .patchMethod(srg("setupCameraTransform"), "(FI)V")
            .with(p -> {
                Hook getPlayerSize = mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityRenderer this
                    mv.visitFieldInsn(GETFIELD, srg("mc", "EntityRenderer"), "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitVarInsn(FLOAD, 1); // local float partialTick
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/entity/EntityPlayerSP", "getEntitySize", "(F)D", false);
                    mv.visitInsn(D2F);
                };
                p.insertAfter(ldcInsn(0.05f), mv -> { // another clipping fix
                    mv.visitHook(getPlayerSize);
                    mv.visitInsn(FMUL);
                });
                p.insertAround(invokeInsn("net/minecraft/client/renderer/EntityRenderer", srg("applyBobbing"), "(F)V"),
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
            .patchMethod(srg("setupFog"), "(IF)V")
            .with(p -> { // scale fog distance by player size
                p.insertBeforeAll(varInsn(FSTORE, 6), mv -> {
                    mv.visitVarInsn(ALOAD, 3); // local Entity entity
                    mv.visitVarInsn(FLOAD, 2); // local float partialTick
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEntitySize", "(F)D", false);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                });
            })
            .patchMethod(srg("applyBobbing"), "(F)V") // bobbing fix (-_-)
            .with(p ->
                p.insertAfterAll(varInsn(FLOAD, 5), mv -> { // local float f2
                    if (mv.getPass() > 2) { // only first two
                        return;
                    }
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, BOBBING_FIELD, "Z");
                    mv.ifJump(IFEQ, () -> {
                        mv.visitVarInsn(ALOAD, 2); // local EntityPlayer entityplayer
                        mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/player/EntityPlayer", SIZE_FIELD, "D");
                        mv.visitInsn(D2F);
                        mv.visitInsn(FMUL);
                    });
                }))
            .patchMethod(srg("renderWorldPass"), "(IFJ)V") // these three are clipping
            .and(srg("renderCloudsCheck"), "(Lnet/minecraft/client/renderer/RenderGlobal;FIDDD)V")
            .and(srg("renderHand", "EntityRenderer", "(FI)V"), "(FI)V")
            .with(p ->
                p.insertAfterAll(ldcInsn(0.05f), mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, srg("mc", "EntityRenderer"), "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/client/entity/EntityPlayerSP", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitHook(cutBiggerThanOne);
                    mv.visitInsn(FMUL);
                }));
    }

    @Transformer
    public void entityRender() {
        inClass("net.minecraft.client.renderer.entity.RenderManager")
            .patchMethod(srg("renderEntity"), "(Lnet/minecraft/entity/Entity;DDDFFZ)V")
            .with(p -> {
                Hook getEntitySize = mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 9); // param float partialTicks
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEntitySize", "(F)D", false);
                };
                p.insertBefore(varInsn(ALOAD, 11), 2, mv -> {
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(D2F);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                });
                p.insertAfter(varInsn(DLOAD, 2), mv -> { // param double x
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(DDIV);
                });
                p.insertAfter(varInsn(DLOAD, 4), mv -> { // param double y
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(DDIV);
                });
                p.insertAfter(varInsn(DLOAD, 6), mv -> { // param double z
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(DDIV);
                });
                p.insertBefore(jumpInsn(GOTO), mv -> {
                    mv.visitInsn(FCONST_1);
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                });
            })
            .patchMethod(srg("renderDebugBoundingBox"), "(Lnet/minecraft/entity/Entity;DDDFF)V")
            .with(p -> {
                Hook getEntitySize = mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                };
                p.insertAfterAll(ldcInsn(2.0), mv -> { // length of blue 'eye sight' vector
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(DMUL);
                });
                p.insertAfterAll(ldcInsn(0.009999999776482582), mv -> { // height of red 'eye heigth' box
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(DMUL);
                });
            });
        inClass("net.minecraft.client.renderer.entity.Render")
            .patchMethod(srg("renderShadow", "Render"), "(Lnet/minecraft/entity/Entity;DDDFF)V")
            .with(p -> {
                Hook getEntitySize = mv -> {
                    mv.visitVarInsn(ALOAD, 1); // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 9); // param float partialTicks
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEntitySize", "(F)D", false);
                };
                p.insertBefore(varInsn(FSTORE, 11), mv -> { // local float f
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                });
                p.insertBefore(varInsn(DSTORE, obfuscated ? 27 : 26), mv -> { // local double d3
                    // d = ... if (size >= 1.0) { } else { ... - 0.015625 * (1.0 - size) }
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(DCONST_1);
                    mv.visitInsn(DCMPG);

                    mv.ifJump(IFGE, () -> {
                        mv.visitLdcInsn(0.015625);
                        mv.visitInsn(DCONST_1);
                        mv.visitHook(getEntitySize);
                        mv.visitInsn(DSUB);
                        mv.visitInsn(DMUL);
                        mv.visitInsn(DSUB);
                    });
                });
                p.insertAfter(varInsn(FLOAD, 8), mv -> { // local float shadowAlpha
                    //  replace shadowAlpha in renderShadowSingle call with

                    //  ... - (float) ((y - (blockpos.getY() + d3)) / 2.0 * (1.0f / size - 1.0f))

                    mv.visitVarInsn(DLOAD, 4);

                    mv.visitVarInsn(ALOAD, obfuscated ? 34 : 33); // local BlockPos blockpos
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", srg("getY", "Vec3i"), "()I", false);
                    mv.visitInsn(I2D);
                    mv.visitVarInsn(DLOAD, obfuscated ? 27 : 26); // local double d3
                    mv.visitInsn(DADD);

                    mv.visitInsn(DSUB);

                    mv.visitLdcInsn(2.0);
                    mv.visitInsn(DDIV);

                    mv.visitInsn(DCONST_1);
                    mv.visitHook(getEntitySize);
                    mv.visitInsn(DDIV);

                    mv.visitInsn(DCONST_1);
                    mv.visitInsn(DSUB);

                    mv.visitInsn(DMUL);

                    mv.visitInsn(D2F);

                    mv.visitInsn(FSUB);
                });
            })
            .patchMethod(srg("renderLivingLabel"), "(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V")
            .with(p ->
                p.replace(varInsn(FLOAD, 16), mv -> {
                    mv.visitVarInsn(FLOAD, 16); // local float f2
                    Hook getSneakingOffset = mv2 -> {
                        mv2.visitVarInsn(ALOAD, 1); // param Entity entityIn
                        mv2.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", srg("isSneaking", "Entity"), "()Z", false);
                        mv2.ifJump(IFEQ,
                            () -> mv2.visitLdcInsn(0.25f),
                            () -> mv2.visitLdcInsn(0.5f));
                    };
                    mv.visitHook(getSneakingOffset);
                    mv.visitInsn(FSUB);
                    mv.visitVarInsn(ALOAD, 1); // param Entity entityIn
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                    mv.visitHook(getSneakingOffset);
                    mv.visitInsn(FADD);
                }));
        Patch renderDistPatch = p -> p.insertBefore(varInsn(DSTORE, 3), 3, sizeOfThis.and(mv -> {
            mv.visitInsn(D2F);
            mv.visitHook(cutBiggerThanOne);
            mv.visitInsn(F2D);
            mv.visitInsn(DDIV);
        }));
        inClass("net.minecraft.entity.Entity")
            .patchMethodOptionally(srg("isInRangeToRenderDist", "EntityOtherPlayerMP"), "(D)Z")
            .with(renderDistPatch);
        inClass("net.minecraft.client.entity.EntityOtherPlayerMP") // because stupid EntityOtherPlayerMP
            .patchMethod(srg("isInRangeToRenderDist", "EntityOtherPlayerMP"), "(D)Z")
            .with(renderDistPatch);
        inClass("net.minecraft.client.gui.inventory.GuiInventory")
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
        Hook mulBySize = sizeOfThis.and(mv -> mv.visitInsn(DMUL));
        Hook fmulBySize = sizeOfThis.and(mv -> {
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
        });
        inClass("net.minecraft.entity.Entity")
            .patchMethod(srg("move", "Entity"), "(Lnet/minecraft/entity/MoverType;DDD)V")
            .with(p -> {
                p.insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 1); // MoverType mover type
                    mv.visitFieldInsn(GETSTATIC, "net/minecraft/entity/MoverType", srg("SELF"), "Lnet/minecraft/entity/MoverType;");
                    mv.ifJump(IF_ACMPNE, () -> {
                        mv.visitVarInsn(DLOAD, 2); // param double x
                        mv.visitHook(mulBySize);
                        mv.visitVarInsn(DSTORE, 2); // param double x
                        mv.visitVarInsn(DLOAD, 4); // param double y
                        mv.visitHook(mulBySize);
                        mv.visitVarInsn(DSTORE, 4); // param double y
                        mv.visitVarInsn(DLOAD, 6); // param double z
                        mv.visitHook(mulBySize);
                        mv.visitVarInsn(DSTORE, 6); // param double z
                    });
                });
                p.insertAfterAll(ldcInsn(0.05), mulBySize);  //
                p.insertAfterAll(ldcInsn(-0.05), mulBySize); // shifting on edges of aabb's

                p.insertAfterAll(fieldInsn(GETFIELD, "net/minecraft/entity/Entity", srg("stepHeight"), "F"), fmulBySize);
//                p.insertBefore(varInsn(DSTORE, 4), 4, mulBySize); // y *= stepHeight < * entitySize >

                p.insertAfterAll(ldcInsn(0.6), sizeOfThis.and(mv -> mv.visitInsn(DDIV))); // div distanceWalked(Modified) for the step sounds
                p.insertAfterAll(ldcInsn(0.35f), sizeOfThis.and(mv -> { // div local f for the swim sounds
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                }));
                p.insertAfter(ldcInsn(0.20000000298023224), mulBySize); // block step collision
                p.insertAfterAll(ldcInsn(0.001), mulBySize); // flammable collision
            })
            .patchMethod(srg("createRunningParticles"), "()V")
            .with(p -> p
                .insertAfter(methodBegin(), sizeOfThis.and(mv -> {
                    mv.visitLdcInsn(0.25);
                    mv.visitInsn(DCMPG);
                    mv.ifJump(IFGT, () -> mv.visitInsn(RETURN));
                })))
            .patchMethod(srg("isInLava"), "()Z")
            .with(p -> {
                p.insertAfterAll(ldcInsn(-0.10000000149011612), mulBySize);
                p.insertAfter(ldcInsn(-0.4000000059604645), mulBySize);
            });
        Patch libmSwingAnimation = p ->
            p.insertAfter(ldcInsn(4.0f), sizeOfThis.and(mv -> { // fix for limb swing animation
                mv.visitInsn(D2F);
                mv.visitInsn(FDIV);
            }));
        inClass("net.minecraft.entity.EntityLivingBase")
            .patchMethod(srg("travel", "EntityLivingBase"), "(FFF)V")
            .with(libmSwingAnimation.and(mv ->
                // scale the vertical fluid collision offset
                mv.insertAfter(ldcInsn(0.6000000238418579), mulBySize)));
        inClass("net.minecraft.client.entity.EntityOtherPlayerMP") // because stupid EntityOtherPlayerMP x 2
            .patchMethod(srg("onUpdate", "EntityOtherPlayerMP"), "()V")
            .with(libmSwingAnimation);
        inClass("net.minecraft.client.entity.EntityPlayerSP")
            .patchMethod(srg("onLivingUpdate", "EntityPlayerSP"), "()V")
            .with(p -> p.insertAfterAll(ldcInsn(0.5), mulBySize)) // push from blocks upward aabb offset
            .patchMethod(srg("updateAutoJump"), "(FF)V")
            .with(p -> {
                p.insertAfterAll(ldcInsn(0.001f), fmulBySize);
                p.insertAfter(ldcInsn(-0.15f), fmulBySize);
                p.insertAfter(ldcInsn(7.0f), 2, fmulBySize);
                p.insertAfter(ldcInsn(0.75f), fmulBySize);
                p.insertAfter(ldcInsn(1.2f), fmulBySize);
                p.insertAfter(ldcInsn(0.5f), 2, fmulBySize);
                p.replace(insn(ICONST_1), 2, mv -> mv.visitInsn(ICONST_0));
                p.insertAfterAll(ldcInsn(0.5099999904632568D), mulBySize);
            });
    }

    @Transformer
    public void serverMotionFixes() {
        inClass("net.minecraft.client.entity.EntityPlayerSP")
            .patchMethod(srg("onUpdateWalkingPlayer"), "()V")
            .with(p -> p
                .insertAfter(ldcInsn(0.0009), mv -> {
                    mv.visitVarInsn(ALOAD, 0);  // EntityPlayerSP this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DMUL);
                    mv.visitInsn(DMUL);
                }));
        inClass("net.minecraft.entity.EntityTrackerEntry")
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
        inClass("net.minecraft.network.NetHandlerPlayServer")
            .patchMethod(srg("processPlayer", "NetHandlerPlayServer"), "(Lnet/minecraft/network/play/client/CPacketPlayer;)V")
            .with(p -> {
                Hook mulByPlayerSize = mv -> { // setup
                    mv.visitVarInsn(ALOAD, 0); // NetHandlerPlayServer this
                    mv.visitFieldInsn(GETFIELD, srg("player", "NetHandlerPlayServer"), "Lnet/minecraft/entity/player/EntityPlayerMP;");
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/player/EntityPlayer", SIZE_FIELD, "D");
                    mv.visitInsn(DMUL);
                };
                p.insertAfterAll(ldcInsn(0.0625), mulByPlayerSize // fix for small aabbs
                    .filter(pass -> pass != 2)); // dont change movement correctness checker (just so you can be relatively fast when small?)
                p.insertAfter(ldcInsn(-0.03125D), mulByPlayerSize); // floating checker
                p.insertAfter(ldcInsn(-0.55D), mulByPlayerSize); // some kind of levitation potion effect checker
            });
        inClass("net.minecraft.entity.player.EntityPlayerMP")
            .patchMethod(srg("handleFalling"), "(DZ)V")
            .with(p ->
                p.insertAfter(ldcInsn(0.20000000298023224D), sizeOfThis.and(mv -> mv.visitInsn(DMUL))));
    }

    @Transformer
    public void entityCollisions() {
        Hook mulBySize = sizeOfThis.and(mv -> mv.visitInsn(DMUL));

        inClass("net.minecraft.entity.player.EntityPlayer")
            .patchMethod(srg("onLivingUpdate", "EntityPlayer"), "()V") // fixes collideEntityWithPlayer aabb expansion
            .with(p -> {
                p.replaceAll(insn(DCONST_1), sizeOfThis);
                p.insertAfter(ldcInsn(0.5), mulBySize);
            });

        Patch ignoreFirstCheck = p -> p.replace(insn(ICONST_1), mv -> mv.visitInsn(ICONST_0));

        inClass("net.minecraft.entity.EntityAgeable")
            .patchMethod(srg("setSize", "EntityAgeable"), "(FF)V")
            .with(ignoreFirstCheck);
        inClass("net.minecraft.entity.monster.EntityZombie")
            .patchMethod(srg("setSize", "EntityZombie"), "(FF)V")
            .with(ignoreFirstCheck);

        inClass("net.minecraft.entity.Entity")
            .patchMethod(srg("setSize", "Entity"), "(FF)V")
            .with(p -> {
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
                });
                p.insertAfter(insn(F2D), mv -> {
                    if (mv.getPass() >= 6) {
                        mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                        mv.visitInsn(DDIV);
                    }
                });
            })
            .patchMethod(srg("isEntityInsideOpaqueBlock", "Entity"), "()Z")
            .with(p -> p.insertAfter(ldcInsn(0.1f), mv -> {
                mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                mv.visitInsn(D2F);
                mv.visitInsn(FMUL);
            }))
            .patchMethod(srg("doBlockCollisions"), "()V")
            .with(p -> p.insertAfterAll(ldcInsn(0.001), mulBySize))
            .patchMethod(srg("handleWaterMovement", "Entity"), "()Z")
            .with(p -> {
                p.insertAfter(ldcInsn(-0.4000000059604645), mulBySize); // vertical AABB extension
                p.insertAfter(ldcInsn(0.001), mulBySize); // AABB shrink
            })
            .patchMethod(srg("isOverWater", "Entity"), "()Z")
            .with(p -> {
                p.insertAfter(ldcInsn(-20.0), mulBySize); // vertical AABB extension
                p.insertAfter(ldcInsn(0.001), mulBySize); // AABB shrink
            });
        inClass("net.minecraft.client.renderer.ItemRenderer")
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
        inClass("net.minecraft.client.renderer.EntityRenderer")
            .patchMethod(srg("getMouseOver"), "(F)V")
            .with(p -> {
                Hook getEntitySize = mv -> {
                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitVarInsn(FLOAD, 1); // local float partialTicks
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEntitySize", "(F)D", false);
                };
                Hook mulBySize = getEntitySize.and(mv -> mv.visitInsn(DMUL));
                p.insertBefore(varInsn(DSTORE, 3), mulBySize);
                p.insertAfter(loadedCoremods.contains("AstralCore") ? varInsn(DLOAD, 8) : ldcInsn(6.0), mulBySize);
                p.insertAfterAll(ldcInsn(3.0), mulBySize);
            });

        inClass("net.minecraft.client.multiplayer.PlayerControllerMP") // on client
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
        inClass("net.minecraft.server.management.PlayerInteractionManager") // on server - only increase reach to match client
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
        inClass("net.minecraft.network.NetHandlerPlayServer") // entity server reach
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
        inClass("net.minecraft.world.World") // when reach distance <= 1 this one null return screws up raytracing a bit so here's a fix
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
        inClass("net.minecraft.entity.player.EntityPlayer")
            .patchMethod(srg("getEyeHeight", "EntityPlayer"), "()F")
            .with(mulBySize);

        // those patches are for shooting mobs with hardcoded height, there is no way to make it universal currently
        inClass("net.minecraft.entity.monster.AbstractSkeleton")
            .patchMethod(srg("getEyeHeight", "AbstractSkeleton"), "()F")
            .with(mulBySize);
        inClass("net.minecraft.entity.monster.EntitySnowman")
            .patchMethod(srg("getEyeHeight", "EntitySnowman"), "()F")
            .with(mulBySize);
        inClass("net.minecraft.entity.monster.EntityGhast")
            .patchMethod(srg("getEyeHeight", "EntityGhast"), "()F")
            .with(mulBySize);
        inClass("net.minecraft.entity.monster.EntityWitch")
            .patchMethod(srg("getEyeHeight", "EntityWitch"), "()F")
            .with(mulBySize);
    }

    private static boolean isInSpigot() {
        try {
            Class.forName("org.bukkit.entity.Player$Spigot");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Transformer
    public void itemFixes() {
        inClass("net.minecraft.entity.item.EntityItem")
            .patchMethod(srg("searchForOtherItemsNearby"), "()V")
            .with(p ->
                p.insertAfterAll(isInSpigot() ? varInsn(DLOAD, 1) : ldcInsn(0.5),
                    mv -> { // items stacking with each other
                        mv.visitVarInsn(ALOAD, 0); // EntityItem this
                        mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                        mv.visitInsn(DMUL);
                    }));

        inClass("net.minecraft.client.particle.ParticleItemPickup")
            .patchConstructor("(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;F)V")
            .with(p -> p
                .insertAfter(varInsn(FLOAD, 4), mv -> { // first person pickup render
                    mv.visitVarInsn(ALOAD, 3); // param Entity target
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                }));
        inClass("net.minecraft.entity.player.EntityPlayer")
            .patchMethod(srg("dropItem", "EntityPlayer", "(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;"),
                "(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;")
            .with(p -> p
                .insertAfter(ldcInsn(0.30000001192092896), mv -> { // player's item drop hardcoded height
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitInsn(DMUL);
                }));
        inClass("net.minecraft.entity.Entity")
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
        Patch baseColorPatch = p -> p
            .replace(fieldInsn(GETSTATIC, "net/minecraft/item/EnumDyeColor", srg("WHITE", "EnumDyeColor"), "Lnet/minecraft/item/EnumDyeColor;"), mv -> {
                mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                mv.visitFieldInsn(GETFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
            });

        // workaround for BetterFps beacon optimisation
        if (Launch.blackboard.containsKey("BetterFpsVersion")) {
            inClass("net.minecraft.tileentity.TileEntityBeacon")
                .patchMethod("updateGlassLayers", "(III)V")
                .with(baseColorPatch);
        } else {
            inClass("net.minecraft.tileentity.TileEntityBeacon")
                .patchMethod(srg("updateSegmentColors"), "()V")
                .with(baseColorPatch);
        }

        inClass("net.minecraft.tileentity.TileEntityBeacon")
            .addField(ACC_PRIVATE, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;")
            .patchConstructor("()V")
            .with(p -> p
                .insertAfter(varInsn(ALOAD, 0), 2, mv -> {
                    mv.visitInsn(DUP);
                    mv.visitFieldInsn(GETSTATIC, "net/minecraft/item/EnumDyeColor", srg("WHITE", "EnumDyeColor"), "Lnet/minecraft/item/EnumDyeColor;");
                    mv.visitFieldInsn(PUTFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
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
        inClass("net.minecraft.entity.projectile.EntityThrowable")
            .patchConstructor("(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)V")
            .with(heightPatch)
            .patchMethod(srg("onUpdate", "EntityThrowable"), "()V")
            .with(sizeFieldPatch
                .and(collisionPatch)
                .and(motionPatchFor(
                    pass -> (pass >= 2 && pass <= 5) || pass == 9 || pass == 10,
                    pass -> (pass >= 2 && pass <= 5) || pass == 7 || pass == 8)));

        inClass("net.minecraft.entity.projectile.EntityArrow")
            .patchConstructor("(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)V")
            .with(heightPatch)
            .patchMethod(srg("onUpdate", "EntityArrow"), "()V")
            .with(sizeFieldPatch
                .and(motionPatchFor(
                    pass -> (pass >= 5 && pass <= 9) || pass == 13 || pass == 14,
                    pass -> (pass >= 3 && pass <= 7) || pass == 9 || pass == 10))
                .and(p ->
                    p.insertAfter(invokeInsn("net/minecraft/entity/projectile/EntityArrow", srg("getIsCritical"), "()Z"),
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

    @Transformer
    public void entityAI() {
        Patch sizePredicate = p ->
            p.replace(insn(ICONST_1), mv -> {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, srg("animal"), "Lnet/minecraft/entity/passive/EntityAnimal;");
                mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, srg("targetMate"), "Lnet/minecraft/entity/passive/EntityAnimal;");
                mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                mv.visitInsn(DCMPL);
                mv.ifJump(IFNE, () -> mv.visitInsn(ICONST_1), () -> mv.visitInsn(ICONST_0));
            });
        inClass("net.minecraft.entity.ai.EntityAIMate")
            // change mating distance (disabled because pathfinding works in block positions :( )
//            .patchMethod(srg("updateTask", "EntityAIMate"), "()V")
//            .with(p ->
//                p.insertAfter(ldcInsn(9.0), mv -> {
//                    mv.visitVarInsn(ALOAD, 0);
//                    mv.visitFieldInsn(GETFIELD, srg("animal"), "Lnet/minecraft/entity/passive/EntityAnimal;");
//                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
//                    mv.visitInsn(DUP2);
//                    mv.visitInsn(DMUL);
//                    mv.visitInsn(DMUL);
//                }))
//            .patchMethod(srg("getNearbyMate"), "()Lnet/minecraft/entity/passive/EntityAnimal;")
//            .with(p ->
//                p.insertAfter(ldcInsn(8.0), mv -> {
//                    mv.visitVarInsn(ALOAD, 0);
//                    mv.visitFieldInsn(GETFIELD, srg("animal"), "Lnet/minecraft/entity/passive/EntityAnimal;");
//                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
//                    mv.visitInsn(DMUL);
//                }))

            // disallow mating between differently sized entities
            .patchMethod(srg("shouldExecute", "EntityAIMate"), "()Z")
            .with(sizePredicate)
            .patchMethod(srg("shouldContinueExecuting", "EntityAIMate"), "()Z")
            .with(sizePredicate);

        Hook sizeofTemptedEntity = mv -> {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, srg("temptedEntity"), "Lnet/minecraft/entity/EntityCreature;");
            mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
        };
        Hook squareMul = sizeofTemptedEntity.and(mv -> {
            mv.visitInsn(DUP2);
            mv.visitInsn(DMUL);
            mv.visitInsn(DMUL);
        });

        // scale area and closest distance for entities that follow you when you hold food
        // still not the best at small sizes since entity AI works in block positions
        inClass("net.minecraft.entity.ai.EntityAITempt")
            .patchMethod(srg("shouldExecute", "EntityAITempt"), "()Z")
            .with(p -> p.insertAfter(ldcInsn(10.0), sizeofTemptedEntity.and(mv -> mv.visitInsn(DMUL))))
            .patchMethod(srg("shouldContinueExecuting", "EntityAITempt"), "()Z")
            .with(p -> {
                p.insertAfter(ldcInsn(36.0), squareMul);
                p.insertAfter(ldcInsn(0.010000000000000002), squareMul);
            })
            .patchMethod(srg("updateTask", "EntityAITempt"), "()V")
            .with(p -> p.insertAfter(ldcInsn(6.25), squareMul));
    }

    @Transformer
    public void spawnEggFixes() {
        inClass("net.minecraft.item.ItemMonsterPlacer")
            .addField(ACC_PRIVATE | ACC_STATIC, SIZE_FIELD, "D")
            .addField(ACC_PRIVATE | ACC_STATIC, "$cm_facing_hack", "Lnet/minecraft/util/EnumFacing;")
            .patchConstructor("()V")
            .with(p ->
                p.insertAfter(methodBegin(), mv -> {
                    mv.visitInsn(DCONST_1);
                    mv.visitFieldInsn(PUTSTATIC, SIZE_FIELD, "D");
                }))
            .patchMethod(srg("onItemUse", "ItemMonsterPlacer"), "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;")
            .with(p -> {
                p.insertAfter(varInsn(DSTORE, obfuscated ? 14 : 13), mv -> {
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitFieldInsn(PUTSTATIC, SIZE_FIELD, "D");
                    mv.visitVarInsn(ALOAD, 5);
                    mv.visitFieldInsn(PUTSTATIC, "$cm_facing_hack", "Lnet/minecraft/util/EnumFacing;");
                });
                p.insertBefore(varInsn(ASTORE, 12), 2, mv -> {
                    mv.visitInsn(POP);
                    mv.visitVarInsn(ALOAD, 3);
                });
                p.replace(ldcInsn(0.5), mv -> {
                    mv.visitVarInsn(FLOAD, 6); // param float hitX
                    mv.visitInsn(F2D);
                });
                p.replace(varInsn(DLOAD, obfuscated ? 14 : 13), mv -> {
                    mv.visitVarInsn(FLOAD, 7); // param float hitY
                    mv.visitInsn(F2D);
                });
                p.replace(ldcInsn(0.5), 2, mv -> {
                    mv.visitVarInsn(FLOAD, 8); // param float hitZ
                    mv.visitInsn(F2D);
                    // z += facing.getZOffset() * entity.width / 2
                });
            })
            .patchMethod(srg("onItemRightClick", "ItemMonsterPlacer"), "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/ActionResult;")
            .with(p ->
                p.insertBefore(varInsn(ALOAD, 1), 5, mv -> {
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", SIZE_FIELD, "D");
                    mv.visitFieldInsn(PUTSTATIC, SIZE_FIELD, "D");
                }))
            .patchMethod(srg("spawnCreature"), "(Lnet/minecraft/world/World;Lnet/minecraft/util/ResourceLocation;DDD)Lnet/minecraft/entity/Entity;")
            .with(p ->
                p.insertAfter(varInsn(ASTORE, 8), 2, mv -> {
                    mv.visitVarInsn(ALOAD, 8);
                    mv.visitFieldInsn(GETSTATIC, SIZE_FIELD, "D");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "setEntitySize", "(D)V", false);
                    mv.visitInsn(DCONST_1);
                    mv.visitFieldInsn(PUTSTATIC, SIZE_FIELD, "D");

                    mv.visitFieldInsn(GETSTATIC, "$cm_facing_hack", "Lnet/minecraft/util/EnumFacing;");
                    // if $cm_facing_hack != null
                    mv.ifJump(IFNULL, () -> {
                        // x += ($cm_facing_hack.getXOffset() / 2.0) * entity.width
                        mv.visitVarInsn(DLOAD, 2);
                        mv.visitFieldInsn(GETSTATIC, "$cm_facing_hack", "Lnet/minecraft/util/EnumFacing;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/EnumFacing", srg("getXOffset", "EnumFacing"), "()I", false);
                        mv.visitInsn(I2D);
                        mv.visitLdcInsn(2.0);
                        mv.visitInsn(DDIV);
                        mv.visitVarInsn(ALOAD, 8);
                        mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", srg("width", "Entity"), "F");
                        mv.visitInsn(F2D);
                        mv.visitInsn(DMUL);
                        mv.visitInsn(DADD);
                        mv.visitVarInsn(DSTORE, 2);
                        // z += ($cm_facing_hack.getZOffset() / 2.0) * entity.width
                        mv.visitVarInsn(DLOAD, 6);
                        mv.visitFieldInsn(GETSTATIC, "$cm_facing_hack", "Lnet/minecraft/util/EnumFacing;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/EnumFacing", srg("getZOffset", "EnumFacing"), "()I", false);
                        mv.visitInsn(I2D);
                        mv.visitLdcInsn(2.0);
                        mv.visitInsn(DDIV);
                        mv.visitVarInsn(ALOAD, 8);
                        mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", srg("width", "Entity"), "F");
                        mv.visitInsn(F2D);
                        mv.visitInsn(DMUL);
                        mv.visitInsn(DADD);
                        mv.visitVarInsn(DSTORE, 6);
                        // $cm_facing_hack = null
                        mv.visitInsn(ACONST_NULL);
                        mv.visitFieldInsn(PUTSTATIC, "$cm_facing_hack", "Lnet/minecraft/util/EnumFacing;");
                    });
                }));
        inClass("net.minecraft.entity.EntityAgeable")
            .patchMethod(srg("processInteract", "EntityAgeable"), "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Z")
            .with(p ->
                p.insertAfter(varInsn(ALOAD, 5), 4, mv -> {
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, SIZE_FIELD, "D");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "setEntitySize", "(D)V", false);
                }));
    }
}
