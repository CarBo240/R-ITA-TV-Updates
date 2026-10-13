package com.rita.eventilive;
import org.junit.Test;import static org.junit.Assert.*;

public class EventPresentationTest {
 @Test public void englishDaysBecomeItalian(){assertEquals("Martedì",EventCatalog.italianDay("TUESDAY"));assertTrue(EventCatalog.italianTiming("TUESDAY","20:30").startsWith("Martedì · "));assertTrue(EventCatalog.italianTiming("TUESDAY","20:30").endsWith(" · ora italiana"));}
 @Test public void sportBadgesAreSpecific(){assertEquals("⚽",SportIcons.forTitle("Champions League"));assertEquals("🏀",SportIcons.forTitle("NBA"));assertEquals("🎾",SportIcons.forTitle("ATP Tennis"));assertEquals("🏎",SportIcons.forTitle("Formula 1"));}
}
