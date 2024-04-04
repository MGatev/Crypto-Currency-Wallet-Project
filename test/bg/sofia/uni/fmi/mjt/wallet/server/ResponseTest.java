package bg.sofia.uni.fmi.mjt.wallet.server;

import bg.sofia.uni.fmi.mjt.wallet.server.api.dto.Asset;
import bg.sofia.uni.fmi.mjt.wallet.server.user.account.Order;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseTest {

    @Test
    void testResponseOkWithEmptyCollectionOfOrders() {
        String expected = "You have these remaining assets. You have 50.0 dollars left in your wallet.";
        String actual = Response.ok(List.of(), 50).toString();

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testResponseOkWithCollectionOfOrders() {

        Order temp = new Order("BTC", 1, 10000, -1);
        Order temp1 = new Order("ETH", 2, 100, -1);
        Collection<Order> tempCol = List.of(temp, temp1);

        String expected = "You have these remaining assets: Order=[id=BTC, amount=1.0, boughtPrice=10000.0]," +
            " Order=[id=ETH, amount=2.0, boughtPrice=100.0]. You have 50.0 dollars left in your wallet.";

        String actual = Response.ok(tempCol, 50).toString();

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testResponseOkWithCollectionOfAssets() {
        Asset asset1 = new Asset("BTC", "BITCOIN", 48120.03, 1);
        Asset asset2 = new Asset("ETH", "ETHEREUM", 10000.03, 1);
        Asset asset3 = new Asset("DOGE", "DOGECOIN", 14.42, 1);

        Collection<Asset> expectedCollection = List.of(asset1, asset2, asset3);
        String expectedResponse =
            "Here are the available listings: " + asset1 + "; " + asset2 + "; " + asset3;

        String actualResponse = Response.ok(expectedCollection).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);

    }
}
