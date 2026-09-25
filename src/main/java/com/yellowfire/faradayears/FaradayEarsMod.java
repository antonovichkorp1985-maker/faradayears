package com.yellowfire.faradayears;

import com.yellowfire.faradayears.capability.PlayerEarsTailData;
import com.yellowfire.faradayears.network.ModPacketHandler;
import com.yellowfire.faradayears.network.SyncEarsTailPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(FaradayEarsMod.MOD_ID)
public class FaradayEarsMod {
    public static final String MOD_ID = "faradayears";

    public FaradayEarsMod(IEventBus modEventBus) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(ModPacketHandler::register);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerEarsTailData data = ModAttachments.get(serverPlayer);
            CompoundTag tag = data.saveNBTData();
            ModPacketHandler.sendToAllTracking(new SyncEarsTailPacket(serverPlayer.getUUID(), tag), serverPlayer);
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player targetPlayer && event.getEntity() instanceof ServerPlayer observer) {
            PlayerEarsTailData data = ModAttachments.get(targetPlayer);
            CompoundTag tag = data.saveNBTData();
            ModPacketHandler.sendToAllTracking(new SyncEarsTailPacket(targetPlayer.getUUID(), tag), observer);
        }
    }
}
