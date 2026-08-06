package com.yellowfire.faradayears.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class FaradayTailModel extends Model {
    private final ModelPart[] tailRoots = new ModelPart[9];
    private final ModelPart[][] tailSegs = new ModelPart[9][6];
    private final ModelPart[] tailTipsRoots = new ModelPart[9];
    private final ModelPart[][] tailTips = new ModelPart[9][6];

    public FaradayTailModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        for (int t = 0; t < 9; t++) {
            this.tailRoots[t] = root.getChild("tailRoot_" + t);
            ModelPart curSeg = this.tailRoots[t];
            for (int s = 0; s < 6; s++) {
                if (s == 0) {
                    this.tailSegs[t][s] = curSeg;
                } else {
                    curSeg = curSeg.getChild("tailSeg_" + t + "_" + s);
                    this.tailSegs[t][s] = curSeg;
                }
            }

            this.tailTipsRoots[t] = root.getChild("tailTipRoot_" + t);
            ModelPart curTip = this.tailTipsRoots[t];
            for (int s = 0; s < 6; s++) {
                if (s == 0) {
                    this.tailTips[t][s] = curTip;
                } else {
                    curTip = curTip.getChild("tailTip_" + t + "_" + s);
                    this.tailTips[t][s] = curTip;
                }
            }
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        for (int t = 0; t < 9; t++) {
            // Сегмент 0 (Корень у поясницы): компактный 3.5x3.5x4, крепится точно к низу спины (Y = 10.0, Z = 2.0)
            PartDefinition curSeg = root.addOrReplaceChild("tailRoot_" + t,
                    CubeListBuilder.create().texOffs(0, 16).addBox(-1.75f, -1.75f, 0.0f, 3.5f, 3.5f, 4.0f),
                    PartPose.offsetAndRotation(0.0f, 10.0f, 2.0f, 0.0f, 0.0f, 0.0f));
            PartDefinition s1 = curSeg.addOrReplaceChild("tailSeg_" + t + "_1",
                    CubeListBuilder.create().texOffs(0, 16).addBox(-2.5f, -2.5f, 0.0f, 5.0f, 5.0f, 6.0f),
                    PartPose.offset(0.0f, 0.0f, 4.0f));
            PartDefinition s2 = s1.addOrReplaceChild("tailSeg_" + t + "_2",
                    CubeListBuilder.create().texOffs(0, 16).addBox(-2.5f, -2.5f, 0.0f, 5.0f, 5.0f, 6.0f),
                    PartPose.offset(0.0f, 0.0f, 6.0f));
            PartDefinition s3 = s2.addOrReplaceChild("tailSeg_" + t + "_3",
                    CubeListBuilder.create().texOffs(0, 16).addBox(-2.1f, -2.1f, 0.0f, 4.2f, 4.2f, 6.0f),
                    PartPose.offset(0.0f, 0.0f, 6.0f));
            PartDefinition s4 = s3.addOrReplaceChild("tailSeg_" + t + "_4",
                    CubeListBuilder.create().texOffs(0, 16).addBox(-1.6f, -1.6f, 0.0f, 3.2f, 3.2f, 5.0f),
                    PartPose.offset(0.0f, 0.0f, 6.0f));
            s4.addOrReplaceChild("tailSeg_" + t + "_5",
                    CubeListBuilder.create().texOffs(0, 16).addBox(-1.1f, -1.1f, 0.0f, 2.2f, 2.2f, 4.0f),
                    PartPose.offset(0.0f, 0.0f, 5.0f));

            PartDefinition curTip = root.addOrReplaceChild("tailTipRoot_" + t,
                    CubeListBuilder.create().texOffs(24, 16).addBox(-1.75f, -1.75f, 0.0f, 3.5f, 3.5f, 4.0f),
                    PartPose.offsetAndRotation(0.0f, 10.0f, 2.0f, 0.0f, 0.0f, 0.0f));
            PartDefinition t1 = curTip.addOrReplaceChild("tailTip_" + t + "_1",
                    CubeListBuilder.create().texOffs(24, 16).addBox(-2.5f, -2.5f, 0.0f, 5.0f, 5.0f, 6.0f),
                    PartPose.offset(0.0f, 0.0f, 4.0f));
            PartDefinition t2 = t1.addOrReplaceChild("tailTip_" + t + "_2",
                    CubeListBuilder.create().texOffs(24, 16).addBox(-2.5f, -2.5f, 0.0f, 5.0f, 5.0f, 6.0f),
                    PartPose.offset(0.0f, 0.0f, 6.0f));
            PartDefinition t3 = t2.addOrReplaceChild("tailTip_" + t + "_3",
                    CubeListBuilder.create().texOffs(24, 16).addBox(-2.1f, -2.1f, 0.0f, 4.2f, 4.2f, 6.0f),
                    PartPose.offset(0.0f, 0.0f, 6.0f));
            PartDefinition t4 = t3.addOrReplaceChild("tailTip_" + t + "_4",
                    CubeListBuilder.create().texOffs(24, 16).addBox(-1.6f, -1.6f, 0.0f, 3.2f, 3.2f, 5.0f),
                    PartPose.offset(0.0f, 0.0f, 6.0f));
            t4.addOrReplaceChild("tailTip_" + t + "_5",
                    CubeListBuilder.create().texOffs(24, 16).addBox(-1.1f, -1.1f, 0.0f, 2.2f, 2.2f, 4.0f),
                    PartPose.offset(0.0f, 0.0f, 5.0f));
        }

        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * ПРИМЕНЕНИЕ ЕДИНЫХ, ТОЧНЫХ УГЛОВ СУСТАВОВ (Strict Unified Joint Rotations).
     * Визуальная модель на 100% использует углы segPitch[s], segYaw[s], segRoll[s],
     * рассчитанные единым физическим движком. Гарантирует 100% совпадение с зелёными хитбоксами на F3+B!
     */
    public void applyTrueWorldPhysicsRotations(int numTails, float fanSpreadDeg, int numSegments, float segLength, float taper,
                                               float scaleX, float scaleY, float scaleZ,
                                               float[] segPitch, float[] segYaw, float[] segRoll,
                                               boolean isRestingOnGround) {
        boolean isSuperVolumetric = (numTails == 10);
        int activeTails = isSuperVolumetric ? 4 : Math.max(1, Math.min(9, numTails));
        int activeSegs = Math.max(1, Math.min(6, numSegments));
        float fanRad = fanSpreadDeg * ((float) Math.PI / 180.0f);

        for (int t = 0; t < 9; t++) {
            if (t < activeTails) {
                this.tailRoots[t].visible = true;
                this.tailTipsRoots[t].visible = true;

                float fanOffsetRadY = 0.0f;
                float fanPitchOffset = 0.0f;
                float fanRollOffset = 0.0f;
                float layerScaleMult = 1.0f;

                if (isSuperVolumetric) {
                    if (t == 1) { fanOffsetRadY = 0.038f; fanRollOffset = 0.035f; layerScaleMult = 0.94f; }
                    else if (t == 2) { fanOffsetRadY = -0.038f; fanRollOffset = -0.035f; layerScaleMult = 0.94f; }
                    else if (t == 3) { fanPitchOffset = 0.035f; layerScaleMult = 0.90f; }
                } else if (activeTails > 1) {
                    float step = fanRad / (activeTails - 1);
                    fanOffsetRadY = (t - (activeTails - 1) / 2.0f) * step;
                    fanPitchOffset = -Math.abs(fanOffsetRadY) * 0.30f;
                }

                // 1. Корневой сустав у поясницы (s = 0) строго получает физический угол segPitch[0]
                for (ModelPart rootPart : new ModelPart[]{this.tailRoots[t], this.tailTipsRoots[t]}) {
                    rootPart.xRot = segPitch[0] + fanPitchOffset;
                    rootPart.yRot = segYaw[0] + fanOffsetRadY;
                    rootPart.zRot = segRoll[0] + fanRollOffset + fanOffsetRadY * 0.15f;
                    rootPart.xScale = scaleX * layerScaleMult;
                    rootPart.yScale = scaleY * layerScaleMult;
                    rootPart.zScale = scaleZ;
                }

                // 2. Последующие сегменты (s = 1..5) получают строго углы segPitch[s] и segYaw[s] без искажений!
                for (int s = 1; s < 6; s++) {
                    boolean segVis = (s < activeSegs);
                    this.tailSegs[t][s].visible = segVis;
                    this.tailTips[t][s].visible = segVis;
                    if (segVis) {
                        for (ModelPart p : new ModelPart[]{this.tailSegs[t][s], this.tailTips[t][s]}) {
                            p.z = (s == 1) ? 4.0f : segLength;
                            p.xRot = segPitch[s];
                            p.yRot = segYaw[s];
                            p.zRot = segRoll[s];
                            p.xScale = taper;
                            p.yScale = taper;
                            p.zScale = 1.0f;
                        }
                    }
                }
            } else {
                this.tailRoots[t].visible = false;
                this.tailTipsRoots[t].visible = false;
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        for (int t = 0; t < 9; t++) {
            if (tailRoots[t].visible) tailRoots[t].render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    public void renderWithColorsAndSegments(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int primaryRgb, int secondaryRgb, int numTails, int numSegments) {
        float r1 = ((primaryRgb >> 16) & 0xFF) / 255.0f;
        float g1 = ((primaryRgb >> 8) & 0xFF) / 255.0f;
        float b1 = (primaryRgb & 0xFF) / 255.0f;

        float r2 = ((secondaryRgb >> 16) & 0xFF) / 255.0f;
        float g2 = ((secondaryRgb >> 8) & 0xFF) / 255.0f;
        float b2 = (secondaryRgb & 0xFF) / 255.0f;

        boolean isSuperVolumetric = (numTails == 10);
        int activeTails = isSuperVolumetric ? 4 : Math.max(1, Math.min(9, numTails));
        int activeSegs = Math.max(1, Math.min(6, numSegments));

        for (int t = 0; t < activeTails; t++) {
            if (activeSegs > 1) {
                for (int s = 0; s < activeSegs; s++) {
                    tailSegs[t][s].visible = (s < activeSegs - 1);
                }
                tailRoots[t].render(poseStack, buffer, packedLight, packedOverlay, r1, g1, b1, 1.0f);
            }
        }

        for (int t = 0; t < activeTails; t++) {
            for (int s = 0; s < activeSegs; s++) {
                tailTips[t][s].visible = (s == activeSegs - 1);
            }
            tailTipsRoots[t].render(poseStack, buffer, packedLight, packedOverlay, r2, g2, b2, 1.0f);
        }

        for (int t = 0; t < activeTails; t++) {
            for (int s = 0; s < activeSegs; s++) {
                tailSegs[t][s].visible = true;
                tailTips[t][s].visible = true;
            }
        }
    }
}
