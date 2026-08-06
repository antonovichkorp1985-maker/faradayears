package com.yellowfire.faradayears.network;

import com.yellowfire.faradayears.capability.PlayerEarsTailProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncEarsTailPacket {
    private final UUID playerId;
    private final CompoundTag dataTag;

    public SyncEarsTailPacket(UUID playerId, CompoundTag dataTag) {
        this.playerId = playerId;
        this.dataTag = dataTag != null ? dataTag : new CompoundTag();
    }

    public SyncEarsTailPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.dataTag = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeNbt(dataTag);
    }

    public static void handle(SyncEarsTailPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayer sender = context.getSender();
                if (sender != null && sender.getUUID().equals(packet.playerId)) {
                    sender.getCapability(PlayerEarsTailProvider.EARS_TAIL_DATA).ifPresent(data -> {
                        data.loadNBTData(packet.dataTag);
                        ModPacketHandler.sendToAllTracking(new SyncEarsTailPacket(sender.getUUID(), packet.dataTag), sender);
                    });
                }
            } else {
                if (Minecraft.getInstance().level != null && packet.playerId != null) {
                    Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.playerId);
                    if (player != null) {
                        player.getCapability(PlayerEarsTailProvider.EARS_TAIL_DATA).ifPresent(data -> {
                            data.loadNBTData(packet.dataTag);
                        });
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
