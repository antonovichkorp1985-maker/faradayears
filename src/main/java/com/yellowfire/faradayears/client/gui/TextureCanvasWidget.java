package com.yellowfire.faradayears.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Base64;

public class TextureCanvasWidget extends AbstractWidget {
    private final int gridSize = 16;
    private final int[][] pixelGrid = new int[gridSize][gridSize];
    private int currentColor = 0xFFEE8C1E; // Оранжево-жёлтый по умолчанию
    private DynamicTexture dynamicTexture;
    private ResourceLocation dynamicTextureLocation;

    public TextureCanvasWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Полотно текстуры"));
        initDefaultFaradayPattern();
        updateDynamicTexture();
    }

    private void initDefaultFaradayPattern() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (i < 8 && j < 8) {
                    pixelGrid[i][j] = 0xFF262220; // Тёмная шерсть
                } else if (i >= 8 && j < 8) {
                    pixelGrid[i][j] = 0xFFEE8C1E; // Оранжевые кисточки
                } else {
                    pixelGrid[i][j] = 0xFFC44D14; // Кончик хвоста
                }
            }
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

    public void importFromNativeImage(NativeImage img) {
        if (img != null) {
            int w = Math.min(gridSize, img.getWidth());
            int h = Math.min(gridSize, img.getHeight());
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int abgr = img.getPixelRGBA(x, y);
                    int r = abgr & 0xFF;
                    int g = (abgr >> 8) & 0xFF;
                    int b = (abgr >> 16) & 0xFF;
                    pixelGrid[x][y] = 0xFF000000 | (r << 16) | (g << 8) | b;
                }
            }
            updateDynamicTexture();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF000000);
        int cellWidth = width / gridSize;
        int cellHeight = height / gridSize;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int px = x + i * cellWidth;
                int py = y + j * cellHeight;
                guiGraphics.fill(px, py, px + cellWidth, py + cellHeight, pixelGrid[i][j]);
            }
        }

        for (int i = 0; i <= gridSize; i++) {
            int lineX = x + i * cellWidth;
            int lineY = y + i * cellHeight;
            guiGraphics.fill(lineX, y, lineX + 1, y + height, 0x33FFFFFF);
            guiGraphics.fill(x, lineY, x + width, lineY + 1, 0x33FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button) && this.isHoveredOrFocused()) {
            applyPaint(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isValidClickButton(button) && this.isHoveredOrFocused()) {
            applyPaint(mouseX, mouseY);
            return true;
        }
        return false;
    }

    private void applyPaint(double mouseX, double mouseY) {
        int cellWidth = getWidth() / gridSize;
        int cellHeight = getHeight() / gridSize;
        int gx = (int) ((mouseX - getX()) / cellWidth);
        int gy = (int) ((mouseY - getY()) / cellHeight);
        if (gx >= 0 && gx < gridSize && gy >= 0 && gy < gridSize) {
            pixelGrid[gx][gy] = currentColor;
            updateDynamicTexture();
        }
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
        try {
            String decoded = new String(Base64.getDecoder().decode(base64));
            String[] parts = decoded.split(";");
            int idx = 0;
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    if (idx < parts.length && !parts[idx].isEmpty()) {
                        pixelGrid[i][j] = Integer.parseUnsignedInt(parts[idx], 16);
                    }
                    idx++;
                }
            }
            updateDynamicTexture();
        } catch (Exception e) {}
    }

    public void loadFromBufferedImage(java.awt.image.BufferedImage img) {
        if (img == null) return;
        for (int i = 0; i < Math.min(gridSize, img.getWidth()); i++) {
            for (int j = 0; j < Math.min(gridSize, img.getHeight()); j++) {
                pixelGrid[i][j] = img.getRGB(i, j) & 0xFFFFFF;
            }
        }
        updateDynamicTexture();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}
}
