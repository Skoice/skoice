package net.clementraynaud.skoice.commands.skoice.arguments;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArgumentInfoTest {

    @Test
    public void testGetJoinedConsoleAllowedList() {
        String joinedConsoleAllowedList = ArgumentInfo.getJoinedConsoleAllowedList();
        assertNotNull(joinedConsoleAllowedList);
        assertTrue(joinedConsoleAllowedList.startsWith("<"));
        assertTrue(joinedConsoleAllowedList.endsWith(">"));
        assertTrue(joinedConsoleAllowedList.contains("configure"));
        assertTrue(joinedConsoleAllowedList.contains("tooltips"));
    }

    @Test
    public void testGet() {
        assertEquals(ArgumentInfo.CONFIGURE, ArgumentInfo.get("configure"));
        assertEquals(ArgumentInfo.TOKEN, ArgumentInfo.get("token"));
        assertNull(ArgumentInfo.get("nonexistent"));
    }

    @Test
    public void testGetList() {
        Set<String> listWithPermission = ArgumentInfo.getList(true);
        assertNotNull(listWithPermission);
        assertTrue(listWithPermission.contains("configure"));
        assertTrue(listWithPermission.contains("tooltips"));
        assertTrue(listWithPermission.contains("token"));

        Set<String> listWithoutPermission = ArgumentInfo.getList(false);
        assertNotNull(listWithoutPermission);
        assertFalse(listWithoutPermission.contains("token"));
        assertTrue(listWithoutPermission.contains("configure"));
    }
}