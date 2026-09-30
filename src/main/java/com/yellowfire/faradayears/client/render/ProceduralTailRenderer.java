package com.yellowfire.faradayears.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.yellowfire.faradayears.capability.PlayerEarsTailData;
import com.yellowfire.faradayears.physics.TailPhysicsEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Procedural world-space tail renderer (`v48-unified-uv-64x64`).
 * 1) Samples tail body fur from `Y = 32..47` (`v = 0.515..0.718`) and tail tip fur from `Y = 48..63` (`v = 0.765..0.968`)
 *    of our unified 64x64 UV layout (`faraday_custom.png / faraday_ears_tail.png`), allowing custom skins to paint ears & tail on 1 sheet!
 * 2) Centered GUI 3D preview (`IS_IN_GUI_PREVIEW`) and zero hump spline (`p0 colinear`).
 *
 * ★ 1.2.0: если у игрока включена кастомная текстура — рендер идёт по ней (без цветового
 * тинта), иначе по-прежнему `faraday_tail_solid.png` с заливкой Primary/Secondary цветами.
 */
public class ProceduralTailRenderer {
    private static final ResourceLocation TAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath("faradayears", "textures/entity/faraday_tail_solid.png");
    private static final int SIDES = 12;
    private static final int SUBDIVISIONS = 4;

    public static boolean IS_IN_GUI_PREVIEW = false;
    public static float GUI_PREVIEW_REAL_YAW = 0.0f;

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                              AbstractClientPlayer player,
                              TailPhysicsEngine.PlayerPhysicsData physics,
                              PlayerEarsTailData data,
                              float partialTick) {
        if (physics == null || physics.smoothRoot == null || physics.tails[0].smoothPoints[0] == null) return;

        boolean customTex = CustomTailTextureManager.hasCustomTexture(data);
        ResourceLocation texture = customTex
                ? CustomTailTextureManager.getTexture(player.getUUID(), data.getCustomTextureBase64())
                : TAIL_TEXTURE;
        if (texture == null) {
            customTex = false;
            texture = TAIL_TEXTURE;
        }
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

        double px = Mth.lerp(partialTick, player.xo, player.getX());
        double py = Mth.lerp(partialTick, player.yo, player.getY());
        double pz = Mth.lerp(partialTick, player.zo, player.getZ());

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.501D, 0.0D);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        float bodyRot = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - bodyRot)));

        poseStack.translate(-px, -py, -pz);

        if (IS_IN_GUI_PREVIEW) {
            poseStack.translate(px, py, pz);
            poseStack.mulPose(Axis.YP.rotationDegrees(GUI_PREVIEW_REAL_YAW));
            poseStack.translate(-px, -py, -pz);
        }

        int activeSegments = Math.max(1, Math.min(TailPhysicsEngine.MAX_PHYSICAL_SEGMENTS, physics.activeSegments));
        boolean superVolumetric = (data.getTailCount() == 10);
        int activeTails = superVolumetric ? 4 : Math.max(1, Math.min(TailPhysicsEngine.MAX_TAILS, data.getTailCount()));

        // ★ 1.3.2: испуг — «шерсть дыбом»: хвост временно распушается (пилоэрекция у кошек,
        // режим «Реалистичная»): радиус трубки ×1.3 на пике испуга:
        double puffScale = 1.0D + (data.getTailPhysicsMode() == 1 ? 0.30D * physics.scareBlend : 0.0D);

        // ★ 1.3.0 ОПТИМИЗАЦИЯ (LOD): чем дальше игрок от камеры, тем грубее трубка хвоста —
        // вблизи 12 сторон × 4 среза, вдали 8×3 и 6×2. Экономит и вершины, и мусор для GC:
        double camDistSq = player.distanceToSqr(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        boolean veryFar = camDistSq > 144.0D * 144.0D;
        boolean midFar = camDistSq > 64.0D * 64.0D;
        int sides = (IS_IN_GUI_PREVIEW || !midFar) ? SIDES : (veryFar ? 6 : 8);
        int subdivisions = (IS_IN_GUI_PREVIEW || !midFar) ? SUBDIVISIONS : (veryFar ? 2 : 3);

        double bodyYawRad = Math.toRadians(player.yBodyRot);
        Vec3 right = new Vec3(Math.cos(bodyYawRad), 0, Math.sin(bodyYawRad));
        Vec3 spineBaseDir = new Vec3(Math.sin(bodyYawRad), 0, -Math.cos(bodyYawRad)).normalize();

        if (superVolumetric) {
            renderContinuousSplineLayer(poseStack, consumer, packedLight, physics.tails[0], physics.smoothRoot, data, activeSegments,
                    0.0D, Vec3.ZERO, 1.28D * puffScale, true, right, spineBaseDir, customTex, sides, subdivisions);
            // ★ 1.3.0: 4 дополнительных объёмных слоя рендерим только вблизи (дальше их не видно):
            if (IS_IN_GUI_PREVIEW || camDistSq < 48.0D * 48.0D) {
                for (int t = 0; t < 4; t++) {
                    double fanAngleRad = getFanAngleRad(t, 4, data.getTailFanSpread(), true);
                    Vec3 layerOffset = getLayerOffset(t, right, true);
                    renderContinuousSplineLayer(poseStack, consumer, packedLight, physics.tails[0], physics.smoothRoot, data, activeSegments,
                            fanAngleRad, layerOffset, 0.72D * puffScale, false, right, spineBaseDir, customTex, sides, subdivisions);
                }
            }
        } else if (activeTails == 1) {
            renderContinuousSplineLayer(poseStack, consumer, packedLight, physics.tails[0], physics.smoothRoot, data, activeSegments,
                    0.0D, Vec3.ZERO, 1.0D * puffScale, false, right, spineBaseDir, customTex, sides, subdivisions);
        } else {
            for (int t = 0; t < activeTails; t++) {
                TailPhysicsEngine.TailChainInstance inst = physics.tails[t];
                if (inst.smoothPoints[0] == null) continue;
                renderContinuousSplineLayer(poseStack, consumer, packedLight, inst, physics.smoothRoot, data, activeSegments,
                        0.0D, Vec3.ZERO, 1.0D * puffScale, false, right, spineBaseDir, customTex, sides, subdivisions);
            }
        }

        poseStack.popPose();
    }

    private static void renderContinuousSplineLayer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                                    TailPhysicsEngine.TailChainInstance inst, Vec3 smoothRoot, PlayerEarsTailData data,
                                                    int activeSegments, double fanAngleRad, Vec3 layerOffset,
                                                    double radiusMultiplier, boolean core, Vec3 shoulderRight, Vec3 spineBaseDir,
                                                    boolean customTex, int sides, int subdivisions) {
        int N = activeSegments + 1;
        Vec3[] P = new Vec3[N];
        P[0] = transformTailPoint(smoothRoot, smoothRoot, 0.0D, layerOffset.scale(0.35D));
        for (int s = 0; s < activeSegments; s++) {
            double factor = (s + 1) / (double) activeSegments;
            P[s + 1] = transformTailPoint(smoothRoot, inst.smoothPoints[s], fanAngleRad * factor,
                    layerOffset.scale(0.35D + factor * 0.65D));
            if (P[s + 1] == null) P[s + 1] = P[s];
        }

        int M = activeSegments * subdivisions + 1;
        Vec3[] C = new Vec3[M];
        double[] R = new double[M];
        int[] color = new int[M];

        for (int j = 0; j < M; j++) {
            int s = Math.min(activeSegments - 1, j / subdivisions);
            double t = (j % subdivisions) / (double) subdivisions;
            if (j == M - 1) {
                s = activeSegments - 1;
                t = 1.0D;
            }

            Vec3 p1 = P[s];
            Vec3 p2 = P[s + 1];
            Vec3 p0 = (s > 0) ? P[s - 1] : P[0].add(P[0].subtract(P[1]));
            Vec3 p3 = (s + 2 < N) ? P[s + 2] : p2.add(p2.subtract(p1));

            Vec3 rawC = catmullRom(p0, p1, p2, p3, t);
            // ★ 1.3.3: старый «анти-бугорок» (принудительное выпрямление первых 35% хвоста)
            // оставлен только для Классики/Поднятой дуги — у реалистичного режима кривая
            // покоя теперь непрерывна от крестца, и хак лишь портил плавность переходов:
            if (data.getTailPhysicsMode() != 1 && j > 0 && j < M * 0.35D && rawC.y > C[0].y) {
                rawC = new Vec3(rawC.x, C[Math.max(0, j - 1)].y, rawC.z);
            }
            C[j] = rawC;

            double rStart = radiusFor(data, s, activeSegments) * radiusMultiplier;
            double rEnd = radiusFor(data, Math.min(s + 1, activeSegments - 1), activeSegments) * radiusMultiplier;
            if (j == M - 1) rEnd = radiusFor(data, activeSegments - 1, activeSegments) * radiusMultiplier;
            R[j] = Mth.lerp(t, rStart, rEnd);

            double overallT = j / (double) (M - 1);
            // ★ При кастомной текстуре цветовой тинт не нужен — рисует сама текстура:
            int c = customTex ? 0xFFFFFF
                    : (overallT >= 0.72D ? data.getTailColorSecondary() : data.getTailColorPrimary());
            if (core && overallT < 0.72D) c = darken(c, 0.86f);
            color[j] = c;
        }

        Vec3[] T = new Vec3[M];
        for (int j = 0; j < M; j++) {
            if (j == 0) {
                T[j] = C[1].subtract(C[0]);
            } else if (j == M - 1) {
                T[j] = C[M - 1].subtract(C[M - 2]);
            } else {
                T[j] = C[j + 1].subtract(C[j - 1]);
            }
            if (T[j].lengthSqr() < 1.0E-6D) T[j] = new Vec3(0, 0, -1);
            T[j] = T[j].normalize();
        }

        Vec3[] S = new Vec3[M];
        Vec3[] U = new Vec3[M];
        for (int j = 0; j < M; j++) {
            if (j == 0) {
                Vec3 sInit = shoulderRight.subtract(T[0].scale(shoulderRight.dot(T[0])));
                if (sInit.lengthSqr() > 1.0E-6D) {
                    S[0] = sInit.normalize();
                } else {
                    S[0] = T[0].cross(new Vec3(0, 1, 0));
                    if (S[0].lengthSqr() < 1.0E-6D) S[0] = new Vec3(1, 0, 0);
                    S[0] = S[0].normalize();
                }
                U[0] = S[0].cross(T[0]).normalize();
            } else {
                Vec3 proj = S[j - 1].subtract(T[j].scale(S[j - 1].dot(T[j])));
                if (proj.lengthSqr() > 1.0E-6D) {
                    S[j] = proj.normalize();
                } else {
                    S[j] = S[j - 1];
                }
                U[j] = S[j].cross(T[j]).normalize();
            }
        }

        Vec3[][] V = new Vec3[M][sides];
        for (int j = 0; j < M; j++) {
            double alongTail = j / (double) (M - 1);
            double furFadeIn = smoothstep(0.12D, 0.38D, alongTail);
            double tipTuftFade = smoothstep(0.72D, 0.94D, alongTail);
            for (int i = 0; i < sides; i++) {
                double angle = (Math.PI * 2.0D * i) / sides;
                Vec3 radial = S[j].scale(Math.cos(angle)).add(U[j].scale(Math.sin(angle)));
                // A subtle broken-fur contour avoids the perfectly smooth rubber-tube silhouette.
                double bodyTuft = 1.0D + 0.045D * furFadeIn * Math.cos(5.0D * angle + 0.35D);
                double tipTuft = 1.0D + 0.075D * tipTuftFade * Math.cos(4.0D * angle + 0.55D);
                V[j][i] = C[j].add(radial.scale(R[j] * bodyTuft * tipTuft));
            }
        }

        for (int j = 0; j < M - 1; j++) {
            double overallT0 = j / (double) (M - 1);
            double overallT1 = (j + 1) / (double) (M - 1);
            // ★ 1.3.0 ФИКС ТЕКСТУР: v теперь тянется ВДОЛЬ хвоста по полосе развёртки —
            // тело (0..72% длины) плавно проходит Y=33..46, кончик (72..100%) — Y=49..62.
            // Раньше v была константой на всю зону, и весь хвост красился одной полосой текстуры!
            float v0 = vAlongTail(overallT0);
            float v1 = vAlongTail(overallT1);

            for (int i = 0; i < sides; i++) {
                int n = (i + 1) % sides;
                float u0 = i / (float) sides;
                float u1 = (i + 1) / (float) sides;
                Vec3 normal = V[j][i].subtract(C[j]).add(V[j + 1][n].subtract(C[j])).normalize();
                quadUV(poseStack, consumer, V[j][i], V[j + 1][i], V[j + 1][n], V[j][n], color[j], packedLight, normal, u0, u1, v0, v1);
            }
        }

        for (int i = 1; i < sides - 1; i++) {
            tri(poseStack, consumer, V[0][0], V[0][i], V[0][i + 1], color[0], packedLight, T[0].scale(-1));
        }
        for (int i = 1; i < sides - 1; i++) {
            tri(poseStack, consumer, V[M - 1][0], V[M - 1][i + 1], V[M - 1][i], color[M - 1], packedLight, T[M - 1]);
        }
    }

    /**
     * ★ 1.3.0: непрерывная привязка позиции вдоль хвоста (0..1) к вертикали единого листа:
     * тело хвоста занимает Y=33..46 (верх = основание), пушистый кончик — Y=49..62.
     */
    private static float vAlongTail(double overallT) {
        if (overallT < 0.72D) {
            double local = overallT / 0.72D;
            return (float) ((33.0D + local * 13.5D) / 64.0D);
        }
        double local = (overallT - 0.72D) / 0.28D;
        return (float) ((49.0D + local * 13.0D) / 64.0D);
    }

    private static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double x = 0.5D * ((2.0D * p1.x) + (-p0.x + p2.x) * t + (2.0D * p0.x - 5.0D * p1.x + 4.0D * p2.x - p3.x) * t2 + (-p0.x + 3.0D * p1.x - 3.0D * p2.x + p3.x) * t3);
        double y = 0.5D * ((2.0D * p1.y) + (-p0.y + p2.y) * t + (2.0D * p0.y - 5.0D * p1.y + 4.0D * p2.y - p3.y) * t2 + (-p0.y + 3.0D * p1.y - 3.0D * p2.y + p3.y) * t3);
        double z = 0.5D * ((2.0D * p1.z) + (-p0.z + p2.z) * t + (2.0D * p0.z - 5.0D * p1.z + 4.0D * p2.z - p3.z) * t2 + (-p0.z + 3.0D * p1.z - 3.0D * p2.z + p3.z) * t3);
        return new Vec3(x, y, z);
    }

    public static double getFanAngleRad(int t, int activeTails, float spreadDeg, boolean superVolumetric) {
        if (superVolumetric) {
            if (t == 1) return Math.toRadians(4.0D);
            if (t == 2) return Math.toRadians(-4.0D);
            return 0.0D;
        }
        if (activeTails <= 1) return 0.0D;
        double spread = Math.toRadians(spreadDeg);
        return (t - (activeTails - 1) / 2.0D) * (spread / (activeTails - 1));
    }

    private static Vec3 getLayerOffset(int t, Vec3 right, boolean superVolumetric) {
        if (!superVolumetric) return Vec3.ZERO;
        if (t == 1) return right.scale(0.060D);
        if (t == 2) return right.scale(-0.060D);
        if (t == 3) return new Vec3(0, 0.055D, 0);
        return Vec3.ZERO;
    }

    private static Vec3 transformTailPoint(Vec3 root, Vec3 point, double angleRad, Vec3 offset) {
        Vec3 rel = point.subtract(root);
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        Vec3 rotated = new Vec3(rel.x * cos - rel.z * sin, rel.y, rel.x * sin + rel.z * cos);
        return root.add(rotated).add(offset);
    }

    private static double radiusFor(PlayerEarsTailData data, int segment, int activeSegments) {
        double t = activeSegments <= 1 ? 0.0D : segment / (double) (activeSegments - 1);
        double taper = Mth.clamp(data.getTailTaper(), 0.35D, 1.45D);
        double taperFactor = (taper - 0.35D) / (1.45D - 0.35D);

        // Fox-plume profile: ★ 1.3.4 — толстый корень-«падоб» (настоящий хвост шире всего
        // у основания, у крестца) → пышная кисть в середине → заострённый кончик.
        // Раньше корень был тоньше середины в 3 раза (0.105) — хвост выглядел палкой,
        // воткнутой в спину, плюс его расталкивал цилиндр тела → «бугорок».
        double rootToPlume = smoothstep(0.0D, 0.20D, t);
        double tipFade = smoothstep(0.64D, 1.0D, t);
        double plumeRadius = Mth.lerp(rootToPlume, 0.170D, 0.325D);
        double pointedTipRadius = Mth.lerp(taperFactor, 0.035D, 0.125D);
        double profile = Mth.lerp(tipFade, plumeRadius, pointedTipRadius);
        profile *= data.getTailScaleX();
        return Mth.clamp(profile, 0.045D, 0.38D);
    }

    private static double smoothstep(double edge0, double edge1, double x) {
        double t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0D, 1.0D);
        return t * t * (3.0D - 2.0D * t);
    }

    private static int darken(int rgb, float factor) {
        int r = Math.min(255, Math.max(0, (int) (((rgb >> 16) & 0xFF) * factor)));
        int g = Math.min(255, Math.max(0, (int) (((rgb >> 8) & 0xFF) * factor)));
        int b = Math.min(255, Math.max(0, (int) ((rgb & 0xFF) * factor)));
        return (r << 16) | (g << 8) | b;
    }

    private static void quadUV(PoseStack poseStack, VertexConsumer c, Vec3 a, Vec3 b, Vec3 cpos, Vec3 d,
                               int rgb, int light, Vec3 normal, float u0, float u1, float v0, float v1) {
        vertex(poseStack, c, a, rgb, light, u0, v0, normal);
        vertex(poseStack, c, b, rgb, light, u1, v0, normal);
        vertex(poseStack, c, cpos, rgb, light, u1, v1, normal);
        vertex(poseStack, c, d, rgb, light, u0, v1, normal);
    }

    private static void tri(PoseStack poseStack, VertexConsumer c, Vec3 a, Vec3 b, Vec3 cpos,
                            int rgb, int light, Vec3 normal) {
        vertex(poseStack, c, a, rgb, light, 0.0f, 0.0f, normal);
        vertex(poseStack, c, b, rgb, light, 1.0f, 0.0f, normal);
        vertex(poseStack, c, cpos, rgb, light, 0.5f, 1.0f, normal);
        vertex(poseStack, c, cpos, rgb, light, 0.5f, 1.0f, normal);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer c, Vec3 p, int rgb, int light, float u, float v, Vec3 n) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        c.addVertex(poseStack.last().pose(), (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, b, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal((float) n.x, (float) n.y, (float) n.z);
    }
}
