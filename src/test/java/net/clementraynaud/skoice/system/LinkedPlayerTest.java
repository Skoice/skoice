/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.config.ConfigYamlFile;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LinkedPlayerTest {

    @Mock
    private Skoice plugin;

    @Mock
    private Player player;

    @Mock
    private VoiceChannel voiceChannel;

    @Mock
    private ConfigYamlFile configYamlFile;

    private LinkedPlayer linkedPlayer;

    @BeforeEach
    void setUp() {
        when(this.plugin.getConfigYamlFile()).thenReturn(this.configYamlFile);
        when(this.configYamlFile.getVoiceChannel()).thenReturn(this.voiceChannel);
        this.linkedPlayer = new LinkedPlayer(this.plugin, this.player, "discordId");
    }

    @Test
    void getPlayersWithinRange_filtersCorrectly() {
        LinkedPlayer anotherPlayer = mock(LinkedPlayer.class);
        when(anotherPlayer.isInMainVoiceChannel()).thenReturn(true);
        when(anotherPlayer.isStateEligible()).thenReturn(true);
        when(anotherPlayer.isCloseEnoughToPlayer(eq(this.linkedPlayer), anyBoolean())).thenReturn(true);

        LinkedPlayer.getOnlineLinkedPlayers().add(this.linkedPlayer);
        LinkedPlayer.getOnlineLinkedPlayers().add(anotherPlayer);

        Set<LinkedPlayer> playersWithinRange = this.linkedPlayer.getPlayersWithinRange();

        assertTrue(playersWithinRange.contains(anotherPlayer));
        assertFalse(playersWithinRange.contains(this.linkedPlayer));
    }
}