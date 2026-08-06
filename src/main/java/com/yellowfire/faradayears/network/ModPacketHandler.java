package com.yellowfire.faradayears.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("faradayears", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // Вызов messageBuilder(type, id) без третьего аргумента автоматически регистрирует двунаправленный пакет (Клиент <-> Сервер) в Forge 1.19.2!
        CHANNEL.messageBuilder(SyncEarsTailPacket.class, packetId++)
                .decoder(SyncEarsTailPacket::new)
                .encoder(SyncEarsTailPacket::toBytes)
                .consumerMainThread(SyncEarsTailPacket::handle)
                .add();
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToAllTracking(Object msg, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), msg);
    }
}
