# Changelog

## 1.0.0 — NeoForge 1.21.1 (V56)

Порт мода с Minecraft 1.19.2 (Forge) на **Minecraft 1.21.1 + NeoForge 21.1.251**.

### Платформа
- Загрузчик: **NeoForge 21.1.x** (вместо Forge 43.x), Java 21
- Система данных игрока: **Data Attachments** (`copyOnDeath`) вместо Forge Capabilities
- Сеть: **CustomPacketPayload + StreamCodec** (`playBidirectional`) вместо SimpleChannel
- GUI переведён на **GuiGraphics**, 3D-предпросмотр — на JOML `Matrix4fStack`/`Axis`
- Сборка через **ModDevGradle 2.x**, GitHub Actions — JDK 21
- `mods.toml` → `META-INF/neoforge.mods.toml`

### Геймплей (без изменений)
- Ушки (5 форм), хвост (Verlet+PBD), фигура с jiggle-физикой полушарий, поясной мешочек
- Слайдеры `Сегментов в длину: 1..6` — внутренняя физика по-прежнему 3× микросегмента
- Гравитация хвоста постоянная: `-0.070` на земле / `-0.060` в воздухе
- Корень хвоста выходит из поясницы строго горизонтально (90°), дистальные сегменты стелются по земле
- Физика груди/бёдер/мешочка с независимыми полушариями
- Скин-бледнинг через UV скина игрока: `texOffs(20,20)`, `texOffs(8,20)`, `texOffs(20,28)`
- Синхронизация настроек между игроками (мультиплеер)
