package com.yellowfire.faradayears.client.gui;

import com.yellowfire.faradayears.ModAttachments;
import com.yellowfire.faradayears.capability.PlayerEarsTailData;
import com.yellowfire.faradayears.client.render.ProceduralTailRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Customization Screen (`v29-blender-orbit-camera`).
 * Features a clean Orbit Camera inside the 3D preview box:
 * - `ЛКМ (Left Click Drag)`: Smooth Pitch & Yaw rotation (`1:1 with mouse`).
 * - `ПКМ (Right Click Drag)` / `Scroll Wheel`: Roll tilt & Zoom (`15.0 to 150.0`).
 * - `СКМ (Middle Click)` / `Shift + ЛКМ`: Pan character position (`offsetX, offsetY`).
 */
public class EarsTailCustomizationScreen extends Screen {
    private final PlayerEarsTailData localData = new PlayerEarsTailData();
    private int activeTab = 0; // 0 = Пресеты, 1 = Ушки, 2 = Хвост, 3 = Текстура

    private TextureCanvasWidget canvasWidget;
    private float playerPreviewRotation = -35.0f; // Yaw
    private float previewPitch = 10.0f;           // Pitch
    private float previewRoll = 0.0f;             // Roll
    private float previewScale = 56.0f;           // Zoom
    private float previewOffsetX = 0.0f;          // Screen X offset
    private float previewOffsetY = 0.0f;          // Screen Y offset

    public EarsTailCustomizationScreen() {
        super(Component.literal("Настройка ушек и хвоста Фарадея"));
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        int panelLeft = width / 2 - 25;
        int topPos = 42;
        int btnWidth = 195;

        addRenderableWidget(Button.builder(Component.literal("★ Пресет"), b -> switchTab(0)).bounds(panelLeft - 18, 16, 42, 20).build());
        addRenderableWidget(Button.builder(Component.literal("🐱 Ушки"), b -> switchTab(1)).bounds(panelLeft + 26, 16, 38, 20).build());
        addRenderableWidget(Button.builder(Component.literal("🦊 Хвост"), b -> switchTab(2)).bounds(panelLeft + 66, 16, 42, 20).build());
        addRenderableWidget(Button.builder(Component.literal("👗 Фигура"), b -> switchTab(4)).bounds(panelLeft + 110, 16, 46, 20).build());
        addRenderableWidget(Button.builder(Component.literal("🎒 Мешочек"), b -> switchTab(5)).bounds(panelLeft + 158, 16, 50, 20).build());
        addRenderableWidget(Button.builder(Component.literal("🎨 Цвет"), b -> switchTab(3)).bounds(panelLeft + 210, 16, 40, 20).build());

        if (activeTab == 0) {
            addRenderableWidget(Button.builder(Component.literal("★ Фарадей (Супер-объёмный!)"), b -> {
                localData.applyFaradayPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos, btnWidth, 22).build());
            addRenderableWidget(Button.builder(Component.literal("🦊 Лисёнок / Котик"), b -> {
                localData.applyFoxPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 26, btnWidth, 20).build());
            addRenderableWidget(Button.builder(Component.literal("🐺 Серый Волк"), b -> {
                localData.applyWolfPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 48, btnWidth, 20).build());
            addRenderableWidget(Button.builder(Component.literal("🐰 Кролик"), b -> {
                localData.applyBunnyPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 70, btnWidth, 20).build());
            addRenderableWidget(Button.builder(Component.literal("✨ Девятихвостая Кицунэ"), b -> {
                localData.applyKitsunePreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 92, btnWidth, 20).build());
            addRenderableWidget(Button.builder(Component.literal("🐱 Феликс"), b -> {
                localData.applyFelixPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 114, btnWidth, 20).build());
        } else if (activeTab == 1) {
            addRenderableWidget(Button.builder(Component.literal("✔ Ушки на голове: " + (localData.isShowEars() ? "ВКЛ" : "ВЫКЛ")), b -> {
                localData.setShowEars(!localData.isShowEars());
                b.setMessage(Component.literal("✔ Ушки на голове: " + (localData.isShowEars() ? "ВКЛ" : "ВЫКЛ")));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos, btnWidth, 20).build());

            addRenderableWidget(Button.builder(Component.literal("Форма ушек: " + getEarShapeName(localData.getEarShape())), b -> {
                localData.setEarShape((localData.getEarShape() + 1) % 4);
                b.setMessage(Component.literal("Форма ушек: " + getEarShapeName(localData.getEarShape())));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 22, btnWidth, 20).build());

            addRenderableWidget(new CustomSlider(panelLeft, topPos + 46, btnWidth, 18, "Размах ушек вбок (Z): ", -45.0f, 45.0f, localData.getEarRotZ(), val -> { localData.setEarRotZ(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 66, btnWidth, 18, "Наклон ушек вперёд/назад (X): ", -45.0f, 45.0f, localData.getEarRotX(), val -> { localData.setEarRotX(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 86, btnWidth, 18, "Поворот ушек вокруг оси (Y): ", -45.0f, 45.0f, localData.getEarRotY(), val -> { localData.setEarRotY(val); applyLiveUpdate(); }));

            addRenderableWidget(new CustomSlider(panelLeft, topPos + 108, btnWidth, 18, "Размер ушек X: ", 0.5f, 2.0f, localData.getEarScaleX(), val -> { localData.setEarScaleX(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 128, btnWidth, 18, "Размер ушек Y: ", 0.5f, 2.0f, localData.getEarScaleY(), val -> { localData.setEarScaleY(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 148, btnWidth, 18, "Размер ушек Z: ", 0.5f, 2.0f, localData.getEarScaleZ(), val -> { localData.setEarScaleZ(val); applyLiveUpdate(); }));

            addRenderableWidget(Button.builder(Component.literal("🔄 Сбросить только настройки ушек"), b -> {
                localData.resetEarsOnly();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 170, btnWidth, 18).build());
        } else if (activeTab == 2) {
            addRenderableWidget(Button.builder(Component.literal("✔ Хвост на спине: " + (localData.isShowTail() ? "ВКЛ" : "ВЫКЛ")), b -> {
                localData.setShowTail(!localData.isShowTail());
                b.setMessage(Component.literal("✔ Хвост на спине: " + (localData.isShowTail() ? "ВКЛ" : "ВЫКЛ")));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos, btnWidth, 18).build());
            addRenderableWidget(Button.builder(Component.literal("Режим: " + getTailCountName(localData.getTailCount())), b -> {
                int[] counts = {10, 1, 2, 3, 5, 7, 9};
                int nextIdx = 0;
                for (int i = 0; i < counts.length; i++) if (localData.getTailCount() == counts[i]) nextIdx = (i + 1) % counts.length;
                localData.setTailCount(counts[nextIdx]);
                b.setMessage(Component.literal("Режим: " + getTailCountName(localData.getTailCount())));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 20, btnWidth, 18).build());

            // ★ 1.2.0: переключатель режима физики хвоста (Классика / Баланс / Поднятая дуга):
            addRenderableWidget(Button.builder(Component.literal("⚙ Физика: " + getPhysicsModeName(localData.getTailPhysicsMode())), b -> {
                localData.setTailPhysicsMode((localData.getTailPhysicsMode() + 1) % 3);
                b.setMessage(Component.literal("⚙ Физика: " + getPhysicsModeName(localData.getTailPhysicsMode())));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 40, btnWidth, 18).build());

            addRenderableWidget(new CustomSlider(panelLeft, topPos + 60, btnWidth, 18, "Веерный развал хвостов Y: ", 20.0f, 160.0f, localData.getTailFanSpread(), val -> { localData.setTailFanSpread(val); applyLiveUpdate(); }));

            addRenderableWidget(Button.builder(Component.literal("Сегментов в длину: " + localData.getTailSegments() + " (" + getSegmentsDesc(localData.getTailSegments()) + ")"), b -> {
                localData.setTailSegments(localData.getTailSegments() % 6 + 1);
                b.setMessage(Component.literal("Сегментов в длину: " + localData.getTailSegments() + " (" + getSegmentsDesc(localData.getTailSegments()) + ")"));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 80, btnWidth, 18).build());

            addRenderableWidget(new CustomSlider(panelLeft, topPos + 100, btnWidth, 18, "Длина каждого сегмента Z: ", 3.0f, 10.0f, localData.getTailSegmentLength(), val -> { localData.setTailSegmentLength(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 120, btnWidth, 18, "Сужение к кончику: ", 0.35f, 1.45f, localData.getTailTaper(), val -> { localData.setTailTaper(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 140, btnWidth, 18, "Общая ширина X: ", 0.5f, 2.5f, localData.getTailScaleX(), val -> { localData.setTailScaleX(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 160, btnWidth, 18, "Подъём от поясницы (сдвиг Z/Y): ", -16.0f, 16.0f, localData.getTailRotX(), val -> { localData.setTailRotX(val); applyLiveUpdate(); }));

            addRenderableWidget(Button.builder(Component.literal("Ось махания: " + getWagAxisName(localData.getTailWagAxis())), b -> {
                localData.setTailWagAxis((localData.getTailWagAxis() + 1) % 4);
                b.setMessage(Component.literal("Ось махания: " + getWagAxisName(localData.getTailWagAxis())));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 180, btnWidth, 18).build());
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 200, btnWidth, 18, "Амплитуда махания: ", 0.0f, 45.0f, localData.getTailWagAmplitude(), val -> { localData.setTailWagAmplitude(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 220, btnWidth, 18, "Скорость махания: ", 0.5f, 5.0f, localData.getTailWagSpeed(), val -> { localData.setTailWagSpeed(val); applyLiveUpdate(); }));

            addRenderableWidget(Button.builder(Component.literal("🔄 Сбросить только настройки хвоста"), b -> {
                localData.resetTailOnly();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 242, btnWidth, 18).build());
        } else if (activeTab == 3) {
            // ★ 1.2.0: тумблер применения кастомной текстуры (единый лист 64x64):
            addRenderableWidget(Button.builder(Component.literal("✔ Кастомная текстура: " + (localData.isCustomTextureEnabled() ? "ВКЛ" : "ВЫКЛ")), b -> {
                localData.setCustomTextureEnabled(!localData.isCustomTextureEnabled());
                b.setMessage(Component.literal("✔ Кастомная текстура: " + (localData.isCustomTextureEnabled() ? "ВКЛ" : "ВЫКЛ")));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos, btnWidth, 18).build());
            addRenderableWidget(Button.builder(Component.literal("💾 Скачать шаблон 64x64 (4 зоны)"), b -> exportCustom2StripTemplateToDisk(b)).bounds(panelLeft, topPos + 20, btnWidth, 18).build());
            addRenderableWidget(Button.builder(Component.literal("📂 Загрузить faraday_custom.png из папки игры"), b -> loadCustomTextureFromDisk(b)).bounds(panelLeft, topPos + 40, btnWidth, 18).build());

            canvasWidget = new TextureCanvasWidget(panelLeft, topPos + 62, 150, 150);
            canvasWidget.setOnPaintListener(() -> {
                localData.setCustomTextureBase64(canvasWidget.exportToBase64());
                localData.setCustomTextureEnabled(true); // рисуешь — значит, хочешь видеть текстуру
                applyLiveUpdate();
            });
            addRenderableWidget(canvasWidget);

            addRenderableWidget(Button.builder(Component.literal("Тёмная шерсть"), b -> canvasWidget.setCurrentColor(0x262220)).bounds(panelLeft + 156, topPos + 62, 88, 18).build());
            addRenderableWidget(Button.builder(Component.literal("Оранжевый"), b -> canvasWidget.setCurrentColor(0xEE8C1E)).bounds(panelLeft + 156, topPos + 82, 88, 18).build());
            addRenderableWidget(Button.builder(Component.literal("Золотой"), b -> canvasWidget.setCurrentColor(0xF1C40F)).bounds(panelLeft + 156, topPos + 102, 88, 18).build());
            addRenderableWidget(Button.builder(Component.literal("Красно-коричн."), b -> canvasWidget.setCurrentColor(0xC44D14)).bounds(panelLeft + 156, topPos + 122, 88, 18).build());
            addRenderableWidget(Button.builder(Component.literal("Белый кончик"), b -> canvasWidget.setCurrentColor(0xFFFFFF)).bounds(panelLeft + 156, topPos + 142, 88, 18).build());
            addRenderableWidget(Button.builder(Component.literal("🧽 Ластик (или ПКМ)"), b -> canvasWidget.setCurrentColorTransparent()).bounds(panelLeft + 156, topPos + 162, 88, 18).build());
        } else if (activeTab == 4) {
            addRenderableWidget(Button.builder(Component.literal("Гендер: " + getGenderName(localData.getGender())), b -> {
                localData.setGender((localData.getGender() + 1) % 4);
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos, btnWidth, 20).build());

            if (localData.getGender() == 1 || localData.getGender() == 3) {
                addRenderableWidget(Button.builder(Component.literal("Грудь: " + (localData.isShowChest() ? "ВКЛ" : "ВЫКЛ")), b -> {
                    localData.setShowChest(!localData.isShowChest());
                    b.setMessage(Component.literal("Грудь: " + (localData.isShowChest() ? "ВКЛ" : "ВЫКЛ")));
                    applyLiveUpdate();
                }).bounds(panelLeft, topPos + 22, btnWidth / 2 - 2, 18).build());
                addRenderableWidget(Button.builder(Component.literal("Бёдра: " + (localData.isShowHips() ? "ВКЛ" : "ВЫКЛ")), b -> {
                    localData.setShowHips(!localData.isShowHips());
                    b.setMessage(Component.literal("Бёдра: " + (localData.isShowHips() ? "ВКЛ" : "ВЫКЛ")));
                    applyLiveUpdate();
                }).bounds(panelLeft + btnWidth / 2 + 2, topPos + 22, btnWidth / 2 - 2, 18).build());

                addRenderableWidget(new CustomSlider(panelLeft, topPos + 44, btnWidth, 18, "Размер груди (выступ Z): ", 0.5f, 2.0f, localData.getChestScaleZ(), val -> { localData.setChestScaleZ(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 64, btnWidth, 18, "Ширина груди X: ", 0.5f, 2.0f, localData.getChestScaleX(), val -> { localData.setChestScaleX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 84, btnWidth, 18, "Высота груди Y: ", -4.0f, 4.0f, localData.getChestOffsetY(), val -> { localData.setChestOffsetY(val); applyLiveUpdate(); }));

                addRenderableWidget(new CustomSlider(panelLeft, topPos + 104, btnWidth, 18, "Размер бёдер (выступ Z): ", 0.5f, 2.0f, localData.getHipsScaleZ(), val -> { localData.setHipsScaleZ(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 124, btnWidth, 18, "Ширина бёдер X: ", 0.5f, 2.0f, localData.getHipsScaleX(), val -> { localData.setHipsScaleX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 144, btnWidth, 18, "Сила физики (мягкость Jiggle): ", 0.0f, 2.0f, localData.getBodyJiggleStrength(), val -> { localData.setBodyJiggleStrength(val); applyLiveUpdate(); }));
            }

            if (localData.getGender() == 2 || localData.getGender() == 3) {
                int startY = (localData.getGender() == 3) ? topPos + 164 : topPos + 22;
                addRenderableWidget(Button.builder(Component.literal("✔ Атлетический торс/плечи: " + (localData.isShowShoulders() ? "ВКЛ" : "ВЫКЛ")), b -> {
                    localData.setShowShoulders(!localData.isShowShoulders());
                    b.setMessage(Component.literal("✔ Атлетический торс/плечи: " + (localData.isShowShoulders() ? "ВКЛ" : "ВЫКЛ")));
                    applyLiveUpdate();
                }).bounds(panelLeft, startY, btnWidth, 18).build());
                addRenderableWidget(new CustomSlider(panelLeft, startY + 20, btnWidth, 18, "Ширина плеч/торса X: ", 0.8f, 1.8f, localData.getShouldersScaleX(), val -> { localData.setShouldersScaleX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, startY + 40, btnWidth, 18, "Глубина торса Z: ", 0.8f, 1.8f, localData.getShouldersScaleZ(), val -> { localData.setShouldersScaleZ(val); applyLiveUpdate(); }));
            }

            int jumpY = (localData.getGender() == 3) ? topPos + 226 : (localData.getGender() == 2 ? topPos + 84 : (localData.getGender() == 1 ? topPos + 166 : topPos + 24));
            addRenderableWidget(Button.builder(Component.literal("🎒 Перейти к настройке поясного мешочка ➡"), b -> switchTab(5)).bounds(panelLeft, jumpY, btnWidth, 18).build());

            int resetY = jumpY + 22;
            addRenderableWidget(Button.builder(Component.literal("🔄 Сбросить настройки фигуры"), b -> {
                localData.resetBodyOnly();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, resetY, btnWidth, 18).build());
        } else if (activeTab == 5) {
            addRenderableWidget(Button.builder(Component.literal("✔ Поясной мешочек спереди: " + (localData.isShowPouch() ? "ВКЛ" : "ВЫКЛ")), b -> {
                localData.setShowPouch(!localData.isShowPouch());
                b.setMessage(Component.literal("✔ Поясной мешочек спереди: " + (localData.isShowPouch() ? "ВКЛ" : "ВЫКЛ")));
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos, btnWidth, 20).build());

            if (localData.isShowPouch()) {
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 24, btnWidth, 18, "Сдвиг мешочка X (влево/вправо): ", -5.0f, 5.0f, localData.getPouchOffsetX(), val -> { localData.setPouchOffsetX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 44, btnWidth, 18, "Сдвиг мешочка Y (вверх/вниз): ", -6.0f, 6.0f, localData.getPouchOffsetY(), val -> { localData.setPouchOffsetY(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 64, btnWidth, 18, "Сдвиг мешочка Z (вперёд/назад): ", -4.0f, 4.0f, localData.getPouchOffsetZ(), val -> { localData.setPouchOffsetZ(val); applyLiveUpdate(); }));

                addRenderableWidget(new CustomSlider(panelLeft, topPos + 88, btnWidth, 18, "Ширина мешочка X: ", 0.10f, 0.50f, localData.getPouchScaleX(), val -> { localData.setPouchScaleX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 108, btnWidth, 18, "Высота мешочка Y: ", 0.10f, 0.50f, localData.getPouchScaleY(), val -> { localData.setPouchScaleY(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 128, btnWidth, 18, "Глубина мешочка Z: ", 0.10f, 0.50f, localData.getPouchScaleZ(), val -> { localData.setPouchScaleZ(val); applyLiveUpdate(); }));

                addRenderableWidget(Button.builder(Component.literal("🔄 Сбросить только настройки мешочка"), b -> {
                    localData.resetPouchOnly();
                    applyLiveUpdate();
                    init();
                }).bounds(panelLeft, topPos + 152, btnWidth, 18).build());
            }

            addRenderableWidget(Button.builder(Component.literal("👗 Перейти к настройкам фигуры/тела ➡"), b -> switchTab(4)).bounds(panelLeft, topPos + (localData.isShowPouch() ? 174 : 26), btnWidth, 18).build());
        }

        addRenderableWidget(Button.builder(Component.literal("🔄 Сбросить вид 3D-модели"), b -> {
            this.playerPreviewRotation = -35.0f;
            this.previewPitch = 10.0f;
            this.previewRoll = 0.0f;
            this.previewScale = 56.0f;
            this.previewOffsetX = 0.0f;
            this.previewOffsetY = 0.0f;
        }).bounds(18, height - 28, width / 2 - 58, 20).build());

        addRenderableWidget(Button.builder(Component.literal("🔄 Сбросить всё по умолчанию"), b -> {
            localData.applyFaradayPreset();
            applyLiveUpdate();
            init();
        }).bounds(width / 2 - 165, height - 28, 155, 20).build());
        addRenderableWidget(Button.builder(Component.literal("✔ Сохранить и закрыть"), b -> {
            saveAndSendToServer();
            onClose();
        }).bounds(width / 2 + 10, height - 28, 155, 20).build());

        if (Minecraft.getInstance().player != null) {
            PlayerEarsTailData data = ModAttachments.get(Minecraft.getInstance().player);
            localData.copyFrom(data);
            if (canvasWidget != null && activeTab == 3) {
                canvasWidget.importFromBase64(localData.getCustomTextureBase64());
            }
        }
    }

    private String getWagAxisName(int axis) {
        return switch (axis) {
            case 0 -> "Горизонтально (влево-вправо)";
            case 1 -> "Вертикально (вверх-вниз)";
            case 2 -> "Круговое (3D-волна)";
            case 3 -> "Выключено (Без махания)";
            default -> "Горизонтально";
        };
    }

    /** ★ 1.2.0: название режима физики хвоста для кнопки во вкладке «Хвост». */
    private String getPhysicsModeName(int mode) {
        return switch (mode) {
            case 1 -> "Баланс (дуга + касание земли)";
            case 2 -> "Поднятая дуга (1.0.1)";
            default -> "Классика (стелется, 1.0.0)";
        };
    }

    private String getEarShapeName(int shape) {
        return switch (shape) {
            case 1 -> "Заячьи / длинные";
            case 2 -> "Кошачьи / острые";
            case 3 -> "Лисьи / пушистые";
            default -> "Фарадей (с кисточками)";
        };
    }

    private String getTailCountName(int count) {
        return switch (count) {
            case 10 -> "★ 1 ХВОСТ (Супер-объёмный плюшевый!)";
            case 1 -> "1 Хвост (Тонкий классический)";
            case 2 -> "2 Хвоста (Некомата веером)";
            case 3 -> "3 Хвоста (Веером)";
            case 5 -> "5 Хвостов (Веером)";
            case 7 -> "7 Хвостов (Веером)";
            case 9 -> "✨ 9 Хвостов (Кицунэ веером!)";
            default -> count + " Хвостов";
        };
    }

    private String getSegmentsDesc(int segs) {
        return switch (segs) {
            case 1 -> "Боб / пушок";
            case 2 -> "Средний";
            case 3 -> "Пушистый Фарадей";
            case 4 -> "Длинный лисий";
            case 5 -> "Китсуне";
            case 6 -> "Драконий / дуга";
            default -> "Фарадей";
        };
    }

    private String getGenderName(int gender) {
        return switch (gender) {
            case 1 -> "♀ Женский (Грудь и Бёдра)";
            case 2 -> "♂ Мужской (Атлетический торс)";
            case 3 -> "⚧ Комбинированный (Все пропорции)";
            default -> "Нейтральный (Без изменений)";
        };
    }

    private void switchTab(int tab) {
        if (this.activeTab != tab) {
            this.activeTab = tab;
            this.init();
        }
    }

    private void applyLiveUpdate() {
        if (Minecraft.getInstance().player != null) {
            PlayerEarsTailData data = ModAttachments.get(Minecraft.getInstance().player);
            data.copyFrom(localData);
        }
    }

    private void saveAndSendToServer() {
        if (Minecraft.getInstance().player != null) {
            if (canvasWidget != null && activeTab == 3) {
                localData.setCustomTextureBase64(canvasWidget.exportToBase64());
                // Пользователь нарисовал текстуру и сохраняет — включаем её применение автоматически:
                localData.setCustomTextureEnabled(true);
            }
            PlayerEarsTailData data = ModAttachments.get(Minecraft.getInstance().player);
            data.copyFrom(localData);
            net.minecraft.nbt.CompoundTag tag = data.saveNBTData();
            com.yellowfire.faradayears.network.ModPacketHandler.sendToServer(new com.yellowfire.faradayears.network.SyncEarsTailPacket(Minecraft.getInstance().player.getUUID(), tag));
        }
    }

    private void exportCustom2StripTemplateToDisk(Button b) {
        try {
            File dir = new File(Minecraft.getInstance().gameDirectory, "faraday_template.png");
            BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = img.createGraphics();

            // 1. ЗОНА УШЕК (Внешний каркас ушей Y = 0..15):
            g.setColor(new java.awt.Color(0xFF262220));
            g.fillRect(0, 0, 64, 16);
            g.setColor(new java.awt.Color(0xFFD84B16));
            g.fillRect(16, 0, 16, 16);
            g.setColor(new java.awt.Color(0xFF4A4D52));
            g.fillRect(32, 0, 16, 16);
            g.setColor(new java.awt.Color(0xFFE6E6E6));
            g.fillRect(48, 0, 16, 16);

            // 2. ЗОНА УШНОЙ РАКОВИНЫ И БАНТИКА (Внутренний мех Y = 16..31):
            g.setColor(new java.awt.Color(0xFFEE8C1E));
            g.fillRect(0, 16, 16, 16);
            g.setColor(new java.awt.Color(0xFFFFFFFF));
            g.fillRect(16, 16, 16, 16);
            g.setColor(new java.awt.Color(0xFFCCCCCC));
            g.fillRect(32, 16, 16, 16);
            g.setColor(new java.awt.Color(0xFFF5F5FF));
            g.fillRect(48, 16, 16, 16); // Бантики Феликса и внутренний мех кролика

            // 3. ЗОНА ХВОСТА (Основная шерсть Y = 32..47, Кончик Y = 48..63):
            g.setColor(new java.awt.Color(0xFF262220));
            g.fillRect(0, 32, 64, 16);
            g.setColor(new java.awt.Color(0xFFC44D14));
            g.fillRect(0, 48, 64, 16);

            // 4. Отрасовка контрастной сетки и понятных подписей прямо на шаблоне:
            g.setColor(java.awt.Color.BLACK);
            g.drawRect(0, 0, 63, 15);
            g.drawRect(0, 16, 63, 15);
            g.drawRect(0, 32, 63, 15);
            g.drawRect(0, 48, 63, 15);

            g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
            g.drawString("EARS OUTER", 4, 11);
            g.drawString("EARS INNER / TUFTS", 4, 27);
            g.drawString("BOW", 50, 27);
            g.setColor(java.awt.Color.WHITE);
            g.drawString("TAIL BODY FUR (Y=32..47)", 6, 43);
            g.drawString("TAIL TIP FUR (Y=48..63)", 6, 59);
            g.dispose();

            ImageIO.write(img, "PNG", dir);
            b.setMessage(Component.literal("✔ Сохранено: faraday_template.png (64x64)"));
        } catch (Exception e) {
            b.setMessage(Component.literal("❌ Ошибка сохранения"));
        }
    }

    private void loadCustomTextureFromDisk(Button b) {
        try {
            File file = new File(Minecraft.getInstance().gameDirectory, "faraday_custom.png");
            if (!file.exists()) {
                b.setMessage(Component.literal("⚠️ Файл faraday_custom.png не найден"));
                return;
            }
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                com.mojang.blaze3d.platform.NativeImage img = com.mojang.blaze3d.platform.NativeImage.read(fis);
                if (img != null && canvasWidget != null) {
                    canvasWidget.importFromNativeImage(img);
                    localData.setCustomTextureBase64(canvasWidget.exportToBase64());
                    localData.setCustomTextureEnabled(true);
                    applyLiveUpdate();
                    b.setMessage(Component.literal("✔ Текстура загружена!"));
                }
            }
        } catch (Exception e) {
            b.setMessage(Component.literal("❌ Ошибка чтения .png"));
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (mouseX < width / 2 - 35 && mouseY > 30 && mouseY < height - 35) {
            if (button == 2 || (button == 0 && Screen.hasShiftDown())) { // СКМ или Shift+ЛКМ: сдвиг камеры по экрану
                this.previewOffsetX += (float) dragX;
                this.previewOffsetY += (float) dragY;
                return true;
            }
            if (button == 0) { // ЛКМ: вращение по горизонтали (Yaw) и вертикали (Pitch)
                this.playerPreviewRotation += (float) dragX * 1.5f;
                this.previewPitch += (float) dragY * 1.5f;
                this.previewPitch = Mth.clamp(this.previewPitch, -89.0f, 89.0f);
                return true;
            }
            if (button == 1) { // ПКМ: наклон по оси Z (Roll) и зум (Zoom)
                this.previewRoll += (float) dragX * 1.4f;
                this.previewScale += (float) dragY * 1.2f;
                this.previewScale = Mth.clamp(this.previewScale, 15.0f, 150.0f);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < width / 2 - 35 && mouseY > 30 && mouseY < height - 35) {
            this.previewScale += (float) scrollY * 6.0f;
            this.previewScale = Mth.clamp(this.previewScale, 15.0f, 150.0f);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.fill(15, 30, width / 2 - 35, height - 35, 0xBB161412);
        guiGraphics.drawCenteredString(font, "🔴 3D Вид: ЛКМ=Вращение X/Y | ПКМ=Наклон Z/Зум | СКМ=Сдвиг", (width / 2 - 35) / 2 + 10, 35, 0xFFEE8C1E);

        if (Minecraft.getInstance().player != null) {
            int prevX = (width / 2 - 35) / 2 + 10;
            int prevY = height / 2 + 45;
            renderCustomEntityPreview(prevX, prevY, this.previewScale, this.playerPreviewRotation, this.previewPitch, this.previewRoll, this.previewOffsetX, this.previewOffsetY, Minecraft.getInstance().player);
        }

        guiGraphics.drawString(font, "Форма: " + getEarShapeName(localData.getEarShape()), 22, height - 64, 0xF5C037);
        guiGraphics.drawString(font, "Режим: " + getTailCountName(localData.getTailCount()), 22, height - 52, 0xF5C037);

        guiGraphics.fill(width / 2 - 30, 35, width - 15, height - 35, 0xBB1E1A17);

        if (activeTab == 3) {
            int infoTop = 42 + 152;
            guiGraphics.drawString(font, "ℹ️ ПОДСКАЗКА ПО ЮВ-РАЗВЁРТКЕ 64x64 (БЕЗ НАЛОЖЕНИЙ):", width / 2 - 20, infoTop, 0xFFEE8C1E);
            guiGraphics.drawString(font, "1. Нажми [💾 Скачать понятный шаблон развёртки (64x64.png)].", width / 2 - 20, infoTop + 12, 0xDDDDDD);
            guiGraphics.drawString(font, "2. В папке игры (.minecraft) появится 'faraday_template.png'.", width / 2 - 20, infoTop + 22, 0xAAAAAA);
            guiGraphics.drawString(font, "   • Полоса Y=0..15 — Внешняя шерсть Ушек.", width / 2 - 20, infoTop + 32, 0x37C0F5);
            guiGraphics.drawString(font, "   • Полоса Y=16..31 — Внутренняя шерсть раковины и Бантик (X=48..63).", width / 2 - 20, infoTop + 42, 0x2ECC71);
            guiGraphics.drawString(font, "   • Полоса Y=32..63 — Шерсть Хвоста (Y=32..47) и его Кончика (Y=48..63).", width / 2 - 20, infoTop + 52, 0xF5C037);
            guiGraphics.drawString(font, "3. Раскрась в редакторе, сохрани как 'faraday_custom.png' и загрузи!", width / 2 - 20, infoTop + 62, 0xDDDDDD);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public static void renderCustomEntityPreview(int pPosX, int pPosY, float pScale, float yawDeg, float pitchDeg, float rollDeg, float offsetX, float offsetY, LivingEntity pLivingEntity) {
        org.joml.Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.translate((float) pPosX + offsetX, (float) pPosY + offsetY, 1050.0F);
        modelViewStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale(pScale, pScale, pScale);

        Quaternionf qRoll = Axis.ZP.rotationDegrees(180.0F + rollDeg);
        Quaternionf qPitch = Axis.XP.rotationDegrees(pitchDeg);
        Quaternionf qYaw = Axis.YP.rotationDegrees(yawDeg);

        qRoll.mul(qPitch);
        qRoll.mul(qYaw);
        posestack1.mulPose(qRoll);

        float f2 = pLivingEntity.yBodyRot;
        float f3 = pLivingEntity.getYRot();
        float f4 = pLivingEntity.getXRot();
        float f5 = pLivingEntity.yHeadRotO;
        float f6 = pLivingEntity.yHeadRot;

        pLivingEntity.yBodyRot = 0.0F;
        pLivingEntity.setYRot(0.0F);
        pLivingEntity.setXRot(0.0F);
        pLivingEntity.yHeadRot = 0.0F;
        pLivingEntity.yHeadRotO = 0.0F;

        net.minecraft.client.renderer.entity.EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        qPitch.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(qPitch);
        entityrenderdispatcher.setRenderShadow(false);
        ProceduralTailRenderer.IS_IN_GUI_PREVIEW = true;
        ProceduralTailRenderer.GUI_PREVIEW_REAL_YAW = f2;
        net.minecraft.client.renderer.MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(pLivingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);
        });
        multibuffersource$buffersource.endBatch();
        ProceduralTailRenderer.IS_IN_GUI_PREVIEW = false;
        entityrenderdispatcher.setRenderShadow(true);

        pLivingEntity.yBodyRot = f2;
        pLivingEntity.setYRot(f3);
        pLivingEntity.setXRot(f4);
        pLivingEntity.yHeadRotO = f5;
        pLivingEntity.yHeadRot = f6;
        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static class CustomSlider extends AbstractSliderButton {
        private final String prefix;
        private final float minVal;
        private final float maxVal;
        private final java.util.function.Consumer<Float> onChange;

        public CustomSlider(int x, int y, int width, int height, String prefix, float minVal, float maxVal, float currentVal, java.util.function.Consumer<Float> onChange) {
            super(x, y, width, height, Component.literal(prefix + String.format("%.2f", currentVal)), (Mth.clamp(currentVal, minVal, maxVal) - minVal) / (maxVal - minVal));
            this.prefix = prefix;
            this.minVal = minVal;
            this.maxVal = maxVal;
            this.onChange = onChange;
        }

        @Override
        protected void updateMessage() {
            float val = minVal + (float) value * (maxVal - minVal);
            setMessage(Component.literal(prefix + String.format("%.2f", val)));
        }

        @Override
        protected void applyValue() {
            float val = minVal + (float) value * (maxVal - minVal);
            onChange.accept(val);
        }
    }
}

