package bg.sofia.uni.fmi.mjt.wallet.server.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssetTest {

    @Test
    void testToString() {
        Asset asset = new Asset("BTC", "Bitcoin", 123.424, 1);
        String expected = "Asset ID: BTC, Asset name: Bitcoin, Asset current price: 123.424";
        String actual = asset.toString();

        assertEquals(expected, actual, "Expected transformation to string is: " + expected
            + ", but was: " + actual);
    }
}
