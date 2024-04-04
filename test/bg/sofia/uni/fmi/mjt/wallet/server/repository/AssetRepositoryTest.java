package bg.sofia.uni.fmi.mjt.wallet.server.repository;

import bg.sofia.uni.fmi.mjt.wallet.server.Response;
import bg.sofia.uni.fmi.mjt.wallet.server.api.AssetHttpClient;
import bg.sofia.uni.fmi.mjt.wallet.server.api.dto.Asset;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.InvalidApiRequestException;
import bg.sofia.uni.fmi.mjt.wallet.server.register.UserRegistration;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;
import bg.sofia.uni.fmi.mjt.wallet.server.user.account.Order;
import bg.sofia.uni.fmi.mjt.wallet.server.user.account.Wallet;
import bg.sofia.uni.fmi.mjt.wallet.server.utils.TimedCache;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssetRepositoryTest {

    AssetHttpClient assetHttpClient = mock();
    UserRegistration users = mock();
    AssetRepository assetRepository = new AssetRepository(assetHttpClient, users);
    User loggedUser = new User("marti");

    @Test
    void testDepositWithNegativeAmount() {
        Response expected = Response.decline("You cannot add negative amount of money.");
        Response actual = assetRepository.deposit(-5, loggedUser);

        assertEquals(expected, actual,
            "Expected response is: " + expected.additionalInfo() + ", but was: " + actual.additionalInfo());
    }

    @Test
    void testDepositWithValidAmount() {
        Response expected = Response.ok("You have successfully added 5.0$ to your wallet.");
        Response actual = assetRepository.deposit(5, loggedUser);

        assertEquals(expected, actual,
            "Expected response is: " + expected.additionalInfo() + ", but was: " + actual.additionalInfo());
    }

    @Test
    void testListOfferingsThrowsInvalidApiRequestException() throws InvalidApiRequestException {
        when(assetHttpClient.getAssets(any(URI.class))).thenThrow(InvalidApiRequestException.class);

        String expectedResponse =
            "Error communicating with the server. Contact an admin by providing the logs from the" +
                " exception_log.txt file";

        String actualResponse = assetRepository.listOfferings(loggedUser).toString();
        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testListOfferings() throws InvalidApiRequestException {
        Asset asset1 = new Asset("BTC", "BITCOIN", 48120.03, 1);
        Asset asset2 = new Asset("ETH", "ETHEREUM", 10000.03, 1);
        Asset asset3 = new Asset("DOGE", "DOGECOIN", 14.42, 1);

        Collection<Asset> expectedCollection = List.of(asset1, asset2, asset3);

        when(assetHttpClient.getAssets(any(URI.class))).thenReturn(expectedCollection);

        String expectedResponse =
            "Here are the available listings: " + asset1 + "; " + asset2 + "; " + asset3;

        String actualResponse = assetRepository.listOfferings(loggedUser).toString();
        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testBuyAssetWithNullAssetId() {
        String expectedResponse = "Provide valid name of the asset!";

        String actualResponse = assetRepository.buyAsset(null, 3.2, loggedUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testBuyAssetWithNegativeAmount() {
        String expectedResponse = "Amount cannot be negative!";

        String actualResponse = assetRepository.buyAsset("btc", -10, loggedUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testBuyAssetWithInvalidApiRequestException() throws InvalidApiRequestException {
        String expectedResponse =
            "Error communicating with the server. Contact an admin by providing the logs from the" +
                " exception_log.txt file";

        when(assetHttpClient.getAssets(any(URI.class))).thenThrow(InvalidApiRequestException.class);

        String actualResponse = assetRepository.buyAsset("BTC", 1, loggedUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testBuyAssetWithNotEnoughBalance() throws InvalidApiRequestException {
        String expectedResponse = "You do not have enough balance.";
        Asset asset1 = new Asset("BTC", "Bitcoin", 10000, 1);
        Collection<Asset> expectedCol = List.of(asset1);

        when(assetHttpClient.getAssets(any(URI.class))).thenReturn(expectedCol);

        User tempUser = new User("petar");
        assetRepository.deposit(10, tempUser);

        String actualResponse = assetRepository.buyAsset("BTC", 1, tempUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testBuyAssetSuccessful() throws InvalidApiRequestException {
        String expectedResponse = "You have successfully bought 1.0 of BTC for 10000.0.";
        Asset asset1 = new Asset("BTC", "Bitcoin", 10000, 1);
        Collection<Asset> expectedCol = List.of(asset1);

        when(assetHttpClient.getAssets(any(URI.class))).thenReturn(expectedCol);

        User tempUser = new User("petar");
        assetRepository.deposit(10005, tempUser);

        String actualResponse = assetRepository.buyAsset("BTC", 1, tempUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testSellAssetWithNullAssetId() {
        String expectedResponse = "Provide valid name of the asset!";

        String actualResponse = assetRepository.sellAsset(null, loggedUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testSellAssetWithUserWithNothingToSell() {
        String expectedResponse = "You cannot sell asset which you do not have.";

        User tempUser = mock();
        when(tempUser.getCurrentOrders()).thenReturn(new HashMap<>());

        String actualResponse = assetRepository.sellAsset("BTC", tempUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testSellAssetWithNotSuchAsset() {
        String expectedResponse = "Cannot find the desired asset.";

        User tempUser = mock();

        Map<String, Collection<Order>> currOrders = new HashMap<>();
        currOrders.put("r2", List.of());
        when(tempUser.getCache()).thenReturn(new TimedCache<>(30));
        when(tempUser.getCurrentOrders()).thenReturn(currOrders);

        String actualResponse = assetRepository.sellAsset("r2", tempUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testSellAssetWithInvalidApiRequestException() throws InvalidApiRequestException {
        String expectedResponse =
            "Error communicating with the server. Contact an admin by providing the logs from the" +
                " exception_log.txt file";

        User tempUser = mock();

        Map<String, Collection<Order>> currOrders = new HashMap<>();
        currOrders.put("r2", List.of());
        when(tempUser.getCache()).thenReturn(new TimedCache<>(30));
        when(tempUser.getCurrentOrders()).thenReturn(currOrders);
        when(assetHttpClient.getAssets(any(URI.class))).thenThrow(InvalidApiRequestException.class);

        String actualResponse = assetRepository.sellAsset("r2", tempUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testSellAssetSuccessful() throws InvalidApiRequestException {
        String expectedResponse = "You have successfully sold BTC for 10000.0.";

        User tempUser = mock();

        Map<String, Collection<Order>> currOrders = new HashMap<>();
        Order temp = new Order("BTC", 1, 10000, -1);
        Collection<Order> tempCol = List.of(temp);
        currOrders.put("BTC", tempCol);

        when(tempUser.getCache()).thenReturn(new TimedCache<>(30));
        when(tempUser.getWallet()).thenReturn(new Wallet(0));
        when(tempUser.getCurrentOrders()).thenReturn(currOrders);

        Asset sellAsset = new Asset("BTC", "Bitcoin", 10000.0, 1);
        Collection<Asset> assets = List.of(sellAsset);
        when(assetHttpClient.getAssets(any(URI.class))).thenReturn(assets);

        String actualResponse = assetRepository.sellAsset("BTC", tempUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testGetActiveWalletSummaryWithNoCurrentOrders() {
        String expectedResponse = "You have these remaining assets. You have 50.0 dollars left in your wallet.";

        User tempUser = mock();

        when(tempUser.getWallet()).thenReturn(new Wallet(50));
        when(tempUser.getCurrentOrders()).thenReturn(new HashMap<>());

        String actualResponse = assetRepository.getActiveWalletSummary(tempUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testGetActiveWalletSummaryWithCurrentOrders() {
        String expectedResponse =
            "You have these remaining assets: Order=[id=BTC, amount=1.0, boughtPrice=10000.0]," +
                " Order=[id=ETH, amount=2.0, boughtPrice=100.0]. You have 50.0 dollars left in your wallet.";

        User tempUser = mock();

        Map<String, Collection<Order>> currOrders = new HashMap<>();

        Order temp = new Order("BTC", 1, 10000, -1);
        Order temp1 = new Order("ETH", 2, 100, -1);

        Collection<Order> tempCol = List.of(temp);
        Collection<Order> tempCol1 = List.of(temp1);
        currOrders.put("BTC", tempCol);
        currOrders.put("ETH", tempCol1);

        when(tempUser.getWallet()).thenReturn(new Wallet(50));
        when(tempUser.getCurrentOrders()).thenReturn(currOrders);
        String actualResponse = assetRepository.getActiveWalletSummary(tempUser).toString();

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testGetOverallWalletSummaryHandlesInvalidApiRequestException() throws InvalidApiRequestException {
        when(assetHttpClient.getAssets(any(URI.class))).thenThrow(new InvalidApiRequestException("test"));
        User tempUser = mock();

        Map<String, Collection<Order>> currOrders = new HashMap<>();
        Order temp = new Order("BTC", 1, 10000, -1);
        Collection<Order> tempCol = List.of(temp);
        currOrders.put("BTC", tempCol);

        when(tempUser.getCurrentOrders()).thenReturn(currOrders);
        when(tempUser.getCache()).thenReturn(new TimedCache<>(20));

        Response expectedResponse =
            Response.decline("Error communicating with the server. Contact an admin by providing the logs from the" +
                " exception_log.txt file");

        Response actualResponse = assetRepository.getOverallWalletSummary(tempUser);

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testGetOverallWalletSummaryHandlesInvalidAssetException() throws InvalidApiRequestException {
        when(assetHttpClient.getAssets(any(URI.class))).thenReturn(List.of());
        User tempUser = mock();

        Map<String, Collection<Order>> currOrders = new HashMap<>();
        Order temp = new Order("BTC", 1, 10000, -1);
        Collection<Order> tempCol = List.of(temp);
        currOrders.put("BTC", tempCol);

        when(tempUser.getCurrentOrders()).thenReturn(currOrders);
        when(tempUser.getCache()).thenReturn(new TimedCache<>(20));

        Response expectedResponse = Response.decline("There is not such asset!");

        Response actualResponse = assetRepository.getOverallWalletSummary(tempUser);

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testGetOverallWalletSummaryWithPositiveSummary() throws InvalidApiRequestException {
        Map<String, Collection<Order>> currOrders = new HashMap<>();
        Order temp = new Order("BTC", 1, 10000, -1);
        Collection<Order> tempCol = List.of(temp);
        currOrders.put("BTC", tempCol);

        User tempUser = mock();
        when(tempUser.getCurrentOrders()).thenReturn(currOrders);
        when(tempUser.getCache()).thenReturn(new TimedCache<>(20));

        Asset sellAsset = new Asset("BTC", "Bitcoin", 15000.0, 1);
        Collection<Asset> assets = List.of(sellAsset);
        when(assetHttpClient.getAssets(any(URI.class))).thenReturn(assets);

        Response expectedResponse =
            Response.ok(
                "Your overall wallet summary is positive. You bought all of your current assets for 10000.0$" +
                    " and your worth at the moment is 15000.0$. This means you gained in total: 5000.0$.");

        Response actualResponse = assetRepository.getOverallWalletSummary(tempUser);

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testGetOverallWalletSummaryWithNegativeSummary() throws InvalidApiRequestException {
        Map<String, Collection<Order>> currOrders = new HashMap<>();
        Order temp = new Order("BTC", 1, 10000, -1);
        Collection<Order> tempCol = List.of(temp);
        currOrders.put("BTC", tempCol);

        User tempUser = mock();
        when(tempUser.getCurrentOrders()).thenReturn(currOrders);
        when(tempUser.getCache()).thenReturn(new TimedCache<>(20));

        Asset sellAsset = new Asset("BTC", "Bitcoin", 5000.0, 1);
        Collection<Asset> assets = List.of(sellAsset);
        when(assetHttpClient.getAssets(any(URI.class))).thenReturn(assets);

        Response expectedResponse =
            Response.ok(
                "Your overall wallet summary is negative. You bought all of your assets for 10000.0$" +
                    " and your worth at the moment is 5000.0$. This means you lost in total: 5000.0$.");

        Response actualResponse = assetRepository.getOverallWalletSummary(tempUser);

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }

    @Test
    void testGetOverallWalletSummaryWithPastOrders() throws InvalidApiRequestException {
        Map<String, Collection<Order>> currOrders = new HashMap<>();
        Order temp = new Order("BTC", 1, 10000, -1);
        Collection<Order> tempCol = List.of(temp);
        currOrders.put("BTC", tempCol);

        Map<String, Collection<Order>> pastOrders = new HashMap<>();
        Order temp1 = new Order("BTC", 1, 10000, 15000);
        Collection<Order> tempPast = List.of(temp1);
        pastOrders.put("BTC", tempPast);

        User tempUser = mock();
        when(tempUser.getCurrentOrders()).thenReturn(currOrders);
        when(tempUser.getPastOrders()).thenReturn(pastOrders);
        when(tempUser.getCache()).thenReturn(new TimedCache<>(20));

        Asset sellAsset = new Asset("BTC", "Bitcoin", 10000.0, 1);
        Collection<Asset> assets = List.of(sellAsset);
        when(assetHttpClient.getAssets(any(URI.class))).thenReturn(assets);

        Response expectedResponse =
            Response.ok(
                "Your overall wallet summary is positive. You bought all of your current assets for 10000.0$" +
                    " and your worth at the moment is 15000.0$. This means you gained in total: 5000.0$.");

        Response actualResponse = assetRepository.getOverallWalletSummary(tempUser);

        assertEquals(expectedResponse, actualResponse,
            "Expected response is: " + expectedResponse + ", but was: " + actualResponse);
    }
}
