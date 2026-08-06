package com.yellowfire.faradayears.capability;

import net.minecraft.nbt.CompoundTag;

/**
 * v51-pouch-and-skin-body: adds `showPouch`, `pouchScaleX/Y/Z`, `pouchOffsetY/Z`,
 * and makes tail/ears/body fully support skin-blended chest, hips, and front belt pouch (`декоративный мешочек`).
 */
public class PlayerEarsTailData {
    private boolean showTail = true;
    private boolean showEars = true;
    private int earShape = 0;
    private int tailCount = 10;
    private float tailFanSpread = 24.0f;
    private int tailSegments = 5;
    private float tailSegmentLength = 7.49f;
    private float tailTaper = 0.54f;
    private int earType = 0;
    private int tailType = 0;
    private float earScaleX = 1.0f;
    private float earScaleY = 1.0f;
    private float earScaleZ = 1.0f;
    private float earRotX = 0.0f;
    private float earRotY = 0.0f;
    private float earRotZ = 0.0f;
    private float earOffsetX = 0.0f;
    private float earOffsetY = 0.0f;
    private float earOffsetZ = 0.0f;
    private float tailScaleX = 0.86f;
    private float tailScaleY = 0.86f;
    private float tailScaleZ = 1.0f;
    private float tailRotX = 0.0f;
    private float tailRotY = 0.0f;
    private float tailRotZ = 0.0f;
    private float tailOffsetX = 0.0f;
    private float tailOffsetY = 0.0f;
    private float tailOffsetZ = 0.0f;
    private int earColorPrimary = 0x262220;
    private int earColorSecondary = 0xEE8C1E;
    private int tailColorPrimary = 0x262220;
    private int tailColorSecondary = 0xC44D14;
    private boolean animate = true;

    private int tailWagAxis = 3;
    private float tailWagAmplitude = 14.0f;
    private float tailWagSpeed = 1.5f;
    private String customTextureBase64 = "";

    // ★ РАЗДЕЛ ГЕНДЕРА, ФИГУРЫ И ДЕКОРАТИВНОГО МЕШОЧКА:
    private int gender = 0;
    private boolean showChest = true;
    private boolean showHips = true;
    private boolean showShoulders = true;
    private boolean showPouch = false; // Декоративный поясной мешочек спереди с физикой
    private float chestScaleX = 1.0f;
    private float chestScaleY = 1.0f;
    private float chestScaleZ = 1.1f;
    private float chestOffsetY = 0.0f;
    private float chestOffsetZ = 0.0f;
    private float hipsScaleX = 1.05f;
    private float hipsScaleY = 1.0f;
    private float hipsScaleZ = 1.15f;
    private float hipsOffsetY = 0.0f;
    private float hipsOffsetZ = 0.0f;
    private float shouldersScaleX = 1.15f;
    private float shouldersScaleY = 1.0f;
    private float shouldersScaleZ = 1.05f;
    private float shouldersOffsetY = 0.0f;
    private float pouchScaleX = 0.35f;
    private float pouchScaleY = 0.35f;
    private float pouchScaleZ = 0.35f;
    private float pouchOffsetX = 0.0f;
    private float pouchOffsetY = 0.0f;
    private float pouchOffsetZ = 0.0f;
    private float bodyJiggleStrength = 1.0f;

    public void copyFrom(PlayerEarsTailData other) {
        this.showTail = other.showTail;
        this.showEars = other.showEars;
        this.earShape = other.earShape;
        this.tailCount = other.tailCount;
        this.tailFanSpread = other.tailFanSpread;
        this.tailSegments = other.tailSegments;
        this.tailSegmentLength = other.tailSegmentLength;
        this.tailTaper = other.tailTaper;
        this.earType = other.earType;
        this.tailType = other.tailType;
        this.earScaleX = other.earScaleX;
        this.earScaleY = other.earScaleY;
        this.earScaleZ = other.earScaleZ;
        this.earRotX = other.earRotX;
        this.earRotY = other.earRotY;
        this.earRotZ = other.earRotZ;
        this.earOffsetX = other.earOffsetX;
        this.earOffsetY = other.earOffsetY;
        this.earOffsetZ = other.earOffsetZ;
        this.tailScaleX = other.tailScaleX;
        this.tailScaleY = other.tailScaleY;
        this.tailScaleZ = other.tailScaleZ;
        this.tailRotX = other.tailRotX;
        this.tailRotY = other.tailRotY;
        this.tailRotZ = other.tailRotZ;
        this.tailOffsetX = other.tailOffsetX;
        this.tailOffsetY = other.tailOffsetY;
        this.tailOffsetZ = other.tailOffsetZ;
        this.earColorPrimary = other.earColorPrimary;
        this.earColorSecondary = other.earColorSecondary;
        this.tailColorPrimary = other.tailColorPrimary;
        this.tailColorSecondary = other.tailColorSecondary;
        this.animate = other.animate;
        this.tailWagAxis = other.tailWagAxis;
        this.tailWagAmplitude = other.tailWagAmplitude;
        this.tailWagSpeed = other.tailWagSpeed;
        this.customTextureBase64 = other.customTextureBase64;

        this.gender = other.gender;
        this.showChest = other.showChest;
        this.showHips = other.showHips;
        this.showShoulders = other.showShoulders;
        this.showPouch = other.showPouch;
        this.chestScaleX = other.chestScaleX;
        this.chestScaleY = other.chestScaleY;
        this.chestScaleZ = other.chestScaleZ;
        this.chestOffsetY = other.chestOffsetY;
        this.chestOffsetZ = other.chestOffsetZ;
        this.hipsScaleX = other.hipsScaleX;
        this.hipsScaleY = other.hipsScaleY;
        this.hipsScaleZ = other.hipsScaleZ;
        this.hipsOffsetY = other.hipsOffsetY;
        this.hipsOffsetZ = other.hipsOffsetZ;
        this.shouldersScaleX = other.shouldersScaleX;
        this.shouldersScaleY = other.shouldersScaleY;
        this.shouldersScaleZ = other.shouldersScaleZ;
        this.shouldersOffsetY = other.shouldersOffsetY;
        this.pouchScaleX = other.pouchScaleX;
        this.pouchScaleY = other.pouchScaleY;
        this.pouchScaleZ = other.pouchScaleZ;
        this.pouchOffsetX = other.pouchOffsetX;
        this.pouchOffsetY = other.pouchOffsetY;
        this.pouchOffsetZ = other.pouchOffsetZ;
        this.bodyJiggleStrength = other.bodyJiggleStrength;
    }

    public void applyFaradayPreset() {
        this.showEars = true;
        this.showTail = true;
        this.earShape = 0;
        this.tailCount = 10;
        this.tailFanSpread = 24.0f;
        this.tailSegments = 5;
        this.tailSegmentLength = 7.49f;
        this.tailTaper = 0.54f;
        this.tailScaleX = 0.86f;
        this.tailRotX = 0.0f;
        this.earColorPrimary = 0x262220;
        this.earColorSecondary = 0xEE8C1E;
        this.tailColorPrimary = 0x262220;
        this.tailColorSecondary = 0xC44D14;
        this.animate = true;
        this.tailWagAxis = 3;
        this.tailWagAmplitude = 14.0f;
        this.tailWagSpeed = 1.5f;
    }

    public void applyFoxPreset() {
        this.showEars = true;
        this.showTail = true;
        this.earShape = 0;
        this.tailCount = 1;
        this.tailFanSpread = 30.0f;
        this.tailSegments = 4;
        this.tailSegmentLength = 5.5f;
        this.tailTaper = 0.85f;
        this.tailScaleX = 1.0f;
        this.tailRotX = 0.0f;
        this.earColorPrimary = 0xD84B16;
        this.earColorSecondary = 0xFFFFFF;
        this.tailColorPrimary = 0xD84B16;
        this.tailColorSecondary = 0xFFFFFF;
        this.animate = true;
        this.tailWagAxis = 0;
        this.tailWagAmplitude = 16.0f;
        this.tailWagSpeed = 1.8f;
    }

    public void applyWolfPreset() {
        this.showEars = true;
        this.showTail = true;
        this.earShape = 0;
        this.tailCount = 1;
        this.tailFanSpread = 30.0f;
        this.tailSegments = 4;
        this.tailSegmentLength = 6.5f;
        this.tailTaper = 0.80f;
        this.tailScaleX = 1.05f;
        this.tailRotX = 0.0f;
        this.earColorPrimary = 0x4A4D52;
        this.earColorSecondary = 0xCCCCCC;
        this.tailColorPrimary = 0x4A4D52;
        this.tailColorSecondary = 0xCCCCCC;
        this.animate = true;
        this.tailWagAxis = 0;
        this.tailWagAmplitude = 12.0f;
        this.tailWagSpeed = 1.2f;
    }

    public void applyBunnyPreset() {
        this.showEars = true;
        this.showTail = true;
        this.earShape = 1;
        this.tailCount = 1;
        this.tailFanSpread = 20.0f;
        this.tailSegments = 1;
        this.tailSegmentLength = 4.0f;
        this.tailTaper = 1.10f;
        this.tailScaleX = 0.85f;
        this.tailRotX = 0.0f;
        this.earColorPrimary = 0xE6E6E6;
        this.earColorSecondary = 0xFFB6C1;
        this.tailColorPrimary = 0xE6E6E6;
        this.tailColorSecondary = 0xFFB6C1;
        this.animate = true;
        this.tailWagAxis = 1;
        this.tailWagAmplitude = 8.0f;
        this.tailWagSpeed = 2.5f;
    }

    public void applyKitsunePreset() {
        this.showEars = true;
        this.showTail = true;
        this.earShape = 0;
        this.tailCount = 9;
        this.tailFanSpread = 120.0f;
        this.tailSegments = 5;
        this.tailSegmentLength = 6.0f;
        this.tailTaper = 0.90f;
        this.tailScaleX = 1.0f;
        this.tailRotX = 0.0f;
        this.earColorPrimary = 0xD84B16;
        this.earColorSecondary = 0xFFFFFF;
        this.tailColorPrimary = 0xD84B16;
        this.tailColorSecondary = 0xFFFFFF;
        this.animate = true;
        this.tailWagAxis = 0;
        this.tailWagAmplitude = 18.0f;
        this.tailWagSpeed = 1.6f;
    }

    public void applyFelixPreset() {
        this.showEars = true;
        this.showTail = true;
        this.earShape = 1;
        this.tailCount = 1;
        this.tailFanSpread = 24.0f;
        this.tailSegments = 6;
        this.tailSegmentLength = 5.2f;
        this.tailTaper = 0.85f;
        this.tailScaleX = 0.95f;
        this.tailRotX = 0.0f;
        this.earColorPrimary = 0xC4A287;
        this.earColorSecondary = 0xFFF5F5;
        this.tailColorPrimary = 0xC4A287;
        this.tailColorSecondary = 0xFFF5F5;
        this.animate = true;
        this.tailWagAxis = 2;
        this.tailWagAmplitude = 16.0f;
        this.tailWagSpeed = 1.8f;
    }

    public void resetTailOnly() {
        this.showTail = true;
        this.tailCount = 10;
        this.tailFanSpread = 24.0f;
        this.tailSegments = 5;
        this.tailSegmentLength = 7.49f;
        this.tailTaper = 0.54f;
        this.tailScaleX = 0.86f;
        this.tailRotX = 0.0f;
        this.tailWagAxis = 3;
        this.tailWagAmplitude = 14.0f;
        this.tailWagSpeed = 1.5f;
    }

    public void resetEarsOnly() {
        this.showEars = true;
        this.earShape = 0;
        this.earType = 0;
        this.earScaleX = 1.0f;
        this.earScaleY = 1.0f;
        this.earScaleZ = 1.0f;
        this.earRotX = 0.0f;
        this.earRotY = 0.0f;
        this.earRotZ = 0.0f;
        this.earOffsetX = 0.0f;
        this.earOffsetY = 0.0f;
        this.earOffsetZ = 0.0f;
        this.earColorPrimary = 0x262220;
        this.earColorSecondary = 0xEE8C1E;
    }

    public void resetBodyOnly() {
        this.gender = 0;
        this.showChest = true;
        this.showHips = true;
        this.showShoulders = true;
        this.showPouch = false;
        this.chestScaleX = 1.0f;
        this.chestScaleY = 1.0f;
        this.chestScaleZ = 1.1f;
        this.chestOffsetY = 0.0f;
        this.chestOffsetZ = 0.0f;
        this.hipsScaleX = 1.05f;
        this.hipsScaleY = 1.0f;
        this.hipsScaleZ = 1.15f;
        this.hipsOffsetY = 0.0f;
        this.hipsOffsetZ = 0.0f;
        this.shouldersScaleX = 1.15f;
        this.shouldersScaleY = 1.0f;
        this.shouldersScaleZ = 1.05f;
        this.shouldersOffsetY = 0.0f;
        this.pouchScaleX = 0.35f;
        this.pouchScaleY = 0.35f;
        this.pouchScaleZ = 0.35f;
        this.pouchOffsetX = 0.0f;
        this.pouchOffsetY = 0.0f;
        this.pouchOffsetZ = 0.0f;
        this.bodyJiggleStrength = 1.0f;
    }

    public void resetPouchOnly() {
        this.showPouch = false;
        this.pouchScaleX = 0.35f;
        this.pouchScaleY = 0.35f;
        this.pouchScaleZ = 0.35f;
        this.pouchOffsetX = 0.0f;
        this.pouchOffsetY = 0.0f;
        this.pouchOffsetZ = 0.0f;
    }

    public CompoundTag saveNBTData() {
        CompoundTag nbt = new CompoundTag();
        saveNBTData(nbt);
        return nbt;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("ShowTail", showTail);
        nbt.putBoolean("ShowEars", showEars);
        nbt.putInt("EarShape", earShape);
        nbt.putInt("TailCount", tailCount);
        nbt.putFloat("TailFanSpread", tailFanSpread);
        nbt.putInt("TailSegments", tailSegments);
        nbt.putFloat("TailSegmentLength", tailSegmentLength);
        nbt.putFloat("TailTaper", tailTaper);
        nbt.putInt("EarType", earType);
        nbt.putInt("TailType", tailType);
        nbt.putFloat("EarScaleX", earScaleX);
        nbt.putFloat("EarScaleY", earScaleY);
        nbt.putFloat("EarScaleZ", earScaleZ);
        nbt.putFloat("EarRotX", earRotX);
        nbt.putFloat("EarRotY", earRotY);
        nbt.putFloat("EarRotZ", earRotZ);
        nbt.putFloat("EarOffsetX", earOffsetX);
        nbt.putFloat("EarOffsetY", earOffsetY);
        nbt.putFloat("EarOffsetZ", earOffsetZ);
        nbt.putFloat("TailScaleX", tailScaleX);
        nbt.putFloat("TailScaleY", tailScaleY);
        nbt.putFloat("TailScaleZ", tailScaleZ);
        nbt.putFloat("TailRotX", tailRotX);
        nbt.putFloat("TailRotY", tailRotY);
        nbt.putFloat("TailRotZ", tailRotZ);
        nbt.putFloat("TailOffsetX", tailOffsetX);
        nbt.putFloat("TailOffsetY", tailOffsetY);
        nbt.putFloat("TailOffsetZ", tailOffsetZ);
        nbt.putInt("EarColorPrimary", earColorPrimary);
        nbt.putInt("EarColorSecondary", earColorSecondary);
        nbt.putInt("TailColorPrimary", tailColorPrimary);
        nbt.putInt("TailColorSecondary", tailColorSecondary);
        nbt.putBoolean("Animate", animate);
        nbt.putInt("TailWagAxis", tailWagAxis);
        nbt.putFloat("TailWagAmplitude", tailWagAmplitude);
        nbt.putFloat("TailWagSpeed", tailWagSpeed);
        if (customTextureBase64 != null && !customTextureBase64.isEmpty()) {
            nbt.putString("CustomTextureBase64", customTextureBase64);
        }
        nbt.putInt("Gender", gender);
        nbt.putBoolean("ShowChest", showChest);
        nbt.putBoolean("ShowHips", showHips);
        nbt.putBoolean("ShowShoulders", showShoulders);
        nbt.putBoolean("ShowPouch", showPouch);
        nbt.putFloat("ChestScaleX", chestScaleX);
        nbt.putFloat("ChestScaleY", chestScaleY);
        nbt.putFloat("ChestScaleZ", chestScaleZ);
        nbt.putFloat("ChestOffsetY", chestOffsetY);
        nbt.putFloat("ChestOffsetZ", chestOffsetZ);
        nbt.putFloat("HipsScaleX", hipsScaleX);
        nbt.putFloat("HipsScaleY", hipsScaleY);
        nbt.putFloat("HipsScaleZ", hipsScaleZ);
        nbt.putFloat("HipsOffsetY", hipsOffsetY);
        nbt.putFloat("HipsOffsetZ", hipsOffsetZ);
        nbt.putFloat("ShouldersScaleX", shouldersScaleX);
        nbt.putFloat("ShouldersScaleY", shouldersScaleY);
        nbt.putFloat("ShouldersScaleZ", shouldersScaleZ);
        nbt.putFloat("ShouldersOffsetY", shouldersOffsetY);
        nbt.putFloat("PouchScaleX", pouchScaleX);
        nbt.putFloat("PouchScaleY", pouchScaleY);
        nbt.putFloat("PouchScaleZ", pouchScaleZ);
        nbt.putFloat("PouchOffsetX", pouchOffsetX);
        nbt.putFloat("PouchOffsetY", pouchOffsetY);
        nbt.putFloat("PouchOffsetZ", pouchOffsetZ);
        nbt.putFloat("BodyJiggleStrength", bodyJiggleStrength);
    }

    public void loadNBTData(CompoundTag nbt) {
        if (nbt.contains("ShowTail")) showTail = nbt.getBoolean("ShowTail");
        if (nbt.contains("ShowEars")) showEars = nbt.getBoolean("ShowEars");
        if (nbt.contains("EarShape")) earShape = nbt.getInt("EarShape");
        if (nbt.contains("TailCount")) tailCount = nbt.getInt("TailCount");
        if (nbt.contains("TailFanSpread")) tailFanSpread = nbt.getFloat("TailFanSpread");
        if (nbt.contains("TailSegments")) tailSegments = nbt.getInt("TailSegments");
        if (nbt.contains("TailSegmentLength")) tailSegmentLength = nbt.getFloat("TailSegmentLength");
        if (nbt.contains("TailTaper")) tailTaper = nbt.getFloat("TailTaper");
        if (nbt.contains("EarType")) earType = nbt.getInt("EarType");
        if (nbt.contains("TailType")) tailType = nbt.getInt("TailType");
        if (nbt.contains("EarScaleX")) earScaleX = nbt.getFloat("EarScaleX");
        if (nbt.contains("EarScaleY")) earScaleY = nbt.getFloat("EarScaleY");
        if (nbt.contains("EarScaleZ")) earScaleZ = nbt.getFloat("EarScaleZ");
        if (nbt.contains("EarRotX")) earRotX = nbt.getFloat("EarRotX");
        if (nbt.contains("EarRotY")) earRotY = nbt.getFloat("EarRotY");
        if (nbt.contains("EarRotZ")) earRotZ = nbt.getFloat("EarRotZ");
        if (nbt.contains("EarOffsetX")) earOffsetX = nbt.getFloat("EarOffsetX");
        if (nbt.contains("EarOffsetY")) earOffsetY = nbt.getFloat("EarOffsetY");
        if (nbt.contains("EarOffsetZ")) earOffsetZ = nbt.getFloat("EarOffsetZ");
        if (nbt.contains("TailScaleX")) tailScaleX = nbt.getFloat("TailScaleX");
        if (nbt.contains("TailScaleY")) tailScaleY = nbt.getFloat("TailScaleY");
        if (nbt.contains("TailScaleZ")) tailScaleZ = nbt.getFloat("TailScaleZ");
        if (nbt.contains("TailRotX")) tailRotX = nbt.getFloat("TailRotX");
        if (nbt.contains("TailRotY")) tailRotY = nbt.getFloat("TailRotY");
        if (nbt.contains("TailRotZ")) tailRotZ = nbt.getFloat("TailRotZ");
        if (nbt.contains("TailOffsetX")) tailOffsetX = nbt.getFloat("TailOffsetX");
        if (nbt.contains("TailOffsetY")) tailOffsetY = nbt.getFloat("TailOffsetY");
        if (nbt.contains("TailOffsetZ")) tailOffsetZ = nbt.getFloat("TailOffsetZ");
        if (nbt.contains("EarColorPrimary")) earColorPrimary = nbt.getInt("EarColorPrimary");
        if (nbt.contains("EarColorSecondary")) earColorSecondary = nbt.getInt("EarColorSecondary");
        if (nbt.contains("TailColorPrimary")) tailColorPrimary = nbt.getInt("TailColorPrimary");
        if (nbt.contains("TailColorSecondary")) tailColorSecondary = nbt.getInt("TailColorSecondary");
        if (nbt.contains("Animate")) animate = nbt.getBoolean("Animate");
        if (nbt.contains("TailWagAxis")) tailWagAxis = nbt.getInt("TailWagAxis");
        if (nbt.contains("TailWagAmplitude")) tailWagAmplitude = nbt.getFloat("TailWagAmplitude");
        if (nbt.contains("TailWagSpeed")) tailWagSpeed = nbt.getFloat("TailWagSpeed");
        if (nbt.contains("CustomTextureBase64")) customTextureBase64 = nbt.getString("CustomTextureBase64");
        if (nbt.contains("Gender")) gender = nbt.getInt("Gender");
        if (nbt.contains("ShowChest")) showChest = nbt.getBoolean("ShowChest");
        if (nbt.contains("ShowHips")) showHips = nbt.getBoolean("ShowHips");
        if (nbt.contains("ShowShoulders")) showShoulders = nbt.getBoolean("ShowShoulders");
        if (nbt.contains("ShowPouch")) showPouch = nbt.getBoolean("ShowPouch");
        if (nbt.contains("ChestScaleX")) chestScaleX = nbt.getFloat("ChestScaleX");
        if (nbt.contains("ChestScaleY")) chestScaleY = nbt.getFloat("ChestScaleY");
        if (nbt.contains("ChestScaleZ")) chestScaleZ = nbt.getFloat("ChestScaleZ");
        if (nbt.contains("ChestOffsetY")) chestOffsetY = nbt.getFloat("ChestOffsetY");
        if (nbt.contains("ChestOffsetZ")) chestOffsetZ = nbt.getFloat("ChestOffsetZ");
        if (nbt.contains("HipsScaleX")) hipsScaleX = nbt.getFloat("HipsScaleX");
        if (nbt.contains("HipsScaleY")) hipsScaleY = nbt.getFloat("HipsScaleY");
        if (nbt.contains("HipsScaleZ")) hipsScaleZ = nbt.getFloat("HipsScaleZ");
        if (nbt.contains("HipsOffsetY")) hipsOffsetY = nbt.getFloat("HipsOffsetY");
        if (nbt.contains("HipsOffsetZ")) hipsOffsetZ = nbt.getFloat("HipsOffsetZ");
        if (nbt.contains("ShouldersScaleX")) shouldersScaleX = nbt.getFloat("ShouldersScaleX");
        if (nbt.contains("ShouldersScaleY")) shouldersScaleY = nbt.getFloat("ShouldersScaleY");
        if (nbt.contains("ShouldersScaleZ")) shouldersScaleZ = nbt.getFloat("ShouldersScaleZ");
        if (nbt.contains("ShouldersOffsetY")) shouldersOffsetY = nbt.getFloat("ShouldersOffsetY");
        if (nbt.contains("PouchScaleX")) pouchScaleX = nbt.getFloat("PouchScaleX");
        if (nbt.contains("PouchScaleY")) pouchScaleY = nbt.getFloat("PouchScaleY");
        if (nbt.contains("PouchScaleZ")) pouchScaleZ = nbt.getFloat("PouchScaleZ");
        if (nbt.contains("PouchOffsetX")) pouchOffsetX = nbt.getFloat("PouchOffsetX");
        if (nbt.contains("PouchOffsetY")) pouchOffsetY = nbt.getFloat("PouchOffsetY");
        if (nbt.contains("PouchOffsetZ")) pouchOffsetZ = nbt.getFloat("PouchOffsetZ");
        if (nbt.contains("BodyJiggleStrength")) bodyJiggleStrength = nbt.getFloat("BodyJiggleStrength");
    }

    public boolean isShowTail() { return showTail; }
    public void setShowTail(boolean s) { this.showTail = s; }
    public boolean isShowEars() { return showEars; }
    public void setShowEars(boolean s) { this.showEars = s; }
    public int getEarShape() { return earShape; }
    public void setEarShape(int s) { this.earShape = s; }
    public int getTailCount() { return tailCount; }
    public void setTailCount(int c) { this.tailCount = c; }
    public float getTailFanSpread() { return tailFanSpread; }
    public void setTailFanSpread(float f) { this.tailFanSpread = f; }
    public int getTailSegments() { return tailSegments; }
    public void setTailSegments(int s) { this.tailSegments = s; }
    public float getTailSegmentLength() { return tailSegmentLength; }
    public void setTailSegmentLength(float l) { this.tailSegmentLength = l; }
    public float getTailTaper() { return tailTaper; }
    public void setTailTaper(float t) { this.tailTaper = t; }
    public int getEarType() { return earType; }
    public void setEarType(int t) { this.earType = t; }
    public int getTailType() { return tailType; }
    public void setTailType(int t) { this.tailType = t; }
    public float getEarScaleX() { return earScaleX; }
    public void setEarScaleX(float v) { this.earScaleX = v; }
    public float getEarScaleY() { return earScaleY; }
    public void setEarScaleY(float v) { this.earScaleY = v; }
    public float getEarScaleZ() { return earScaleZ; }
    public void setEarScaleZ(float v) { this.earScaleZ = v; }
    public float getEarRotX() { return earRotX; }
    public void setEarRotX(float v) { this.earRotX = v; }
    public float getEarRotY() { return earRotY; }
    public void setEarRotY(float v) { this.earRotY = v; }
    public float getEarRotZ() { return earRotZ; }
    public void setEarRotZ(float v) { this.earRotZ = v; }
    public float getEarOffsetX() { return earOffsetX; }
    public void setEarOffsetX(float v) { this.earOffsetX = v; }
    public float getEarOffsetY() { return earOffsetY; }
    public void setEarOffsetY(float v) { this.earOffsetY = v; }
    public float getEarOffsetZ() { return earOffsetZ; }
    public void setEarOffsetZ(float v) { this.earOffsetZ = v; }
    public float getTailScaleX() { return tailScaleX; }
    public void setTailScaleX(float v) { this.tailScaleX = v; }
    public float getTailScaleY() { return tailScaleY; }
    public void setTailScaleY(float v) { this.tailScaleY = v; }
    public float getTailScaleZ() { return tailScaleZ; }
    public void setTailScaleZ(float v) { this.tailScaleZ = v; }
    public float getTailRotX() { return tailRotX; }
    public void setTailRotX(float v) { this.tailRotX = v; }
    public float getTailRotY() { return tailRotY; }
    public void setTailRotY(float v) { this.tailRotY = v; }
    public float getTailRotZ() { return tailRotZ; }
    public void setTailRotZ(float v) { this.tailRotZ = v; }
    public float getTailOffsetX() { return tailOffsetX; }
    public void setTailOffsetX(float v) { this.tailOffsetX = v; }
    public float getTailOffsetY() { return tailOffsetY; }
    public void setTailOffsetY(float v) { this.tailOffsetY = v; }
    public float getTailOffsetZ() { return tailOffsetZ; }
    public void setTailOffsetZ(float v) { this.tailOffsetZ = v; }
    public int getEarColorPrimary() { return earColorPrimary; }
    public void setEarColorPrimary(int c) { this.earColorPrimary = c; }
    public int getEarColorSecondary() { return earColorSecondary; }
    public void setEarColorSecondary(int c) { this.earColorSecondary = c; }
    public int getTailColorPrimary() { return tailColorPrimary; }
    public void setTailColorPrimary(int c) { this.tailColorPrimary = c; }
    public int getTailColorSecondary() { return tailColorSecondary; }
    public void setTailColorSecondary(int c) { this.tailColorSecondary = c; }
    public boolean isAnimate() { return animate; }
    public void setAnimate(boolean a) { this.animate = a; }
    public int getTailWagAxis() { return tailWagAxis; }
    public void setTailWagAxis(int a) { this.tailWagAxis = a; }
    public float getTailWagAmplitude() { return tailWagAmplitude; }
    public void setTailWagAmplitude(float a) { this.tailWagAmplitude = a; }
    public float getTailWagSpeed() { return tailWagSpeed; }
    public void setTailWagSpeed(float s) { this.tailWagSpeed = s; }
    public String getCustomTextureBase64() { return customTextureBase64; }
    public void setCustomTextureBase64(String s) { this.customTextureBase64 = s; }

    public int getGender() { return gender; }
    public void setGender(int g) { this.gender = g; }
    public boolean isShowChest() { return showChest; }
    public void setShowChest(boolean c) { this.showChest = c; }
    public boolean isShowHips() { return showHips; }
    public void setShowHips(boolean h) { this.showHips = h; }
    public boolean isShowShoulders() { return showShoulders; }
    public void setShowShoulders(boolean s) { this.showShoulders = s; }
    public boolean isShowPouch() { return showPouch; }
    public void setShowPouch(boolean p) { this.showPouch = p; }
    public float getChestScaleX() { return chestScaleX; }
    public void setChestScaleX(float v) { this.chestScaleX = v; }
    public float getChestScaleY() { return chestScaleY; }
    public void setChestScaleY(float v) { this.chestScaleY = v; }
    public float getChestScaleZ() { return chestScaleZ; }
    public void setChestScaleZ(float v) { this.chestScaleZ = v; }
    public float getChestOffsetY() { return chestOffsetY; }
    public void setChestOffsetY(float v) { this.chestOffsetY = v; }
    public float getChestOffsetZ() { return chestOffsetZ; }
    public void setChestOffsetZ(float v) { this.chestOffsetZ = v; }
    public float getHipsScaleX() { return hipsScaleX; }
    public void setHipsScaleX(float v) { this.hipsScaleX = v; }
    public float getHipsScaleY() { return hipsScaleY; }
    public void setHipsScaleY(float v) { this.hipsScaleY = v; }
    public float getHipsScaleZ() { return hipsScaleZ; }
    public void setHipsScaleZ(float v) { this.hipsScaleZ = v; }
    public float getHipsOffsetY() { return hipsOffsetY; }
    public void setHipsOffsetY(float v) { this.hipsOffsetY = v; }
    public float getHipsOffsetZ() { return hipsOffsetZ; }
    public void setHipsOffsetZ(float v) { this.hipsOffsetZ = v; }
    public float getShouldersScaleX() { return shouldersScaleX; }
    public void setShouldersScaleX(float v) { this.shouldersScaleX = v; }
    public float getShouldersScaleY() { return shouldersScaleY; }
    public void setShouldersScaleY(float v) { this.shouldersScaleY = v; }
    public float getShouldersScaleZ() { return shouldersScaleZ; }
    public void setShouldersScaleZ(float v) { this.shouldersScaleZ = v; }
    public float getShouldersOffsetY() { return shouldersOffsetY; }
    public void setShouldersOffsetY(float v) { this.shouldersOffsetY = v; }
    public float getPouchScaleX() { return pouchScaleX; }
    public void setPouchScaleX(float v) { this.pouchScaleX = v; }
    public float getPouchScaleY() { return pouchScaleY; }
    public void setPouchScaleY(float v) { this.pouchScaleY = v; }
    public float getPouchScaleZ() { return pouchScaleZ; }
    public void setPouchScaleZ(float v) { this.pouchScaleZ = v; }
    public float getPouchOffsetX() { return pouchOffsetX; }
    public void setPouchOffsetX(float v) { this.pouchOffsetX = v; }
    public float getPouchOffsetY() { return pouchOffsetY; }
    public void setPouchOffsetY(float v) { this.pouchOffsetY = v; }
    public float getPouchOffsetZ() { return pouchOffsetZ; }
    public void setPouchOffsetZ(float v) { this.pouchOffsetZ = v; }
    public float getBodyJiggleStrength() { return bodyJiggleStrength; }
    public void setBodyJiggleStrength(float v) { this.bodyJiggleStrength = v; }
}
