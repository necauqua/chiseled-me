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

package necauqua.mods.cm.asm;

import necauqua.mods.cm.asm.dsl.AsmMethodHook;
import necauqua.mods.cm.asm.dsl.PatchDsl;
import necauqua.mods.cm.asm.dsl.Transformer;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import static necauqua.mods.cm.asm.dsl.ASM.*;
import static org.objectweb.asm.Opcodes.*;

// intellij sees to many random duplicates in the hooks
@SuppressWarnings("Duplicates")
public class Transformers {

    private static final AsmMethodHook getSize =
        mv -> mv.visitMethodInsn(INVOKESTATIC,
            "necauqua/mods/cm/Hooks",
            "getSize",
            "(Lnet/minecraft/entity/Entity;)F",
            false);

    private static final AsmMethodHook getRenderSize =
        mv -> mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks",
            "getRenderSize",
            "(Lnet/minecraft/entity/Entity;F)F",
            false);

    private static final AsmMethodHook thePlayer =
        mv -> mv.visitFieldInsn(GETFIELD,
            "net/minecraft/client/Minecraft",
            srg("player", "Minecraft"),
            "Lnet/minecraft/client/entity/EntityPlayerSP;");

    private static final AsmMethodHook scale =
        mv -> mv.visitMethodInsn(INVOKESTATIC,
            "net/minecraft/client/renderer/GlStateManager",
            srg("scale", "GlStateManager"),
            "(FFF)V",
            false);

    private static final AsmMethodHook cutBiggerThanOne = cut(FCMPL, IFLE);
    private static final AsmMethodHook cutSmallerThanOne = cut(FCMPG, IFGE);

    private static AsmMethodHook cut(int compareInsn, int jumpInsn) {
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
    public void cameraView() {
        inClass("net.minecraft.client.renderer.EntityRenderer")
            .patchMethod(srg("orientCamera"), "(F)V") // camera height & shift
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertBefore(varInsn(FSTORE, 3), mv -> { // local float f
                    mv.visitInsn(POP); // meh, just ignore original getEyeHeight call
                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitVarInsn(FLOAD, 1); // local float partialTicks
                    mv.visitHook(getRenderSize);
                    mv.visitVarInsn(FSTORE, "size");
                    mv.visitVarInsn(ALOAD, 2); // local Entity entity
                    mv.visitVarInsn(FLOAD, 1); // local float partialTicks
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "getScreenInterpEyeHeight", "(Lnet/minecraft/entity/Entity;F)F", false);
                })
                .insertBefore(varInsn(DSTORE, 12), mv -> { // local double d3
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                })
                .insertAfterAll(ldcInsn(0.1F), mv -> { // f5 fix
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                })
                .insertAfter(ldcInsn(0.05F), mv -> { // forward cam shift fix
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                })
            )
            .patchMethod(srg("setupCameraTransform"), "(FI)V")
            .with(patch()
                    .addLocal("size", Type.FLOAT_TYPE)
                    .insertAfter(methodBegin(), mv -> {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, srg("mc", "EntityRenderer"), "Lnet/minecraft/client/Minecraft;");
                        mv.visitHook(thePlayer);
                        mv.visitVarInsn(FLOAD, 1);
                        mv.visitHook(getRenderSize);
                        mv.visitVarInsn(FSTORE, "size");
                    })
                    .insertAfter(insn(I2F), mv -> { // far plane decrease (closer fog)
                        mv.visitVarInsn(FLOAD, "size");
                        mv.visitHook(cutBiggerThanOne);
                        mv.visitInsn(F2D);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
                        mv.visitInsn(D2F);
                        mv.visitInsn(FMUL);
                    })

                    // TODO below hook makes fov more natural, but also messes up chunk render clipping somehow
//                .insertBefore(varInsn(ALOAD, 0), 4, mv -> { // scaling the projection matrix to make fov feel natural
//                    mv.visitInsn(FCONST_1);
//                    mv.visitVarInsn(FLOAD, "size");
//                    mv.visitInsn(FDIV);
//                    mv.visitInsn(DUP);
//                    mv.visitInsn(FCONST_1);
//                    mv.visitHook(scale);
//                })
                    .insertAfterAll(ldcInsn(0.05F), mv -> { // camera offset fix
                        mv.visitVarInsn(FLOAD, "size");
                        mv.visitInsn(FMUL);
                    })
            )
            .patchMethod(srg("applyBobbing"), "(F)V") // bobbing fix (-_-)
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertBefore(varInsn(ASTORE, 2), mv -> { // local EntityPlayer entityplayer
                    mv.visitInsn(DUP);
                    mv.visitHook(getSize);
                    mv.visitVarInsn(FSTORE, "size");
                })
                .insertBefore(varInsn(FSTORE, 4), mv -> {
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FDIV);
                })
                .insertAfterAll(varInsn(FLOAD, 5), mv -> { // local float f2
                    if (mv.getPass() <= 2) { // only first two
                        mv.visitVarInsn(FLOAD, "size");
                        mv.visitInsn(FMUL);
                    }
                })
            )
            .patchMethod(srg("renderWorldPass"), "(IFJ)V") // these three are clipping
            .and(srg("renderCloudsCheck"), "(Lnet/minecraft/client/renderer/RenderGlobal;FI)V")
            .and(srg("renderHand", "EntityRenderer"), "(FI)V")
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, srg("mc", "EntityRenderer"), "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitHook(getSize);
                    mv.visitHook(cutBiggerThanOne);
                    mv.visitVarInsn(FSTORE, "size");
                })
                .insertAfterAll(ldcInsn(0.05F), mv -> {
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                })
            );
    }

    @Transformer
    public void entityRender() {
        inClass("net.minecraft.client.renderer.entity.RenderManager")
            .patchMethod(srg("doRenderEntity"), "(Lnet/minecraft/entity/Entity;DDDFFZ)V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertBefore(varInsn(ALOAD, 11), 2, mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 9); // param float partialTicks
                    mv.visitHook(getRenderSize);
                    mv.visitInsn(DUP);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                })
                .insertAfter(varInsn(DLOAD, 2), mv -> { // param double x
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                })
                .insertAfter(varInsn(DLOAD, 4), mv -> { // param double y
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                })
                .insertAfter(varInsn(DLOAD, 6), mv -> { // param double z
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                })
                .insertBefore(jumpInsn(GOTO), mv -> {
                    mv.visitInsn(FCONST_1);
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(D2F);
                    mv.visitInsn(FDIV);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    mv.visitHook(scale);
                })
            )
            .patchMethod(srg("renderDebugBoundingBox"), "(Lnet/minecraft/entity/Entity;DDDFF)V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                })
                .insertAfterAll(ldcInsn(2.0), mv -> { // length of blue 'eye sight' vector
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
                .insertAfterAll(ldcInsn(0.009999999776482582), mv -> { // height of red 'eye heigth' box
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net.minecraft.client.renderer.entity.Render")
            .patchMethod(srg("renderShadow", "Render"), "(Lnet/minecraft/entity/Entity;DDDFF)V")
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 1); // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 9); // param float partialTicks
                    mv.visitHook(getRenderSize);
                    mv.visitVarInsn(FSTORE, "size");
                })
                .insertBefore(varInsn(FSTORE, 11), mv -> { // local float f
                    mv.visitVarInsn(FLOAD, "size");
                    mv.visitInsn(FMUL);
                })
                .insertBefore(varInsn(DSTORE, 27), mv -> { // local double d3
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
            .patchMethod(srg("renderLivingLabel"), "(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V")
            .with(patch()
                .replace(varInsn(FLOAD, 16), mv -> {
                    mv.visitVarInsn(ALOAD, 1);  // param Entity entityIn
                    mv.visitVarInsn(FLOAD, 16);
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "getLabelHeight", "(Lnet/minecraft/entity/Entity;F)F", false);
                })
            );
        PatchDsl renderDistPatch = patch()
            .insertBefore(varInsn(DSTORE, 3), 3, mv -> {
                mv.visitVarInsn(ALOAD, 0); // Entity this
                mv.visitHook(getSize);
                mv.visitHook(cutBiggerThanOne);
                mv.visitInsn(F2D);
                mv.visitInsn(DDIV);
            });
        inClass("net.minecraft.entity.Entity")
            .patchMethodOptionally(srg("isInRangeToRenderDist", "EntityOtherPlayerMP"), "(D)Z")
            .with(renderDistPatch);
        inClass("net.minecraft.client.entity.EntityOtherPlayerMP") // because stupid EntityOtherPlayerMP
            .patchMethod(srg("isInRangeToRenderDist", "EntityOtherPlayerMP"), "(D)Z")
            .with(renderDistPatch);
        inClass("net.minecraft.client.gui.inventory.GuiInventory")
            .patchMethod(srg("drawEntityOnScreen"), "(IIIFFLnet/minecraft/entity/EntityLivingBase;)V")
            .with(patch()
                .insertAfter(varInsn(FSTORE, 10), mv -> { // local float f4
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
            .patchMethod(srg("move", "Entity"), "(DDD)V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertAfter(methodBegin(), mv -> { // the speed
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
                })
                .insertAfterAll(ldcInsn(0.05), mv -> { // shifting on edges of aabb's
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
                .insertBefore(varInsn(DSTORE, 3), 3, mv -> { // stepHeight
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
            )
            .patchMethod(srg("createRunningParticles"), "()V")
            .with(patch()
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "cancelRunningParticlesHook", "(Lnet/minecraft/entity/Entity;)Z", false);
                    Label skipReturn = new Label();
                    mv.visitJumpInsn(IFEQ, skipReturn);
                    mv.visitInsn(RETURN);
                    mv.visitLabel(skipReturn);
                })
            );
        PatchDsl libmSwingAnimation = patch()
            .insertAfter(ldcInsn(4.0F), mv -> { // fix for limb swing animation
                mv.visitVarInsn(ALOAD, 0);  // EntityLivingBase this
                mv.visitHook(getSize);
                mv.visitInsn(FDIV);
            });
        inClass("net.minecraft.entity.EntityLivingBase")
            .patchMethod(srg("moveEntityWithHeading", "EntityLivingBase"), "(FF)V")
            .with(libmSwingAnimation);
        inClass("net.minecraft.client.entity.EntityOtherPlayerMP") // because stupid EntityOtherPlayerMP x 2
            .patchMethod(srg("onUpdate", "EntityOtherPlayerMP"), "()V")
            .with(libmSwingAnimation);
    }

    @Transformer
    public void serverMotionFixes() {
        inClass("net.minecraft.client.entity.EntityPlayerSP")
            .patchMethod(srg("onUpdateWalkingPlayer"), "()V")
            .with(patch()
                .insertAfter(ldcInsn(0.0009), mv -> {
                    mv.visitVarInsn(ALOAD, 0);  // EntityPlayerSP this
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DMUL);
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net.minecraft.entity.EntityTrackerEntry")
            .patchMethod(srg("updatePlayerList", "EntityTrackerEntry"), "(Ljava/util/List;)V")
            .with(patch()
                .replace(ldcInsn(128L), mv -> {
                    mv.visitLdcInsn(128.0F);
                    mv.visitVarInsn(ALOAD, 0); // EntityTrackerEntry this
                    mv.visitFieldInsn(GETFIELD, srg("trackedEntity"), "Lnet/minecraft/entity/Entity;");
                    mv.visitHook(getSize);
                    mv.visitInsn(DUP);  //
                    mv.visitInsn(FMUL); // bytecode squaring <3
                    mv.visitInsn(FMUL);
                    mv.visitInsn(F2L);
                })
            );
        inClass("net.minecraft.network.NetHandlerPlayServer")
            .patchMethod(srg("processPlayer", "NetHandlerPlayServer"), "(Lnet/minecraft/network/play/client/CPacketPlayer;)V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertBefore(varInsn(ALOAD, 2), mv -> { // setup
                    mv.visitVarInsn(ALOAD, 0); // NetHandlerPlayServer this
                    mv.visitFieldInsn(GETFIELD, "playerEntity", "Lnet/minecraft/entity/player/EntityPlayerMP;");
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                })
                .insertAfter(varInsn(DLOAD, 19), 4, mv -> { // local double d7
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                })
                .insertAfter(varInsn(DLOAD, 21), 5, mv -> { // local double d8
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                })
                .insertAfter(varInsn(DLOAD, 23), 4, mv -> { // local double d9
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DDIV);
                })
                .insertAfterAll(ldcInsn(0.0625), mv -> { // fix for small aabbs
                    if (mv.getPass() != 2) { // dont change movement correctness checker
                        mv.visitVarInsn(DLOAD, "size");
                        mv.visitInsn(DMUL);
                    }
                })
            );
    }

    @Transformer
    public void entityCollisions() {
        inClass("net.minecraft.entity.player.EntityPlayer")
            .patchMethod(srg("updateSize", "EntityPlayer"), "()V")
            .with(patch()
                .addLocal("size", Type.FLOAT_TYPE)
                .insertBefore(varInsn(FLOAD, 1), mv -> {
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
            .patchMethod(srg("onLivingUpdate", "EntityPlayer"), "()V") // fixes collideEntityWithPlayer aabb expansion
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertBefore(jumpInsn(IFEQ), 5, mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                })
                .replaceAll(insn(DCONST_1), mv -> mv.visitVarInsn(DLOAD, "size"))
                .insertAfter(ldcInsn(0.5), mv -> {
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
            );
        Label skipBlockCollision = new Label(); // lol, meh
        inClass("net.minecraft.entity.Entity")
            .patchMethod(srg("onUpdate", "Entity"), "()V") // hooking in updates of ALL entities. I feel so bad about this
            .with(patch()
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "updateSize", "(Lnet/minecraft/entity/Entity;)V", false);
                })
            )
            .patchMethod(srg("isEntityInsideOpaqueBlock", "Entity"), "()Z")
            .with(patch()
                .insertAfter(ldcInsn(0.1F), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityLivingBase this
                    mv.visitHook(getSize);
                    mv.visitInsn(FMUL);
                })
            )
            .patchMethod(srg("doBlockCollisions"), "()V")
            .with(patch()
                .insertBefore(varInsn(ALOAD, 8), mv -> { // could've hooked in BlockPortal#onEntityCollidedWithBlock but this is more flexible
                    mv.visitVarInsn(ALOAD, 0); // Entity this
                    mv.visitVarInsn(ALOAD, 8); // local IBlockState iblockstate
                    mv.visitVarInsn(ALOAD, 4); // local BlockPos blockpos$pooledmutableblockpos2
                    mv.visitMethodInsn(INVOKESTATIC, "necauqua/mods/cm/Hooks", "cancelBlockCollision", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;)Z", false);
                    mv.visitJumpInsn(IFNE, skipBlockCollision);
                })
                .insertAfter(methodInsn(INVOKEVIRTUAL, "net/minecraft/block/Block", srg("onEntityCollidedWithBlock", "Block"), "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V"),
                    mv -> mv.visitLabel(skipBlockCollision))
            );
    }

    @Transformer
    public void reachDistance() {
        inClass("net.minecraft.client.multiplayer.PlayerControllerMP") // on client
            .patchMethod(srg("getBlockReachDistance"), "()F")
            .with(patch()
                .insertBefore(insn(FRETURN), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // PlayerControllerMP this
                    mv.visitFieldInsn(GETFIELD, srg("mc", "PlayerControllerMP"), "Lnet/minecraft/client/Minecraft;");
                    mv.visitHook(thePlayer);
                    mv.visitHook(getSize);
                    mv.visitHook(cutSmallerThanOne);
                    mv.visitInsn(FMUL);
                })
            );
        inClass("net.minecraft.server.management.PlayerInteractionManager") // on server - only increase reach to match client
            .patchMethod(srg("getBlockReachDistance"), "()D") // lol
            .with(patch()
                .insertBefore(insn(DRETURN), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // PlayerInteractionManager this
                    mv.visitFieldInsn(GETFIELD, srg("player", "PlayerInteractionManager"), "Lnet/minecraft/entity/player/EntityPlayerMP;");
                    mv.visitHook(getSize);
                    mv.visitHook(cutSmallerThanOne);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net.minecraft.network.NetHandlerPlayServer") // entity server reach
            .patchMethod(srg("processUseEntity", "NetHandlerPlayServer"), "(Lnet/minecraft/network/play/client/CPacketUseEntity;)V")
            .with(patch()
                .insertAfter(varInsn(DLOAD, 5), mv -> { // local double d0
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
            .patchMethod(srg("rayTraceBlocks", "World"), "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;")
            .with(patch().replace(insn(ARETURN), 3, mv -> mv.visitInsn(POP)));
    }

    @Transformer
    public void playerEyeHeight() {
        inClass("net.minecraft.entity.player.EntityPlayer")
            .patchMethod(srg("getEyeHeight", "EntityPlayer"), "()F")
            .with(patch()
                .insertBefore(insn(FRETURN), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitHook(getSize);
                    mv.visitInsn(FMUL);
                })
            );
    }

    @Transformer
    public void itemFixes() {
        inClass("net.minecraft.entity.item.EntityItem")
            .patchMethod(srg("searchForOtherItemsNearby"), "()V")
            .with(patch()
                .addLocal("size", Type.DOUBLE_TYPE)
                .insertAfter(methodBegin(), mv -> { // items stacking with each other
                    mv.visitVarInsn(ALOAD, 0); // EntityItem this
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitVarInsn(DSTORE, "size");
                })
                .insertAfterAll(ldcInsn(0.5), mv -> { // patch item combining search radius
                    mv.visitVarInsn(DLOAD, "size");
                    mv.visitInsn(DMUL);
                })
            );
        inClass("net.minecraft.client.particle.ParticleItemPickup")
            .patchConstructor("(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;F)V")
            .with(patch()
                .insertAfter(varInsn(FLOAD, 4), mv -> { // first person pickup render
                    mv.visitVarInsn(ALOAD, 3); // param Entity target
                    mv.visitHook(getSize);
                    mv.visitInsn(FMUL);
                })
            );
        inClass("net.minecraft.entity.player.EntityPlayer")
            .patchMethod(srg("dropItem", "EntityPlayer"), "(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;")
            .with(patch()
                .insertAfter(ldcInsn(0.30000001192092896), mv -> { // player's item drop hardcoded height
                    mv.visitVarInsn(ALOAD, 0); // EntityPlayer this
                    mv.visitHook(getSize);
                    mv.visitInsn(F2D);
                    mv.visitInsn(DMUL);
                })
            );
    }

    @Transformer
    public void beaconBaseColor() { // this is the coolest thing i did with asm so far :V
        String WHITE = srg("WHITE", "EnumDyeColor");
        inClass("net.minecraft.tileentity.TileEntityBeacon")
            .addField(ACC_PRIVATE, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;")
            .patchConstructor("()V")
            .with(patch()
                .insertAfter(varInsn(ALOAD, 0), 2, mv -> {
                    mv.visitInsn(DUP);
                    mv.visitFieldInsn(GETSTATIC, "net/minecraft/item/EnumDyeColor", WHITE, "Lnet/minecraft/item/EnumDyeColor;");
                    mv.visitFieldInsn(PUTFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                })
            )
            .patchMethod(srg("updateSegmentColors"), "()V")
            .with(patch()
                .replace(fieldInsn(GETSTATIC, "net/minecraft/item/EnumDyeColor", WHITE, "Lnet/minecraft/item/EnumDyeColor;"), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                    mv.visitFieldInsn(GETFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                })
            )
            .patchMethod(srg("readFromNBT", "TileEntityBeacon"), "(Lnet/minecraft/nbt/NBTTagCompound;)V")
            .with(patch()
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                    mv.visitVarInsn(ALOAD, 1); // param NBTTagCompound compound
                    mv.visitLdcInsn("chiseled_me:color");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", srg("getByte", "NBTTagCompound"), "(Ljava/lang/String;)B", false);
                    mv.visitMethodInsn(INVOKESTATIC, "net/minecraft/item/EnumDyeColor", srg("byMetadata", "EnumDyeColor"), "(I)Lnet/minecraft/item/EnumDyeColor;", false);
                    mv.visitFieldInsn(PUTFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                })
            )
            .patchMethod(srg("writeToNBT", "TileEntityBeacon"), "(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;")
            .with(patch()
                .insertAfter(methodBegin(), mv -> {
                    mv.visitVarInsn(ALOAD, 1); // param NBTTagCompound compound
                    mv.visitLdcInsn("chiseled_me:color");
                    mv.visitVarInsn(ALOAD, 0); // TileEntityBeacon this
                    mv.visitFieldInsn(GETFIELD, "$cm_baseColor", "Lnet/minecraft/item/EnumDyeColor;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/item/EnumDyeColor", srg("getMetadata", "EnumDyeColor"), "()I", false);
                    mv.visitInsn(I2B);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", srg("setByte", "NBTTagCompound"), "(Ljava/lang/String;B)V", false);
                })
            );
    }
}
