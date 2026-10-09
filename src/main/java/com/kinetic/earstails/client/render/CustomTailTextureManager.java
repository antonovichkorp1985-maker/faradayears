package com.kinetic.earstails.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ★ 1.2.0: Менеджер кастомных текстур хвоста/ушек.
 *
 * Раньше пиксельный редактор сохранял текстуру в NBT и синхронизировал её по сети,
 * но рендер её полностью игнорировал — хвост всегда рисовался залитым цветом.
 * Теперь данные реально попадают на GPU: для каждого игрока создаётся динамическая
 * текстура 64x64 (единый UV-лист: ушки Y=0..31, хвост Y=32..63), которая обновляется
 * при изменении `customTextureBase64` и подставляется в рендер вместо стандартной.
 *
 * Формат base64 — тот же, что экспортирует {@link com.kinetic.earstails.client.gui.TextureCanvasWidget}:
 * hex-цвета ARGB, разделённые ';', порядок колонко-мажорный (index = x*64 + y).
 * Поддерживается легаси-формат 16x16 из прошлых версий: он разворачивается
 * в хвостовую зону (верхние 8 строк -> шерсть Y=32..47, нижние 8 -> кончик Y=48..63).
 */
public final class CustomTailTextureManager {
    public static final int TEXTURE_SIZE = 64;
    private static final int LEGACY_SIZE = 16;
    private static final int MAX_CACHE = 128;

    private static final Map<UUID, Entry> CACHE = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Entry> eldest) {
            if (size() > MAX_CACHE) {
                // Запись в TextureManager остаётся и будет заменена при повторном register
                // того же пути; здесь достаточно освободить пиксельный буфер:
                try {
                    eldest.getValue().texture.close();
                } catch (Exception ignored) {
                }
                return true;
            }
            return false;
        }
    };

    private static class Entry {
        final DynamicTexture texture;
        final ResourceLocation location;
        String lastData = null;

        Entry(DynamicTexture texture, ResourceLocation location) {
            this.texture = texture;
            this.location = location;
        }
    }

    private CustomTailTextureManager() {
    }

    /**
     * Возвращает локацию динамической текстуры игрока или null, если кастомная
     * текстура не задана. Обновляет пиксели на GPU только при изменении данных.
     */
    public static ResourceLocation getTexture(UUID playerId, String base64) {
        if (base64 == null || base64.isEmpty()) return null;

        Entry e = CACHE.get(playerId);
        if (e == null) {
            DynamicTexture texture = new DynamicTexture(TEXTURE_SIZE, TEXTURE_SIZE, true);
            String path = "faradayears_custom_" + playerId.toString().replace('-', '_');
            ResourceLocation location = Minecraft.getInstance().getTextureManager().register(path, texture);
            e = new Entry(texture, location);
            CACHE.put(playerId, e);
        }

        if (!base64.equals(e.lastData)) {
            e.lastData = base64;
            int[][] grid = decodeBase64(base64);
            NativeImage img = e.texture.getPixels();
            if (img != null) {
                for (int x = 0; x < TEXTURE_SIZE; x++) {
                    for (int y = 0; y < TEXTURE_SIZE; y++) {
                        int c = grid[x][y];
                        int a = (c >>> 24) & 0xFF;
                        int r = (c >> 16) & 0xFF;
                        int g = (c >> 8) & 0xFF;
                        int b = c & 0xFF;
                        // NativeImage хранит ABGR:
                        img.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                    }
                }
                e.texture.upload();
            }
        }
        return e.location;
    }

    /** Быстрая проверка без создания текстуры. */
    public static boolean hasCustomTexture(com.kinetic.earstails.capability.PlayerEarsTailData data) {
        return data != null && data.isCustomTextureEnabled()
                && data.getCustomTextureBase64() != null && !data.getCustomTextureBase64().isEmpty();
    }

    /**
     * Декодирует base64 в сетку [x][y] 64x64. Понимает и новый формат (4096 значений),
     * и легаси 16x16 (256 значений -> растягивается в хвостовую зону Y=32..63).
     */
    public static int[][] decodeBase64(String base64) {
        int[][] grid = defaultGrid();
        try {
            String decoded = new String(Base64.getDecoder().decode(base64));
            String[] parts = decoded.split(";");
            if (parts.length >= TEXTURE_SIZE * TEXTURE_SIZE) {
                // Новый формат 64x64, колонко-мажорный порядок (совпадает с экспортом канваса):
                for (int x = 0; x < TEXTURE_SIZE; x++) {
                    for (int y = 0; y < TEXTURE_SIZE; y++) {
                        String part = parts[x * TEXTURE_SIZE + y];
                        if (!part.isEmpty()) {
                            grid[x][y] = Integer.parseUnsignedInt(part, 16);
                        }
                    }
                }
            } else if (parts.length >= LEGACY_SIZE * LEGACY_SIZE) {
                // Легаси 16x16: строки 0..7 -> шерсть (Y=32..47), строки 8..15 -> кончик (Y=48..63):
                int[][] old = new int[LEGACY_SIZE][LEGACY_SIZE];
                for (int x = 0; x < LEGACY_SIZE; x++) {
                    for (int y = 0; y < LEGACY_SIZE; y++) {
                        String part = parts[x * LEGACY_SIZE + y];
                        old[x][y] = part.isEmpty() ? 0xFF262220 : Integer.parseUnsignedInt(part, 16);
                    }
                }
                for (int x = 0; x < TEXTURE_SIZE; x++) {
                    for (int y = 32; y < TEXTURE_SIZE; y++) {
                        int oldX = x / 4;
                        int oldY = (y - 32) / 2;
                        grid[x][y] = old[oldX][oldY];
                    }
                }
            }
        } catch (Exception ignored) {
            // Битые данные -> остаётся сетка по умолчанию.
        }
        return grid;
    }

    /** Стандартная раскладка единого листа (совпадает с шаблоном kinetic_template.png). */
    public static int[][] defaultGrid() {
        int[][] grid = new int[TEXTURE_SIZE][TEXTURE_SIZE];
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                if (y < 16) {
                    // Зона внешнего каркаса ушек:
                    grid[x][y] = (x >= 16 && x < 32) ? 0xFFD84B16
                            : (x >= 32 && x < 48) ? 0xFF4A4D52
                            : (x >= 48) ? 0xFFE6E6E6
                            : 0xFF262220;
                } else if (y < 32) {
                    // Зона внутреннего меха ушек и кисточек:
                    grid[x][y] = (x >= 16 && x < 32) ? 0xFFFFFFFF
                            : (x >= 32 && x < 48) ? 0xFFCCCCCC
                            : (x >= 48) ? 0xFFF5F5FF
                            : 0xFFEE8C1E;
                } else if (y < 48) {
                    // Основная шерсть хвоста:
                    grid[x][y] = 0xFF262220;
                } else {
                    // Шерсть кончика хвоста:
                    grid[x][y] = 0xFFC44D14;
                }
            }
        }
        return grid;
    }
}
