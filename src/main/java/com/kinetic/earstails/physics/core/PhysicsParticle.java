package com.kinetic.earstails.physics.core;

import net.minecraft.world.phys.Vec3;

/**
 * Universal Verlet particle for cosmetic physics parts: tails, ears, tassels,
 * hair strands, scarves, etc. It intentionally has no Minecraft rendering
 * dependency, so future adapters can reuse it.
 */
public class PhysicsParticle {
    public Vec3 position;
    public Vec3 previousPosition;
    public Vec3 acceleration = Vec3.ZERO;

    public double mass = 1.0D;
    public double radius = 0.20D;
    public boolean pinned = false;
    public boolean touchingGround = false;

    public PhysicsParticle(Vec3 position, double radius) {
        this.position = position;
        this.previousPosition = position;
        this.radius = radius;
    }

    public void applyForce(Vec3 force) {
        this.acceleration = this.acceleration.add(force.scale(1.0D / Math.max(0.0001D, mass)));
    }

    public void verlet(double damping) {
        if (pinned) {
            previousPosition = position;
            acceleration = Vec3.ZERO;
            return;
        }

        Vec3 velocity = position.subtract(previousPosition).scale(damping);
        previousPosition = position;
        position = position.add(velocity).add(acceleration);
        acceleration = Vec3.ZERO;
    }

    public void killVelocity() {
        previousPosition = position;
    }

    /**
     * When a tail segment touches or slides on ground/grass, we kill any vertical
     * bouncing velocity so it rests flat, but preserve horizontal sliding momentum
     * dampened by ground friction. This allows the tail to softly drag/crawl along
     * the terrain when walking instead of freezing instantly.
     */
    public void slideOnGround(double groundFriction) {
        double vx = (position.x - previousPosition.x) * groundFriction;
        double vz = (position.z - previousPosition.z) * groundFriction;
        previousPosition = new Vec3(position.x - vx, position.y, position.z - vz);
    }
}
