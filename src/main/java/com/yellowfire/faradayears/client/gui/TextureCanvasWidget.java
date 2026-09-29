package com.yellowfire.faradayears.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.yellowfire.faradayears.client.render.CustomTailTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Base64;

/**
 * ★ 1.2.0: полотно пиксельного редактора теперь 64x64 и в точности повторяет
 * единый UV-лист мода (совпадает с faraday_template.png):
 *   Y = 0..15  — внешний каркас ушек (Primary),
 *   Y = 16..31 — внутренний мех ушек и кисточки (Secondary),
 *   Y = 32..47 — основная шерсть хвоста,
 *   Y = 48..63 — шерсть кончика хвоста.
 * ЛКМ — рисовать выбранным цветом, ПКМ — ластик (прозрачный пиксель, сквозь
 * который видно основной цвет). Раскраска сразу видна в 3D-превью.
 */
public class TextureCanvasWidget extends AbstractWidget {
    private final int gridSize = 64;
    private final int[][] pixelGrid = new int[gridSize][gridSize];
    private int currentColor = 0xFFEE8C1E; // Оранжево-жёлтый по умолчанию
    private DynamicTexture dynamicTexture;
    private ResourceLocation dynamicTextureLocation;
    private Runnable onPaintListener;

    public TextureCanvasWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Полотно текстуры"));
        initDefaultPattern();
        updateDynamicTexture();
    }

    /** Вызывается после каждого изменения пикселей (для live-обновления 3D-превью). */
    public void setOnPaintListener(Runnable listener) {
        this.onPaintListener = listener;
    }

    private void initDefaultPattern() {
        int[][] def = CustomTailTextureManager.defaultGrid();
        for (int x = 0; x < gridSize; x++) {
            System.arraycopy(def[x], 0, pixelGrid[x], 0, gridSize);
        }
    }

    private void updateDynamicTexture() {
        if (dynamicTexture == null) {
            dynamicTexture = new DynamicTexture(gridSize, gridSize, true);
            dynamicTextureLocation = Minecraft.getInstance().getTextureManager().register("faradayears_custom_canvas", dynamicTexture);
        }
        NativeImage img = dynamicTexture.getPixels();
        if (img != null) {
            for (int x = 0; x < gridSize; x++) {
                for (int y = 0; y < gridSize; y++) {
                    int c = pixelGrid[x][y];
                    int a = (c >> 24) & 0xFF;
                    int r = (c >> 16) & 0xFF;
                    int g = (c >> 8) & 0xFF;
                    int b = c & 0xFF;
                    int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                    img.setPixelRGBA(x, y, abgr);
                }
            }
            dynamicTexture.upload();
        }
    }

    public void setCurrentColor(int rgbColor) {
        this.currentColor = 0xFF000000 | (rgbColor & 0xFFFFFF);
    }

    /** Выбрать «ластик»: ЛКМ будет рисовать прозрачные пиксели (до выбора другого цвета). */
    public void setCurrentColorTransparent() {
        this.currentColor = 0x00000000;
    }

    public void importFromNativeImage(NativeImage img) {
        if (img != null) {
            int w = Math.min(gridSize, img.getWidth());
            int h = Math.min(gridSize, img.getHeight());
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int abgr = img.getPixelRGBA(x, y);
                    int a = (abgr >> 24) & 0xFF;
                    int r = abgr & 0xFF;
                    int g = (abgr >> 8) & 0xFF;
                    int b = (abgr >> 16) & 0xFF;
                    pixelGrid[x][y] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
            updateDynamicTexture();
            notifyPainted();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF000000);
        int cellWidth = Math.max(1, width / gridSize);
        int cellHeight = Math.max(1, height / gridSize);

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int px = x + i * cellWidth;
                int py = y + j * cellHeight;
                guiGraphics.fill(px, py, px + cellWidth, py + cellHeight, pixelGrid[i][j]);
            }
        }

        // Крупная сетка каждые 16 пикселей — границы четырёх зон единого листа:
        for (int i = 0; i <= gridSize; i += 16) {
            int lineX = x + i * cellWidth;
            int lineY = y + i * cellHeight;
            guiGraphics.fill(lineX, y, lineX + 1, y + height, 0x66FFFFFF);
            guiGraphics.fill(x, lineY, x + width, lineY + 1, 0x66FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button) && this.isHoveredOrFocused()) {
            applyPaint(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isValidClickButton(button) && this.isHoveredOrFocused()) {
            applyPaint(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    private void applyPaint(double mouseX, double mouseY, int button) {
        int cellWidth = Math.max(1, getWidth() / gridSize);
        int cellHeight = Math.max(1, getHeight() / gridSize);
        int gx = (int) ((mouseX - getX()) / cellWidth);
        int gy = (int) ((mouseY - getY()) / cellHeight);
        if (gx >= 0 && gx < gridSize && gy >= 0 && gy < gridSize) {
            // ПКМ — ластик: прозрачный пиксель (сквозь него виден основной цвет рендера):
            pixelGrid[gx][gy] = (button == 1) ? 0x00000000 : currentColor;
            updateDynamicTexture();
            notifyPainted();
        }
    }

    private void notifyPainted() {
        if (onPaintListener != null) onPaintListener.run();
    }

    public String exportToBase64() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                sb.append(Integer.toHexString(pixelGrid[i][j])).append(";");
            }
        }
        return Base64.getEncoder().encodeToString(sb.toString().getBytes());
    }

    public void importFromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return;
        int[][] grid = CustomTailTextureManager.decodeBase64(base64);
        for (int x = 0; x < gridSize; x++) {
            System.arraycopy(grid[x], 0, pixelGrid[x], 0, gridSize);
        }
        updateDynamicTexture();
    }

    public void loadFromBufferedImage(java.awt.image.BufferedImage img) {
        if (img == null) return;
        for (int i = 0; i < Math.min(gridSize, img.getWidth()); i++) {
            for (int j = 0; j < Math.min(gridSize, img.getHeight()); j++) {
                pixelGrid[i][j] = img.getRGB(i, j); // сохраняем и альфа-канал
            }
        }
        updateDynamicTexture();
        notifyPainted();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}
}
