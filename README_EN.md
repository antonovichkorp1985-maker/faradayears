# FaradayEarsMod — Customizable Ears, Procedural Tail, Body Physics & Belt Pouch (1.21.1 NeoForge)

[🇷🇺 Русская версия](README.md) · [📜 Changelog](CHANGELOG.md) · [🚀 Publishing guide](GITHUB_SETUP.md)

A client/server Minecraft **1.21.1 Forge** mod that adds fully customizable 3D ears, a physics-driven procedural tail, body/figure modifications with independent jiggle physics, a 3D GUI preview and a decorative front belt pouch — all in the style of the **Yellow Fire** YouTube channel character.

> **License:** MIT — open source, free to use, modify and distribute.

---

## ✨ Features

### 🐱 Ears
* 5 anatomical ear shapes: ★ Faraday (with tufts), 🦊 Kitsune / 🐱 Cat, 🐺 Wolf, 🐰 Rabbit, 🐻 Bear.
* True 3D volume: U-shaped shells with hollow cavity and fuzzy inner fur.
* White bow loops with royal blue streamers (Felix Argyle style).
* Independent left/right ear physics with **async ceiling collision** (3 stages of flattening in low corridors).
* Rotating 3D hitboxes (`F3+B`) — 6 separate AABBs that follow the head.

### 🦊 Tail
* Hybrid **Verlet + PBD** rope physics engine with constant gravity (`-0.070D` on ground / `-0.060D` in air) — no levitation, no sagging dips.
* First segment exits the lower back strictly horizontally (90° out of the spine), flush against the skin; distal segments drape smoothly onto any terrain.
* 1–6 user segments (GUI slider) over a high-resolution internal physical chain.
* Up to **9 independent tails** (Kitsune mode), each with its own `F3+B` hitbox chain.
* Live synchronized wagging as a traveling wave.

### 👗 Body & physics
* 4 figure modes: **0 Neutral, 1 ♀ Female, 2 ♂ Athletic male, 3 ⚧ Combined**.
* Independent physics for **each half** of the chest (`chestLeft/chestRight`) and hips (`hipsLeft/hipsRight`) — 4 separate inertial bodies with their own spring-damper state, frequencies (`tick*1.15` vs `tick*0.72`) and counter-phase turn inertia.
* True **3D world collisions (AABB)** for chest, hips and pouch — squash against walls, fences, columns; pouch/chest mutual repulsion.
* Reduced vertical inertia (bouncy but not floaty), deep horizontal/depth oscillations.
* 100% skin-blended rendering (`player.getSkinTextureLocation()`): chest reads `texOffs(20,20)`, hips/pants `texOffs(8,20)`, pouch `texOffs(20,28)` — seamless continuation of the player's skin.

### 🎒 Belt pouch
* Decorative front belt pouch with its own jiggle physics.
* Position offsets (X/Y/Z) and size sliders (X/Y/Z, `0.10..0.50`), centered pivot so scaling never shifts it into the chest.

### 🎨 GUI
* Open with a hotkey (**V** or **G**, configurable) — 6 clean tabs: ★ Presets, 🐱 Ears, 🦊 Tail, 🎨 Colors, 👗 Figure, 🎒 Pouch.
* **Orbit camera 3D preview:** LMB-drag rotate, RMB/scroll zoom, MMB/Shift+LMB pan.
* Presets: Faraday (Yellow Fire), Felix, Fox/Cat, Wolf, Rabbit.
* Pixel-art texture painting right inside the GUI.
* Network sync — other players see your ears/tail/body (multiplayer!).

---

## 🖥️ Requirements
* Minecraft **1.21.1**
* Forge **43.3.0** or newer (`[43,)`)
* Java **17**

## 📥 Installation
1. Install NeoForge 21.1.x.
2. Copy `FaradayEarsMod-1.21.1-1.0.0.jar` into your `.minecraft/mods/` folder.
3. Launch, press **V** (or **G**) in-game to open the customization GUI.

## 🛠️ Building from source
Windows:
```powershell
.\gradlew.bat build
```
Linux/macOS:
```bash
chmod +x gradlew
./gradlew build
```
The built jar appears in `build/libs/`.

## 🧩 Project structure
```
src/main/java/com/yellowfire/faradayears/
├── FaradayEarsMod.java            # Mod entry point (FML events)
├── capability/                    # PlayerEarsTailData — persistent player settings (NBT)
├── client/
│   ├── ClientEvents.java          # Keybinds, tick hooks, network
│   ├── gui/                       # EarsTailCustomizationScreen, TextureCanvasWidget
│   ├── model/                     # FaradayEarsModel, FaradayTailModel, FaradayBodyModel
│   └── render/                    # EarsAndTailLayer, ProceduralTailRenderer
├── network/                       # ModPacketHandler, SyncEarsTailPacket
└── physics/
    ├── TailPhysicsEngine.java     # Verlet+PBD rope, body jiggle, world collisions
    └── core/                      # PhysicsChain, PhysicsParticle, PhysicsWorldCollider
```

## 🤝 Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md).

## 🔒 Security
See [SECURITY.md](SECURITY.md).

## 📄 License
MIT — see [LICENSE](LICENSE).
