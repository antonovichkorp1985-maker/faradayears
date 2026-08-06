package com.yellowfire.faradayears;

import com.yellowfire.faradayears.capability.PlayerEarsTailData;
import com.yellowfire.faradayears.capability.PlayerEarsTailProvider;
import com.yellowfire.faradayears.network.ModPacketHandler;
import com.yellowfire.faradayears.network.SyncEarsTailPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("faradayears")
public class FaradayEarsMod {
    public static final String MOD_ID = "faradayears";

    public FaradayEarsMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModPacketHandler::register);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerEarsTailData.class);
    }

    @SubscribeEvent
    public void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerEarsTailProvider.EARS_TAIL_DATA).isPresent()) {
                event.addCapability(new ResourceLocation(MOD_ID, "properties"), new PlayerEarsTailProvider());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerEarsTailProvider.EARS_TAIL_DATA).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerEarsTailProvider.EARS_TAIL_DATA).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerEarsTailProvider.EARS_TAIL_DATA).ifPresent(data -> {
                CompoundTag tag = new CompoundTag();
                data.saveNBTData(tag);
                ModPacketHandler.sendToAllTracking(new SyncEarsTailPacket(serverPlayer.getUUID(), tag), serverPlayer);
            });
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player targetPlayer && event.getEntity() instanceof ServerPlayer observer) {
            targetPlayer.getCapability(PlayerEarsTailProvider.EARS_TAIL_DATA).ifPresent(data -> {
                CompoundTag tag = new CompoundTag();
                data.saveNBTData(tag);
                ModPacketHandler.sendToAllTracking(new SyncEarsTailPacket(targetPlayer.getUUID(), tag), observer);
            });
        }
    }
}
