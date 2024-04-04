package bg.sofia.uni.fmi.mjt.wallet.server.api;

import java.net.URI;

public class AssetHttpRequest implements Request {
    private static final String URI_SCHEME = "https://";
    private static final String URI_HOST = "rest.coinapi.io/";
    private static final String URI_PATH = "v1/assets";
    private static final String SYMBOL_BEFORE_PARAMETERS = "?";
    private static final String FILTER = "filter_asset_id=";

    private final String assetId;
    private final URI uri;

    @Override
    public URI getUri() {
        return uri;
    }

    private URI buildUri() {
        StringBuilder uri = new StringBuilder(URI_SCHEME);

        uri.append(URI_HOST).append(URI_PATH);

        if (assetId != null) {
            uri.append(SYMBOL_BEFORE_PARAMETERS).append(FILTER).append(assetId);
        }

        return URI.create(uri.toString());
    }

    public static AssetRequestBuilder builder() {
        return new AssetRequestBuilder();
    }

    private AssetHttpRequest(AssetRequestBuilder builder) {
        assetId = builder.assetId;
        uri = buildUri();
    }

    public static class AssetRequestBuilder {
        private String assetId;

        public AssetRequestBuilder setAssetId(String assetId) {
            this.assetId = assetId;
            return this;
        }

        public AssetHttpRequest build() {
            return new AssetHttpRequest(this);
        }
    }
}
