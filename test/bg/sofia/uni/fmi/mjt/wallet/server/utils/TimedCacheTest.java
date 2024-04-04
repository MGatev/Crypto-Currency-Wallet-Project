package bg.sofia.uni.fmi.mjt.wallet.server.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TimedCacheTest {

    @Test
    void testGetWithExpiredValue() {
        Cache<String, String> timedCache = new TimedCache<>(-1);
        timedCache.put("test", "value");

        assertNull(timedCache.get("test"), "Expected null when the time has expired!");
    }

    @Test
    void testGetWithNotExpiredValue() {
        Cache<String, String> timedCache = new TimedCache<>(1);
        timedCache.put("test", "value");

        String expected = "value";
        String actual = timedCache.get("test");

        assertEquals(expected, actual, "Expected value from the cache is: " + expected +
            ", but it was: " + actual);
    }
}
