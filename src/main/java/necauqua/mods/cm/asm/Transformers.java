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

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static necauqua.mods.cm.asm.ASM.*;

public class Transformers {

    private static final AsmMethodHook getSize =
        mv -> mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "getSize", "(Lnet/minecraft/entity/Entity;)F", false);
    private static final AsmMethodHook getRenderSize =
        mv -> mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "getRenderSize", "(Lnet/minecraft/entity/Entity;F)F", false);
    private static final AsmMethodHook cutBiggerThanOne = mv -> {
        mv.visitInsn(DUP);
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FCMPL);
        Label skipOne = new Label();
        mv.visitJumpInsn(IFLE, skipOne);
        mv.visitInsn(POP);
        mv.visitInsn(FCONST_1);
        mv.visitLabel(skipOne);
    };
    private static final AsmMethodHook cutSmallerThanOne = mv -> {
        mv.visitInsn(DUP);
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FCMPG);
        Label skipOne = new Label();
        mv.visitJumpInsn(IFGE, skipOne);
        mv.visitInsn(POP);
        mv.visitInsn(FCONST_1);
        mv.visitLabel(skipOne);
    };
    private static final AsmMethodHook thePlayer =
        mv -> mv.visitFieldInsn(GETFIELD, "net/minecraft/client/Minecraft", "thePlayer", "field_71439_g", "Lnet/minecraft/client/entity/EntityPlayerSP;");
    private static final AsmMethodHook scale =
        mv -> mv.visitMethodInsn(INVOKESTATIC, "net/minecraft/client/renderer/GlStateManager", "scale", "func_179152_a", "(FFF)V");

    @Transformer
    public void cameraView() {
        inClass("net.minecraft.client.renderer.EntityRenderer")
          .patchMethod("orientCamera", "func_78467_g", "(F)V") // camera height & shift
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertBefore(varInsn(FSTORE, 3)).code(mv -> { // local float f
                    mv.visitInsn(POP); // meh, just ignore original getEyeHeight call
                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitVarInsn(FLOAD, 1); // local float partialTicks
                    mv.visitHook(getRenderSize);
                    mv.visitVarInsn(FSTORE, "size");
                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitVarInsn(FLOAD, 1); // local float partialTicks
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "getScreenInterpEyeHeight", "(Lnet/minecraft/entity/Entity;F)F", false);
                }).insertBefore(varInsn(DSTORE, 12)).code(mv -> { // local double d3
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                }).insertAfterAll(ldcInsn(0.1F)).code(mv -> { // f5 fix
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                }).insertAfter(ldcInsn(0.05F)).code(mv -> { // forward cam shift fix
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                })
            )
          .patchMethod("setupCameraTransform", "func_78479_a", "(FI)V") // far plane decrease (closer fog)
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertAfter(insn(I2F)).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/EntityRenderer", "mc", "field_78531_r", "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitVarInsn(FLOAD, 1);
                    mv.visitHook(getRenderSize);
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(FSTORE, "size");
                    mv.visitHook(cutBiggerThanOne);
                    mv.visitInsn(F2D);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
                    mv.visitInsn(D2F);
                    mv.visitInsn(FMUL);
                }).insertAfterAll(ldcInsn(0.05F)).code(mv -> { // camera offset fix
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                })
            )
          .patchMethod("setupViewBobbing", "func_78475_f", "(F)V") // bobbing fix (-_-)
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertBefore(varInsn(ASTORE, 2)).code(mv -> { // local EntityPlayer entityplayer
                    mv.visitInsn(DUP);
                    mv.visitHook(getSize);
                    mv.visitVarInsn(FSTORE, "size");
                })
                .insertBefore(varInsn(FSTORE, 4)).code(mv -> {
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FDIV);
                })
                .insertAfterAll(varInsn(FLOAD, 5)).code(mv -> { // local float f2
                    if(mv.pass <= 2) { // only first two
                        mv.visitVarInsn(FLOAD, "size");
                        mv.visitInsn(FMUL);
                    }
                })
            )
          .patchMethod("renderWorldPass", "func_175068_a", "(IFJ)V") // these three are clipping
                  .and("renderCloudsCheck", "func_180437_a", "(Lnet/minecraft/client/renderer/RenderGlobal;FI)V")
                  .and("renderHand", "func_78476_b", "(FI)V")
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertAfter(methodBegin).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/EntityRenderer", "mc", "field_78531_r", "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitHook(getSize);
                    mv.visitHook(cutBiggerThanOne);
                    mv.visitVarInsn(FSTORE, "size");
                }).insertAfterAll(ldcInsn(0.05F)).code(mv -> {
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                })
            );
    }

    @Transformer
    public void entityRender() {
        inClass("net.minecraft.client.renderer.entity.RenderManager")
          .patchMethod("doRenderEntity", "func_188391_a", "(Lnet/minecraft/entity/Entity;DDDFFZ)V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertBefore(varInsn(ALOAD, 11)).nth(2).code(mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 9); // param float partialTicks
                    mv.visitHook(getRenderSize);
                    mv.visitInsn(DUP);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                }).insertAfter(varInsn(DLOAD, 2)).code(mv -> { // param double x
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                }).insertAfter(varInsn(DLOAD, 4)).code(mv -> { // param double y
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                }).insertAfter(varInsn(DLOAD, 6)).code(mv -> { // param double z
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                }).insertBefore(jumpInsn(GOTO)).code(mv -> {
                    mv.visitInsn(FCONST_1);
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                })
            )
          .patchMethod("renderDebugBoundingBox", "func_85094_b", "(Lnet/minecraft/entity/Entity;DDDFF)V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertAfter(methodBegin).code(mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                }).insertAfterAll(ldcInsn(2.0)).code(mv -> { // length of blue 'eye sight' vector
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                }).insertAfterAll(ldcInsn(0.009999999776482582)).code(mv -> { // height of red 'eye heigth' box
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net.minecraft.client.renderer.entity.Render")
          .patchMethod("renderShadow", "func_76975_c", "(Lnet/minecraft/entity/Entity;DDDFF)V")
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertAfter(methodBegin).code(mv -> {
                    mv.visitVarInsn(ALOAD, 1); // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 9); // param float partialTicks
                    mv.visitHook(getRenderSize);
                    mv.visitVarInsn(FSTORE, "size");
                }).insertBefore(varInsn(FSTORE, 11)).code(mv -> { // local float f
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                }).insertBefore(varInsn(DSTORE, 27)).code(mv -> { // local double d3
                    // code below is this: d3 -= size < 1.0F ? 0.015625F * (1.0F - size) : 0.0F
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FCONST_1);
                    mv.visitInsn(FCMPG);
                    Label skipHook = new Label();
                    mv.visitJumpInsn(IFGE, skipHook);
                    mv.visitLdcInsn(0.015625F);
                    mv.visitInsn(FCONST_1);
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FSUB);
                    mv.visitInsn(FMUL);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DSUB);
                    mv.visitLabel(skipHook);
                })
            )
          .patchMethod("renderLivingLabel", "func_147906_a", "(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V")
            .with(patch()
                .replace(varInsn(FLOAD, 16)).code(mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 16);
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "getLabelHeight", "(Lnet/minecraft/entity/Entity;F)F", false);
                })
            );
        Patch renderDistPatch = patch()
            .insertBefore(varInsn(DSTORE, 3)).nth(3).code(mv -> {
                mv.visitVarInsn(ALOAD, 0); // Entity this
                mv.visitHook(getSize);
                mv.visitHook(cutBiggerThanOne);
                mv.visitInsn(F2D);
                mv.visitInsn(DDIV);
            });
        inClass("net.minecraft.entity.Entity")
          .patchMethodOptionally("isInRangeToRenderDist", "func_70112_a", "(D)Z")
            .with(renderDistPatch);
        inClass("net.minecraft.client.entity.EntityOtherPlayerMP") // because stupid EntityOtherPlayerMP
          .patchMethod("isInRangeToRenderDist", "func_70112_a", "(D)Z")
            .with(renderDistPatch);
        inClass("net.minecraft.client.gui.inventory.GuiInventory")
          .patchMethod("drawEntityOnScreen", "func_147046_a", "(IIIFFLnet/minecraft/entity/EntityLivingBase;)V")
            .with(patch()
                .insertAfter(varInsn(FSTORE, 10)).code(mv -> { // local float f4
                    mv.visitInsn(FCONST_1);
                    mv.visitVarInsn(ALOAD, 5);  // param EntityLivingBase ent
                    mv.visitHook(getSize);
                    mv.visitInsn(FDIV);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                })
            );
    }

    @Transformer
    public void entityMotion() {
        inClass("net.minecraft.entity.Entity")
          .patchMethod("moveEntity", "func_70091_d", "(DDD)V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertAfter(methodBegin).code(mv -> { // the speed
                    mv.visitVarInsn(ALOAD, 0);  // Entity this
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                    mv.visitVarInsn(DLOAD, 1); // param double x
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                    mv.visitVarInsn(DSTORE, 1); // param double x
                    mv.visitVarInsn(DLOAD, 3); // param double y
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                    mv.visitVarInsn(DSTORE, 3); // param double y
                    mv.visitVarInsn(DLOAD, 5); // param double z
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                    mv.visitVarInsn(DSTORE, 5); // param double z
                }).insertAfterAll(ldcInsn(0.05)).code(mv -> { // shifting on edges of aabb's
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                }).insertBefore(varInsn(DSTORE, 3)).nth(3).code(mv -> { // stepHeight
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
            )
          .patchMethod("createRunningParticles", "func_174808_Z", "()V")
            .with(patch()
                .insertAfter(methodBegin).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "cancelRunningParticlesHook", "(Lnet/minecraft/entity/Entity;)Z", false);
                    Label skipReturn = new Label();
                    mv.visitJumpInsn(IFEQ, skipReturn);
                    mv.visitInsn(RETURN);
                    mv.visitLabel(skipReturn);
                })
            );
        Patch libmSwingAnimation = patch()
            .insertAfter(ldcInsn(4.0F)).code(mv -> { // fix for limb swing animation
                mv.visitVarInsn(ALOAD, 0);  // EntityLivingBase this
                mv.visitHook(getSize);
                mv.visitInsn(FDIV);
            });
        inClass("net.minecraft.entity.EntityLivingBase")
          .patchMethod("moveEntityWithHeading", "func_70612_e", "(FF)V")
            .with(libmSwingAnimation);
        inClass("net.minecraft.client.entity.EntityOtherPlayerMP") // because stupid EntityOtherPlayerMP x 2
          .patchMethod("onUpdate", "func_70071_h_", "()V")
            .with(libmSwingAnimation);
    }

    @Transformer
    public void serverMotionFixes() {
        inClass("net.minecraft.client.entity.EntityPlayerSP")
          .patchMethod("onUpdateWalkingPlayer", "func_175161_p", "()V")
            .with(patch()
                .insertAfter(ldcInsn(0.0009)).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0);  // EntityPlayerSP this
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DMUL);
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net.minecraft.entity.EntityTrackerEntry")
          .patchMethod("updatePlayerList", "func_73122_a", "(Ljava/util/List;)V")
            .with(patch()
                .replace(ldcInsn(128L)).code(mv -> {
                    mv.visitLdcInsn(128.0F);
                    mv.visitVarInsn(ALOAD, 0); // EntityTrackerEntry this
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/EntityTrackerEntry", "trackedEntity", "field_73132_a", "Lnet/minecraft/entity/Entity;");
                    mv.visitHook(getSize);
                    mv.visitInsn(DUP);  //
                    mv.visitInsn(FMUL); // bytecode squaring <3
                    mv.visitInsn(FMUL);
                    mv.visitInsn(F2L);
                })
            );
        inClass("net.minecraft.network.NetHandlerPlayServer")
          .patchMethod("processPlayer", "func_147347_a", "(Lnet/minecraft/network/play/client/CPacketPlayer;)V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertBefore(varInsn(ALOAD, 2)).code(mv -> { // setup
                    mv.visitVarInsn(ALOAD, 0); // NetHandlerPlayServer this
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/network/NetHandlerPlayServer", "playerEntity", "field_147369_b", "Lnet/minecraft/entity/player/EntityPlayerMP;");
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                }).insertAfter(varInsn(DLOAD, 19)).nth(4).code(mv -> { // local double d7
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                }).insertAfter(varInsn(DLOAD, 21)).nth(5).code(mv -> { // local double d8
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                }).insertAfter(varInsn(DLOAD, 23)).nth(4).code(mv -> { // local double d9
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                }).insertAfterAll(ldcInsn(0.0625)).code(mv -> { // fix for small aabbs
                    if(mv.pass != 2) { // dont change movement correctness checker
                        mv.visitVarInsn(DLOAD, "size");
                        mv.visitInsn(DMUL);
                    }
                })
            );
    }

    @Transformer
    public void entityCollisions() {
        inClass("net.minecraft.entity.player.EntityPlayer")
          .patchMethod("updateSize", "func_184808_cD", "()V")
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertBefore(varInsn(FLOAD, 1)).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitHook(getSize);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(FSTORE, "size");
                    mv.visitVarInsn(FLOAD, 1); // local float f
                    mv.visitInsn(FMUL);
                    mv.visitVarInsn(FSTORE, 1); // local float f
                    mv.visitVarInsn(FLOAD, 2); // local float f1
                    mv.visitInsn(FMUL);
                    mv.visitVarInsn(FSTORE, 2); // local float f1
                })
            )
          .patchMethod("onLivingUpdate", "func_70636_d", "()V") // fixes collideEntityWithPlayer aabb expansion
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertBefore(jumpInsn(IFEQ)).nth(5).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                }).replaceAll(insn(DCONST_1)).code(mv -> mv.visitVarInsn(DLOAD, "size"))
                  .insertAfter(ldcInsn(0.5)).code(mv -> {
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
            );
        Label skipBlockCollision = new Label(); // lol, meh
        inClass("net.minecraft.entity.Entity")
          .patchMethod("onUpdate", "func_70071_h_", "()V") // hooking in updates of ALL entities. I feel so bad about this
            .with(patch()
                .insertAfter(methodBegin).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "updateSize", "(Lnet/minecraft/entity/Entity;)V", false);
                })
            )
          .patchMethod("isEntityInsideOpaqueBlock", "func_70094_T", "()Z")
            .with(patch()
                .insertAfter(ldcInsn(0.1F)).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityLivingBase this
                    mv.visitHook(getSize);
                    mv.visitInsn(FMUL);
                })
            )
          .patchMethod("doBlockCollisions", "func_145775_I", "()V")
            .with(patch()
                .insertBefore(varInsn(ALOAD, 8)).code(mv -> { // could've hooked in BlockPortal#onEntityCollidedWithBlock but this is more flexible
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitVarInsn(ALOAD, 8); // local IBlockState iblockstate
                    mv.visitVarInsn(ALOAD, 4); // local BlockPos blockpos$pooledmutableblockpos2
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "cancelBlockCollision", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;)Z", false);
                    mv.visitJumpInsn(IFNE, skipBlockCollision);
                }).insertAfter(methodInsn(INVOKEVIRTUAL, "net/minecraft/block/Block", "onEntityCollidedWithBlock", "func_180634_a", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V"))
                    .code(mv -> mv.visitLabel(skipBlockCollision))
            );
    }

    @Transformer
    public void reachDistance() {
        inClass("net.minecraft.client.multiplayer.PlayerControllerMP") // on client
          .patchMethod("getBlockReachDistance", "func_78757_d", "()F")
            .with(patch()
                .insertBefore(insn(FRETURN)).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // PlayerControllerMP this
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/client/multiplayer/PlayerControllerMP", "mc", "field_78776_a", "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitHook(getSize);
                    mv.visitHook(cutSmallerThanOne);
                    mv.visitInsn(FMUL);
                })
            );
        inClass("net.minecraft.server.management.PlayerInteractionManager") // on server - only increase reach to match client
          .patchMethod("getBlockReachDistance", "getBlockReachDistance", "()D") // lol
            .with(patch()
                .insertBefore(insn(DRETURN)).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // PlayerInteractionManager this
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/server/management/PlayerInteractionManager", "thisPlayerMP", "field_73090_b", "Lnet/minecraft/entity/player/EntityPlayerMP;");
                    mv.visitHook(getSize);
                    mv.visitHook(cutSmallerThanOne);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net.minecraft.network.NetHandlerPlayServer") // entity server reach
          .patchMethod("processUseEntity", "func_147340_a", "(Lnet/minecraft/network/play/client/CPacketUseEntity;)V")
            .with(patch()
                .insertAfter(varInsn(DLOAD, 5)).code(mv -> { // local double d0
                    mv.visitVarInsn(ALOAD, 3); // local Entity entity
                    mv.visitHook(getSize);
                    mv.visitHook(cutSmallerThanOne);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DMUL);
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net.minecraft.world.World") // when reach distance <= 1 this one return screws up raytracing (for other mods) a bit so here's a fix
          .patchMethod("rayTraceBlocks", "func_147447_a", "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;")
            .with(patch().replace(insn(ARETURN)).nth(3).code(mv -> mv.visitInsn(POP)));
    }

    @Transformer
    public void playerEyeHeight() {
        inClass("net.minecraft.entity.player.EntityPlayer")
          .patchMethod("getEyeHeight", "func_70047_e", "()F")
            .with(patch()
                .insertBefore(insn(FRETURN)).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitHook(getSize);
                    mv.visitInsn(FMUL);
                })
            );
    }

    @Transformer
    public void itemFixes() {
        inClass("net.minecraft.entity.item.EntityItem")
          .patchMethod("searchForOtherItemsNearby", "func_85054_d", "()V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertAfter(methodBegin).code(mv -> { // items stacking with each other
                    mv.visitVarInsn(ALOAD, 0); // EntityItem this
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                }).insertAfterAll(ldcInsn(0.5)).code(mv -> { // patch item combining search radius
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net.minecraft.client.particle.ParticleItemPickup")
          .patchConstructor("(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;F)V")
            .with(patch()
                .insertAfter(varInsn(FLOAD, 4)).code(mv -> { // first person pickup render
                    mv.visitVarInsn(ALOAD, 3); // param Entity target
                    mv.visitHook(getSize);
                    mv.visitInsn(FMUL);
                })
            );
        inClass("net.minecraft.entity.player.EntityPlayer")
          .patchMethod("dropItem", "func_146097_a", "(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;")
            .with(patch()
                .insertAfter(ldcInsn(0.30000001192092896)).code(mv -> { // player's item drop hardcoded height
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                })
            );
    }

    @Transformer
    public void beaconBaseColor() { // this is the coolest thing i did with asm so far :V
        inClass("net.minecraft.tileentity.TileEntityBeacon")
          .addField(ACC_PRIVATE, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;")
          .patchConstructor("()V")
            .with(patch()
                .insertAfter(varInsn(ALOAD, 0)).nth(2).code(mv -> {
                    mv.visitInsn(DUP);
                    mv.visitFieldInsn(GETSTATIC, "net/minecraft/item/EnumDyeColor", "WHITE", "WHITE", "Lnet/minecraft/item/EnumDyeColor;");
                    mv.visitFieldInsn(PUTFIELD, "net/minecraft/tileentity/TileEntityBeacon", "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                })
            )
          .patchMethod("updateSegmentColors", "func_146003_y", "()V")
            .with(patch()
                .replace(fieldInsn(GETSTATIC, "net/minecraft/item/EnumDyeColor", "WHITE", "WHITE", "Lnet/minecraft/item/EnumDyeColor;"))
                .code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/tileentity/TileEntityBeacon", "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                })
            )
          .patchMethod("readFromNBT", "func_145839_a", "(Lnet/minecraft/nbt/NBTTagCompound;)V")
            .with(patch()
                .insertAfter(methodBegin).code(mv -> {
                    mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                    mv.visitVarInsn(ALOAD, 1); // param NBTTagCompound compound
                    mv.visitLdcInsn("chiseled_me:color");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", "getByte", "func_150299_b", "(Ljava/lang/String;)B");
                    mv.visitMethodInsn(INVOKESTATIC, "net/minecraft/item/EnumDyeColor", "byMetadata", "func_176764_b", "(I)Lnet/minecraft/item/EnumDyeColor;");
                    mv.visitFieldInsn(PUTFIELD, "net/minecraft/tileentity/TileEntityBeacon", "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                })
            )
          .patchMethod("writeToNBT", "func_189515_b", "(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;")
            .with(patch()
                .insertAfter(methodBegin).code(mv -> {
                    mv.visitVarInsn(ALOAD, 1); // param NBTTagCompound compound
                    mv.visitLdcInsn("chiseled_me:color");
                    mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                    mv.visitFieldInsn(GETFIELD, "net/minecraft/tileentity/TileEntityBeacon", "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/item/EnumDyeColor", "getMetadata", "func_176765_a", "()I");
                    mv.visitInsn(I2B);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", "setByte", "func_74774_a", "(Ljava/lang/String;B)V");
                })
            );
    }
}