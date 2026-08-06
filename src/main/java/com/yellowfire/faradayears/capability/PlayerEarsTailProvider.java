package com.yellowfire.faradayears.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerEarsTailProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<PlayerEarsTailData> EARS_TAIL_DATA = CapabilityManager.get(new CapabilityToken<PlayerEarsTailData>() {});

    private PlayerEarsTailData data = null;
    private final LazyOptional<PlayerEarsTailData> optional = LazyOptional.of(this::createData);

    private PlayerEarsTailData createData() {
        if (this.data == null) {
            this.data = new PlayerEarsTailData();
        }
        return this.data;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == EARS_TAIL_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createData().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createData().loadNBTData(nbt);
    }
}
