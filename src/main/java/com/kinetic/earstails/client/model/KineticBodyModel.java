package com.kinetic.earstails.client.model;

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

/**
 * Volumetric 3D Figure & Pouch Model (`v51-skin-blended`).
 * 1) Renders customizable 3D volumetric chest (`chestLeft, chestRight`) and hips (`hipsLeft, hipsRight`),
 *    and front belt pouch (`pouchFront`) sampling from exact player skin UV coordinates (`texOffs(20,20) / texOffs(8,20)`),
 *    guaranteeing that they seamlessly act as a continuation of the player's own skin/clothes!
 * 2) Dynamic jiggle & momentum physics on all components.
 */
public class KineticBodyModel extends Model {
    private final ModelPart chestLeft, chestRight, hipsLeft, hipsRight, shouldersLeft, shouldersRight, pouchFront;

    public KineticBodyModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.chestLeft = root.getChild("chestLeft");
        this.chestRight = root.getChild("chestRight");
        this.hipsLeft = root.getChild("hipsLeft");
        this.hipsRight = root.getChild("hipsRight");
        this.shouldersLeft = root.getChild("shouldersLeft");
        this.shouldersRight = root.getChild("shouldersRight");
        this.pouchFront = root.getChild("pouchFront");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Грудь спереди на торсе (texOffs(20, 20) — берёт текстуру передней части футболки скина!)
        root.addOrReplaceChild("chestLeft",
                CubeListBuilder.create().texOffs(20, 20).addBox(0.1f, 2.2f, -4.6f, 3.8f, 3.6f, 2.6f),
                PartPose.ZERO);
        root.addOrReplaceChild("chestRight",
                CubeListBuilder.create().texOffs(20, 20).addBox(-3.9f, 2.2f, -4.6f, 3.8f, 3.6f, 2.6f),
                PartPose.ZERO);

        // Бёдра / Попа сзади на тазу (texOffs(8, 20) — берёт текстуру штанов/пояса скина!)
        root.addOrReplaceChild("hipsLeft",
                CubeListBuilder.create().texOffs(8, 20).addBox(0.1f, 8.4f, 1.8f, 3.9f, 3.6f, 2.6f),
                PartPose.ZERO);
        root.addOrReplaceChild("hipsRight",
                CubeListBuilder.create().texOffs(8, 20).addBox(-4.0f, 8.4f, 1.8f, 3.9f, 3.6f, 2.6f),
                PartPose.ZERO);

        // Мужской / Атлетический верхний торс (texOffs(20, 20))
        root.addOrReplaceChild("shouldersLeft",
                CubeListBuilder.create().texOffs(20, 20).addBox(2.8f, 0.5f, -2.4f, 2.2f, 4.8f, 4.8f),
                PartPose.ZERO);
        root.addOrReplaceChild("shouldersRight",
                CubeListBuilder.create().texOffs(20, 20).addBox(-5.0f, 0.5f, -2.4f, 2.2f, 4.8f, 4.8f),
                PartPose.ZERO);

        // Декоративный поясной мешочек спереди (texOffs(20, 28) — берёт текстуру ремня спереди на скине!)
        // Центрируем бокс относительно точки крепления на ремне (Y=10.5f, Z=-2.1f), чтобы масштабирование (scale) происходило вокруг центра талии и не сдвигало мешочек в грудь:
        root.addOrReplaceChild("pouchFront",
                CubeListBuilder.create().texOffs(20, 28).addBox(-2.5f, -1.75f, -2.1f, 5.0f, 3.5f, 2.1f),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setupAndRender(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               int gender, boolean showChest, boolean showHips, boolean showShoulders, boolean showPouch,
                               float chestSX, float chestSY, float chestSZ, float chestOY, float chestOZ,
                               float jiggleCLY, float jiggleCLZ, float jiggleCRY, float jiggleCRZ,
                               float hipsSX, float hipsSY, float hipsSZ, float hipsOY, float hipsOZ,
                               float jiggleHLY, float jiggleHLZ, float jiggleHRY, float jiggleHRZ,
                               float shouldersSX, float shouldersSY, float shouldersSZ, float shouldersOY,
                               float pouchSX, float pouchSY, float pouchSZ, float pouchOX, float pouchOY, float pouchOZ, float jigglePY, float jigglePZ,
                               int colorRgb) {
        int color = 0xFF000000 | (colorRgb & 0xFFFFFF);

        if ((gender == 1 || gender == 3) && showChest) {
            poseStack.pushPose();
            poseStack.translate(0.0f, (chestOY + jiggleCLY) / 16.0f, (chestOZ + jiggleCLZ) / 16.0f);
            poseStack.scale(chestSX, chestSY, chestSZ);
            chestLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0.0f, (chestOY + jiggleCRY) / 16.0f, (chestOZ + jiggleCRZ) / 16.0f);
            poseStack.scale(chestSX, chestSY, chestSZ);
            chestRight.render(poseStack, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }

        if ((gender == 1 || gender == 3) && showHips) {
            poseStack.pushPose();
            poseStack.translate(0.0f, (hipsOY + jiggleHLY) / 16.0f, (hipsOZ + jiggleHLZ) / 16.0f);
            poseStack.scale(hipsSX, hipsSY, hipsSZ);
            hipsLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0.0f, (hipsOY + jiggleHRY) / 16.0f, (hipsOZ + jiggleHRZ) / 16.0f);
            poseStack.scale(hipsSX, hipsSY, hipsSZ);
            hipsRight.render(poseStack, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }

        if ((gender == 2 || gender == 3) && showShoulders) {
            poseStack.pushPose();
            poseStack.translate(0.0f, shouldersOY / 16.0f, 0.0f);
            poseStack.scale(shouldersSX, shouldersSY, shouldersSZ);
            shouldersLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
            shouldersRight.render(poseStack, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }

        if (showPouch) {
            poseStack.pushPose();
            poseStack.translate(pouchOX / 16.0f, (10.5f + pouchOY + jigglePY) / 16.0f, (-2.1f + pouchOZ + jigglePZ) / 16.0f);
            poseStack.scale(pouchSX, pouchSY, pouchSZ);
            pouchFront.render(poseStack, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        chestLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        chestRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        hipsLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        hipsRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        shouldersLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        shouldersRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        pouchFront.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
