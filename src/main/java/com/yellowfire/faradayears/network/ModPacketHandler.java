package com.yellowfire.faradayears.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge 1.21.1: сеть на CustomPacketPayload + StreamCodec
 * (замена старого Forge SimpleChannel).
 */
public class ModPacketHandler {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(SyncEarsTailPacket.TYPE, SyncEarsTailPacket.STREAM_CODEC, SyncEarsTailPacket::handle);
    }

    public static void sendToServer(SyncEarsTailPacket msg) {
        PacketDistributor.sendToServer(msg);
    }

    public static void sendToAllTracking(SyncEarsTailPacket msg, ServerPlayer player) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, msg);
    }
}
