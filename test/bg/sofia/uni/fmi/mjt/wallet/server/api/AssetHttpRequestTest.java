package bg.sofia.uni.fmi.mjt.wallet.server.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssetHttpRequestTest {

    @Test
    void testAssetRequestBuilderWithDefault() {
        AssetHttpRequest assetHttpRequest = AssetHttpRequest.builder().build();
        String expected = "https://rest.coinapi.io/v1/assets";
        String actual = assetHttpRequest.getUri().toString();

        assertEquals(expected, actual, "Expected a request with uri: " + expected + " but was: " + actual);
    }

    @Test
    void testAssetRequestBuilderWithAssetId() {
        AssetHttpRequest assetHttpRequest = AssetHttpRequest.builder().setAssetId("BTC").build();
        String expected = "https://rest.coinapi.io/v1/assets?filter_asset_id=BTC";
        String actual = assetHttpRequest.getUri().toString();

        assertEquals(expected, actual, "Expected a request with uri: " + expected + " but was: " + actual);
    }
}
