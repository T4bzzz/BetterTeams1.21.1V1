package com.t4bzzz.betterteams_fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class Betterteams_fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("BetterTeams Mod initializing...");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TeamCommand.register(dispatcher);
        });



        // Load team data on server start
        ServerLifecycleEvents.SERVER_STARTING.register(TeamManager::load);

        // Save team data on server stop
        ServerLifecycleEvents.SERVER_STOPPING.register(TeamManager::save);
    }
}