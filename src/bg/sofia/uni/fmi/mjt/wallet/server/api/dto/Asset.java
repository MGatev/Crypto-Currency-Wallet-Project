package bg.sofia.uni.fmi.mjt.wallet.server.api.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public record Asset(@SerializedName("asset_id") String assetId, String name,
                    @SerializedName("price_usd") double price,
                    @SerializedName("type_is_crypto") int isCrypto) implements Serializable {

    @Override
    public String toString() {
        return "Asset ID: " + assetId +
            ", Asset name: " + name + ", Asset current price: " + price;
    }
}
