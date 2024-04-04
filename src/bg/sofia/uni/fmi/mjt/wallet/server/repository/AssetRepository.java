package bg.sofia.uni.fmi.mjt.wallet.server.repository;

import bg.sofia.uni.fmi.mjt.wallet.server.Response;
import bg.sofia.uni.fmi.mjt.wallet.server.api.AssetHttpClient;
import bg.sofia.uni.fmi.mjt.wallet.server.api.AssetHttpRequest;
import bg.sofia.uni.fmi.mjt.wallet.server.api.dto.Asset;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.BuyException;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.InvalidApiRequestException;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.InvalidAssetException;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.SellException;
import bg.sofia.uni.fmi.mjt.wallet.server.register.UserRegistration;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;
import bg.sofia.uni.fmi.mjt.wallet.server.user.account.Order;

import static bg.sofia.uni.fmi.mjt.wallet.server.utils.ExceptionLogger.logExceptionToFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class AssetRepository implements Repository {

    private static final int INVALID_NUMBER = -1;
    private static final String NOT_SUCH_ASSET_MESSAGE = "There is not such asset!";
    private static final String NOT_ENOUGH_BALANCE_MESSAGE = "You do not have enough balance.";
    private static final String CANNOT_FIND_ASSET_MESSAGE = "Cannot find the desired asset.";
    private static final String INVALID_ASSET_SELL_MESSAGE = "You cannot sell asset which you do not have.";
    private static final String NULL_ASSET_MESSAGE = "Provide valid name of the asset!";
    private static final String NEGATIVE_AMOUNT_MESSAGE = "Amount cannot be negative!";
    private static final String INVALID_REQUEST_RESPONSE_MESSAGE =
        "Error communicating with the server. Contact an admin by providing the logs from the" +
            " exception_log.txt file";
    private static final String POSITIVE_SUMMARY_MESSAGE_BEGIN =
        "Your overall wallet summary is positive. You bought all of your current assets for ";
    private static final String SUMMARY_WORTH_MESSAGE = "$ and your worth at the moment is ";
    private static final String POSITIVE_SUMMARY_GAINED_MESSAGE = "$. This means you gained in total: ";
    private static final String NEGATIVE_SUMMARY_MESSAGE_BEGIN =
        "Your overall wallet summary is negative. You bought all of your assets for ";
    private static final String NEGATIVE_SUMMARY_LOST_MESSAGE = "$. This means you lost in total: ";
    private final UserRegistration users;
    private final AssetHttpClient httpClient;

    public AssetRepository(AssetHttpClient httpClient, UserRegistration users) {
        this.httpClient = httpClient;
        this.users = users;
    }

    @Override
    public Response deposit(double money, User user) {
        Response response = user.getWallet().addMoney(money);
        users.saveUsers();

        return response;
    }

    @Override
    public Response listOfferings(User user) {
        AssetHttpRequest request = AssetHttpRequest.builder().build();

        Collection<Asset> returned;
        try {
            returned = httpClient.getAssets(request.getUri());
        } catch (InvalidApiRequestException exception) {
            logExceptionToFile(exception);
            return Response.decline(INVALID_REQUEST_RESPONSE_MESSAGE);
        }

        for (Asset asset : returned) {
            user.getCache().put(asset.assetId(), asset);
        }

        return Response.ok(returned);
    }

    @Override
    public Response buyAsset(String assetId, double amount, User user) {
        Response buyResponse;

        try {
            validateBuy(assetId, amount);
            buyResponse = doBuyingProcess(assetId, amount, user);
        } catch (BuyException | InvalidAssetException exception) {
            return Response.decline(exception.getMessage());
        } catch (InvalidApiRequestException exception) {
            logExceptionToFile(exception);
            return Response.decline(INVALID_REQUEST_RESPONSE_MESSAGE);
        }

        return buyResponse;
    }

    @Override
    public Response sellAsset(String assetId, User user) {
        try {
            validateSell(assetId, user);
            return doSellingProcess(assetId, user);
        } catch (InvalidAssetException | SellException exception) {
            return Response.decline(exception.getMessage());
        } catch (InvalidApiRequestException exception) {
            logExceptionToFile(exception);
            return Response.decline(INVALID_REQUEST_RESPONSE_MESSAGE);
        }
    }

    @Override
    public Response getActiveWalletSummary(User user) {
        Collection<Order> currentPossessions = new ArrayList<>();

        for (String assetId : user.getCurrentOrders().keySet()) {
            currentPossessions.addAll(user.getCurrentOrders().get(assetId));
        }

        return Response.ok(currentPossessions, user.getWallet().getBalance());
    }

    @Override
    public Response getOverallWalletSummary(User user) {
        double boughtPrice = 0.0;
        double canSaleFor = 0.0;

        for (String assetId : user.getCurrentOrders().keySet()) {
            try {
                canSaleFor += getMoneyForSale(assetId, user);
            } catch (InvalidAssetException exception) {
                return Response.decline(exception.getMessage());
            } catch (InvalidApiRequestException exception) {
                logExceptionToFile(exception);
                return Response.decline(INVALID_REQUEST_RESPONSE_MESSAGE);
            }
            boughtPrice += getBoughtPrice(assetId, user);
        }
        double summaryPastOrders = getPastOrdersSummary(user);

        double overallSummary = canSaleFor - boughtPrice + summaryPastOrders;
        double worthNow = canSaleFor + summaryPastOrders;

        if (Double.compare(overallSummary, 0.0) >= 0) {
            return Response.ok(
                POSITIVE_SUMMARY_MESSAGE_BEGIN + boughtPrice + SUMMARY_WORTH_MESSAGE + worthNow
                    + POSITIVE_SUMMARY_GAINED_MESSAGE + overallSummary + "$.");
        } else {
            return Response.ok(
                NEGATIVE_SUMMARY_MESSAGE_BEGIN + boughtPrice + SUMMARY_WORTH_MESSAGE + canSaleFor
                    + NEGATIVE_SUMMARY_LOST_MESSAGE + (-overallSummary) + "$.");
        }
    }

    private double getPastOrdersSummary(User user) {
        double summaryPastOrders = 0.0;
        for (String assetId : user.getPastOrders().keySet()) {
            Collection<Order> orders = user.getPastOrders().get(assetId);
            for (Order order : orders) {
                double orderAmount = order.getAmount();
                double orderSummary = orderAmount * (order.getSoldPrice() - order.getBoughtPrice());
                summaryPastOrders += orderSummary;
            }
        }
        return summaryPastOrders;
    }

    private void validateBuy(String assetId, double amount) throws BuyException {
        if (assetId == null) {
            throw new BuyException(NULL_ASSET_MESSAGE);
        }

        if (Double.compare(amount, 0.0) <= 0) {
            throw new BuyException(NEGATIVE_AMOUNT_MESSAGE);
        }
    }

    private void validateSell(String assetId, User user) throws SellException, InvalidAssetException {
        if (assetId == null) {
            throw new InvalidAssetException(NULL_ASSET_MESSAGE);
        }
        if (!user.getCurrentOrders().containsKey(assetId)) {
            throw new SellException(INVALID_ASSET_SELL_MESSAGE);
        }
    }

    private double getBoughtPrice(String assetId, User user) {
        double boughtPrice = 0.0;
        for (Order order : user.getCurrentOrders().get(assetId)) {
            boughtPrice += order.getAmount() * order.getBoughtPrice();
        }

        return boughtPrice;
    }

    private double getMoneyForSale(String assetId, User user) throws InvalidAssetException, InvalidApiRequestException {
        double price = getPriceForAsset(assetId, user);

        double amountOfPossession = user.getCurrentOrders().get(assetId)
            .stream()
            .mapToDouble(Order::getAmount)
            .sum();

        return amountOfPossession * price;
    }

    private Response doSellingProcess(String assetId, User user)
        throws SellException, InvalidAssetException, InvalidApiRequestException {

        double soldMoney;
        try {
            soldMoney = getMoneyForSale(assetId, user);
        } catch (InvalidAssetException exception) {
            throw new SellException(CANNOT_FIND_ASSET_MESSAGE, exception);
        }

        user.getWallet().addMoney(soldMoney);
        double currentPriceForAsset = getPriceForAsset(assetId, user);

        for (Order order : user.getCurrentOrders().get(assetId)) {
            order.setSoldPrice(currentPriceForAsset);
        }

        user.getPastOrders().computeIfAbsent(assetId, k -> new ArrayList<>())
            .addAll(user.getCurrentOrders().get(assetId));

        user.getCurrentOrders().remove(assetId);

        users.saveUsers();
        return Response.ok(
            "You have successfully sold " + assetId + " for " + soldMoney + ".");
    }

    private Response doBuyingProcess(String assetId, double amount, User user)
        throws InvalidAssetException, BuyException, InvalidApiRequestException {

        double price = getPriceForAsset(assetId, user);

        double priceToBuy = amount * price;

        if (Double.compare(user.getWallet().getBalance(), priceToBuy) < 0) {
            throw new BuyException(NOT_ENOUGH_BALANCE_MESSAGE);
        }

        user.getWallet().withdrawMoney(priceToBuy);

        Map<String, Collection<Order>> currentOrders = user.getCurrentOrders();
        currentOrders.computeIfAbsent(assetId, k -> new ArrayList<>()).add(new Order(assetId, amount, price, 0));

        users.saveUsers();
        return Response.ok("You have successfully bought " + amount + " of " + assetId + " for " + priceToBuy + ".");
    }

    private double getPriceFromAPIForOneAsset(String assetId, User user)
        throws InvalidAssetException, InvalidApiRequestException {

        AssetHttpRequest request = AssetHttpRequest.builder().setAssetId(assetId).build();

        Collection<Asset> returned = httpClient.getAssets(request.getUri());

        Asset foundAsset = returned
            .stream()
            .findFirst()
            .orElse(new Asset("", "", INVALID_NUMBER, INVALID_NUMBER));

        if (foundAsset.price() != INVALID_NUMBER) {
            user.getCache().put(assetId, foundAsset);
        } else {
            throw new InvalidAssetException(NOT_SUCH_ASSET_MESSAGE);
        }

        return foundAsset.price();
    }

    private double getPriceForAsset(String assetId, User user) throws InvalidAssetException,
        InvalidApiRequestException {

        Asset storedInCache = user.getCache().get(assetId);

        if (storedInCache != null) {
            return storedInCache.price();
        } else {
            return getPriceFromAPIForOneAsset(assetId, user);
        }
    }
}
