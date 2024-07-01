package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.Skoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkTest {

    @Mock
    private Skoice plugin;

    private Network network;

    @BeforeEach
    void setUp() {
        this.network = new Network(this.plugin, "channelId");
    }

    @Test
    void canPlayerConnect_checksPlayerEligibility() {
        LinkedPlayer player = mock(LinkedPlayer.class);
        LinkedPlayer anotherPlayer = mock(LinkedPlayer.class);

        when(player.isStateEligible()).thenReturn(true);
        when(anotherPlayer.isStateEligible()).thenReturn(true);
        when(anotherPlayer.isCloseEnoughToPlayer(eq(player), eq(false))).thenReturn(true);

        this.network.add(anotherPlayer);

        assertTrue(this.network.canPlayerConnect(player));

        when(anotherPlayer.isCloseEnoughToPlayer(eq(player), eq(false))).thenReturn(false);
        assertFalse(this.network.canPlayerConnect(player));
    }

    @Test
    void canPlayerStayConnected_checksPlayerEligibility() {
        LinkedPlayer player = mock(LinkedPlayer.class);
        LinkedPlayer anotherPlayer = mock(LinkedPlayer.class);

        when(player.isStateEligible()).thenReturn(true);
        when(anotherPlayer.isStateEligible()).thenReturn(true);
        when(anotherPlayer.isCloseEnoughToPlayer(eq(player), eq(true))).thenReturn(true);

        this.network.add(anotherPlayer);

        assertTrue(this.network.canPlayerStayConnected(player));

        when(anotherPlayer.isCloseEnoughToPlayer(eq(player), eq(true))).thenReturn(false);
        assertFalse(this.network.canPlayerStayConnected(player));
    }
}