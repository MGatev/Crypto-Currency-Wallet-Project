package bg.sofia.uni.fmi.mjt.wallet.server.api;

import bg.sofia.uni.fmi.mjt.wallet.server.api.dto.Asset;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.InvalidApiRequestException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AssetHttpClientTest {

    @Test
    void testGetInstance() {
        AssetHttpClient instance1 = AssetHttpClient.getInstance();
        AssetHttpClient instance2 = AssetHttpClient.getInstance();

        assertSame(instance1, instance2,
            "The AssetHttpClient uses the Singleton design pattern and the two instances should be the same");
    }

    @Test
    void testAssetHttpClientThreadSafety() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<AssetHttpClient> future1 = executorService.submit(AssetHttpClient::getInstance);
        Future<AssetHttpClient> future2 = executorService.submit(AssetHttpClient::getInstance);

        AssetHttpClient instance1 = future1.get();
        AssetHttpClient instance2 = future2.get();

        assertSame(instance1, instance2,
            "Two instances of AssetHttpClient should be the same even if there is a concurrent access.");
    }

    @Test
    void testGetAssetsThrowsInvalidApiRequestException() throws Exception {
        HttpClient httpClient = Mockito.mock();
        URI uri = URI.create("https://rest.coinapi.io/v1/assets?filter_asset_id=BTC");

        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(IOException.class);
        AssetHttpClient.setDependencyUsingReflection(httpClient);

        assertThrows(InvalidApiRequestException.class, () -> AssetHttpClient.getInstance().getAssets(uri),
            "InvalidApiRequestException was expected to be thrown!");
    }

    @Test
    void testGetAssetsThrowsInvalidApiRequestExceptionWhenStatusCode401() throws Exception {
        HttpClient httpClient = Mockito.mock();
        URI uri = URI.create("https://rest.coinapi.io/v1/assets?filter_asset_id=BTC");

        HttpResponse<String> returned = Mockito.mock();
        when(returned.statusCode()).thenReturn(401);

        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(returned);
        AssetHttpClient.setDependencyUsingReflection(httpClient);

        assertThrows(InvalidApiRequestException.class, () -> AssetHttpClient.getInstance().getAssets(uri),
            "InvalidApiRequestException was expected to be thrown!");
    }

    @Test
    void testGetAssetsThrowsInvalidApiRequestExceptionWhenStatusCode404() throws Exception {
        HttpClient httpClient = Mockito.mock();
        URI uri = URI.create("https://rest.coinapi.io/v1/assets?filter_asset_id=BTC");

        HttpResponse<String> returned = Mockito.mock();
        when(returned.statusCode()).thenReturn(404);

        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(returned);
        AssetHttpClient.setDependencyUsingReflection(httpClient);

        assertThrows(InvalidApiRequestException.class, () -> AssetHttpClient.getInstance().getAssets(uri),
            "InvalidApiRequestException was expected to be thrown!");
    }

    @Test
    void testGetAssetsWithOneReturnedAsset() throws Exception, InvalidApiRequestException {
        HttpClient httpClient = Mockito.mock();

        URI uri = URI.create("https://rest.coinapi.io/v1/assets?filter_asset_id=BTC");

        HttpResponse<String> returned = Mockito.mock();

        when(returned.statusCode()).thenReturn(200);
        when(returned.body()).thenReturn("""
            [
              {
                "asset_id": "BTC",
                "name": "Bitcoin",
                "type_is_crypto": 1,
                "data_quote_start": "2014-02-24T00:00:00.0000000Z",
                "data_quote_end": "2024-02-12T00:00:00.0000000Z",
                "data_orderbook_start": "2014-02-24T17:43:05.0000000Z",
                "data_orderbook_end": "2023-07-07T00:00:00.0000000Z",
                "data_trade_start": "2010-07-17T00:00:00.0000000Z",
                "data_trade_end": "2024-02-12T00:00:00.0000000Z",
                "data_symbols_count": 204569,
                "volume_1hrs_usd": 6369605817702.31,
                "volume_1day_usd": 54441693323754076.22,
                "volume_1mth_usd": 4160671866292326160.28,
                "price_usd": 48735.12485158565631682441078,
                "id_icon": "4caf2b16-a017-4e26-a348-2cea69c34cba",
                "data_start": "2010-07-17",
                "data_end": "2024-02-12"
              }
            ]
            """);
        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(returned);
        AssetHttpClient.setDependencyUsingReflection(httpClient);

        Asset asset = new Asset("BTC", "Bitcoin", 48735.12485158565631682441078, 1);

        Collection<Asset> expected = new ArrayList<>();
        expected.add(asset);

        Collection<Asset> actual = AssetHttpClient.getInstance().getAssets(uri);
        assertIterableEquals(expected, actual,
            "Expected to return a collection with one asset, but was returned: " + actual.size());
    }

    @Test
    void testGetAssetsWithEmptyReturn() throws Exception, InvalidApiRequestException {
        HttpClient httpClient = Mockito.mock();

        URI uri = URI.create("https://rest.coinapi.io/v1/assets?filter_asset_id=MARTI");

        HttpResponse<String> returned = Mockito.mock();

        when(returned.statusCode()).thenReturn(200);
        when(returned.body()).thenReturn("[]");

        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(returned);
        AssetHttpClient.setDependencyUsingReflection(httpClient);

        Collection<Asset> expected = new ArrayList<>();

        Collection<Asset> actual = AssetHttpClient.getInstance().getAssets(uri);
        assertIterableEquals(expected, actual, "Expected to return empty collection, but it was not!");
    }

    @Test
    void testGetAssetsWithMultipleReturnedAssets() throws Exception, InvalidApiRequestException {
        HttpClient httpClient = Mockito.mock();

        URI uri = URI.create("https://rest.coinapi.io/v1/assets?filter_asset_id=BTC;ETH");

        HttpResponse<String> returned = Mockito.mock();

        when(returned.statusCode()).thenReturn(200);
        when(returned.body()).thenReturn("""
            [
               {
                 "asset_id": "BTC",
                 "name": "Bitcoin",
                 "type_is_crypto": 1,
                 "data_quote_start": "2014-02-24T00:00:00.0000000Z",
                 "data_quote_end": "2024-02-12T00:00:00.0000000Z",
                 "data_orderbook_start": "2014-02-24T17:43:05.0000000Z",
                 "data_orderbook_end": "2023-07-07T00:00:00.0000000Z",
                 "data_trade_start": "2010-07-17T00:00:00.0000000Z",
                 "data_trade_end": "2024-02-12T00:00:00.0000000Z",
                 "data_symbols_count": 204577,
                 "volume_1hrs_usd": 48628541746.55,
                 "volume_1day_usd": 2574586959567532736.30,
                 "volume_1mth_usd": 129702379051526905216.18,
                 "price_usd": 49090.100429989744246854629996,
                 "id_icon": "4caf2b16-a017-4e26-a348-2cea69c34cba",
                 "data_start": "2010-07-17",
                 "data_end": "2024-02-12"
               },
               {
                 "asset_id": "ETH",
                 "name": "Ethereum",
                 "type_is_crypto": 1,
                 "data_quote_start": "2015-08-08T00:00:00.0000000Z",
                 "data_quote_end": "2024-02-12T00:00:00.0000000Z",
                 "data_orderbook_start": "2015-08-07T14:50:38.1774950Z",
                 "data_orderbook_end": "2023-07-07T00:00:00.0000000Z",
                 "data_trade_start": "2015-08-07T00:00:00.0000000Z",
                 "data_trade_end": "2024-02-12T00:00:00.0000000Z",
                 "data_symbols_count": 164620,
                 "volume_1hrs_usd": 26828408780.61,
                 "volume_1day_usd": 64482245019910.11,
                 "volume_1mth_usd": 353826517160601138080194.99,
                 "price_usd": 2628.4156914439332633644873317,
                 "id_icon": "604ae453-3d9f-4ad0-9a48-9905cce617c2",
                 "data_start": "2015-08-07",
                 "data_end": "2024-02-12"
               }
             ]
            """);
        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(returned);
        AssetHttpClient.setDependencyUsingReflection(httpClient);

        Asset asset = new Asset("BTC", "Bitcoin", 49090.100429989744246854629996, 1);
        Asset asset1 = new Asset("ETH", "Ethereum", 2628.4156914439332633644873317, 1);

        Collection<Asset> expected = new ArrayList<>();
        expected.add(asset);
        expected.add(asset1);

        Collection<Asset> actual = AssetHttpClient.getInstance().getAssets(uri);
        assertIterableEquals(expected, actual,
            "Expected to return a collection with two assets, but was returned: " + actual.size());
    }
}
