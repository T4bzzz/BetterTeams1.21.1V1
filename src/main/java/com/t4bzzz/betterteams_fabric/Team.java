package com.t4bzzz.betterteams_fabric;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {
    public String name;
    public UUID owner;
    public Set<UUID> members;
    public Set<UUID> invites;

    public Team(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members = new HashSet<>();
        this.invites = new HashSet<>();
        this.members.add(owner);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public boolean isInvited(UUID uuid) {
        return invites.contains(uuid);
    }
}
