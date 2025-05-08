package com.t4bzzz.betterteams_fabric;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;


public class TeamManager {
    private static final Logger LOGGER = Logger.getLogger("BetterTeams");
    private static final Gson gson = new Gson();
    private static final Map<UUID, String> playerTeamMap = new HashMap<>();
    private static final Map<String, Team> teams = new HashMap<>();

    private static File getFile(MinecraftServer server) {
        // Holen des Server-Laufverzeichnisses als Path
        File configDir = new File(server.getRunDirectory().toFile(), "betterteams");

        // Erstelle das Verzeichnis, falls es nicht existiert
        if (!configDir.exists() && !configDir.mkdirs()) {
            LOGGER.severe("[BetterTeams] Failed to create config directory: " + configDir.getAbsolutePath());
        }
        return new File(configDir, "teams.json");
    }




    public static void load(MinecraftServer server) {
        try {
            File file = getFile(server);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    LOGGER.severe("[BetterTeams] Failed to create team data file: " + file.getAbsolutePath());
                    return;
                }
                save(server);
                LOGGER.info("[BetterTeams] New team data file created.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                Type type = new TypeToken<Map<String, Team>>() {}.getType();
                Map<String, Team> loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    teams.clear();
                    teams.putAll(loaded);
                    playerTeamMap.clear();
                    for (Map.Entry<String, Team> entry : teams.entrySet()) {
                        for (UUID member : entry.getValue().members) {
                            playerTeamMap.put(member, entry.getKey());
                        }
                    }
                    LOGGER.info("[BetterTeams] Loaded " + teams.size() + " teams with " + playerTeamMap.size() + " members.");
                }
            } catch (IOException e) {
                LOGGER.severe("[BetterTeams] Error reading team data: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        } catch (Exception e) {
            LOGGER.severe("[BetterTeams] Error loading team data: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    public static void save(MinecraftServer server) {
        try {
            File file = getFile(server);
            file.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                gson.toJson(teams, writer);
                LOGGER.info("[BetterTeams] Teams data saved successfully.");
            } catch (IOException e) {
                LOGGER.severe("[BetterTeams] Error saving team data: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        } catch (Exception e) {
            LOGGER.severe("[BetterTeams] Error saving team data: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    public static Team getTeam(UUID player) {
        String teamName = playerTeamMap.get(player);
        return teamName == null ? null : teams.get(teamName);
    }

    public static boolean isInTeam(UUID player) {
        return playerTeamMap.containsKey(player);
    }

    public static boolean teamExists(String name) {
        return teams.containsKey(name.toLowerCase());
    }

    public static boolean createTeam(String name, UUID owner) {
        // Debug-Output
        System.out.println("[BetterTeams] Attempting to create team: " + name + " for owner: " + owner);

        if (isInTeam(owner)) {
            System.out.println("[BetterTeams] Failed: Player already in a team");
            return false;
        }
        if (teamExists(name)) {
            System.out.println("[BetterTeams] Failed: Team name already exists");
            return false;
        }

        Team team = new Team(name, owner);
        teams.put(name.toLowerCase(), team);
        playerTeamMap.put(owner, name.toLowerCase());

        System.out.println("[BetterTeams] Team created successfully: " + name);
        return true;
    }

    public static boolean invitePlayer(UUID owner, String teamName, UUID target) {
        // Debug-Output
        System.out.println("[BetterTeams] Attempting to invite player to team: " + teamName);

        Team team = teams.get(teamName.toLowerCase());
        if (team == null) {
            System.out.println("[BetterTeams] Failed: Team not found");
            return false;
        }
        if (!team.isOwner(owner)) {
            System.out.println("[BetterTeams] Failed: Player is not the team owner");
            return false;
        }
        if (team.isMember(target)) {
            System.out.println("[BetterTeams] Failed: Player is already a team member");
            return false;
        }

        boolean result = team.invites.add(target);
        System.out.println("[BetterTeams] Invite result: " + result);
        return result;
    }

    public static boolean acceptInvite(UUID player) {
        // Debug-Output
        System.out.println("[BetterTeams] Player attempting to accept invite: " + player);

        for (Team team : teams.values()) {
            if (team.isInvited(player)) {
                team.invites.remove(player);
                team.members.add(player);
                playerTeamMap.put(player, team.name.toLowerCase());
                System.out.println("[BetterTeams] Player joined team: " + team.name);
                return true;
            }
        }

        System.out.println("[BetterTeams] Failed: No pending invites found");
        return false;
    }

    public static boolean denyInvite(UUID player) {
        // Debug-Output
        System.out.println("[BetterTeams] Player attempting to deny invite: " + player);

        for (Team team : teams.values()) {
            if (team.invites.remove(player)) {
                System.out.println("[BetterTeams] Invite denied for team: " + team.name);
                return true;
            }
        }

        System.out.println("[BetterTeams] Failed: No pending invites found");
        return false;
    }

    public static boolean leaveTeam(UUID player) {
        // Debug-Output
        System.out.println("[BetterTeams] Player attempting to leave team: " + player);

        Team team = getTeam(player);
        if (team == null) {
            System.out.println("[BetterTeams] Failed: Player not in a team");
            return false;
        }
        if (team.isOwner(player)) {
            System.out.println("[BetterTeams] Failed: Player is the team owner");
            return false;
        }

        team.members.remove(player);
        playerTeamMap.remove(player);
        System.out.println("[BetterTeams] Player left team successfully");
        return true;
    }

    public static boolean resetTeam(UUID player) {
        // Debug-Output
        System.out.println("[BetterTeams] Attempting to reset team for player: " + player);

        Team team = getTeam(player);
        if (team == null) {
            System.out.println("[BetterTeams] Failed: Player not in a team");
            return false;
        }
        if (!team.isOwner(player)) {
            System.out.println("[BetterTeams] Failed: Player is not the team owner");
            return false;
        }

        for (UUID member : new HashSet<>(team.members)) {
            playerTeamMap.remove(member);
        }
        teams.remove(team.name.toLowerCase());
        System.out.println("[BetterTeams] Team reset successfully: " + team.name);
        return true;
    }

    public static Collection<Team> getAllTeams() {
        return teams.values();
    }
}