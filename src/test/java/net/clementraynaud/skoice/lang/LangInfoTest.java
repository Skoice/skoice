package net.clementraynaud.skoice.lang;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LangInfoTest {

    @Test
    public void testGetList() {
        Set<String> langList = LangInfo.getList();
        assertNotNull(langList);
        assertTrue(langList.contains("en"));
        assertTrue(langList.contains("fr"));
        assertTrue(langList.contains("de"));
    }

    @Test
    public void testGetJoinedList() {
        String joinedList = LangInfo.getJoinedList();
        assertNotNull(joinedList);
        assertTrue(joinedList.startsWith("<"));
        assertTrue(joinedList.endsWith(">"));
        assertTrue(joinedList.contains("en"));
        assertTrue(joinedList.contains("fr"));
        assertTrue(joinedList.contains("de"));
    }
}