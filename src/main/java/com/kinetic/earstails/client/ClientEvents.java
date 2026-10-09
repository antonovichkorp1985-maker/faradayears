package com.kinetic.earstails.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.kinetic.earstails.KineticEarsTailsMod;
import com.kinetic.earstails.client.gui.EarsTailCustomizationScreen;
import com.kinetic.earstails.client.render.EarsAndTailLayer;
import com.kinetic.earstails.physics.TailPhysicsEngine;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = KineticEarsTailsMod.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    public static KeyMapping OPEN_GUI_KEY;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_GUI_KEY = new KeyMapping(
                "key.faradayears.open_customizer",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.categories.faradayears"
        );
        event.register(OPEN_GUI_KEY);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new EarsAndTailLayer(playerRenderer));
            }
        }
    }

    @EventBusSubscriber(modid = KineticEarsTailsMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientGameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                // ★ 1.3.0 ОПТИМИЗАЦИЯ: раз в 5 секунд чистим данные ушедших игроков (анти-утечка)…
                boolean doPrune = mc.player != null && mc.player.tickCount % 100 == 0;
                java.util.Set<java.util.UUID> present = doPrune ? new java.util.HashSet<>() : null;
                for (AbstractClientPlayer player : mc.level.players()) {
                    // ★ 1.3.0 ОПТИМИЗАЦИЯ: не считаем физику для игроков дальше 96 блоков —
                    //   их хвосты всё равно не видны, а PBD-солвер дорогой:
                    boolean near = mc.player == null || player == mc.player
                            || player.distanceToSqr(mc.player) < 96.0D * 96.0D;
                    if (near) {
                        TailPhysicsEngine.INSTANCE.onClientTick(player);
                    }
                    if (doPrune) present.add(player.getUUID());
                }
                if (doPrune) {
                    TailPhysicsEngine.INSTANCE.pruneMissing(present);
                }
            }
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (OPEN_GUI_KEY != null && OPEN_GUI_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.screen == null) {
                    mc.setScreen(new EarsTailCustomizationScreen());
                }
            }
        }
    }
}
