package bg.sofia.uni.fmi.mjt.wallet.server.api;

import bg.sofia.uni.fmi.mjt.wallet.server.api.dto.Asset;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.InvalidApiRequestException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.util.Collection;

import static bg.sofia.uni.fmi.mjt.wallet.server.utils.ExceptionLogger.logExceptionToFile;

public class AssetHttpClient {

    private static final Gson GSON = new Gson();
    private static final int MAX_ASSETS_COUNT = 50;
    private static final String API_KEY_FILE = "apiKey.txt";
    private static final String AUTH_HEADER = "X-CoinAPI-Key";
    private static final String GENERAL_EXCEPTION_MESSAGE =
        "Unable to retrieve information from the asset service" +
            "Contact administrator by providing logs.";

    private static final String EXCEPTION_MESSAGE_401 =
        "Unable to retrieve information from the asset service, because of invalid authorization." +
            "Contact administrator by providing logs.";

    private static final String EXCEPTION_MESSAGE_404 =
        "Trying to access invalid part of the service. Contact administrator by providing logs.";

    private static final String CANNOT_FIND_API_KEY =
        "Cannot find the api key. Contact an admin and provide information";
    private static final int ERROR_STATUS_401 = 401;
    private static final int ERROR_STATUS_404 = 404;
    private final HttpClient httpClient;
    private static AssetHttpClient instance;
    private final String apiKey;

    private AssetHttpClient() {
        String apiKeyFromFile = null;
        httpClient = HttpClient.newBuilder().build();
        try {
            apiKeyFromFile = readApiKey();
        } catch (IOException exception) {
            logExceptionToFile(exception);
        }

        apiKey = apiKeyFromFile;
    }

    public static synchronized AssetHttpClient getInstance() {
        if (instance == null) {
            instance = new AssetHttpClient();
        }
        return instance;
    }

    public Collection<Asset> getAssets(URI requestUri) throws InvalidApiRequestException {
        if (apiKey == null) {
            throw new InvalidApiRequestException(CANNOT_FIND_API_KEY);
        }

        HttpResponse<String> response;
        try {
            HttpRequest httpRequest =
                HttpRequest.newBuilder().uri(requestUri).setHeader(AUTH_HEADER, apiKey).build();
            response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            throw new InvalidApiRequestException(GENERAL_EXCEPTION_MESSAGE, exception);
        }

        if (response.statusCode() == ERROR_STATUS_401) {
            throw new InvalidApiRequestException(EXCEPTION_MESSAGE_401);
        } else if (response.statusCode() == ERROR_STATUS_404) {
            throw new InvalidApiRequestException(EXCEPTION_MESSAGE_404);
        }

        Type collectionType = new TypeToken<Collection<Asset>>() { }.getType();
        Collection<Asset> parsedResponse = GSON.fromJson(response.body(), collectionType);

        return parsedResponse
            .stream()
            .filter(asset -> asset.isCrypto() == 1)
            .limit(MAX_ASSETS_COUNT)
            .toList();
    }

    public static void setDependencyUsingReflection(HttpClient httpClient) throws Exception {
        Field field = AssetHttpClient.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        field.set(getInstance(), httpClient);
    }

    private String readApiKey() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(API_KEY_FILE))) {
            return reader.readLine();
        }
    }
}
