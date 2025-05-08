package com.t4bzzz.betterteams_fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class TeamCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        System.out.println("[BetterTeams] Registering /team command...");

        // Für neuere Fabric-Versionen
        LiteralCommandNode<ServerCommandSource> teamCommand = dispatcher.register(
                CommandManager.literal("team")
                        .requires(source -> true) // .hasPermissionLevel(0) alternatives: .isPlayer() oder true



                        // CREATE
                        .then(CommandManager.literal("create")
                                .then(CommandManager.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            try {
                                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                                String name = StringArgumentType.getString(ctx, "name");
                                                if (TeamManager.createTeam(name, player.getUuid())) {
                                                    ctx.getSource().sendFeedback(() -> Text.literal("✅ Team \"" + name + "\" created!"), false);
                                                } else {
                                                    ctx.getSource().sendError(Text.literal("❌ Couldn't create team. Are you already in one, or does the name exist?"));
                                                }
                                            } catch (CommandSyntaxException e) {
                                                ctx.getSource().sendError(Text.literal("❌ You must be a player to use this command."));
                                            }
                                            return 1;
                                        })
                                )
                        )

                        // Rest der Befehle wie vorher...
                        // INVITE
                        .then(CommandManager.literal("invite")
                                .then(CommandManager.argument("target", StringArgumentType.word())
                                        .executes(ctx -> {
                                            try {
                                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                                String targetName = StringArgumentType.getString(ctx, "target");
                                                ServerPlayerEntity target = ctx.getSource().getServer().getPlayerManager().getPlayer(targetName);

                                                if (target == null) {
                                                    ctx.getSource().sendError(Text.literal("❌ Player not found."));
                                                    return 0;
                                                }

                                                Team playerTeam = TeamManager.getTeam(player.getUuid());
                                                if (playerTeam == null) {
                                                    ctx.getSource().sendError(Text.literal("❌ You're not in a team."));
                                                    return 0;
                                                }

                                                boolean success = TeamManager.invitePlayer(player.getUuid(), playerTeam.name, target.getUuid());
                                                if (success) {
                                                    ctx.getSource().sendFeedback(() -> Text.literal("📨 Invitation sent to " + target.getName().getString()), false);
                                                    target.sendMessage(Text.literal("📨 You've been invited to team " + playerTeam.name + ". Use /team accept or /team deny."), false);
                                                } else {
                                                    ctx.getSource().sendError(Text.literal("❌ Invitation failed. Make sure you're the team owner."));
                                                }
                                            } catch (CommandSyntaxException e) {
                                                ctx.getSource().sendError(Text.literal("❌ You must be a player to use this command."));
                                            }
                                            return 1;
                                        })
                                )
                        )

                        // ACCEPT
                        .then(CommandManager.literal("accept")
                                .executes(ctx -> {
                                    try {
                                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                        if (TeamManager.acceptInvite(player.getUuid())) {
                                            ctx.getSource().sendFeedback(() -> Text.literal("✅ You joined the team!"), false);
                                        } else {
                                            ctx.getSource().sendError(Text.literal("❌ You don't have any pending invites."));
                                        }
                                    } catch (CommandSyntaxException e) {
                                        ctx.getSource().sendError(Text.literal("❌ You must be a player to use this command."));
                                    }
                                    return 1;
                                })
                        )

                        // DENY
                        .then(CommandManager.literal("deny")
                                .executes(ctx -> {
                                    try {
                                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                        if (TeamManager.denyInvite(player.getUuid())) {
                                            ctx.getSource().sendFeedback(() -> Text.literal("❌ You denied the team invitation."), false);
                                        } else {
                                            ctx.getSource().sendError(Text.literal("❌ You don't have any pending invites."));
                                        }
                                    } catch (CommandSyntaxException e) {
                                        ctx.getSource().sendError(Text.literal("❌ You must be a player to use this command."));
                                    }
                                    return 1;
                                })
                        )

                        // LEAVE
                        .then(CommandManager.literal("leave")
                                .executes(ctx -> {
                                    try {
                                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                        if (TeamManager.leaveTeam(player.getUuid())) {
                                            ctx.getSource().sendFeedback(() -> Text.literal("✅ You left the team."), false);
                                        } else {
                                            ctx.getSource().sendError(Text.literal("❌ You're either not a member or the team owner."));
                                        }
                                    } catch (CommandSyntaxException e) {
                                        ctx.getSource().sendError(Text.literal("❌ You must be a player to use this command."));
                                    }
                                    return 1;
                                })
                        )

                        // INFO
                        .then(CommandManager.literal("info")
                                .executes(ctx -> {
                                    try {
                                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                        Team team = TeamManager.getTeam(player.getUuid());

                                        if (team == null) {
                                            ctx.getSource().sendError(Text.literal("❌ You're not in a team."));
                                            return 0;
                                        }

                                        ServerPlayerEntity ownerPlayer = ctx.getSource().getServer().getPlayerManager().getPlayer(team.owner);
                                        String ownerName;

                                        if (ownerPlayer != null) {
                                            // Spieler ist online, Name ist verfügbar
                                            ownerName = ownerPlayer.getName().getString();
                                        } else {
                                            // Spieler ist offline oder nicht gefunden
                                            ownerName = "Unbekannt";
                                        }


                                        StringBuilder membersText = new StringBuilder();
                                        for (UUID uuid : team.members) {
                                            ServerPlayerEntity member = ctx.getSource().getServer().getPlayerManager().getPlayer(uuid);
                                            membersText.append(" - ")
                                                    .append(member != null ? member.getName().getString() : "Offline")
                                                    .append("\n");
                                        }

                                        Text msg = Text.literal("📋 Team Information\n")
                                                .append(Text.literal("🔸 Name: ").copy().append(team.name + "\n"))
                                                .append(Text.literal("👑 Owner: ").copy().append(ownerName + "\n"))
                                                .append(Text.literal("👥 Members:\n" + membersText));

                                        ctx.getSource().sendFeedback(() -> msg, false);
                                    } catch (CommandSyntaxException e) {
                                        ctx.getSource().sendError(Text.literal("❌ You must be a player to use this command."));
                                    }
                                    return 1;
                                })
                        )
        );

        // Alternative Registrierung für ältere Minecraft-Versionen
        dispatcher.register(CommandManager.literal("t")
                .requires(source -> true)
                .redirect(teamCommand));

// ⬇️ HIER: Liste aller registrierten Root-Commands ausgeben
        System.out.println("[BetterTeams] Root commands after registration:");
        dispatcher.getRoot().getChildren().forEach(node -> {
            System.out.println(" - " + node.getName());
        });

    }

    // Alternative Registrierungsmethode für die neueste Fabric-API
    public static void registerModern(CommandDispatcher<ServerCommandSource> dispatcher,
                                      CommandRegistryAccess registryAccess,
                                      CommandManager.RegistrationEnvironment environment) {

        register(dispatcher);
    }
}