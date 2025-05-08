package com.t4bzzz.betterteams_fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Betterteams_fabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Optional: client-only logic (HUD, keybinds, etc.)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LOGGER.info("[BetterTeams] CLIENT root commands at init:");
            dispatcher.getRoot().getChildren().forEach(node -> LOGGER.info(" - {}", node.getName()));
        });

    }
}
