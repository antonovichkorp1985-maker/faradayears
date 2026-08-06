package com.yellowfire.faradayears.client;

import com.yellowfire.faradayears.FaradayEarsMod;
import com.yellowfire.faradayears.client.gui.EarsTailCustomizationScreen;
import com.yellowfire.faradayears.client.render.EarsAndTailLayer;
import com.yellowfire.faradayears.physics.TailPhysicsEngine;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = FaradayEarsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
        for (String skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new EarsAndTailLayer(playerRenderer));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = FaradayEarsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    for (AbstractClientPlayer player : mc.level.players()) {
                        TailPhysicsEngine.INSTANCE.onClientTick(player);
                    }
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
