/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 *
 * This file is part of Skoice.
 *
 * Skoice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skoice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skoice.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.clementraynaud.skoice.velocity.network;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;
import com.velocitypowered.api.proxy.ProxyServer;
import net.clementraynaud.skoice.common.model.minecraft.PlayerInfo;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceGameMode;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceLocation;
import net.clementraynaud.skoice.velocity.SkoiceVelocity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkoicePacketListener extends PacketListenerAbstract {

    private final SkoiceVelocity skoice;
    private final ProxyServer proxy;
    private final Map<UUID, PlayerState> states = new ConcurrentHashMap<>();

    public SkoicePacketListener(SkoiceVelocity skoice, ProxyServer proxy) {
        super(PacketListenerPriority.NORMAL);
        this.skoice = skoice;
        this.proxy = proxy;
    }

    public void removePlayer(UUID uuid) {
        this.states.remove(uuid);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getUser() == null || event.getUser().getUUID() == null) {
            return;
        }
        UUID uuid = event.getUser().getUUID();
        PlayerState state = this.states.computeIfAbsent(uuid, k -> new PlayerState());

        if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
            WrapperPlayServerJoinGame join = new WrapperPlayServerJoinGame(event);
            state.gameMode = join.getGameMode().getId();
            state.worldName = join.getWorldName();
            state.teamName = null;
            state.dead = false;
            state.x = 0;
            state.y = 0;
            state.z = 0;
            state.serverName = this.getServerName(uuid);
            this.updatePlayerInfo(uuid, state);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            WrapperPlayServerRespawn respawn = new WrapperPlayServerRespawn(event);
            state.gameMode = respawn.getGameMode().getId();
            state.worldName = respawn.getWorldName().orElse(state.worldName);
            state.serverName = this.getServerName(uuid);
            this.updatePlayerInfo(uuid, state);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.UPDATE_HEALTH) {
            boolean dead = new WrapperPlayServerUpdateHealth(event).getHealth() <= 0f;
            if (dead != state.dead) {
                state.dead = dead;
                this.updatePlayerInfo(uuid, state);
            }
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.CHANGE_GAME_STATE) {
            WrapperPlayServerChangeGameState cgs = new WrapperPlayServerChangeGameState(event);
            if (cgs.getReason() == WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE) {
                int newGm = (int) cgs.getValue();
                if (newGm != state.gameMode) {
                    state.gameMode = newGm;
                    this.updatePlayerInfo(uuid, state);
                }
            }
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.TEAMS) {
            WrapperPlayServerTeams teams = new WrapperPlayServerTeams(event);
            String teamName = teams.getTeamName();
            WrapperPlayServerTeams.TeamMode mode = teams.getTeamMode();
            String playerName = event.getUser().getName();
            boolean changed = false;

            if (mode == WrapperPlayServerTeams.TeamMode.CREATE
                    || mode == WrapperPlayServerTeams.TeamMode.ADD_ENTITIES) {
                if (teams.getPlayers().contains(playerName)) {
                    state.teamName = teamName;
                    changed = true;
                }
            }

            if (mode == WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES) {
                if (teams.getPlayers().contains(playerName)) {
                    state.teamName = null;
                    changed = true;
                }
            }

            if (mode == WrapperPlayServerTeams.TeamMode.REMOVE && teamName.equals(state.teamName)) {
                state.teamName = null;
                changed = true;
            }

            if (changed) {
                this.updatePlayerInfo(uuid, state);
            }
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getUser() == null || event.getUser().getUUID() == null) {
            return;
        }
        UUID uuid = event.getUser().getUUID();
        PlayerState state = this.states.get(uuid);
        if (state == null) {
            return;
        }

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
            if (flying.hasPositionChanged()) {
                double newX = flying.getLocation().getX();
                double newY = flying.getLocation().getY();
                double newZ = flying.getLocation().getZ();
                if (Math.round(newX * 100) != Math.round(state.x * 100)
                        || Math.round(newY * 100) != Math.round(state.y * 100)
                        || Math.round(newZ * 100) != Math.round(state.z * 100)) {
                    state.x = newX;
                    state.y = newY;
                    state.z = newZ;
                    this.updatePlayerInfo(uuid, state);
                }
            }
        }
    }

    private void updatePlayerInfo(UUID uuid, PlayerState state) {
        PlayerInfo info = new PlayerInfo(
                uuid,
                state.dead,
                this.mapGameMode(state.gameMode),
                state.worldName + ":" + state.serverName,
                new SkoiceLocation(state.x, state.y, state.z),
                state.teamName
        );
        this.skoice.setPlayerInfo(info);
    }

    private SkoiceGameMode mapGameMode(int id) {
        return switch (id) {
            case 1 -> SkoiceGameMode.CREATIVE;
            case 2 -> SkoiceGameMode.ADVENTURE;
            case 3 -> SkoiceGameMode.SPECTATOR;
            default -> SkoiceGameMode.SURVIVAL;
        };
    }

    private String getServerName(UUID uuid) {
        return this.proxy.getPlayer(uuid)
                .flatMap(p -> p.getCurrentServer())
                .map(sc -> sc.getServerInfo().getName())
                .orElse("unknown");
    }

    private static final class PlayerState {

        boolean dead = false;
        int gameMode = 0;
        double x = 0, y = 0, z = 0;
        String teamName = null;
        String worldName = "unknown";
        String serverName = "unknown";
    }
}
