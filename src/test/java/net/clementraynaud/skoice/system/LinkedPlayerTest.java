package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.storage.config.ConfigYamlFile;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.bukkit.GameMode;
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
    void isStateEligible_checksPlayerState() {
        when(this.configYamlFile.getBoolean(ConfigField.PLAYERS_ON_DEATH_SCREEN_INCLUDED.toString())).thenReturn(true);
        when(this.configYamlFile.getBoolean(ConfigField.SPECTATORS_INCLUDED.toString())).thenReturn(true);
        when(this.player.isDead()).thenReturn(false);
        when(this.player.getGameMode()).thenReturn(GameMode.SURVIVAL);

        assertTrue(this.linkedPlayer.isStateEligible());

        when(this.player.isDead()).thenReturn(true);
        when(this.player.getGameMode()).thenReturn(GameMode.SPECTATOR);

        assertTrue(this.linkedPlayer.isStateEligible());

        when(this.configYamlFile.getBoolean(ConfigField.PLAYERS_ON_DEATH_SCREEN_INCLUDED.toString())).thenReturn(false);
        assertFalse(this.linkedPlayer.isStateEligible());
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