package com.yellowfire.faradayears.client.render;

import com.yellowfire.faradayears.ModAttachments;
import com.yellowfire.faradayears.capability.PlayerEarsTailData;
import com.yellowfire.faradayears.client.model.FaradayBodyModel;
import com.yellowfire.faradayears.client.model.FaradayEarsModel;
import com.yellowfire.faradayears.client.model.FaradayTailModel;
import com.yellowfire.faradayears.physics.TailPhysicsEngine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class EarsAndTailLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath("faradayears", "textures/entity/faraday_ears_tail.png");

    private final FaradayEarsModel earsModel;
    private final FaradayTailModel tailModel;
    private final FaradayBodyModel bodyModel;

    public EarsAndTailLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
        LayerDefinition earsDef = FaradayEarsModel.createLayer();
        this.earsModel = new FaradayEarsModel(earsDef.bakeRoot());

        LayerDefinition tailDef = FaradayTailModel.createLayer();
        this.tailModel = new FaradayTailModel(tailDef.bakeRoot());

        LayerDefinition bodyDef = FaradayBodyModel.createLayer();
        this.bodyModel = new FaradayBodyModel(bodyDef.bakeRoot());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) return;

        PlayerEarsTailData data = ModAttachments.get(player);
        {
            ResourceLocation texture = DEFAULT_TEXTURE;
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

            TailPhysicsEngine.INSTANCE.onRenderInterpolate(player, partialTick);
            TailPhysicsEngine.PlayerPhysicsData physicsData = TailPhysicsEngine.INSTANCE.getOrData(player);

            // ================== ФИГУРА, ГЕНДЕР И МЕШОЧЕК (Сливается со скином!) ==================
            if (data.getGender() > 0 || data.isShowPouch()) {
                poseStack.pushPose();
                getParentModel().body.translateAndRotate(poseStack);
                // ★ ТЕКСТУРА СКИНА ИГРОКА: используем скин игрока для 100% слияния со скином!
                VertexConsumer skinBuffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(player.getSkin().texture()));
                bodyModel.setupAndRender(poseStack, skinBuffer, packedLight, OverlayTexture.NO_OVERLAY,
                        data.getGender(), data.isShowChest(), data.isShowHips(), data.isShowShoulders(), data.isShowPouch(),
                        data.getChestScaleX(), data.getChestScaleY(), data.getChestScaleZ(), data.getChestOffsetY(), data.getChestOffsetZ(),
                        physicsData.smoothChestLeftJiggleY, physicsData.smoothChestLeftJiggleZ, physicsData.smoothChestRightJiggleY, physicsData.smoothChestRightJiggleZ,
                        data.getHipsScaleX(), data.getHipsScaleY(), data.getHipsScaleZ(), data.getHipsOffsetY(), data.getHipsOffsetZ(),
                        physicsData.smoothHipsLeftJiggleY, physicsData.smoothHipsLeftJiggleZ, physicsData.smoothHipsRightJiggleY, physicsData.smoothHipsRightJiggleZ,
                        data.getShouldersScaleX(), data.getShouldersScaleY(), data.getShouldersScaleZ(), data.getShouldersOffsetY(),
                        data.getPouchScaleX(), data.getPouchScaleY(), data.getPouchScaleZ(), data.getPouchOffsetX(), data.getPouchOffsetY(), data.getPouchOffsetZ(), physicsData.smoothPouchJiggleY, physicsData.smoothPouchJiggleZ,
                        0xFFFFFF);
                poseStack.popPose();
            }

            // ================== ОТРИСОВКА И ФИЗИКА УШЕК ==================
            if (data.isShowEars()) {
                poseStack.pushPose();

                ModelPart head = getParentModel().head;
                head.translateAndRotate(poseStack);

                poseStack.translate(data.getEarOffsetX() / 16.0f, data.getEarOffsetY() / 16.0f, data.getEarOffsetZ() / 16.0f);

                earsModel.setupCustomRotations(
                        data.getEarShape(),
                        data.getEarRotX() + physicsData.smoothLeftEarPitch,
                        data.getEarRotY(),
                        data.getEarRotZ() + physicsData.smoothLeftEarRoll,
                        data.getEarRotX() + physicsData.smoothRightEarPitch,
                        data.getEarRotY(),
                        data.getEarRotZ() + physicsData.smoothRightEarRoll,
                        data.getEarScaleX(), data.getEarScaleY(), data.getEarScaleZ()
                );

                earsModel.renderWithColorsAndShape(data.getEarShape(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, data.getEarColorPrimary(), data.getEarColorSecondary());

                poseStack.popPose();
            }

            // ================== ПРОЦЕДУРНЫЙ ХВОСТ ПО ФИЗИЧЕСКИМ ТОЧКАМ ==================
            if (data.isShowTail()) {
                ProceduralTailRenderer.render(poseStack, bufferSource, packedLight, player, physicsData, data, partialTick);
            }

            // ★ 3D-ХИТБОКСЫ НА F3 + B:
            if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() && physicsData.initialized) {
                poseStack.pushPose();
                double px = Mth.lerp(partialTick, player.xo, player.getX());
                double py = Mth.lerp(partialTick, player.yo, player.getY());
                double pz = Mth.lerp(partialTick, player.zo, player.getZ());

                poseStack.translate(0.0D, 1.501D, 0.0D);
                poseStack.scale(-1.0F, -1.0F, 1.0F);
                float bodyRot = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
                poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - bodyRot)));

                poseStack.translate(-px, -py, -pz);

                VertexConsumer lineBuffer = bufferSource.getBuffer(RenderType.lines());

                for (int t = 0; t < physicsData.activeTails; t++) {
                    TailPhysicsEngine.TailChainInstance inst = physicsData.tails[t];
                    for (int s = 0; s < physicsData.activeSegments; s++) {
                        AABB box = TailPhysicsEngine.getSegmentAABB(inst.worldX[s], inst.worldY[s], inst.worldZ[s], inst.worldRadius[s]);
                        float g = inst.isGround[s] ? 1.0f : 0.8f;
                        float r = inst.isGround[s] ? 1.0f : 0.2f;
                        LevelRenderer.renderLineBox(poseStack, lineBuffer, box, r, g, 0.2f, 1.0f);
                    }
                }

                for (int ear = 0; ear < 2; ear++) {
                    for (int s = 0; s < 3; s++) {
                        TailPhysicsEngine.EarHitbox hb = physicsData.earHitboxes[ear][s];
                        if (hb != null && hb.radius > 0.01D) {
                            AABB earBox = TailPhysicsEngine.getSegmentAABB(hb.worldX, hb.worldY, hb.worldZ, hb.radius);
                            float r = hb.collided ? 1.0f : 0.2f;
                            float g = hb.collided ? 0.2f : 0.9f;
                            LevelRenderer.renderLineBox(poseStack, lineBuffer, earBox, r, g, 1.0f, 1.0f);
                        }
                    }
                }

                poseStack.popPose();
            }
        }
    }
}
