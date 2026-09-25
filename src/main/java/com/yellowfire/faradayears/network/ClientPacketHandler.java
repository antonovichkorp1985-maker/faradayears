package com.yellowfire.faradayears.network;

import com.yellowfire.faradayears.ModAttachments;
import com.yellowfire.faradayears.capability.PlayerEarsTailData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Клиентская часть обработки сети (грузится только на физическом клиенте).
 */
public final class ClientPacketHandler {

    private ClientPacketHandler() {
    }

    public static void handleSync(SyncEarsTailPacket packet) {
        if (Minecraft.getInstance().level != null && packet.getPlayerId() != null) {
            Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayerId());
            if (player != null) {
                PlayerEarsTailData data = ModAttachments.get(player);
                data.loadNBTData(packet.getDataTag());
            }
        }
    }
}
