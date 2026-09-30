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
 *  - {@link #MODE_CLASSIC}   «Классика»     — поведение 1.0.0: мягкая верёвка, стелется по земле
 *                                              (градиент гравитации к кончику + settleDistalNearGround).
 *  - {@link #MODE_REALISTIC} «Реалистичная»  — ★ 1.3.2: кошачий/лисий хвост с КОНТЕКСТНЫМ несением:
 *                                              в покое свисает вниз до земли, на рыси опущен и вытянут,
 *                                              в спринте — прямой вымпел-противовес, на охоте прижат
 *                                              к земле, в прыжке — стабилизатор; при повороте хлещет
 *                                              против поворота (руль гепарда), при уроне — распушается
 *                                              и хлещет; сидя/лёжа — обвивается вокруг ног.
 */
public class PhysicsChain {
    public static final int MODE_CLASSIC = 0;
    public static final int MODE_REALISTIC = 1;

    /** ★ 1.3.5: угол выхода хвоста из поясницы (рад; вниз < 0) — анатомия ДВУНОГОГО зверолюда:
     *  позвоночник человека вертикален, хвост выходит из крестца/копчика ВНИЗ-НАЗАД (~−35°),
     *  а не горизонтально, как у четвероногих (Reddit r/Artadvice, tumblr fantasy-anatomy). */
    public static final double EXIT_ANGLE = -0.62D;

    public final List<PhysicsParticle> particles = new ArrayList<>();

    public double segmentLength = 0.42D;
    public double damping = 0.84D;
    public int iterations = 10;
    public int physicsMode = MODE_CLASSIC;

    /**
     * Диагностика A/B: JVM-аргумент -Dfaradayears.passiveTail=true отключает мышечные
     * моменты и активную мимику только режима 1, оставляя скелет, массу и коллизии.
     */
    private static final boolean PASSIVE_REALISTIC_TAIL = Boolean.getBoolean("faradayears.passiveTail");

    /** ★ 1.3.0: 0..1 — насколько хвост сейчас «обвит» вокруг ног (плавный вход/выход из позы сидя). */
    public double sitBlend = 0.0D;

    // ★ 1.3.2 КОНТЕКСТНАЯ ПОЗА «КОШКА/ЛИСА»: несение хвоста зависит от того, что делает игрок.
    // Значения сглаживаются в TailPhysicsEngine и присваиваются цепочке каждый тик.
    /** Угол выноса от горизонтали (рад; отрицательный = вниз): стоя ~-60°, рысь ~-23°, спринт ~-5°. */
    public double carryAngle = -1.05D;
    /** Подкрутка кончика к пятке в покое (рад) — как у расслабленной кошки. */
    public double tipCurl = 0.35D;
    /** «Тонус» мышц 0..1: 0 = вяло висит под своей тяжестью, 1 = несётся прямо (спринт/прыжок). */
    public double tension = 0.30D;
    /** 0..1 — движется ли игрок (для походочного покачивания в такт шагам). */
    public double moveBlend = 0.0D;
    /** 0..1 — крадётся (хвост прижат к земле, как кошка на охоте). */
    public double sneakBlend = 0.0D;
    /** Фаза шага (рад) — набирается пройденной дистанцией, а не временем: в покое хвост не качается. */
    public double gaitPhase = 0.0D;
    /** Сдвиг фазы для веера хвостов — чтобы хвосты качались вразнобой, а не строем. */
    public double swayPhase = 0.0D;
    /** «Руль» (ограниченный): инерционный удар хвостом ПРОТИВ поворота — как хвост гепарда. */
    public double turnLash = 0.0D;
    /** 0..1 — испуг (урон): хвост распушается и хлещет из стороны в сторону. */
    public double scareBlend = 0.0D;
    /** Таймеры редких «нервных тиков» кончика в покое (кошка чем-то заинтересована). */
    public int flickCooldown = 80;
    public int flickTick = 0;

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
            // Реалистичный режим стартует прямой цепью позвонков от копчика. Форма должна
            // возникнуть из гравитации, суставов и мышечного момента, а не из готовой S-кривой.
            Vec3 step;
            step = physicsMode == MODE_REALISTIC ? skeletalRootDirection(dir) : dir;
            p = p.add(step.scale(segLen));
            double taper = count <= 1 ? 0.0D : i / (double) (count - 1);
            PhysicsParticle particle = new PhysicsParticle(p, Math.max(0.09D, baseRadius * (1.0D - taper * 0.42D)));
            // Распределённая масса мягких тканей: мясистое основание тяжелее, костный
            // и меховой кончик легче. Гравитационное ускорение ниже компенсируется массой.
            particle.mass = Mth.lerp(taper, 1.40D, 0.48D);
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
                                 int wagAxis, float wagAmp, float wagSpeed, int tailIndex, double sitBlendIn) {
        if (particles.isEmpty()) return;
        this.sitBlend = Math.max(0.0D, Math.min(1.0D, sitBlendIn));

        // ★ 1.3.2: автоколебания кончика — редкие короткие «тики» в покое (не в обвиве/движении):
        if (physicsMode == MODE_REALISTIC && sitBlend < 0.5D) {
            if (flickTick > 0) {
                flickTick--;
                if (flickTick == 0) flickCooldown = 70 + (int) (Math.random() * 140.0D);
            } else if (--flickCooldown <= 0 && moveBlend < 0.30D && scareBlend < 0.30D) {
                flickTick = 12;
            }
        }

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

        // ★ 1.3.0: цели «обвивания вокруг ног» для позы сидя/сна (кошачий заворот):
        // Старые режимы сохраняют процедурный обвив. В реалистичном режиме готовая спираль
        // отключена: на первом скелетном этапе хвост должен лечь под массой и коллизиями.
        Vec3[] sitTargets = (physicsMode != MODE_REALISTIC && sitBlend > 0.01D && owner != null)
                ? computeSitCurlTargets(level, owner, baseDir, root)
                : null;

        // 1) Verlet integration. Распределение гравитации зависит от режима:
        //    Классика   — почти невесомое основание и усиленный к кончику градиент (1.0.0);
        //    Реалистичная — естественный градиент: несущее основание + тяжёлый кончик;
        for (int i = 0; i < particles.size(); i++) {
            PhysicsParticle p = particles.get(i);
            p.touchingGround = false;
            double distal = (particles.size() <= 1) ? 1.0D : i / (double) (particles.size() - 1);
            Vec3 lateralWag = Vec3.ZERO;
            // В реалистичном режиме старый синус осей не используется: wagAxis теперь лишь
            // переключатель намеренного поведения (3=выкл., остальные legacy=реалистичное).
            boolean wagActive = (physicsMode != MODE_REALISTIC && wagAxis != 3 && wagAmp > 0.05f && i > 0);
            if (wagActive) {
                double segTime = time - distal * 1.5D; // Бегущая волна виляния по длине хвоста
                double wagForce = Math.sin(segTime) * (wagAmp * 0.0075D);
                double wagForce2 = Math.cos(segTime * 0.8D) * (wagAmp * 0.006D);
                if (wagAxis == 0) lateralWag = sideVector.scale(wagForce * distal);
                else if (wagAxis == 1) lateralWag = upVector.scale(wagForce * distal);
                else if (wagAxis == 2) lateralWag = sideVector.scale(wagForce * distal).add(upVector.scale(wagForce2 * distal));
            }
            // ★ 1.3.2 ЖИВАЯ МИМИКА ХВОСТА (реалистичный режим) — по наблюдениям за настоящими
            // кошками и лисами: ленивое покачивание в покое, редкий «нервный тик» кончика,
            // покачивание в такт шагам, инерционный хлыст ПРОТИВ поворота (руль гепарда),
            // а при испуге — быстрый хлёст из стороны в сторону.
            if (physicsMode == MODE_REALISTIC && i > 0) {
                double tick = (owner != null ? owner.tickCount : 0.0D);
                double idleScale = (1.0D - sitBlend) * (1.0D - moveBlend) * (wagActive ? 0.35D : 1.0D);
                // Физический противовес остаётся даже при выключенной мимике.
                double fx = turnLash;
                if (wagAxis != 3) {
                    fx += Math.sin(tick * 0.045D + swayPhase - distal * 2.2D) * 0.0012D * idleScale;
                    if (flickTick > 0) {
                        double flickEnv = Math.sin(flickTick * 0.85D) * (flickTick / 12.0D);
                        fx += flickEnv * 0.006D * smoothstep(0.55D, 1.0D, distal) * idleScale;
                    }
                    // Основное движение при ходьбе создаёт инерция; мимика лишь дополняет её.
                    fx += Math.sin(gaitPhase + swayPhase - distal * 1.8D) * 0.0008D * moveBlend * tension;
                    if (scareBlend > 0.01D) {
                        fx += Math.sin(tick * 0.75D + distal * 1.5D) * 0.006D * scareBlend;
                    }
                }
                lateralWag = lateralWag.add(sideVector.scale(fx * distal));
            }
            double gScale;
            if (physicsMode == MODE_CLASSIC) {
                gScale = (i == 0) ? 0.08D : (1.0D + distal * 0.35D);
            } else if (physicsMode == MODE_REALISTIC) {
                // Скелетно-мышечная модель: масса не исчезает при напряжении мышц. Тонус
                // создаёт ограниченный момент в суставах ниже, а гравитация действует всегда.
                gScale = (i == 0) ? 0.48D : (0.82D + distal * 0.38D);
            } else {
                gScale = 1.0D;
            }
            if (sitBlend > 0.0D && physicsMode != MODE_REALISTIC) {
                gScale *= (1.0D - 0.65D * sitBlend); // процедурный обвив старых режимов
            }
            // applyForce делит на массу: умножаем гравитацию на массу, чтобы ускорение
            // свободного падения не зависело от толщины тканей. Масса остаётся доступна
            // для последующего mass-weighted решателя суставов.
            p.applyForce(gravity.scale(gScale * p.mass));
            if (lateralWag.lengthSqr() > 0.0D
                    && !(physicsMode == MODE_REALISTIC && PASSIVE_REALISTIC_TAIL)) {
                p.applyForce(lateralWag);
            }
            // Мягкие ткани и густой мех гасят высокочастотный хлыст к кончику.
            double tissueDamping = physicsMode == MODE_REALISTIC
                    ? Mth.clamp(damping - distal * 0.035D, 0.72D, 0.92D)
                    : damping;
            p.verlet(tissueDamping);
        }

        boolean crouching = (owner != null && owner.isShiftKeyDown());

        // Запоминаем результат чистой интеграции. Позже отделим физическую скорость от
        // позиционной коррекции PBD — иначе коррекция сустава превращается в новый импульс
        // и хвост попеременно «замирает / выстреливает».
        Vec3[] integratedPositions = physicsMode == MODE_REALISTIC ? new Vec3[particles.size()] : null;
        if (integratedPositions != null) {
            for (int i = 0; i < particles.size(); i++) integratedPositions[i] = particles.get(i).position;
        }

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

                Vec3 jointDir = physicsMode == MODE_REALISTIC
                        ? skeletalRootDirection(baseDir)
                        : baseDir;
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

                if (physicsMode == MODE_REALISTIC) {
                    // СКЕЛЕТ + МЫШЦЫ: никаких arcTarget/S-кривых. Сначала сохраняем длину,
                    // затем сустав ограничивает резкий перелом, а мышцы прикладывают слабый
                    // ограниченный момент. Земля и инерция имеют право победить мышцы.
                    Vec3 currentDir = projected.subtract(anchor).normalize();
                    if (i == 0) {
                        Vec3 rootDir = skeletalRootDirection(baseDir);
                        double rootMuscle = PASSIVE_REALISTIC_TAIL ? 0.0D
                                : Mth.clamp(baseStiffness * (0.08D + 0.18D * tension), 0.03D, 0.22D);
                        currentDir = normalizedLerp(currentDir, rootDir, rootMuscle);
                    } else {
                        Vec3 previousAnchor = (i == 1) ? root : particles.get(i - 2).position;
                        Vec3 previousDir = anchor.subtract(previousAnchor);
                        if (previousDir.lengthSqr() < 1.0E-8D) previousDir = skeletalRootDirection(baseDir);
                        else previousDir = previousDir.normalize();

                        // Позвонки у основания жёстче; к кончику допустимый угол больше.
                        double maxBend = Math.toRadians(Mth.lerp(distal, 11.0D, 27.0D));
                        currentDir = limitBend(previousDir, currentDir, maxBend);

                        // Мышечное намерение задаёт локальную кривизну, не мировую позицию.
                        // carryAngle определяет направление работы мышц, но сила ограничена.
                        Vec3 muscleAxis = dirFromAngle(horizontalBack(baseDir), carryAngle);
                        double proximal = 1.0D - smoothstep(0.45D, 1.0D, distal);
                        double muscleStrength = PASSIVE_REALISTIC_TAIL ? 0.0D
                                : baseStiffness * (0.006D + 0.030D * tension) * proximal;
                        if (crouching) muscleStrength *= 0.85D;
                        currentDir = normalizedLerp(currentDir, muscleAxis, Mth.clamp(muscleStrength, 0.0D, 0.045D));
                        currentDir = limitBend(previousDir, currentDir, maxBend);
                    }
                    p.position = anchor.add(currentDir.scale(segLen));
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

                // ★ 1.3.0: кошачий заворот — в позе сидя/сна мягко тянем частицы к спирали вокруг ног:
                if (sitTargets != null && i > 0) {
                    Vec3 curlTarget = sitTargets[Math.min(i, sitTargets.length - 1)];
                    p.position = lerp(p.position, curlTarget, sitBlend * 0.20D);
                }

                p.position = collideOwnerCylinder(owner, p.position, p.radius, i);

                PhysicsWorldCollider.CollisionResult result = collider.collideSphere(level, owner, p.position, p.radius);
                p.position = result.position;
                p.touchingGround = p.touchingGround || result.ground;
                if (result.ground) {
                    p.slideOnGround(physicsMode == MODE_REALISTIC ? 0.38D : 0.80D);
                }
            }
        }

        settleDistalNearGround(level, owner);

        if (integratedPositions != null) {
            for (int i = 0; i < particles.size(); i++) {
                PhysicsParticle p = particles.get(i);
                Vec3 correction = p.position.subtract(integratedPositions[i]);
                // 85% служебной PBD-коррекции не становится скоростью следующего тика.
                p.previousPosition = p.previousPosition.add(correction.scale(0.85D));

                double distal = particles.size() <= 1 ? 1.0D : i / (double) (particles.size() - 1);
                double maxVelocity = Mth.lerp(distal, 0.13D, 0.23D);
                Vec3 velocity = p.position.subtract(p.previousPosition);
                double speed = velocity.length();
                if (speed > maxVelocity) {
                    velocity = velocity.scale(maxVelocity / speed);
                    p.previousPosition = p.position.subtract(velocity);
                }
            }
        }
    }

    /**
     * Режимная «присадка» дистальной части к земле:
     *  - Классика:     как в 1.0.0 — сильное притяжение (0.95) в пределах 45 см над землёй;
     *  - Реалистичная: искусственная присадка отключена; работают гравитация и коллизии.
     */
    private void settleDistalNearGround(Level level, Entity owner) {
        // Скелетный реалистичный режим ложится на землю только гравитацией и коллизиями.
        // Искусственная «присадка» дистальной части была ещё одной скрытой позой.
        if (physicsMode == MODE_REALISTIC) return;
        if (sitBlend > 0.4D) return; // в обвиве старых режимов землю обрабатывает спираль
        if (level == null || owner == null || !owner.onGround() || particles.size() < 2) return;

        boolean realistic = (physicsMode == MODE_REALISTIC);
        double maxGap = realistic ? 0.35D * (1.0D - 0.45D * moveBlend) : 0.45D;
        double pull;
        if (realistic) {
            // ★ 1.3.2: в покое кончик спокойно лежит на земле (отдыхающая кошка/лиса);
            //   на рыси — лишь изредка касается; на охоте (крадучись) — прижат к земле:
            pull = sneakBlend > 0.5D ? 0.85D : 0.62D * (1.0D - 0.70D * moveBlend);
        } else {
            pull = 0.95D;
        }
        int from = realistic ? particles.size() / 3 : 1;

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
                    p.slideOnGround(physicsMode == MODE_REALISTIC ? 0.38D : 0.80D);
                }
            }
        }
    }

    /**
     * ★ 1.3.0: Кошачий заворот — целевые точки спирали вокруг ног сидящего игрока.
     * Хвост выходит из-за спины, огибает ногу сбоку и закручивается кольцом
     * спереди (у спящего — плотнее и ближе к телу, как у свернувшегося кота).
     */
    private Vec3[] computeSitCurlTargets(Level level, Entity owner, Vec3 baseDir, Vec3 root) {
        int n = particles.size();
        Vec3 back = new Vec3(baseDir.x, 0.0D, baseDir.z);
        if (back.lengthSqr() < 1.0E-8D) back = new Vec3(0.0D, 0.0D, -1.0D);
        back = back.normalize();
        Vec3 forward = new Vec3(-back.x, 0.0D, -back.z);
        Vec3 right = new Vec3(-back.z, 0.0D, back.x);

        boolean sleeping = owner.getPose() == net.minecraft.world.entity.Pose.SLEEPING;
        // ★ 1.3.2: настоящие кошки в позе «хлебушка» кладут хвост ПЕРЕД лапами, а кончик —
        //   поверх основания обвива (лёгкое перекрытие), а не сужающейся спиралью-раструбом:
        double ahead = sleeping ? 0.04D : 0.21D;
        double side = sleeping ? 0.05D : 0.10D;
        double baseR = sleeping ? 0.33D : 0.44D;

        Vec3 center = new Vec3(owner.getX() + forward.x * ahead + right.x * side, 0.0D,
                owner.getZ() + forward.z * ahead + right.z * side);

        double groundY = owner.getY();
        if (level != null) {
            double g = collider.findGroundTopBelow(level, new Vec3(center.x, owner.getY() + 0.5D, center.z), 0.30D, 2.0D);
            if (!Double.isNaN(g)) groundY = g;
        }

        // Стартовый угол — направление от центра спирали к корню хвоста:
        Vec3 toRoot = root.subtract(center.x, groundY, center.z);
        double theta0 = Math.atan2(toRoot.dot(forward), toRoot.dot(right));

        Vec3[] targets = new Vec3[n];
        for (int i = 0; i < n; i++) {
            double t = (n <= 1) ? 1.0D : i / (double) (n - 1);
            double sweep = sleeping ? 4.6D : 4.0D; // ~230°: обвив, а не раструб
            double theta = theta0 - sweep * t;
            double r = baseR * (1.0D - 0.35D * t) + 0.03D; // кольцо почти не сужается к кончику
            double px = center.x + (right.x * Math.cos(theta) + forward.x * Math.sin(theta)) * r;
            double pz = center.z + (right.z * Math.cos(theta) + forward.z * Math.sin(theta)) * r;
            double py = groundY + Math.max(0.07D, particles.get(i).radius * 0.80D);
            if (!sleeping && t > 0.70D) {
                py += ((t - 0.70D) / 0.30D) * particles.get(i).radius * 0.55D; // кончик поверх обвива
            }
            targets[i] = new Vec3(px, py, pz);
        }
        return targets;
    }

    /** Направление первого хвостового позвонка — продолжение крестца вниз-назад. */
    private Vec3 skeletalRootDirection(Vec3 baseDirection) {
        return dirFromAngle(horizontalBack(baseDirection), EXIT_ANGLE);
    }

    private Vec3 horizontalBack(Vec3 direction) {
        Vec3 back = new Vec3(direction.x, 0.0D, direction.z);
        return back.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, 0.0D, -1.0D) : back.normalize();
    }

    /** Ограничение угла между соседними позвонками без задания мировой целевой точки. */
    private Vec3 limitBend(Vec3 previousDir, Vec3 currentDir, double maxAngle) {
        double dot = Mth.clamp(previousDir.dot(currentDir), -1.0D, 1.0D);
        double angle = Math.acos(dot);
        if (angle <= maxAngle || angle < 1.0E-7D) return currentDir;
        return normalizedLerp(previousDir, currentDir, maxAngle / angle);
    }

    /** Нормализованная интерполяция направлений; применяется как ограниченный мышечный момент. */
    private Vec3 normalizedLerp(Vec3 from, Vec3 to, double amount) {
        Vec3 mixed = from.scale(1.0D - amount).add(to.scale(amount));
        return mixed.lengthSqr() < 1.0E-8D ? from : mixed.normalize();
    }

    private double smoothstep(double edge0, double edge1, double x) {
        double t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0D, 1.0D);
        return t * t * (3.0D - 2.0D * t);
    }

    /** ★ 1.3.3: направление в плоскости спины по углу от горизонтали (рад; вниз < 0). */
    private Vec3 dirFromAngle(Vec3 back, double angle) {
        return new Vec3(back.x * Math.cos(angle), Math.sin(angle), back.z * Math.cos(angle)).normalize();
    }

    private Vec3 lerp(Vec3 a, Vec3 b, double t) {
        double k = Math.max(0.0D, Math.min(1.0D, t));
        return a.scale(1.0D - k).add(b.scale(k));
    }

    private Vec3 collideOwnerCylinder(Entity owner, Vec3 point, double radius, int segmentIndex) {
        // ★ 1.3.4: первые 4 сегмента НЕ расталкиваем от тела — хвост РАСТЁТ из крестца
        // и обязан обнимать круп. Раньше цилиндр (r≈0.31) выталкивал основание
        // горизонтально наружу (корень всего в 0.11 блока от центра тела) —
        // из-за этого у поясницы торчала «полка»-бугорок, как будто хвост приставной.
        if (owner == null || segmentIndex < 4) return point;
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
