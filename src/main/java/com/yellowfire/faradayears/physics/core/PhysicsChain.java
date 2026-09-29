package com.yellowfire.faradayears.physics.core;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable Verlet/PBD chain (`v34-base-micro-joints`).
 * Implements Base-Concentrated Micro-Joint Distribution (`getSegmentLength`):
 * Places 6 ultra-short micro-joints (~6-8 cm each) precisely right at the base where the tail
 * exits the lower back and bends toward the ground (`именно между первыми суставами`).
 *
 * ★ 1.2.0 — ТРИ РЕЖИМА ФИЗИКИ ХВОСТА (переключаются в GUI, синхронизируются по сети):
 *  - {@link #MODE_CLASSIC}  «Классика»   — поведение 1.0.0: мягкая верёвка, стелется по земле
 *                                          (градиент гравитации к кончику + settleDistalNearGround).
 *  - {@link #MODE_BALANCED} «Баланс»     — новый средний режим: приподнятая дуга, но кончик
 *                                          мягко касается земли (полсилы поддержки и присадки).
 *  - {@link #MODE_LIFTED}   «Поднятая»   — поведение 1.0.1 fox-tail-lift: горизонтальный выход
 *                                          из поясницы, приподнятая дуга, земля не притягивает.
 */
public class PhysicsChain {
    public static final int MODE_CLASSIC = 0;
    public static final int MODE_BALANCED = 1;
    public static final int MODE_LIFTED = 2;

    public final List<PhysicsParticle> particles = new ArrayList<>();

    public double segmentLength = 0.42D;
    public double damping = 0.84D;
    public int iterations = 10;
    public int physicsMode = MODE_CLASSIC;

    private final PhysicsWorldCollider collider = new PhysicsWorldCollider();

    public static double getSegmentLength(int i, int count, double baseSegmentLength) {
        if (count <= 6) return baseSegmentLength;
        // ★ УПЛОТНЕНИЕ СУСТАВОВ У ОСНОВАНИЯ (Base-Concentrated Micro-Joints):
        // Первые 6 суставов имеют длину в 2 раза меньше (~6-8 см каждый!), создавая плотный
        // физический каркас на месте изгиба за поясницей. Это гарантирует 100% плавную дугу без углов!
        if (i < 6) {
            return baseSegmentLength * 0.50D;
        } else {
            double saved = 6.0D * 0.50D;
            return baseSegmentLength * (1.0D + saved / (double) Math.max(1, count - 6));
        }
    }

    public void reset(Vec3 root, Vec3 direction, int count, double segmentLength, double baseRadius) {
        particles.clear();
        this.segmentLength = segmentLength;
        Vec3 dir = direction.lengthSqr() < 1.0E-6D ? new Vec3(0, -0.75D, 1).normalize() : direction.normalize();
        Vec3 p = root;
        for (int i = 0; i < count; i++) {
            double segLen = getSegmentLength(i, count, segmentLength);
            // В режимах Баланс/Поднятая стартовая укладка идёт по анатомической дуге:
            Vec3 step = (physicsMode == MODE_CLASSIC) ? dir : restDirection(dir, i, count);
            p = p.add(step.scale(segLen));
            double taper = count <= 1 ? 0.0D : i / (double) (count - 1);
            PhysicsParticle particle = new PhysicsParticle(p, Math.max(0.09D, baseRadius * (1.0D - taper * 0.42D)));
            particle.mass = 1.0D + taper * 0.40D;
            particles.add(particle);
        }
    }

    public void ensureSize(Vec3 root, Vec3 direction, int count, double segmentLength, double baseRadius) {
        if (particles.size() != count || Math.abs(this.segmentLength - segmentLength) > 0.035D) {
            reset(root, direction, count, segmentLength, baseRadius);
        }
    }

    public void simulateTailRope(Level level, Entity owner, Vec3 root, Vec3 rootVelocity,
                                 Vec3 gravity, Vec3 baseDirection, double baseStiffness,
                                 int wagAxis, float wagAmp, float wagSpeed, int tailIndex) {
        if (particles.isEmpty()) return;

        if (rootVelocity.lengthSqr() > 9.0D) {
            reset(root, baseDirection, particles.size(), segmentLength, particles.get(0).radius);
            return;
        }

        Vec3 baseDir = baseDirection.lengthSqr() < 1.0E-6D ? new Vec3(0, -0.75D, 1).normalize() : baseDirection.normalize();

        double time = (owner != null ? owner.tickCount : 0.0D) * wagSpeed * 0.15D;
        Vec3 sideVector = baseDir.cross(new Vec3(0, 1, 0));
        if (sideVector.lengthSqr() < 1.0E-6D) sideVector = new Vec3(1, 0, 0);
        sideVector = sideVector.normalize();
        Vec3 upVector = sideVector.cross(baseDir).normalize();

        // 1) Verlet integration. Распределение гравитации зависит от режима:
        //    Классика — почти невесомое основание и усиленный к кончику градиент (1.0.0);
        //    Баланс   — смягчённый градиент;
        //    Поднятая — одинаковая гравитация для всех частиц (1.0.1).
        for (int i = 0; i < particles.size(); i++) {
            PhysicsParticle p = particles.get(i);
            p.touchingGround = false;
            double distal = (particles.size() <= 1) ? 1.0D : i / (double) (particles.size() - 1);
            Vec3 lateralWag = Vec3.ZERO;
            if (wagAxis != 3 && wagAmp > 0.05f && i > 0) {
                double segTime = time - distal * 1.5D; // Бегущая волна виляния по длине хвоста
                double wagForce = Math.sin(segTime) * (wagAmp * 0.0075D);
                double wagForce2 = Math.cos(segTime * 0.8D) * (wagAmp * 0.006D);
                if (wagAxis == 0) lateralWag = sideVector.scale(wagForce * distal);
                else if (wagAxis == 1) lateralWag = upVector.scale(wagForce * distal);
                else if (wagAxis == 2) lateralWag = sideVector.scale(wagForce * distal).add(upVector.scale(wagForce2 * distal));
            }
            double gScale;
            if (physicsMode == MODE_CLASSIC) {
                gScale = (i == 0) ? 0.08D : (1.0D + distal * 0.35D);
            } else if (physicsMode == MODE_BALANCED) {
                gScale = (i == 0) ? 0.45D : (0.85D + distal * 0.20D);
            } else {
                gScale = 1.0D;
            }
            p.applyForce(gravity.scale(gScale));
            if (lateralWag.lengthSqr() > 0.0D) p.applyForce(lateralWag);
            p.verlet(damping);
        }

        boolean crouching = (owner != null && owner.isShiftKeyDown());

        // 2) PBD constraints with Base-Concentrated Micro-Joints (`segLen`):
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < particles.size(); i++) {
                PhysicsParticle p = particles.get(i);
                Vec3 anchor = (i == 0) ? root : particles.get(i - 1).position;
                double segLen = getSegmentLength(i, particles.size(), segmentLength);

                double distal = (particles.size() <= 1) ? 0.0D : i / (double) (particles.size() - 1);
                double segTime = time - distal * 1.5D;
                double segWagRad = Math.toRadians(wagAmp * Math.sin(segTime));
                double segWagRad2 = Math.toRadians(wagAmp * Math.cos(segTime * 0.8D));

                Vec3 jointDir = (physicsMode == MODE_CLASSIC) ? baseDir : restDirection(baseDir, i, particles.size());
                if (wagAxis != 3 && wagAmp > 0.05f) {
                    if (wagAxis == 0) {
                        double cos = Math.cos(segWagRad * (0.35D + distal * 0.45D));
                        double sin = Math.sin(segWagRad * (0.35D + distal * 0.45D));
                        jointDir = new Vec3(jointDir.x * cos - jointDir.z * sin, jointDir.y, jointDir.x * sin + jointDir.z * cos).normalize();
                    } else if (wagAxis == 1) {
                        jointDir = jointDir.add(upVector.scale(Math.sin(segTime) * (wagAmp * 0.008D))).normalize();
                    } else if (wagAxis == 2) {
                        double cos = Math.cos(segWagRad * (0.25D + distal * 0.35D));
                        double sin = Math.sin(segWagRad * (0.25D + distal * 0.35D));
                        Vec3 horiz = new Vec3(jointDir.x * cos - jointDir.z * sin, jointDir.y, jointDir.x * sin + jointDir.z * cos);
                        jointDir = horiz.add(upVector.scale(Math.sin(segWagRad2) * (wagAmp * 0.006D))).normalize();
                    }
                }

                Vec3 diff = p.position.subtract(anchor);
                if (diff.lengthSqr() < 1.0E-7D) diff = jointDir.scale(segLen);
                Vec3 projected = anchor.add(diff.normalize().scale(segLen));

                double groundFade = 1.0D;
                if (level != null) {
                    double groundY = collider.findGroundTopBelow(level, p.position, p.radius, 1.4D);
                    if (!Double.isNaN(groundY)) {
                        double gap = (p.position.y - p.radius) - groundY;
                        groundFade = Mth.clamp(gap / 0.65D, 0.0D, 1.0D);
                    }
                }
                if (crouching) groundFade = Math.min(groundFade, 0.25D);

                if (physicsMode == MODE_LIFTED) {
                    // ★ ПОДНЯТАЯ ДУГА (1.0.1): точный горизонтальный выход у поясницы,
                    //   лёгкая поддерживающая дуга по всей длине, к земле не притягиваем.
                    if (i == 0) {
                        double k0 = baseStiffness * (0.75D + 0.25D * groundFade);
                        p.position = lerp(projected, root.add(jointDir.scale(segLen)), k0);
                    } else {
                        Vec3 arcTarget = anchor.add(jointDir.scale(segLen));
                        double restWeight = baseStiffness * Mth.lerp(distal, 0.52D, 0.30D);
                        restWeight *= 1.0D + 0.35D * (1.0D - groundFade);
                        if (crouching) restWeight *= 0.90D;
                        p.position = lerp(projected, arcTarget, Mth.clamp(restWeight, 0.06D, 0.30D));
                    }
                } else if (physicsMode == MODE_BALANCED) {
                    // ★ БАЛАНС: средние значения между Классикой и Поднятой —
                    //   дуга держится, но мягче; кончику позволено опускаться к земле.
                    if (i == 0) {
                        double k0 = baseStiffness * (0.50D + 0.50D * groundFade);
                        p.position = lerp(projected, root.add(jointDir.scale(segLen)), k0);
                    } else if (i < 4) {
                        Vec3 arcTarget = anchor.add(jointDir.scale(segLen));
                        double kSpan = (0.38D - i * 0.07D) * baseStiffness;
                        double effectiveK = kSpan * (0.20D + 0.80D * groundFade);
                        p.position = lerp(projected, arcTarget, effectiveK);
                    } else {
                        Vec3 arcTarget = anchor.add(jointDir.scale(segLen));
                        double restWeight = baseStiffness * Mth.lerp(distal, 0.34D, 0.16D);
                        restWeight *= 1.0D + 0.18D * (1.0D - groundFade);
                        if (crouching) restWeight *= 0.90D;
                        p.position = lerp(projected, arcTarget, Mth.clamp(restWeight, 0.04D, 0.18D));
                    }
                } else {
                    // ★ КЛАССИКА (1.0.0): слабое основание, микро-суставы у корня,
                    //   дальше — свободная верёвка, которую гравитация кладёт на землю.
                    if (i == 0) {
                        Vec3 baseTarget = root.add(jointDir.scale(segLen));
                        double k0 = baseStiffness * (0.25D + 0.75D * groundFade);
                        p.position = lerp(projected, baseTarget, k0);
                    } else if (i < 6) {
                        // Первые 6 микросуставов у основания мягко удерживают плавное анатомическое скругление:
                        Vec3 arcTarget = anchor.add(jointDir.scale(segLen));
                        double kSpan = (0.50D - i * 0.06D) * baseStiffness;
                        double effectiveK = kSpan * (0.15D + 0.85D * groundFade);
                        p.position = lerp(projected, arcTarget, effectiveK);
                    } else {
                        p.position = projected;
                    }
                }

                p.position = collideOwnerCylinder(owner, p.position, p.radius, i);

                PhysicsWorldCollider.CollisionResult result = collider.collideSphere(level, owner, p.position, p.radius);
                p.position = result.position;
                p.touchingGround = p.touchingGround || result.ground;
                if (result.ground) {
                    p.slideOnGround(0.80D);
                }
            }
        }

        settleDistalNearGround(level, owner);
    }

    /**
     * Режимная «присадка» дистальной части к земле:
     *  - Классика: как в 1.0.0 — сильное притяжение (0.95) в пределах 45 см над землёй;
     *  - Баланс:   мягкое (0.45) и только для дальней половины хвоста в пределах 28 см;
     *  - Поднятая: отключено (хвост не должен «прилипать» к земле).
     */
    private void settleDistalNearGround(Level level, Entity owner) {
        if (physicsMode == MODE_LIFTED) return;
        if (level == null || owner == null || !owner.onGround() || particles.size() < 2) return;

        boolean balanced = (physicsMode == MODE_BALANCED);
        double maxGap = balanced ? 0.28D : 0.45D;
        double pull = balanced ? 0.45D : 0.95D;
        int from = balanced ? particles.size() / 2 : 1;

        for (int i = from; i < particles.size(); i++) {
            PhysicsParticle p = particles.get(i);
            double groundY = collider.findGroundTopBelow(level, p.position, p.radius, 1.25D);
            if (Double.isNaN(groundY)) continue;
            double gap = (p.position.y - p.radius) - groundY;
            if (gap > 0.001D && gap < maxGap) {
                Vec3 correction = new Vec3(0, -gap * pull, 0);
                p.position = p.position.add(correction);
                PhysicsWorldCollider.CollisionResult result = collider.collideSphere(level, owner, p.position, p.radius);
                p.position = result.position;
                p.touchingGround = p.touchingGround || result.ground;
                if (p.touchingGround) {
                    p.slideOnGround(0.80D);
                }
            }
        }
    }

    /**
     * Анатомическое направление покоя для сустава `index` (режимы Баланс/Поднятая):
     * первый сегмент выходит из поясницы горизонтально, середина мягко приподнята,
     * кончик слегка опускается. Классика использует направление как есть (с наклоном вниз).
     */
    private Vec3 restDirection(Vec3 horizontalBack, int index, int count) {
        if (physicsMode == MODE_CLASSIC) return horizontalBack;
        Vec3 back = new Vec3(horizontalBack.x, 0.0D, horizontalBack.z);
        if (back.lengthSqr() < 1.0E-8D) back = new Vec3(0.0D, 0.0D, -1.0D);
        back = back.normalize();
        if (index <= 0 || count <= 1) return back;

        boolean lifted = (physicsMode == MODE_LIFTED);
        double lift = lifted ? 0.34D : 0.20D;
        double drop = lifted ? 0.18D : 0.10D;
        double tipStart = lifted ? 0.72D : 0.78D;

        double t = Mth.clamp(index / (double) (count - 1), 0.0D, 1.0D);
        double liftAngle = lift * Math.sin(Math.PI * t);
        double tipDrop = drop * smoothstep(tipStart, 1.0D, t);
        double angle = liftAngle - tipDrop;
        return new Vec3(back.x * Math.cos(angle), Math.sin(angle), back.z * Math.cos(angle)).normalize();
    }

    private double smoothstep(double edge0, double edge1, double x) {
        double t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0D, 1.0D);
        return t * t * (3.0D - 2.0D * t);
    }

    private Vec3 lerp(Vec3 a, Vec3 b, double t) {
        double k = Math.max(0.0D, Math.min(1.0D, t));
        return a.scale(1.0D - k).add(b.scale(k));
    }

    private Vec3 collideOwnerCylinder(Entity owner, Vec3 point, double radius, int segmentIndex) {
        if (owner == null || segmentIndex <= 0) return point;
        double y = point.y - owner.getY();
        if (y < -0.05D || y > 1.35D) return point;

        double dx = point.x - owner.getX();
        double dz = point.z - owner.getZ();
        double distSq = dx * dx + dz * dz;
        double min = 0.18D + radius * 0.65D;
        if (distSq < min * min && distSq > 1.0E-6D) {
            double dist = Math.sqrt(distSq);
            double push = (min - dist) + 0.005D;
            return point.add((dx / dist) * push, 0, (dz / dist) * push);
        }
        return point;
    }

    public Vec3 getPoint(int index) {
        return particles.get(index).position;
    }

    public double getRadius(int index) {
        return particles.get(index).radius;
    }

    public boolean isGround(int index) {
        return particles.get(index).touchingGround;
    }
}
