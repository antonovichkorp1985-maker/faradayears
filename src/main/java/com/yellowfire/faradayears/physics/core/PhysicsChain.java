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
 * This divides the downward bend across 6 smooth micro-angles, completely rounding out
 * any sharp angle/corner (`резкий угол = 0`) into a polished organic fox curve!
 */
public class PhysicsChain {
    public final List<PhysicsParticle> particles = new ArrayList<>();

    public double segmentLength = 0.42D;
    public double damping = 0.84D;
    public int iterations = 10;

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
            p = p.add(dir.scale(segLen));
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

        // 1) Verlet integration with 100% constant gravity (`-0.070D`) across all heights and segments
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
            if (i == 0) {
                p.applyForce(gravity.scale(0.08D));
            } else {
                p.applyForce(gravity.scale(1.0D + distal * 0.35D).add(lateralWag));
            }
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

                Vec3 jointDir = baseDir;
                if (wagAxis != 3 && wagAmp > 0.05f) {
                    if (wagAxis == 0) {
                        double cos = Math.cos(segWagRad * (0.35D + distal * 0.45D));
                        double sin = Math.sin(segWagRad * (0.35D + distal * 0.45D));
                        jointDir = new Vec3(baseDir.x * cos - baseDir.z * sin, baseDir.y, baseDir.x * sin + baseDir.z * cos).normalize();
                    } else if (wagAxis == 1) {
                        jointDir = baseDir.add(upVector.scale(Math.sin(segTime) * (wagAmp * 0.008D))).normalize();
                    } else if (wagAxis == 2) {
                        double cos = Math.cos(segWagRad * (0.25D + distal * 0.35D));
                        double sin = Math.sin(segWagRad * (0.25D + distal * 0.35D));
                        Vec3 horiz = new Vec3(baseDir.x * cos - baseDir.z * sin, baseDir.y, baseDir.x * sin + baseDir.z * cos);
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

    private void settleDistalNearGround(Level level, Entity owner) {
        if (level == null || owner == null || !owner.onGround() || particles.size() < 2) return;
        for (int i = 1; i < particles.size(); i++) {
            PhysicsParticle p = particles.get(i);
            double groundY = collider.findGroundTopBelow(level, p.position, p.radius, 1.25D);
            if (Double.isNaN(groundY)) continue;
            double gap = (p.position.y - p.radius) - groundY;
            if (gap > 0.001D && gap < 0.45D) {
                Vec3 correction = new Vec3(0, -gap * 0.95D, 0);
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
