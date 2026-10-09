package com.kinetic.earstails.network;

import com.kinetic.earstails.ModAttachments;
import com.kinetic.earstails.capability.PlayerEarsTailData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Пакет синхронизации настроек ушек/хвоста (Клиент <-> Сервер).
 * NeoForge 1.21.1: реализует CustomPacketPayload.
 */
public class SyncEarsTailPacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncEarsTailPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("faradayears", "main"));

    public static final StreamCodec<FriendlyByteBuf, SyncEarsTailPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUUID(pkt.playerId);
                buf.writeNbt(pkt.dataTag);
            },
            buf -> new SyncEarsTailPacket(buf.readUUID(), buf.readNbt()));

    private final UUID playerId;
    private final CompoundTag dataTag;

    public SyncEarsTailPacket(UUID playerId, CompoundTag dataTag) {
        this.playerId = playerId;
        this.dataTag = dataTag != null ? dataTag : new CompoundTag();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public CompoundTag getDataTag() {
        return dataTag;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Единый обработчик обеих сторон: на сервере принимает изменения от владельца
     * персонажа и рассылает их всем, на клиенте применяет данные к модели игрока.
     * Клиентская часть вынесена в {@link ClientPacketHandler}, чтобы класс
     * net.minecraft.client.* не грузился на выделенном сервере.
     */
    public static void handle(SyncEarsTailPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                if (sender.getUUID().equals(packet.playerId)) {
                    PlayerEarsTailData data = ModAttachments.get(sender);
                    data.loadNBTData(packet.dataTag);
                    ModPacketHandler.sendToAllTracking(new SyncEarsTailPacket(sender.getUUID(), packet.dataTag), sender);
                }
            } else {
                ClientPacketHandler.handleSync(packet);
            }
        });
    }
}
