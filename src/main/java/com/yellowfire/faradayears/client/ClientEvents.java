package com.yellowfire.faradayears.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.yellowfire.faradayears.FaradayEarsMod;
import com.yellowfire.faradayears.client.gui.EarsTailCustomizationScreen;
import com.yellowfire.faradayears.client.render.EarsAndTailLayer;
import com.yellowfire.faradayears.physics.TailPhysicsEngine;
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

@EventBusSubscriber(modid = FaradayEarsMod.MOD_ID, value = Dist.CLIENT)
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

    @EventBusSubscriber(modid = FaradayEarsMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientGameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                for (AbstractClientPlayer player : mc.level.players()) {
                    TailPhysicsEngine.INSTANCE.onClientTick(player);
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
