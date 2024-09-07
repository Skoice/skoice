package net.clementraynaud.skoice;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class BukkitTestSetup {

    private static MockedStatic<Bukkit> mockedBukkit;

    @BeforeAll
    static void setUpAll() {
        BukkitTestSetup.mockedBukkit = mockStatic(Bukkit.class);
        when(Bukkit.isPrimaryThread()).thenReturn(false);
        PluginManager mockPluginManager = mock(PluginManager.class);
        when(Bukkit.getPluginManager()).thenReturn(mockPluginManager);
        when(mockPluginManager.isPluginEnabled("Skoice")).thenReturn(true);
    }

    @AfterAll
    static void tearDownAll() {
        BukkitTestSetup.mockedBukkit.close();
    }

}
