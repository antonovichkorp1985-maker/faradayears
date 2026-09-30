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
        super(Component.translatable("gui.faradayears.title"));
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        int panelLeft = width / 2 - 25;
        int topPos = 42;
        int btnWidth = 195;

        addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tab.presets"), b -> switchTab(0)).bounds(panelLeft - 18, 16, 42, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tab.ears"), b -> switchTab(1)).bounds(panelLeft + 26, 16, 38, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tab.tail"), b -> switchTab(2)).bounds(panelLeft + 66, 16, 42, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tab.body"), b -> switchTab(4)).bounds(panelLeft + 110, 16, 46, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tab.pouch"), b -> switchTab(5)).bounds(panelLeft + 158, 16, 50, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tab.texture"), b -> switchTab(3)).bounds(panelLeft + 210, 16, 40, 20).build());

        if (activeTab == 0) {
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.preset.faraday"), b -> {
                localData.applyFaradayPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos, btnWidth, 22).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.preset.fox"), b -> {
                localData.applyFoxPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 26, btnWidth, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.preset.wolf"), b -> {
                localData.applyWolfPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 48, btnWidth, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.preset.bunny"), b -> {
                localData.applyBunnyPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 70, btnWidth, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.preset.kitsune"), b -> {
                localData.applyKitsunePreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 92, btnWidth, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.preset.felix"), b -> {
                localData.applyFelixPreset();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 114, btnWidth, 20).build());
        } else if (activeTab == 1) {
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.ears.show", onOff(localData.isShowEars())), b -> {
                localData.setShowEars(!localData.isShowEars());
                b.setMessage(Component.translatable("gui.faradayears.ears.show", onOff(localData.isShowEars())));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos, btnWidth, 20).build());

            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.ears.shape", Component.translatable(getEarShapeKey(localData.getEarShape()))), b -> {
                localData.setEarShape((localData.getEarShape() + 1) % 4);
                b.setMessage(Component.translatable("gui.faradayears.ears.shape", Component.translatable(getEarShapeKey(localData.getEarShape()))));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 22, btnWidth, 20).build());

            addRenderableWidget(new CustomSlider(panelLeft, topPos + 46, btnWidth, 18, "slider.faradayears.ear_rot_z", -45.0f, 45.0f, localData.getEarRotZ(), val -> { localData.setEarRotZ(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 66, btnWidth, 18, "slider.faradayears.ear_rot_x", -45.0f, 45.0f, localData.getEarRotX(), val -> { localData.setEarRotX(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 86, btnWidth, 18, "slider.faradayears.ear_rot_y", -45.0f, 45.0f, localData.getEarRotY(), val -> { localData.setEarRotY(val); applyLiveUpdate(); }));

            addRenderableWidget(new CustomSlider(panelLeft, topPos + 108, btnWidth, 18, "slider.faradayears.ear_scale_x", 0.5f, 2.0f, localData.getEarScaleX(), val -> { localData.setEarScaleX(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 128, btnWidth, 18, "slider.faradayears.ear_scale_y", 0.5f, 2.0f, localData.getEarScaleY(), val -> { localData.setEarScaleY(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 148, btnWidth, 18, "slider.faradayears.ear_scale_z", 0.5f, 2.0f, localData.getEarScaleZ(), val -> { localData.setEarScaleZ(val); applyLiveUpdate(); }));

            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.ears.reset"), b -> {
                localData.resetEarsOnly();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 170, btnWidth, 18).build());
        } else if (activeTab == 2) {
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tail.show", onOff(localData.isShowTail())), b -> {
                localData.setShowTail(!localData.isShowTail());
                b.setMessage(Component.translatable("gui.faradayears.tail.show", onOff(localData.isShowTail())));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos, btnWidth, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tail.count", Component.translatable(getTailCountKey(localData.getTailCount()))), b -> {
                int[] counts = {10, 1, 2, 3, 5, 7, 9};
                int nextIdx = 0;
                for (int i = 0; i < counts.length; i++) if (localData.getTailCount() == counts[i]) nextIdx = (i + 1) % counts.length;
                localData.setTailCount(counts[nextIdx]);
                b.setMessage(Component.translatable("gui.faradayears.tail.count", Component.translatable(getTailCountKey(localData.getTailCount()))));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 20, btnWidth, 18).build());

            // ★ 1.2.0: переключатель режима физики хвоста (Классика / Баланс / Поднятая дуга):
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.physics", Component.translatable(getPhysicsModeKey(localData.getTailPhysicsMode()))), b -> {
                localData.setTailPhysicsMode((localData.getTailPhysicsMode() + 1) % 3);
                b.setMessage(Component.translatable("gui.faradayears.physics", Component.translatable(getPhysicsModeKey(localData.getTailPhysicsMode()))));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 40, btnWidth, 18).build());

            addRenderableWidget(new CustomSlider(panelLeft, topPos + 60, btnWidth, 18, "slider.faradayears.tail_fan_spread", 20.0f, 160.0f, localData.getTailFanSpread(), val -> { localData.setTailFanSpread(val); applyLiveUpdate(); }));

            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tail.segments", localData.getTailSegments(), Component.translatable(getSegmentsDescKey(localData.getTailSegments()))), b -> {
                localData.setTailSegments(localData.getTailSegments() % 6 + 1);
                b.setMessage(Component.translatable("gui.faradayears.tail.segments", localData.getTailSegments(), Component.translatable(getSegmentsDescKey(localData.getTailSegments()))));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 80, btnWidth, 18).build());

            addRenderableWidget(new CustomSlider(panelLeft, topPos + 100, btnWidth, 18, "slider.faradayears.tail_segment_length", 3.0f, 10.0f, localData.getTailSegmentLength(), val -> { localData.setTailSegmentLength(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 120, btnWidth, 18, "slider.faradayears.tail_taper", 0.35f, 1.45f, localData.getTailTaper(), val -> { localData.setTailTaper(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 140, btnWidth, 18, "slider.faradayears.tail_scale_x", 0.5f, 2.5f, localData.getTailScaleX(), val -> { localData.setTailScaleX(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 160, btnWidth, 18, "slider.faradayears.tail_rot_x", -16.0f, 16.0f, localData.getTailRotX(), val -> { localData.setTailRotX(val); applyLiveUpdate(); }));

            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.wag", Component.translatable(getWagAxisKey(localData.getTailWagAxis()))), b -> {
                localData.setTailWagAxis((localData.getTailWagAxis() + 1) % 4);
                b.setMessage(Component.translatable("gui.faradayears.wag", Component.translatable(getWagAxisKey(localData.getTailWagAxis()))));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos + 180, btnWidth, 18).build());
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 200, btnWidth, 18, "slider.faradayears.wag_amplitude", 0.0f, 45.0f, localData.getTailWagAmplitude(), val -> { localData.setTailWagAmplitude(val); applyLiveUpdate(); }));
            addRenderableWidget(new CustomSlider(panelLeft, topPos + 220, btnWidth, 18, "slider.faradayears.wag_speed", 0.5f, 5.0f, localData.getTailWagSpeed(), val -> { localData.setTailWagSpeed(val); applyLiveUpdate(); }));

            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tail.reset"), b -> {
                localData.resetTailOnly();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos + 242, btnWidth, 18).build());
        } else if (activeTab == 3) {
            // ★ 1.2.0: тумблер применения кастомной текстуры (единый лист 64x64):
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.texture.toggle", onOff(localData.isCustomTextureEnabled())), b -> {
                localData.setCustomTextureEnabled(!localData.isCustomTextureEnabled());
                b.setMessage(Component.translatable("gui.faradayears.texture.toggle", onOff(localData.isCustomTextureEnabled())));
                applyLiveUpdate();
            }).bounds(panelLeft, topPos, btnWidth, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.texture.template"), b -> exportCustom2StripTemplateToDisk(b)).bounds(panelLeft, topPos + 20, btnWidth, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.texture.load"), b -> loadCustomTextureFromDisk(b)).bounds(panelLeft, topPos + 40, btnWidth, 18).build());

            canvasWidget = new TextureCanvasWidget(panelLeft, topPos + 62, 150, 150);
            canvasWidget.setOnPaintListener(() -> {
                localData.setCustomTextureBase64(canvasWidget.exportToBase64());
                localData.setCustomTextureEnabled(true); // рисуешь — значит, хочешь видеть текстуру
                applyLiveUpdate();
            });
            addRenderableWidget(canvasWidget);

            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.color.dark"), b -> canvasWidget.setCurrentColor(0x262220)).bounds(panelLeft + 156, topPos + 62, 88, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.color.orange"), b -> canvasWidget.setCurrentColor(0xEE8C1E)).bounds(panelLeft + 156, topPos + 82, 88, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.color.gold"), b -> canvasWidget.setCurrentColor(0xF1C40F)).bounds(panelLeft + 156, topPos + 102, 88, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.color.red"), b -> canvasWidget.setCurrentColor(0xC44D14)).bounds(panelLeft + 156, topPos + 122, 88, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.color.white"), b -> canvasWidget.setCurrentColor(0xFFFFFF)).bounds(panelLeft + 156, topPos + 142, 88, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.tool.eraser"), b -> canvasWidget.setCurrentColorTransparent()).bounds(panelLeft + 156, topPos + 162, 88, 18).build());
            // ★ 1.3.0: «поверхностный слой» — подписи зон поверх канваса (какая область за что отвечает):
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.texture.zones", onOff(canvasWidget.isZoneOverlayVisible())), b -> {
                canvasWidget.toggleZoneOverlay();
                b.setMessage(Component.translatable("gui.faradayears.texture.zones", onOff(canvasWidget.isZoneOverlayVisible())));
            }).bounds(panelLeft + 156, topPos + 182, 88, 18).build());
        } else if (activeTab == 4) {
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.body.gender", Component.translatable(getGenderKey(localData.getGender()))), b -> {
                localData.setGender((localData.getGender() + 1) % 4);
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos, btnWidth, 20).build());

            if (localData.getGender() == 1 || localData.getGender() == 3) {
                addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.body.chest", onOff(localData.isShowChest())), b -> {
                    localData.setShowChest(!localData.isShowChest());
                    b.setMessage(Component.translatable("gui.faradayears.body.chest", onOff(localData.isShowChest())));
                    applyLiveUpdate();
                }).bounds(panelLeft, topPos + 22, btnWidth / 2 - 2, 18).build());
                addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.body.hips", onOff(localData.isShowHips())), b -> {
                    localData.setShowHips(!localData.isShowHips());
                    b.setMessage(Component.translatable("gui.faradayears.body.hips", onOff(localData.isShowHips())));
                    applyLiveUpdate();
                }).bounds(panelLeft + btnWidth / 2 + 2, topPos + 22, btnWidth / 2 - 2, 18).build());

                addRenderableWidget(new CustomSlider(panelLeft, topPos + 44, btnWidth, 18, "slider.faradayears.chest_scale_z", 0.5f, 2.0f, localData.getChestScaleZ(), val -> { localData.setChestScaleZ(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 64, btnWidth, 18, "slider.faradayears.chest_scale_x", 0.5f, 2.0f, localData.getChestScaleX(), val -> { localData.setChestScaleX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 84, btnWidth, 18, "slider.faradayears.chest_offset_y", -4.0f, 4.0f, localData.getChestOffsetY(), val -> { localData.setChestOffsetY(val); applyLiveUpdate(); }));

                addRenderableWidget(new CustomSlider(panelLeft, topPos + 104, btnWidth, 18, "slider.faradayears.hips_scale_z", 0.5f, 2.0f, localData.getHipsScaleZ(), val -> { localData.setHipsScaleZ(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 124, btnWidth, 18, "slider.faradayears.hips_scale_x", 0.5f, 2.0f, localData.getHipsScaleX(), val -> { localData.setHipsScaleX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 144, btnWidth, 18, "slider.faradayears.jiggle", 0.0f, 2.0f, localData.getBodyJiggleStrength(), val -> { localData.setBodyJiggleStrength(val); applyLiveUpdate(); }));
            }

            if (localData.getGender() == 2 || localData.getGender() == 3) {
                int startY = (localData.getGender() == 3) ? topPos + 164 : topPos + 22;
                addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.body.shoulders", onOff(localData.isShowShoulders())), b -> {
                    localData.setShowShoulders(!localData.isShowShoulders());
                    b.setMessage(Component.translatable("gui.faradayears.body.shoulders", onOff(localData.isShowShoulders())));
                    applyLiveUpdate();
                }).bounds(panelLeft, startY, btnWidth, 18).build());
                addRenderableWidget(new CustomSlider(panelLeft, startY + 20, btnWidth, 18, "slider.faradayears.shoulders_scale_x", 0.8f, 1.8f, localData.getShouldersScaleX(), val -> { localData.setShouldersScaleX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, startY + 40, btnWidth, 18, "slider.faradayears.shoulders_scale_z", 0.8f, 1.8f, localData.getShouldersScaleZ(), val -> { localData.setShouldersScaleZ(val); applyLiveUpdate(); }));
            }

            int jumpY = (localData.getGender() == 3) ? topPos + 226 : (localData.getGender() == 2 ? topPos + 84 : (localData.getGender() == 1 ? topPos + 166 : topPos + 24));
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.body.goto_pouch"), b -> switchTab(5)).bounds(panelLeft, jumpY, btnWidth, 18).build());

            int resetY = jumpY + 22;
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.body.reset"), b -> {
                localData.resetBodyOnly();
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, resetY, btnWidth, 18).build());
        } else if (activeTab == 5) {
            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.pouch.show", onOff(localData.isShowPouch())), b -> {
                localData.setShowPouch(!localData.isShowPouch());
                b.setMessage(Component.translatable("gui.faradayears.pouch.show", onOff(localData.isShowPouch())));
                applyLiveUpdate();
                init();
            }).bounds(panelLeft, topPos, btnWidth, 20).build());

            if (localData.isShowPouch()) {
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 24, btnWidth, 18, "slider.faradayears.pouch_offset_x", -5.0f, 5.0f, localData.getPouchOffsetX(), val -> { localData.setPouchOffsetX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 44, btnWidth, 18, "slider.faradayears.pouch_offset_y", -6.0f, 6.0f, localData.getPouchOffsetY(), val -> { localData.setPouchOffsetY(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 64, btnWidth, 18, "slider.faradayears.pouch_offset_z", -4.0f, 4.0f, localData.getPouchOffsetZ(), val -> { localData.setPouchOffsetZ(val); applyLiveUpdate(); }));

                addRenderableWidget(new CustomSlider(panelLeft, topPos + 88, btnWidth, 18, "slider.faradayears.pouch_scale_x", 0.10f, 0.50f, localData.getPouchScaleX(), val -> { localData.setPouchScaleX(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 108, btnWidth, 18, "slider.faradayears.pouch_scale_y", 0.10f, 0.50f, localData.getPouchScaleY(), val -> { localData.setPouchScaleY(val); applyLiveUpdate(); }));
                addRenderableWidget(new CustomSlider(panelLeft, topPos + 128, btnWidth, 18, "slider.faradayears.pouch_scale_z", 0.10f, 0.50f, localData.getPouchScaleZ(), val -> { localData.setPouchScaleZ(val); applyLiveUpdate(); }));

                addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.pouch.reset"), b -> {
                    localData.resetPouchOnly();
                    applyLiveUpdate();
                    init();
                }).bounds(panelLeft, topPos + 152, btnWidth, 18).build());
            }

            addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.pouch.goto_body"), b -> switchTab(4)).bounds(panelLeft, topPos + (localData.isShowPouch() ? 174 : 26), btnWidth, 18).build());
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.view.reset"), b -> {
            this.playerPreviewRotation = -35.0f;
            this.previewPitch = 10.0f;
            this.previewRoll = 0.0f;
            this.previewScale = 56.0f;
            this.previewOffsetX = 0.0f;
            this.previewOffsetY = 0.0f;
        }).bounds(18, height - 28, width / 2 - 58, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.reset_all"), b -> {
            localData.applyFaradayPreset();
            applyLiveUpdate();
            init();
        }).bounds(width / 2 - 165, height - 28, 155, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.faradayears.save_close"), b -> {
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

    /** ★ 1.3.1: возвращает ключ локализации оси махания. */
    private String getWagAxisKey(int axis) {
        return switch (axis) {
            case 0 -> "gui.faradayears.wag.0";
            case 1 -> "gui.faradayears.wag.1";
            case 2 -> "gui.faradayears.wag.2";
            case 3 -> "gui.faradayears.wag.3";
            default -> "gui.faradayears.wag.0";
        };
    }

    /** ★ 1.3.1: ВКЛ/ВЫКЛ через lang-файлы. */
    private Component onOff(boolean on) {
        return Component.translatable(on ? "gui.faradayears.on" : "gui.faradayears.off");
    }

    /** ★ 1.3.1: ключ локализации режима физики хвоста. */
    private String getPhysicsModeKey(int mode) {
        return switch (mode) {
            case 1 -> "gui.faradayears.physics.1";
            case 2 -> "gui.faradayears.physics.2";
            default -> "gui.faradayears.physics.0";
        };
    }

    /** ★ 1.3.1: ключ локализации формы ушек. */
    private String getEarShapeKey(int shape) {
        return switch (shape) {
            case 1 -> "gui.faradayears.ear_shape.1";
            case 2 -> "gui.faradayears.ear_shape.2";
            case 3 -> "gui.faradayears.ear_shape.3";
            default -> "gui.faradayears.ear_shape.0";
        };
    }

    /** ★ 1.3.1: ключ локализации количества хвостов. */
    private String getTailCountKey(int count) {
        return switch (count) {
            case 10 -> "gui.faradayears.tail_count.10";
            case 1 -> "gui.faradayears.tail_count.1";
            case 2 -> "gui.faradayears.tail_count.2";
            case 3 -> "gui.faradayears.tail_count.3";
            case 5 -> "gui.faradayears.tail_count.5";
            case 7 -> "gui.faradayears.tail_count.7";
            case 9 -> "gui.faradayears.tail_count.9";
            default -> "gui.faradayears.tail_count.many";
        };
    }

    /** ★ 1.3.1: ключ локализации описания длины. */
    private String getSegmentsDescKey(int segs) {
        return switch (segs) {
            case 1 -> "gui.faradayears.segments.1";
            case 2 -> "gui.faradayears.segments.2";
            case 3 -> "gui.faradayears.segments.3";
            case 4 -> "gui.faradayears.segments.4";
            case 5 -> "gui.faradayears.segments.5";
            case 6 -> "gui.faradayears.segments.6";
            default -> "gui.faradayears.segments.3";
        };
    }

    /** ★ 1.3.1: ключ локализации гендера. */
    private String getGenderKey(int gender) {
        return switch (gender) {
            case 1 -> "gui.faradayears.gender.1";
            case 2 -> "gui.faradayears.gender.2";
            case 3 -> "gui.faradayears.gender.3";
            default -> "gui.faradayears.gender.0";
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
            b.setMessage(Component.translatable("gui.faradayears.texture.saved"));
        } catch (Exception e) {
            b.setMessage(Component.translatable("gui.faradayears.texture.save_error"));
        }
    }

    private void loadCustomTextureFromDisk(Button b) {
        try {
            File file = new File(Minecraft.getInstance().gameDirectory, "faraday_custom.png");
            if (!file.exists()) {
                b.setMessage(Component.translatable("gui.faradayears.texture.not_found"));
                return;
            }
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                com.mojang.blaze3d.platform.NativeImage img = com.mojang.blaze3d.platform.NativeImage.read(fis);
                if (img != null && canvasWidget != null) {
                    canvasWidget.importFromNativeImage(img);
                    localData.setCustomTextureBase64(canvasWidget.exportToBase64());
                    localData.setCustomTextureEnabled(true);
                    applyLiveUpdate();
                    b.setMessage(Component.translatable("gui.faradayears.texture.loaded"));
                }
            }
        } catch (Exception e) {
            b.setMessage(Component.translatable("gui.faradayears.texture.read_error"));
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
    /** ★ 1.3.4: БЕЗ ванильного блюра мира. Стандартный renderBackground в 1.21.1 размывает
     *  весь мир позади меню гауссовым блюром, а наши панели полупрозрачные — размытый
     *  мир просвечивал сквозь всё меню и 3D-превью, всё выглядело «мыльным».
     *  Кастомайзеру нужна читаемость: просто затемняем мир мягким градиентом. */
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xB0100F0D, 0xC812100E);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.fill(15, 30, width / 2 - 35, height - 35, 0xBB161412);
        guiGraphics.drawCenteredString(font, Component.translatable("gui.faradayears.preview.help"), (width / 2 - 35) / 2 + 10, 35, 0xFFEE8C1E);

        if (Minecraft.getInstance().player != null) {
            int prevX = (width / 2 - 35) / 2 + 10;
            int prevY = height / 2 + 45;
            renderCustomEntityPreview(prevX, prevY, this.previewScale, this.playerPreviewRotation, this.previewPitch, this.previewRoll, this.previewOffsetX, this.previewOffsetY, Minecraft.getInstance().player);
        }

        guiGraphics.drawString(font, Component.translatable("gui.faradayears.status.shape", Component.translatable(getEarShapeKey(localData.getEarShape()))), 22, height - 64, 0xF5C037);
        guiGraphics.drawString(font, Component.translatable("gui.faradayears.status.mode", Component.translatable(getTailCountKey(localData.getTailCount()))), 22, height - 52, 0xF5C037);

        guiGraphics.fill(width / 2 - 30, 35, width - 15, height - 35, 0xBB1E1A17);

        if (activeTab == 3 && canvasWidget != null) {
            // ★ 1.3.0: индикатор текущего цвета — сразу видно, чем сейчас рисуешь:
            int cc = canvasWidget.getCurrentColorArgb();
            boolean eraser = (cc >>> 24) == 0;
            guiGraphics.fill(width / 2 + 131, 246, width / 2 + 143, 258, eraser ? 0xFF666666 : cc);
            guiGraphics.drawString(font, Component.translatable(eraser ? "gui.faradayears.indicator.eraser" : "gui.faradayears.indicator.color"), width / 2 + 147, 248, 0xFFEEEEEE, false);
            if (height > 280) {
                // ★ 1.3.0: короткая шпаргалка по развёртке (теперь v идёт ВДОЛЬ хвоста):
                guiGraphics.drawString(font, Component.translatable("gui.faradayears.hint.vertical"), width / 2 - 25, 259, 0xFFEE8C1E, false);
                guiGraphics.drawString(font, Component.translatable("gui.faradayears.hint.horizontal"), width / 2 - 25, 270, 0xFFBBBBBB, false);
            }
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
            super(x, y, width, height, Component.translatable(prefix, String.format("%.2f", currentVal)), (Mth.clamp(currentVal, minVal, maxVal) - minVal) / (maxVal - minVal));
            this.prefix = prefix;
            this.minVal = minVal;
            this.maxVal = maxVal;
            this.onChange = onChange;
        }

        @Override
        protected void updateMessage() {
            float val = minVal + (float) value * (maxVal - minVal);
            setMessage(Component.translatable(prefix, String.format("%.2f", val)));
        }

        @Override
        protected void applyValue() {
            float val = minVal + (float) value * (maxVal - minVal);
            onChange.accept(val);
        }
    }
}

