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
 * Realistic 3D Volumetric Ears (`v48-unified-uv-64x64`).
 * Perfectly maps all 5 ear outer shells to `Y = 0..15`, inner cavities & tufts to `Y = 16..31`,
 * and Ribbon Cat ribbon bows to `texOffs(48, 16)`, matching our unified 64x64 skin UV template!
 */
public class KineticEarsModel extends Model {
    private final ModelPart s0LeftEar, s0RightEar, s0LeftTuft, s0RightTuft;
    private final ModelPart s1LeftEar, s1RightEar, s1LeftInner, s1RightInner, s1LeftBow, s1RightBow;
    private final ModelPart s2LeftEar, s2RightEar, s2LeftInner, s2RightInner;
    private final ModelPart s3LeftEar, s3RightEar, s3LeftInner, s3RightInner;
    private final ModelPart s4LeftEar, s4RightEar, s4LeftInner, s4RightInner;

    public KineticEarsModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.s0LeftEar = root.getChild("s0LeftEar");
        this.s0RightEar = root.getChild("s0RightEar");
        this.s0LeftTuft = this.s0LeftEar.getChild("s0LeftTuft");
        this.s0RightTuft = this.s0RightEar.getChild("s0RightTuft");

        this.s1LeftEar = root.getChild("s1LeftEar");
        this.s1RightEar = root.getChild("s1RightEar");
        this.s1LeftInner = this.s1LeftEar.getChild("s1LeftInner");
        this.s1RightInner = this.s1RightEar.getChild("s1RightInner");
        this.s1LeftBow = this.s1LeftEar.getChild("s1LeftBow");
        this.s1RightBow = this.s1RightEar.getChild("s1RightBow");

        this.s2LeftEar = root.getChild("s2LeftEar");
        this.s2RightEar = root.getChild("s2RightEar");
        this.s2LeftInner = this.s2LeftEar.getChild("s2LeftInner");
        this.s2RightInner = this.s2RightEar.getChild("s2RightInner");

        this.s3LeftEar = root.getChild("s3LeftEar");
        this.s3RightEar = root.getChild("s3RightEar");
        this.s3LeftInner = this.s3LeftEar.getChild("s3LeftInner");
        this.s3RightInner = this.s3RightEar.getChild("s3RightInner");

        this.s4LeftEar = root.getChild("s4LeftEar");
        this.s4RightEar = root.getChild("s4RightEar");
        this.s4LeftInner = this.s4LeftEar.getChild("s4LeftInner");
        this.s4RightInner = this.s4RightEar.getChild("s4RightInner");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ================= Форма 0: ★ Огненные кисточки (texOffs Y=0..15 внешние, Y=16..31 внутренние) =================
        PartDefinition s0Left = root.addOrReplaceChild("s0LeftEar",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.6f, -5.5f, 0.4f, 3.2f, 5.5f, 1.4f)
                        .texOffs(0, 7).addBox(-2.0f, -4.8f, -1.6f, 0.8f, 4.8f, 2.0f)
                        .texOffs(8, 7).addBox(1.2f, -4.8f, -1.6f, 0.8f, 4.8f, 2.0f)
                        .texOffs(0, 14).addBox(-0.8f, -7.2f, -0.2f, 1.6f, 1.8f, 1.4f),
                PartPose.offsetAndRotation(2.4f, -8.0f, -0.2f, 0.08f, -0.1f, 0.18f));
        s0Left.addOrReplaceChild("s0LeftTuft",
                CubeListBuilder.create().texOffs(0, 16).addBox(-1.1f, -5.2f, -1.0f, 2.2f, 4.6f, 1.2f)
                        .texOffs(0, 22).addBox(-0.6f, -8.8f, -0.4f, 1.2f, 2.6f, 1.0f),
                PartPose.ZERO);

        PartDefinition s0Right = root.addOrReplaceChild("s0RightEar",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.6f, -5.5f, 0.4f, 3.2f, 5.5f, 1.4f)
                        .texOffs(8, 7).addBox(1.2f, -4.8f, -1.6f, 0.8f, 4.8f, 2.0f)
                        .texOffs(0, 7).addBox(-2.0f, -4.8f, -1.6f, 0.8f, 4.8f, 2.0f)
                        .texOffs(0, 14).addBox(-0.8f, -7.2f, -0.2f, 1.6f, 1.8f, 1.4f),
                PartPose.offsetAndRotation(-2.4f, -8.0f, -0.2f, 0.08f, 0.1f, -0.18f));
        s0Right.addOrReplaceChild("s0RightTuft",
                CubeListBuilder.create().texOffs(0, 16).addBox(-1.1f, -5.2f, -1.0f, 2.2f, 4.6f, 1.2f)
                        .texOffs(0, 22).addBox(-0.6f, -8.8f, -0.4f, 1.2f, 2.6f, 1.0f),
                PartPose.ZERO);

        // ================= Форма 1: 🦊 Кицунэ / 🐱 Котик с бантиком Феликса (texOffs X=16..31) =================
        PartDefinition s1Left = root.addOrReplaceChild("s1LeftEar",
                CubeListBuilder.create().texOffs(16, 0).addBox(-2.0f, -6.2f, 0.4f, 4.0f, 6.2f, 1.4f)
                        .texOffs(16, 8).addBox(-2.4f, -5.4f, -1.6f, 0.8f, 5.4f, 2.0f)
                        .texOffs(22, 8).addBox(1.6f, -5.4f, -1.6f, 0.8f, 5.4f, 2.0f)
                        .texOffs(28, 0).addBox(-0.8f, -8.0f, -0.2f, 1.6f, 1.8f, 1.4f),
                PartPose.offsetAndRotation(2.4f, -8.0f, -0.2f, 0.10f, -0.08f, 0.20f));
        s1Left.addOrReplaceChild("s1LeftInner",
                CubeListBuilder.create().texOffs(16, 16).addBox(-1.5f, -5.8f, -1.0f, 3.0f, 5.0f, 1.2f),
                PartPose.ZERO);
        s1Left.addOrReplaceChild("s1LeftBow",
                CubeListBuilder.create().texOffs(48, 16).addBox(-3.4f, -3.8f, -1.6f, 1.8f, 2.4f, 1.4f)
                        .texOffs(48, 20).addBox(-3.6f, -1.8f, -1.4f, 1.6f, 2.0f, 1.2f)
                        .texOffs(48, 24).addBox(-2.2f, -2.4f, -1.8f, 1.0f, 1.4f, 1.0f)
                        .texOffs(55, 16).addBox(-3.2f, 0.2f, -1.0f, 1.4f, 2.8f, 0.6f)
                        .texOffs(55, 20).addBox(-2.4f, 0.5f, -0.6f, 1.2f, 2.5f, 0.6f),
                PartPose.ZERO);

        PartDefinition s1Right = root.addOrReplaceChild("s1RightEar",
                CubeListBuilder.create().texOffs(16, 0).addBox(-2.0f, -6.2f, 0.4f, 4.0f, 6.2f, 1.4f)
                        .texOffs(22, 8).addBox(1.6f, -5.4f, -1.6f, 0.8f, 5.4f, 2.0f)
                        .texOffs(16, 8).addBox(-2.4f, -5.4f, -1.6f, 0.8f, 5.4f, 2.0f)
                        .texOffs(28, 0).addBox(-0.8f, -8.0f, -0.2f, 1.6f, 1.8f, 1.4f),
                PartPose.offsetAndRotation(-2.4f, -8.0f, -0.2f, 0.10f, -0.08f, -0.20f));
        s1Right.addOrReplaceChild("s1RightInner",
                CubeListBuilder.create().texOffs(16, 16).addBox(-1.5f, -5.8f, -1.0f, 3.0f, 5.0f, 1.2f),
                PartPose.ZERO);
        s1Right.addOrReplaceChild("s1RightBow",
                CubeListBuilder.create().texOffs(48, 16).addBox(1.6f, -3.8f, -1.6f, 1.8f, 2.4f, 1.4f)
                        .texOffs(48, 20).addBox(2.0f, -1.8f, -1.4f, 1.6f, 2.0f, 1.2f)
                        .texOffs(48, 24).addBox(1.2f, -2.4f, -1.8f, 1.0f, 1.4f, 1.0f)
                        .texOffs(55, 16).addBox(1.8f, 0.2f, -1.0f, 1.4f, 2.8f, 0.6f)
                        .texOffs(55, 20).addBox(1.2f, 0.5f, -0.6f, 1.2f, 2.5f, 0.6f),
                PartPose.ZERO);

        // ================= Форма 2: 🐺 Волк (texOffs X=32..47) =================
        PartDefinition s2Left = root.addOrReplaceChild("s2LeftEar",
                CubeListBuilder.create().texOffs(32, 0).addBox(-1.6f, -6.4f, 0.4f, 3.2f, 6.4f, 1.4f)
                        .texOffs(32, 8).addBox(-2.0f, -5.5f, -1.5f, 0.8f, 5.5f, 1.9f)
                        .texOffs(38, 8).addBox(1.2f, -5.5f, -1.5f, 0.8f, 5.5f, 1.9f)
                        .texOffs(43, 0).addBox(-0.7f, -8.4f, -0.1f, 1.4f, 2.0f, 1.2f),
                PartPose.offsetAndRotation(2.4f, -8.0f, -0.2f, 0.06f, -0.04f, 0.12f));
        s2Left.addOrReplaceChild("s2LeftInner",
                CubeListBuilder.create().texOffs(32, 16).addBox(-1.1f, -6.0f, -0.9f, 2.2f, 5.0f, 1.1f),
                PartPose.ZERO);

        PartDefinition s2Right = root.addOrReplaceChild("s2RightEar",
                CubeListBuilder.create().texOffs(32, 0).addBox(-1.6f, -6.4f, 0.4f, 3.2f, 6.4f, 1.4f)
                        .texOffs(38, 8).addBox(1.2f, -5.5f, -1.5f, 0.8f, 5.5f, 1.9f)
                        .texOffs(32, 8).addBox(-2.0f, -5.5f, -1.5f, 0.8f, 5.5f, 1.9f)
                        .texOffs(43, 0).addBox(-0.7f, -8.4f, -0.1f, 1.4f, 2.0f, 1.2f),
                PartPose.offsetAndRotation(-2.4f, -8.0f, -0.2f, 0.06f, -0.04f, -0.12f));
        s2Right.addOrReplaceChild("s2RightInner",
                CubeListBuilder.create().texOffs(32, 16).addBox(-1.1f, -6.0f, -0.9f, 2.2f, 5.0f, 1.1f),
                PartPose.ZERO);

        // ================= Форма 3: 🐰 Кролик (texOffs X=48..63) =================
        PartDefinition s3Left = root.addOrReplaceChild("s3LeftEar",
                CubeListBuilder.create().texOffs(48, 0).addBox(-1.6f, -11.5f, 0.4f, 3.2f, 11.5f, 1.4f)
                        .texOffs(48, 8).addBox(-2.0f, -10.0f, -1.4f, 0.8f, 10.0f, 1.8f)
                        .texOffs(54, 8).addBox(1.2f, -10.0f, -1.4f, 0.8f, 10.0f, 1.8f)
                        .texOffs(60, 0).addBox(-0.8f, -13.0f, 0.0f, 1.6f, 1.8f, 1.2f),
                PartPose.offsetAndRotation(2.4f, -8.0f, -0.2f, 0.22f, -0.05f, -0.28f));
        s3Left.addOrReplaceChild("s3LeftInner",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.1f, -11.0f, -0.8f, 2.2f, 10.2f, 1.0f),
                PartPose.ZERO);

        PartDefinition s3Right = root.addOrReplaceChild("s3RightEar",
                CubeListBuilder.create().texOffs(48, 0).addBox(-1.6f, -11.5f, 0.4f, 3.2f, 11.5f, 1.4f)
                        .texOffs(54, 8).addBox(1.2f, -10.0f, -1.4f, 0.8f, 10.0f, 1.8f)
                        .texOffs(48, 8).addBox(-2.0f, -10.0f, -1.4f, 0.8f, 10.0f, 1.8f)
                        .texOffs(60, 0).addBox(-0.8f, -13.0f, 0.0f, 1.6f, 1.8f, 1.2f),
                PartPose.offsetAndRotation(-2.4f, -8.0f, -0.2f, 0.22f, -0.05f, -0.28f));
        s3Right.addOrReplaceChild("s3RightInner",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.1f, -11.0f, -0.8f, 2.2f, 10.2f, 1.0f),
                PartPose.ZERO);

        // ================= Форма 4: 🐻 Медвежьи (texOffs Y=8..15, Y=24..31) =================
        PartDefinition s4Left = root.addOrReplaceChild("s4LeftEar",
                CubeListBuilder.create().texOffs(32, 8).addBox(-2.2f, -4.5f, 0.4f, 4.4f, 4.5f, 1.4f)
                        .texOffs(40, 8).addBox(-2.6f, -4.0f, -1.4f, 0.8f, 4.0f, 1.8f)
                        .texOffs(44, 8).addBox(1.8f, -4.0f, -1.4f, 0.8f, 4.0f, 1.8f),
                PartPose.offsetAndRotation(2.6f, -8.0f, -0.2f, 0.06f, -0.08f, 0.20f));
        s4Left.addOrReplaceChild("s4LeftInner",
                CubeListBuilder.create().texOffs(32, 24).addBox(-1.6f, -4.2f, -0.8f, 3.2f, 3.6f, 1.1f),
                PartPose.ZERO);

        PartDefinition s4Right = root.addOrReplaceChild("s4RightEar",
                CubeListBuilder.create().texOffs(32, 8).addBox(-2.2f, -4.5f, 0.4f, 4.4f, 4.5f, 1.4f)
                        .texOffs(44, 8).addBox(1.8f, -4.0f, -1.4f, 0.8f, 4.0f, 1.8f)
                        .texOffs(40, 8).addBox(-2.6f, -4.0f, -1.4f, 0.8f, 4.0f, 1.8f),
                PartPose.offsetAndRotation(-2.6f, -8.0f, -0.2f, 0.06f, -0.08f, -0.20f));
        s4Right.addOrReplaceChild("s4RightInner",
                CubeListBuilder.create().texOffs(32, 24).addBox(-1.6f, -4.2f, -0.8f, 3.2f, 3.6f, 1.1f),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setupCustomRotations(int shape,
                                     float leftRotXDeg, float leftRotYDeg, float leftRotZDeg,
                                     float rightRotXDeg, float rightRotYDeg, float rightRotZDeg,
                                     float scaleX, float scaleY, float scaleZ) {
        float radLeftX = leftRotXDeg * ((float) Math.PI / 180.0f);
        float radLeftY = leftRotYDeg * ((float) Math.PI / 180.0f);
        float radLeftZ = leftRotZDeg * ((float) Math.PI / 180.0f);

        float radRightX = rightRotXDeg * ((float) Math.PI / 180.0f);
        float radRightY = rightRotYDeg * ((float) Math.PI / 180.0f);
        float radRightZ = rightRotZDeg * ((float) Math.PI / 180.0f);

        ModelPart[] lefts = { s0LeftEar, s1LeftEar, s2LeftEar, s3LeftEar, s4LeftEar };
        ModelPart[] rights = { s0RightEar, s1RightEar, s2RightEar, s3RightEar, s4RightEar };

        for (int i = 0; i < 5; i++) {
            lefts[i].visible = (i == shape);
            rights[i].visible = (i == shape);
            if (i == shape) {
                lefts[i].xRot = 0.08f + radLeftX;
                lefts[i].yRot = -0.10f + radLeftY;
                lefts[i].zRot = (i == 3 ? -0.28f : 0.18f) - radLeftZ;

                rights[i].xRot = 0.08f + radRightX;
                rights[i].yRot = 0.10f - radRightY;
                rights[i].zRot = (i == 3 ? 0.28f : -0.18f) + radRightZ;

                lefts[i].xScale = scaleX; lefts[i].yScale = scaleY; lefts[i].zScale = scaleZ;
                rights[i].xScale = scaleX; rights[i].yScale = scaleY; rights[i].zScale = scaleZ;
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        s0LeftEar.render(poseStack, buffer, packedLight, packedOverlay, color);
        s0RightEar.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    public void renderWithColorsAndShape(int shape, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int primaryRgb, int secondaryRgb) {
        int color1 = 0xFF000000 | (primaryRgb & 0xFFFFFF);
        int color2 = 0xFF000000 | (secondaryRgb & 0xFFFFFF);

        ModelPart[] lefts = { s0LeftEar, s1LeftEar, s2LeftEar, s3LeftEar, s4LeftEar };
        ModelPart[] rights = { s0RightEar, s1RightEar, s2RightEar, s3RightEar, s4RightEar };
        ModelPart[] leftInners = { s0LeftTuft, s1LeftInner, s2LeftInner, s3LeftInner, s4LeftInner };
        ModelPart[] rightInners = { s0RightTuft, s1RightInner, s2RightInner, s3RightInner, s4RightInner };

        int active = Math.max(0, Math.min(4, shape));

        leftInners[active].visible = false;
        rightInners[active].visible = false;
        if (active == 1) {
            s1LeftBow.visible = false;
            s1RightBow.visible = false;
        }
        lefts[active].render(poseStack, buffer, packedLight, packedOverlay, color1);
        rights[active].render(poseStack, buffer, packedLight, packedOverlay, color1);

        leftInners[active].visible = true;
        rightInners[active].visible = true;
        poseStack.pushPose();
        lefts[active].translateAndRotate(poseStack);
        leftInners[active].render(poseStack, buffer, packedLight, packedOverlay, color2);
        if (active == 1) {
            s1LeftBow.visible = true;
            s1LeftBow.render(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
        }
        poseStack.popPose();

        poseStack.pushPose();
        rights[active].translateAndRotate(poseStack);
        rightInners[active].render(poseStack, buffer, packedLight, packedOverlay, color2);
        if (active == 1) {
            s1RightBow.visible = true;
            s1RightBow.render(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
        }
        poseStack.popPose();
    }
}
