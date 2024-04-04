package bg.sofia.uni.fmi.mjt.wallet.server.user.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {

    @Test
    void testToString() {
        Order asset = new Order("BTC", 0.15, 123.424, 1);
        String expected ="Order=[id=BTC, amount=0.15, boughtPrice=123.424]";
        String actual = asset.toString();

        assertEquals(expected, actual, "Expected transformation to string is: " + expected
            + ", but was: " + actual);
    }
}
