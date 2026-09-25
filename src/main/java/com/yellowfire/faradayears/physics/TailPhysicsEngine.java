package com.yellowfire.faradayears.physics;

import com.yellowfire.faradayears.ModAttachments;
import com.yellowfire.faradayears.capability.PlayerEarsTailData;
import com.yellowfire.faradayears.client.render.ProceduralTailRenderer;
import com.yellowfire.faradayears.physics.core.PhysicsChain;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * v49-gender-body-physics: adds dynamic jiggle/spring-damper physics (`simulateBodyPhysics`)
 * for 3D volumetric chest (`chestJiggleY/Z`) and hips/butt (`hipsJiggleY/Z`) when `gender == 1`.
 */
public class TailPhysicsEngine {
    public static final TailPhysicsEngine INSTANCE = new TailPhysicsEngine();
    public static final int MAX_USER_SEGMENTS = 6;
    public static final int MAX_PHYSICAL_SEGMENTS = 18;
    public static final int MAX_TAILS = 9;

    private final Map<UUID, PlayerPhysicsData> playerData = new HashMap<>();

    public static class EarHitbox {
        public double worldX, worldY, worldZ, radius;
        public boolean collided;
    }

    public static class TailChainInstance {
        public final PhysicsChain chain = new PhysicsChain();
        public Vec3[] points = new Vec3[MAX_PHYSICAL_SEGMENTS];
        public Vec3[] prevPoints = new Vec3[MAX_PHYSICAL_SEGMENTS];
        public Vec3[] smoothPoints = new Vec3[MAX_PHYSICAL_SEGMENTS];
        public double[] worldX = new double[MAX_PHYSICAL_SEGMENTS];
        public double[] worldY = new double[MAX_PHYSICAL_SEGMENTS];
        public double[] worldZ = new double[MAX_PHYSICAL_SEGMENTS];
        public double[] worldRadius = new double[MAX_PHYSICAL_SEGMENTS];
        public boolean[] isGround = new boolean[MAX_PHYSICAL_SEGMENTS];
    }

    public static class PlayerPhysicsData {
        public final TailChainInstance[] tails = new TailChainInstance[MAX_TAILS];
        public final EarHitbox[][] earHitboxes = new EarHitbox[2][3];

        public Vec3 root = Vec3.ZERO, prevRoot = Vec3.ZERO, smoothRoot = Vec3.ZERO;
        public Vec3 prevPlayerPos = Vec3.ZERO;

        public float leftEarPitch, prevLeftEarPitch, smoothLeftEarPitch;
        public float leftEarRoll, prevLeftEarRoll, smoothLeftEarRoll;
        private float velLeftEarPitch = 0.0f, velLeftEarRoll = 0.0f;

        public float rightEarPitch, prevRightEarPitch, smoothRightEarPitch;
        public float rightEarRoll, prevRightEarRoll, smoothRightEarRoll;
        private float velRightEarPitch = 0.0f, velRightEarRoll = 0.0f;

        // ★ ФИЗИКА ФИГУРЫ (Отдельная физика для каждого полушария груди, попы и мешочка):
        public float chestLeftJiggleY, prevChestLeftJiggleY, smoothChestLeftJiggleY;
        public float chestLeftJiggleZ, prevChestLeftJiggleZ, smoothChestLeftJiggleZ;
        private float velChestLeftY = 0.0f, velChestLeftZ = 0.0f;

        public float chestRightJiggleY, prevChestRightJiggleY, smoothChestRightJiggleY;
        public float chestRightJiggleZ, prevChestRightJiggleZ, smoothChestRightJiggleZ;
        private float velChestRightY = 0.0f, velChestRightZ = 0.0f;

        public float hipsLeftJiggleY, prevHipsLeftJiggleY, smoothHipsLeftJiggleY;
        public float hipsLeftJiggleZ, prevHipsLeftJiggleZ, smoothHipsLeftJiggleZ;
        private float velHipsLeftY = 0.0f, velHipsLeftZ = 0.0f;

        public float hipsRightJiggleY, prevHipsRightJiggleY, smoothHipsRightJiggleY;
        public float hipsRightJiggleZ, prevHipsRightJiggleZ, smoothHipsRightJiggleZ;
        private float velHipsRightY = 0.0f, velHipsRightZ = 0.0f;

        public float pouchJiggleY, prevPouchJiggleY, smoothPouchJiggleY;
        public float pouchJiggleZ, prevPouchJiggleZ, smoothPouchJiggleZ;
        private float velPouchY = 0.0f, velPouchZ = 0.0f;

        public float prevBodyRot = 0.0f;
        public float prevHeadRot = 0.0f;
        public boolean initialized = false;
        public int activeSegments = 12;
        public int activeTails = 1;

        public PlayerPhysicsData() {
            for (int i = 0; i < MAX_TAILS; i++) {
                tails[i] = new TailChainInstance();
            }
            for (int ear = 0; ear < 2; ear++) {
                for (int s = 0; s < 3; s++) {
                    earHitboxes[ear][s] = new EarHitbox();
                }
            }
        }

        public TailChainInstance getMain() { return tails[0]; }
    }

    public synchronized PlayerPhysicsData getOrData(AbstractClientPlayer player) {
        return playerData.computeIfAbsent(player.getUUID(), k -> new PlayerPhysicsData());
    }

    public synchronized void onClientTick(AbstractClientPlayer player) {
        if (player == null || player.isInvisible() || !player.isAlive()) return;

        PlayerPhysicsData state = getOrData(player);
        state.prevRoot = state.root;
        state.prevLeftEarPitch = state.leftEarPitch;
        state.prevLeftEarRoll = state.leftEarRoll;
        state.prevRightEarPitch = state.rightEarPitch;
        state.prevRightEarRoll = state.rightEarRoll;

        state.prevChestLeftJiggleY = state.chestLeftJiggleY;
        state.prevChestLeftJiggleZ = state.chestLeftJiggleZ;
        state.prevChestRightJiggleY = state.chestRightJiggleY;
        state.prevChestRightJiggleZ = state.chestRightJiggleZ;

        state.prevHipsLeftJiggleY = state.hipsLeftJiggleY;
        state.prevHipsLeftJiggleZ = state.hipsLeftJiggleZ;
        state.prevHipsRightJiggleY = state.hipsRightJiggleY;
        state.prevHipsRightJiggleZ = state.hipsRightJiggleZ;

        state.prevPouchJiggleY = state.pouchJiggleY;
        state.prevPouchJiggleZ = state.pouchJiggleZ;

        for (int t = 0; t < MAX_TAILS; t++) {
            TailChainInstance inst = state.tails[t];
            for (int i = 0; i < MAX_PHYSICAL_SEGMENTS; i++) {
                if (inst.points[i] != null) {
                    inst.prevPoints[i] = inst.points[i];
                }
            }
        }

        PlayerEarsTailData data = ModAttachments.get(player);
        {
            Vec3 playerPos = player.position();
            Vec3 root = applyTailOffset(computeTailRoot(player, data), player, data.getTailOffsetX(), data.getTailOffsetY(), data.getTailOffsetZ());
            Vec3 rootVelocity = root.subtract(state.prevRoot);
            float deltaHeadYaw = Mth.wrapDegrees(player.yHeadRot - state.prevHeadRot);

            boolean teleported = rootVelocity.lengthSqr() > 100.0D;
            if (teleported) {
                state.prevRoot = root;
                state.prevPlayerPos = playerPos;
            }

            int userSegments = Math.max(1, Math.min(MAX_USER_SEGMENTS, data.getTailSegments()));
            int activePhysicalSegments = userSegments * 3;
            double baseLength = Mth.clamp(data.getTailSegmentLength() / 16.0D * 1.22D, 0.28D, 0.66D);
            double segmentLength = baseLength / 3.0D;
            double baseRadius = Mth.clamp(0.25D * data.getTailScaleX(), 0.14D, 0.38D);
            state.activeSegments = activePhysicalSegments;

            int tailCount = data.getTailCount();
            boolean superVolumetric = (tailCount == 10);
            int activeTails = superVolumetric ? 1 : Math.max(1, Math.min(MAX_TAILS, tailCount));
            state.activeTails = activeTails;

            Vec3 centerDir = computeBackDirection(player, -0.75D);

            if (!state.initialized || teleported) {
                for (int t = 0; t < MAX_TAILS; t++) {
                    double fanRad = ProceduralTailRenderer.getFanAngleRad(t, activeTails, data.getTailFanSpread(), false);
                    Vec3 baseDir = centerDir;
                    if (fanRad != 0.0D && !superVolumetric) {
                        double cos = Math.cos(fanRad);
                        double sin = Math.sin(fanRad);
                        baseDir = new Vec3(centerDir.x * cos - centerDir.z * sin, centerDir.y, centerDir.x * sin + centerDir.z * cos).normalize();
                    }
                    state.tails[t].chain.reset(root, baseDir, activePhysicalSegments, segmentLength, baseRadius);
                    copyChainToInstance(state.tails[t], root, true);
                }
                state.root = root;
                state.prevRoot = root;
                state.smoothRoot = root;
                state.prevPlayerPos = playerPos;
                state.prevBodyRot = player.yBodyRot;
                state.prevHeadRot = player.yHeadRot;
                state.initialized = true;
            }

            for (int t = 0; t < activeTails; t++) {
                TailChainInstance inst = state.tails[t];
                double fanRad = ProceduralTailRenderer.getFanAngleRad(t, activeTails, data.getTailFanSpread(), false);
                Vec3 baseDir = centerDir;
                if (fanRad != 0.0D && !superVolumetric) {
                    double cos = Math.cos(fanRad);
                    double sin = Math.sin(fanRad);
                    baseDir = new Vec3(centerDir.x * cos - centerDir.z * sin, centerDir.y, centerDir.x * sin + centerDir.z * cos).normalize();
                }

                inst.chain.ensureSize(root, baseDir, activePhysicalSegments, segmentLength, baseRadius);
                inst.chain.damping = player.isInWaterOrBubble() ? 0.60D : 0.845D;
                inst.chain.iterations = 11;

                if (data.isAnimate()) {
                    Vec3 gravity = new Vec3(0, player.isFallFlying() ? -0.050D : (player.onGround() ? -0.070D : -0.060D), 0);
                    double baseStiffness = player.onGround() ? 0.45D : 0.36D;
                    inst.chain.simulateTailRope(player.level(), player, root, rootVelocity, gravity, baseDir, baseStiffness,
                            data.getTailWagAxis(), data.getTailWagAmplitude(), data.getTailWagSpeed(), t);
                } else {
                    inst.chain.reset(root, baseDir, activePhysicalSegments, segmentLength, baseRadius);
                }
                copyChainToInstance(inst, root, false);
            }

            state.root = root;
            simulateEars(state, player, playerPos, deltaHeadYaw);
            simulateBodyPhysics(state, player, playerPos, data);

            state.prevPlayerPos = playerPos;
            state.prevBodyRot = player.yBodyRot;
            state.prevHeadRot = player.yHeadRot;
        }
    }

    private Vec3 computeTailRoot(AbstractClientPlayer player, PlayerEarsTailData data) {
        double bodyYawRad = Math.toRadians(player.yBodyRot);
        double sinBody = Math.sin(bodyYawRad);
        double cosBody = Math.cos(bodyYawRad);

        double zOffsetBlocks = 0.0D;
        double yOffsetBlocks = 0.0D;
        if (data != null) {
            zOffsetBlocks = data.getTailRotX() / 60.0D;
            yOffsetBlocks = data.getTailRotX() / 32.0D;
        }

        double totalBackDist = 0.11D + zOffsetBlocks;
        double x = player.getX() + sinBody * totalBackDist;
        double y = player.getY() + (player.isCrouching() ? 0.59D : 0.73D) + yOffsetBlocks;
        double z = player.getZ() - cosBody * totalBackDist;
        return new Vec3(x, y, z);
    }

    private Vec3 applyTailOffset(Vec3 root, AbstractClientPlayer player, float offsetX, float offsetY, float offsetZ) {
        double bodyYawRad = Math.toRadians(player.yBodyRot);
        Vec3 right = new Vec3(Math.cos(bodyYawRad), 0, Math.sin(bodyYawRad));
        Vec3 back = new Vec3(Math.sin(bodyYawRad), 0, -Math.cos(bodyYawRad));
        return root.add(right.scale(offsetX / 16.0D)).add(0, offsetY / 16.0D, 0).add(back.scale(offsetZ / 16.0D));
    }

    private Vec3 computeBackDirection(AbstractClientPlayer player, double down) {
        double bodyYawRad = Math.toRadians(player.yBodyRot);
        return new Vec3(Math.sin(bodyYawRad), down, -Math.cos(bodyYawRad)).normalize();
    }

    private void copyChainToInstance(TailChainInstance inst, Vec3 root, boolean initializePrevious) {
        for (int i = 0; i < MAX_PHYSICAL_SEGMENTS; i++) {
            int source = Math.min(i, Math.max(0, inst.chain.particles.size() - 1));
            Vec3 point = inst.chain.particles.isEmpty() ? root : inst.chain.getPoint(source);
            double radius = inst.chain.particles.isEmpty() ? 0.20D : inst.chain.getRadius(source);
            boolean ground = !inst.chain.particles.isEmpty() && inst.chain.isGround(source);

            inst.points[i] = point;
            if (initializePrevious || inst.prevPoints[i] == null) inst.prevPoints[i] = point;
            inst.worldX[i] = point.x;
            inst.worldY[i] = point.y;
            inst.worldZ[i] = point.z;
            inst.worldRadius[i] = radius;
            inst.isGround[i] = ground;
        }
    }

    private static boolean isSolidBlock(BlockState state, Level level, BlockPos pos) {
        return !state.isAir() && !state.getCollisionShape(level, pos).isEmpty();
    }

    private void simulateEars(PlayerPhysicsData state, AbstractClientPlayer player, Vec3 playerPos, float deltaHeadYaw) {
        Vec3 playerVelocity = playerPos.subtract(state.prevPlayerPos);
        double horizontalSpeed = Math.sqrt(playerVelocity.x * playerVelocity.x + playerVelocity.z * playerVelocity.z);
        double vy = playerVelocity.y;

        double radPitch = Math.toRadians(player.getXRot() + state.leftEarPitch);
        double radYaw = Math.toRadians(player.yHeadRot);
        double headCenterY = playerPos.y + (player.isCrouching() ? 1.45D : 1.62D);
        double topOfHeadY = playerPos.y + (player.isCrouching() ? 1.48D : 1.80D);

        Level level = player.level();
        int leftStage = 0;
        int rightStage = 0;

        double cos = Math.cos(Math.toRadians(player.yBodyRot));
        double sin = Math.sin(Math.toRadians(player.yBodyRot));

        if (level != null) {
            for (int ear = 0; ear < 2; ear++) {
                double ex = playerPos.x + (ear == 0 ? 0.18D : -0.18D) * cos;
                double ez = playerPos.z + (ear == 0 ? 0.18D : -0.18D) * sin;

                double lowestCeiling = Double.NaN;
                AABB checkZone = new AABB(ex - 0.24D, topOfHeadY, ez - 0.24D,
                                          ex + 0.24D, topOfHeadY + 0.60D, ez + 0.24D);
                BlockPos minPos = BlockPos.containing(checkZone.minX, checkZone.minY, checkZone.minZ);
                BlockPos maxPos = BlockPos.containing(checkZone.maxX, checkZone.maxY, checkZone.maxZ);
                for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
                    BlockState bState = level.getBlockState(pos);
                    if (isSolidBlock(bState, level, pos)) {
                        VoxelShape shape = bState.getCollisionShape(level, pos);
                        if (!shape.isEmpty()) {
                            AABB box = shape.bounds().move(pos);
                            if (box.intersects(checkZone)) {
                                if (Double.isNaN(lowestCeiling) || box.minY < lowestCeiling) {
                                    lowestCeiling = box.minY;
                                }
                            }
                        }
                    }
                }

                int stage = 0;
                if (!Double.isNaN(lowestCeiling)) {
                    double clearance = lowestCeiling - topOfHeadY;
                    if (clearance <= 0.10D) stage = 3;
                    else if (clearance <= 0.25D) stage = 2;
                    else if (clearance <= 0.55D) stage = 1;
                }
                if (ear == 0) leftStage = stage; else rightStage = stage;
            }
        }

        boolean leftHitWall = false;
        boolean rightHitWall = false;
        for (int ear = 0; ear < 2; ear++) {
            double earRollRad = Math.toRadians((ear == 0 ? state.leftEarRoll : state.rightEarRoll) * (ear == 0 ? 1.0D : -1.0D));
            for (int s = 0; s < 3; s++) {
                EarHitbox hb = state.earHitboxes[ear][s];
                double d = 0.08D + s * 0.12D;
                double lx = (ear == 0 ? 0.16D : -0.16D) + Math.sin(earRollRad) * d;
                double ly = 0.22D + Math.cos(Math.toRadians(ear == 0 ? state.leftEarPitch : state.rightEarPitch)) * Math.cos(earRollRad) * d;
                double lz = -0.04D + Math.sin(Math.toRadians(ear == 0 ? state.leftEarPitch : state.rightEarPitch)) * d;

                double rx = lx;
                double ry = ly * Math.cos(radPitch) - lz * Math.sin(radPitch);
                double rz = ly * Math.sin(radPitch) + lz * Math.cos(radPitch);

                hb.worldX = playerPos.x + rx * Math.cos(radYaw) + rz * Math.sin(radYaw);
                hb.worldY = headCenterY + ry;
                hb.worldZ = playerPos.z - rx * Math.sin(radYaw) + rz * Math.cos(radYaw);
                hb.radius = 0.11D - s * 0.02D;
                hb.collided = false;

                if (level != null) {
                    AABB sphereBox = getSegmentAABB(hb.worldX, hb.worldY, hb.worldZ, hb.radius);
                    BlockPos minP = BlockPos.containing(sphereBox.minX, sphereBox.minY, sphereBox.minZ);
                    BlockPos maxP = BlockPos.containing(sphereBox.maxX, sphereBox.maxY, sphereBox.maxZ);
                    for (BlockPos pos : BlockPos.betweenClosed(minP, maxP)) {
                        BlockState bState = level.getBlockState(pos);
                        if (isSolidBlock(bState, level, pos)) {
                            VoxelShape shape = bState.getCollisionShape(level, pos);
                            if (!shape.isEmpty()) {
                                AABB box = shape.bounds().move(pos);
                                if (box.intersects(sphereBox)) {
                                    hb.collided = true;
                                    if (ear == 0) leftHitWall = true; else rightHitWall = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        float gravityTilt = player.onGround() ? 3.0f : 5.0f;
        float baseTargetPitch = gravityTilt - (float) (horizontalSpeed * 22.0D) + (float) (vy * 18.0D);
        float baseTargetRoll = -deltaHeadYaw * 0.45f;

        float targetLeftPitch = baseTargetPitch;
        float targetLeftRoll = baseTargetRoll;
        if (leftStage == 3 || (leftHitWall && leftStage > 0)) { targetLeftPitch += 88.0f; targetLeftRoll -= 52.0f; }
        else if (leftStage == 2 || leftHitWall) { targetLeftPitch += 68.0f; targetLeftRoll -= 38.0f; }
        else if (leftStage == 1) { targetLeftPitch += 38.0f; targetLeftRoll -= 18.0f; }

        float forceLeftPitch = (targetLeftPitch - state.leftEarPitch) * 0.72f;
        state.velLeftEarPitch = (state.velLeftEarPitch + forceLeftPitch) * 0.76f;
        state.leftEarPitch = Mth.clamp(state.leftEarPitch + state.velLeftEarPitch, -40.0f, 90.0f);

        float forceLeftRoll = (targetLeftRoll - state.leftEarRoll) * 0.72f;
        state.velLeftEarRoll = (state.velLeftEarRoll + forceLeftRoll) * 0.76f;
        state.leftEarRoll = Mth.clamp(state.leftEarRoll + state.velLeftEarRoll, -55.0f, 55.0f);

        float targetRightPitch = baseTargetPitch;
        float targetRightRoll = baseTargetRoll;
        if (rightStage == 3 || (rightHitWall && rightStage > 0)) { targetRightPitch += 88.0f; targetRightRoll += 52.0f; }
        else if (rightStage == 2 || rightHitWall) { targetRightPitch += 68.0f; targetRightRoll += 38.0f; }
        else if (rightStage == 1) { targetRightPitch += 38.0f; targetRightRoll += 18.0f; }

        float forceRightPitch = (targetRightPitch - state.rightEarPitch) * 0.72f;
        state.velRightEarPitch = (state.velRightEarPitch + forceRightPitch) * 0.76f;
        state.rightEarPitch = Mth.clamp(state.rightEarPitch + state.velRightEarPitch, -40.0f, 90.0f);

        float forceRightRoll = (targetRightRoll - state.rightEarRoll) * 0.72f;
        state.velRightEarRoll = (state.velRightEarRoll + forceRightRoll) * 0.76f;
        state.rightEarRoll = Mth.clamp(state.rightEarRoll + state.velRightEarRoll, -55.0f, 55.0f);
    }

    private void simulateBodyPhysics(PlayerPhysicsData state, AbstractClientPlayer player, Vec3 playerPos, PlayerEarsTailData data) {
        if ((data.getGender() != 1 && data.getGender() != 3 && !data.isShowPouch()) || !data.isAnimate()) {
            state.velChestLeftY = state.velChestLeftZ = state.velChestRightY = state.velChestRightZ = 0.0f;
            state.velHipsLeftY = state.velHipsLeftZ = state.velHipsRightY = state.velHipsRightZ = 0.0f;
            state.velPouchY = state.velPouchZ = 0.0f;
            state.chestLeftJiggleY = state.chestLeftJiggleZ = state.chestRightJiggleY = state.chestRightJiggleZ = 0.0f;
            state.hipsLeftJiggleY = state.hipsLeftJiggleZ = state.hipsRightJiggleY = state.hipsRightJiggleZ = 0.0f;
            state.pouchJiggleY = state.pouchJiggleZ = 0.0f;
            return;
        }

        Vec3 vel = playerPos.subtract(state.prevPlayerPos);
        double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        double vy = vel.y;
        float str = Mth.clamp(data.getBodyJiggleStrength(), 0.0f, 2.0f);
        double deltaYaw = Mth.wrapDegrees(player.yBodyRot - state.prevBodyRot);
        float turnInertia = (float) (deltaYaw * 0.09D) * str;

        if (data.getGender() == 1 || data.getGender() == 3) {
            // ★ 1. ФИЗИКА ГРУДИ (Умеренная вертикальная инерция и упругие горизонтальные колебания):
            double chestAngle = player.tickCount * 1.15D;
            float targetCLY = (float) (-vy * 2.4D + Math.sin(chestAngle) * speed * 1.6D + Math.cos(player.tickCount * 0.45D) * 0.1D) * str;
            float targetCLZ = (float) (-speed * 4.0D + Math.cos(chestAngle) * speed * 2.2D - turnInertia * 0.8f) * str;
            float forceCLY = (targetCLY - state.chestLeftJiggleY) * 0.44f;
            state.velChestLeftY = (state.velChestLeftY + forceCLY) * 0.74f;
            state.chestLeftJiggleY = Mth.clamp(state.chestLeftJiggleY + state.velChestLeftY, -1.2f, 1.2f);

            float forceCLZ = (targetCLZ - state.chestLeftJiggleZ) * 0.36f;
            state.velChestLeftZ = (state.velChestLeftZ + forceCLZ) * 0.86f;
            state.chestLeftJiggleZ = Mth.clamp(state.chestLeftJiggleZ + state.velChestLeftZ, -2.2f, 2.2f);

            float targetCRY = (float) (-vy * 2.4D + Math.sin(chestAngle + Math.PI * 0.85D) * speed * 1.6D + Math.cos(player.tickCount * 0.45D + 0.3D) * 0.1D) * str;
            float targetCRZ = (float) (-speed * 4.0D + Math.cos(chestAngle + Math.PI * 0.85D) * speed * 2.2D + turnInertia * 0.8f) * str;
            float forceCRY = (targetCRY - state.chestRightJiggleY) * 0.44f;
            state.velChestRightY = (state.velChestRightY + forceCRY) * 0.74f;
            state.chestRightJiggleY = Mth.clamp(state.chestRightJiggleY + state.velChestRightY, -1.2f, 1.2f);

            float forceCRZ = (targetCRZ - state.chestRightJiggleZ) * 0.36f;
            state.velChestRightZ = (state.velChestRightZ + forceCRZ) * 0.86f;
            state.chestRightJiggleZ = Mth.clamp(state.chestRightJiggleZ + state.velChestRightZ, -2.2f, 2.2f);

            // ★ 2. ФИЗИКА ПОПЫ / БЁДЕР (Умеренная вертикальная инерция, медленная частота, противофаза к груди и глубокие горизонтальные колебания Z):
            double hipsAngle = player.tickCount * 0.72D;
            float targetHLY = (float) (-vy * 2.8D + Math.sin(hipsAngle + Math.PI * 0.65D) * speed * 2.0D) * str;
            float targetHLZ = (float) (-speed * 5.5D + Math.cos(hipsAngle + Math.PI * 0.65D) * speed * 3.6D + turnInertia * 1.5f) * str;
            float forceHLY = (targetHLY - state.hipsLeftJiggleY) * 0.40f;
            state.velHipsLeftY = (state.velHipsLeftY + forceHLY) * 0.76f;
            state.hipsLeftJiggleY = Mth.clamp(state.hipsLeftJiggleY + state.velHipsLeftY, -1.3f, 1.3f);

            float forceHLZ = (targetHLZ - state.hipsLeftJiggleZ) * 0.22f;
            state.velHipsLeftZ = (state.velHipsLeftZ + forceHLZ) * 0.92f;
            state.hipsLeftJiggleZ = Mth.clamp(state.hipsLeftJiggleZ + state.velHipsLeftZ, -2.6f, 2.6f);

            float targetHRY = (float) (-vy * 2.8D + Math.sin(hipsAngle + Math.PI * 1.65D) * speed * 2.0D) * str;
            float targetHRZ = (float) (-speed * 5.5D + Math.cos(hipsAngle + Math.PI * 1.65D) * speed * 3.6D - turnInertia * 1.5f) * str;
            float forceHRY = (targetHRY - state.hipsRightJiggleY) * 0.40f;
            state.velHipsRightY = (state.velHipsRightY + forceHRY) * 0.76f;
            state.hipsRightJiggleY = Mth.clamp(state.hipsRightJiggleY + state.velHipsRightY, -1.3f, 1.3f);

            float forceHRZ = (targetHRZ - state.hipsRightJiggleZ) * 0.22f;
            state.velHipsRightZ = (state.velHipsRightZ + forceHRZ) * 0.92f;
            state.hipsRightJiggleZ = Mth.clamp(state.hipsRightJiggleZ + state.velHipsRightZ, -2.6f, 2.6f);
        }

        if (data.isShowPouch()) {
            float targetPY = (float) (-vy * 4.8D + Math.sin(player.tickCount * 1.1D) * speed * 3.5D) * str;
            float targetPZ = (float) (speed * 3.6D) * str;
            float forcePY = (targetPY - state.pouchJiggleY) * 0.32f;
            state.velPouchY = (state.velPouchY + forcePY) * 0.89f;
            state.pouchJiggleY = Mth.clamp(state.pouchJiggleY + state.velPouchY, -2.2f, 2.2f);

            float forcePZ = (targetPZ - state.pouchJiggleZ) * 0.32f;
            state.velPouchZ = (state.velPouchZ + forcePZ) * 0.89f;
            state.pouchJiggleZ = Mth.clamp(state.pouchJiggleZ + state.velPouchZ, -2.2f, 2.2f);
        }

        simulateBodyCollisions(state, player, playerPos, data);
    }

    private void simulateBodyCollisions(PlayerPhysicsData state, AbstractClientPlayer player, Vec3 playerPos, PlayerEarsTailData data) {
        Level level = player.level();
        if (level == null) return;

        double cos = Math.cos(Math.toRadians(player.yBodyRot));
        double sin = Math.sin(Math.toRadians(player.yBodyRot));
        double fx = sin;
        double fz = -cos;
        double rx = cos;
        double rz = sin;

        // ★ 1. ВЗАИМНАЯ КОЛЛИЗИЯ И ОГРАНИЧИТЕЛИ (Inter-part boundaries & mutual repulsion):
        if (data.isShowPouch() && (data.getGender() == 1 || data.getGender() == 3)) {
            // Мешочек и грудь отталкиваются, если колебания сдвигают их слишком близко друг к другу:
            float minChestY = Math.min(state.chestLeftJiggleY, state.chestRightJiggleY);
            if (state.pouchJiggleY > minChestY - 1.3f) {
                float push = (state.pouchJiggleY - (minChestY - 1.3f)) * 0.5f;
                state.pouchJiggleY -= push;
                state.velPouchY -= push * 0.6f;
                if (state.chestLeftJiggleY <= state.chestRightJiggleY) {
                    state.chestLeftJiggleY += push;
                    state.velChestLeftY += push * 0.6f;
                } else {
                    state.chestRightJiggleY += push;
                    state.velChestRightY += push * 0.6f;
                }
            }
        }

        // ★ 2. КОЛЛИЗИЯ КАЖДОГО ПОЛУШАРИЯ ГРУДИ С БЛОКАМИ И СТЕНАМИ В МИРЕ:
        if (data.getGender() == 1 || data.getGender() == 3) {
            double chestHeight = playerPos.y + (player.isCrouching() ? 1.08D : 1.25D) + (data.getChestOffsetY() + state.chestLeftJiggleY) * 0.0625D;
            double clX = playerPos.x + 0.13D * rx + fx * (0.16D + data.getChestScaleZ() * 0.15D + state.chestLeftJiggleZ * 0.04D);
            double clZ = playerPos.z + 0.13D * rz + fz * (0.16D + data.getChestScaleZ() * 0.15D + state.chestLeftJiggleZ * 0.04D);
            AABB boxCL = new AABB(clX - 0.22D, chestHeight - 0.18D, clZ - 0.22D, clX + 0.22D, chestHeight + 0.18D, clZ + 0.22D);
            collideAndSquashChest(level, boxCL, state, true);

            double crX = playerPos.x - 0.13D * rx + fx * (0.16D + data.getChestScaleZ() * 0.15D + state.chestRightJiggleZ * 0.04D);
            double crZ = playerPos.z - 0.13D * rz + fz * (0.16D + data.getChestScaleZ() * 0.15D + state.chestRightJiggleZ * 0.04D);
            AABB boxCR = new AABB(crX - 0.22D, chestHeight - 0.18D, crZ - 0.22D, crX + 0.22D, chestHeight + 0.18D, crZ + 0.22D);
            collideAndSquashChest(level, boxCR, state, false);

            // ★ 3. КОЛЛИЗИЯ КАЖДОГО ПОЛУШАРИЯ ПОПЫ / БЁДЕР С БЛОКАМИ, СТЕНАМИ И СТУПЕНЯМИ В МИРЕ:
            double hipsHeight = playerPos.y + (player.isCrouching() ? 0.68D : 0.85D) + (data.getHipsOffsetY() + state.hipsLeftJiggleY) * 0.0625D;
            double hlX = playerPos.x + 0.14D * rx - fx * (0.15D + data.getHipsScaleZ() * 0.16D - state.hipsLeftJiggleZ * 0.04D);
            double hlZ = playerPos.z + 0.14D * rz - fz * (0.15D + data.getHipsScaleZ() * 0.16D - state.hipsLeftJiggleZ * 0.04D);
            AABB boxHL = new AABB(hlX - 0.24D, hipsHeight - 0.20D, hlZ - 0.24D, hlX + 0.24D, hipsHeight + 0.20D, hlZ + 0.24D);
            collideAndSquashHips(level, boxHL, state, true);

            double hrX = playerPos.x - 0.14D * rx - fx * (0.15D + data.getHipsScaleZ() * 0.16D - state.hipsRightJiggleZ * 0.04D);
            double hrZ = playerPos.z - 0.14D * rz - fz * (0.15D + data.getHipsScaleZ() * 0.16D - state.hipsRightJiggleZ * 0.04D);
            AABB boxHR = new AABB(hrX - 0.24D, hipsHeight - 0.20D, hrZ - 0.24D, hrX + 0.24D, hipsHeight + 0.20D, hrZ + 0.24D);
            collideAndSquashHips(level, boxHR, state, false);
        }

        // ★ 4. КОЛЛИЗИЯ ПЯСНОГО МЕШОЧКА С БЛОКАМИ И ЗАБОРАМИ В МИРЕ:
        if (data.isShowPouch()) {
            double pouchHeight = playerPos.y + (player.isCrouching() ? 0.76D : 0.96D) + (data.getPouchOffsetY() + 10.5f + state.pouchJiggleY) * 0.0625D;
            double pX = playerPos.x + (data.getPouchOffsetX() * 0.0625D) * rx + fx * (0.14D + (data.getPouchOffsetZ() - 2.1f) * 0.0625D + data.getPouchScaleZ() * 0.15D);
            double pZ = playerPos.z + (data.getPouchOffsetX() * 0.0625D) * rz + fz * (0.14D + (data.getPouchOffsetZ() - 2.1f) * 0.0625D + data.getPouchScaleZ() * 0.15D);
            AABB boxP = new AABB(pX - 0.20D, pouchHeight - 0.15D, pZ - 0.20D, pX + 0.20D, pouchHeight + 0.15D, pZ + 0.20D);
            collideAndSquashPouch(level, boxP, state);
        }
    }

    private void collideAndSquashChest(Level level, AABB zone, PlayerPhysicsData state, boolean isLeft) {
        BlockPos minPos = BlockPos.containing(zone.minX, zone.minY, zone.minZ);
        BlockPos maxPos = BlockPos.containing(zone.maxX, zone.maxY, zone.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState bState = level.getBlockState(pos);
            if (isSolidBlock(bState, level, pos)) {
                VoxelShape shape = bState.getCollisionShape(level, pos);
                if (!shape.isEmpty()) {
                    AABB box = shape.bounds().move(pos);
                    if (box.intersects(zone)) {
                        double overlap = Math.max(0.0D, Math.min(zone.maxX - box.minX, box.maxX - zone.minX));
                        float squash = (float) Math.min(2.0D, overlap * 8.0D);
                        if (squash > 0.01f) {
                            if (isLeft) {
                                state.chestLeftJiggleZ -= squash * 1.5f;
                                state.velChestLeftZ = Mth.clamp(state.velChestLeftZ - squash * 0.8f, -2.4f, 0.0f);
                                state.chestLeftJiggleY += squash * 0.35f;
                                state.chestLeftJiggleZ = Mth.clamp(state.chestLeftJiggleZ, -2.4f, 2.4f);
                            } else {
                                state.chestRightJiggleZ -= squash * 1.5f;
                                state.velChestRightZ = Mth.clamp(state.velChestRightZ - squash * 0.8f, -2.4f, 0.0f);
                                state.chestRightJiggleY += squash * 0.35f;
                                state.chestRightJiggleZ = Mth.clamp(state.chestRightJiggleZ, -2.4f, 2.4f);
                            }
                        }
                    }
                }
            }
        }
    }

    private void collideAndSquashHips(Level level, AABB zone, PlayerPhysicsData state, boolean isLeft) {
        BlockPos minPos = BlockPos.containing(zone.minX, zone.minY, zone.minZ);
        BlockPos maxPos = BlockPos.containing(zone.maxX, zone.maxY, zone.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState bState = level.getBlockState(pos);
            if (isSolidBlock(bState, level, pos)) {
                VoxelShape shape = bState.getCollisionShape(level, pos);
                if (!shape.isEmpty()) {
                    AABB box = shape.bounds().move(pos);
                    if (box.intersects(zone)) {
                        double overlap = Math.max(0.0D, Math.min(zone.maxX - box.minX, box.maxX - zone.minX));
                        float squash = (float) Math.min(2.2D, overlap * 8.5D);
                        if (squash > 0.01f) {
                            if (isLeft) {
                                state.hipsLeftJiggleZ += squash * 1.6f;
                                state.velHipsLeftZ = Mth.clamp(state.velHipsLeftZ + squash * 0.85f, 0.0f, 2.6f);
                                state.hipsLeftJiggleY += squash * 0.35f;
                                state.hipsLeftJiggleZ = Mth.clamp(state.hipsLeftJiggleZ, -2.6f, 2.6f);
                            } else {
                                state.hipsRightJiggleZ += squash * 1.6f;
                                state.velHipsRightZ = Mth.clamp(state.velHipsRightZ + squash * 0.85f, 0.0f, 2.6f);
                                state.hipsRightJiggleY += squash * 0.35f;
                                state.hipsRightJiggleZ = Mth.clamp(state.hipsRightJiggleZ, -2.6f, 2.6f);
                            }
                        }
                    }
                }
            }
        }
    }

    private void collideAndSquashPouch(Level level, AABB zone, PlayerPhysicsData state) {
        BlockPos minPos = BlockPos.containing(zone.minX, zone.minY, zone.minZ);
        BlockPos maxPos = BlockPos.containing(zone.maxX, zone.maxY, zone.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState bState = level.getBlockState(pos);
            if (isSolidBlock(bState, level, pos)) {
                VoxelShape shape = bState.getCollisionShape(level, pos);
                if (!shape.isEmpty()) {
                    AABB box = shape.bounds().move(pos);
                    if (box.intersects(zone)) {
                        double overlap = Math.max(0.0D, Math.min(zone.maxX - box.minX, box.maxX - zone.minX));
                        float squash = (float) Math.min(2.0D, overlap * 8.0D);
                        if (squash > 0.01f) {
                            state.pouchJiggleZ -= squash * 1.6f;
                            state.velPouchZ = Mth.clamp(state.velPouchZ - squash * 0.8f, -2.4f, 0.0f);
                            state.pouchJiggleY -= squash * 0.25f;
                            state.pouchJiggleZ = Mth.clamp(state.pouchJiggleZ, -2.4f, 2.4f);
                        }
                    }
                }
            }
        }
    }

    public synchronized void onRenderInterpolate(AbstractClientPlayer player, float partialTick) {
        if (player == null || player.isInvisible()) return;
        PlayerPhysicsData state = getOrData(player);

        state.smoothRoot = lerpVec(partialTick, state.prevRoot, state.root);
        for (int t = 0; t < state.activeTails; t++) {
            TailChainInstance inst = state.tails[t];
            for (int i = 0; i < MAX_PHYSICAL_SEGMENTS; i++) {
                Vec3 prev = inst.prevPoints[i] == null ? inst.points[i] : inst.prevPoints[i];
                Vec3 cur = inst.points[i] == null ? state.root : inst.points[i];
                inst.smoothPoints[i] = lerpVec(partialTick, prev, cur);
            }
        }
        state.smoothLeftEarPitch = Mth.lerp(partialTick, state.prevLeftEarPitch, state.leftEarPitch);
        state.smoothLeftEarRoll = Mth.lerp(partialTick, state.prevLeftEarRoll, state.leftEarRoll);
        state.smoothRightEarPitch = Mth.lerp(partialTick, state.prevRightEarPitch, state.rightEarPitch);
        state.smoothRightEarRoll = Mth.lerp(partialTick, state.prevRightEarRoll, state.rightEarRoll);

        state.smoothChestLeftJiggleY = Mth.lerp(partialTick, state.prevChestLeftJiggleY, state.chestLeftJiggleY);
        state.smoothChestLeftJiggleZ = Mth.lerp(partialTick, state.prevChestLeftJiggleZ, state.chestLeftJiggleZ);
        state.smoothChestRightJiggleY = Mth.lerp(partialTick, state.prevChestRightJiggleY, state.chestRightJiggleY);
        state.smoothChestRightJiggleZ = Mth.lerp(partialTick, state.prevChestRightJiggleZ, state.chestRightJiggleZ);

        state.smoothHipsLeftJiggleY = Mth.lerp(partialTick, state.prevHipsLeftJiggleY, state.hipsLeftJiggleY);
        state.smoothHipsLeftJiggleZ = Mth.lerp(partialTick, state.prevHipsLeftJiggleZ, state.hipsLeftJiggleZ);
        state.smoothHipsRightJiggleY = Mth.lerp(partialTick, state.prevHipsRightJiggleY, state.hipsRightJiggleY);
        state.smoothHipsRightJiggleZ = Mth.lerp(partialTick, state.prevHipsRightJiggleZ, state.hipsRightJiggleZ);

        state.smoothPouchJiggleY = Mth.lerp(partialTick, state.prevPouchJiggleY, state.pouchJiggleY);
        state.smoothPouchJiggleZ = Mth.lerp(partialTick, state.prevPouchJiggleZ, state.pouchJiggleZ);
    }

    private Vec3 lerpVec(float partial, Vec3 a, Vec3 b) {
        if (a == null) return b == null ? Vec3.ZERO : b;
        if (b == null) return a;
        return new Vec3(Mth.lerp(partial, a.x, b.x), Mth.lerp(partial, a.y, b.y), Mth.lerp(partial, a.z, b.z));
    }

    public static AABB getSegmentAABB(double worldX, double worldY, double worldZ, double radius) {
        return new AABB(worldX - radius, worldY - radius, worldZ - radius,
                worldX + radius, worldY + radius, worldZ + radius);
    }
}
