package com.kinetic.earstails.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.function.IntConsumer;

/** Compact HSV picker: saturation/value field plus hue strip. */
public final class ColorPickerWidget extends AbstractWidget {
    private final IntConsumer listener;
    private float hue = 0.08f;
    private float saturation = 0.87f;
    private float value = 0.93f;

    public ColorPickerWidget(int x, int y, int width, int height, IntConsumer listener) {
        super(x, y, width, height, Component.translatable("gui.faradayears.color.picker"));
        this.listener = listener;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int fieldHeight = getHeight() - 14;
        int hueColor = Color.HSBtoRGB(hue, 1.0f, 1.0f) | 0xFF000000;
        for (int px = 0; px < getWidth(); px++) {
            float s = px / (float) Math.max(1, getWidth() - 1);
            for (int py = 0; py < fieldHeight; py++) {
                float v = 1.0f - py / (float) Math.max(1, fieldHeight - 1);
                graphics.fill(getX() + px, getY() + py, getX() + px + 1, getY() + py + 1,
                        Color.HSBtoRGB(hue, s, v) | 0xFF000000);
            }
        }
        for (int px = 0; px < getWidth(); px++) {
            int color = Color.HSBtoRGB(px / (float) Math.max(1, getWidth() - 1), 1.0f, 1.0f) | 0xFF000000;
            graphics.fill(getX() + px, getY() + fieldHeight + 3, getX() + px + 1, getY() + getHeight(), color);
        }
        int cx = getX() + Math.round(saturation * (getWidth() - 1));
        int cy = getY() + Math.round((1.0f - value) * (fieldHeight - 1));
        graphics.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0xFFFFFFFF);
        graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, hueColor);
        int hx = getX() + Math.round(hue * (getWidth() - 1));
        graphics.fill(hx - 1, getY() + fieldHeight + 1, hx + 2, getY() + getHeight(), 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOver(mouseX, mouseY)) return false;
        choose(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0) return false;
        choose(mouseX, mouseY);
        return true;
    }

    private void choose(double mouseX, double mouseY) {
        int fieldHeight = getHeight() - 14;
        float localX = clamp((float) (mouseX - getX()) / Math.max(1, getWidth() - 1));
        float localY = (float) (mouseY - getY());
        if (localY >= fieldHeight) hue = localX;
        else {
            saturation = localX;
            value = 1.0f - clamp(localY / Math.max(1, fieldHeight - 1));
        }
        listener.accept(Color.HSBtoRGB(hue, saturation, value) & 0xFFFFFF);
    }

    private static float clamp(float value) { return Math.max(0.0f, Math.min(1.0f, value)); }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
