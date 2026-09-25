package com.yellowfire.faradayears.physics.core;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Robust sphere collider for tail and cosmetic physics.
 * Ensures the tail softly lays on ground without hovering/levitating and checks
 * surrounding block footprints to prevent falling through slopes or edges.
 *
 * NeoForge 1.21.1: Material API удалён — используем форму коллизии блока.
 */
public class PhysicsWorldCollider {
    public static class CollisionResult {
        public final Vec3 position;
        public final boolean ground;

        public CollisionResult(Vec3 position, boolean ground) {
            this.position = position;
            this.ground = ground;
        }
    }

    private static boolean isSolid(BlockState state, Level level, BlockPos pos) {
        return !state.isAir() && !state.getCollisionShape(level, pos).isEmpty();
    }

    public CollisionResult collideSphere(Level level, Entity owner, Vec3 point, double radius) {
        Vec3 result = point;
        boolean ground = false;

        if (level == null) return new CollisionResult(result, false);

        BlockPos minPos = BlockPos.containing(result.x - radius, result.y - radius, result.z - radius);
        BlockPos maxPos = BlockPos.containing(result.x + radius, result.y + radius, result.z + radius);

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState bState = level.getBlockState(pos);
            if (isSolid(bState, level, pos)) {
                VoxelShape shape = bState.getCollisionShape(level, pos);
                if (!shape.isEmpty()) {
                    AABB box = shape.bounds().move(pos).inflate(radius);
                    if (box.contains(result)) {
                        PushOut push = pushOut(result, box);
                        result = push.position;
                        ground = ground || push.ground;
                    }
                }
            }
        }

        // Exact zero-gap ground contact: if the bottom of the tail sphere touches or is slightly
        // below the top of the terrain block below, clamp its center exactly to groundY + radius
        // so it rests smoothly on the grass without hovering/levitating!
        double groundY = findGroundTopBelow(level, result, radius, 1.75D);
        if (!Double.isNaN(groundY) && result.y - radius <= groundY + 0.012D) {
            result = new Vec3(result.x, groundY + radius, result.z);
            ground = true;
        }

        // Soft push from other entities
        AABB searchBox = new AABB(result.x - 0.65D, result.y - 0.65D, result.z - 0.65D,
                result.x + 0.65D, result.y + 0.65D, result.z + 0.65D);
        List<Entity> entities = level.getEntities(owner, searchBox, e -> e.isAlive() && !e.isSpectator());
        for (Entity e : entities) {
            AABB entityBox = e.getBoundingBox().inflate(radius);
            if (entityBox.contains(result)) {
                double dx = result.x - e.getX();
                double dz = result.z - e.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 1.0E-5D) {
                    double push = Math.max(0.0D, radius + 0.30D - dist) * 0.30D;
                    result = result.add((dx / dist) * push, 0, (dz / dist) * push);
                }
            }
        }

        return new CollisionResult(result, ground);
    }

    public double findGroundTopBelow(Level level, Vec3 point, double radius, double searchDepth) {
        double best = Double.NaN;
        int minX = Mth.floor(point.x - radius * 0.65D);
        int maxX = Mth.floor(point.x + radius * 0.65D);
        int minZ = Mth.floor(point.z - radius * 0.65D);
        int maxZ = Mth.floor(point.z + radius * 0.65D);
        int topY = Mth.floor(point.y + radius + 0.25D);
        int bottomY = Mth.floor(point.y - searchDepth);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = topY; y >= bottomY; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState bState = level.getBlockState(pos);
                    if (isSolid(bState, level, pos)) {
                        VoxelShape shape = bState.getCollisionShape(level, pos);
                        if (!shape.isEmpty()) {
                            AABB box = shape.bounds().move(pos);
                            if (point.x + radius * 0.65D >= box.minX && point.x - radius * 0.65D <= box.maxX &&
                                point.z + radius * 0.65D >= box.minZ && point.z - radius * 0.65D <= box.maxZ) {
                                double top = box.maxY;
                                if (top <= point.y + radius + 0.25D && (Double.isNaN(best) || top > best)) {
                                    best = top;
                                }
                            }
                        }
                    }
                }
            }
        }
        return best;
    }

    private static class PushOut {
        final Vec3 position;
        final boolean ground;
        PushOut(Vec3 position, boolean ground) { this.position = position; this.ground = ground; }
    }

    private PushOut pushOut(Vec3 point, AABB box) {
        double pushTop = box.maxY - point.y;
        double pushBottom = point.y - box.minY;
        double pushLeft = point.x - box.minX;
        double pushRight = box.maxX - point.x;
        double pushFront = point.z - box.minZ;
        double pushBack = box.maxZ - point.z;
        double minPush = Math.min(Math.min(pushTop, pushBottom), Math.min(Math.min(pushLeft, pushRight), Math.min(pushFront, pushBack)));

        if (minPush == pushTop) return new PushOut(new Vec3(point.x, box.maxY, point.z), true);
        if (minPush == pushBottom) return new PushOut(new Vec3(point.x, box.minY, point.z), false);
        if (minPush == pushLeft) return new PushOut(new Vec3(box.minX, point.y, point.z), false);
        if (minPush == pushRight) return new PushOut(new Vec3(box.maxX, point.y, point.z), false);
        if (minPush == pushFront) return new PushOut(new Vec3(point.x, point.y, box.minZ), false);
        return new PushOut(new Vec3(point.x, point.y, box.maxZ), false);
    }
}
