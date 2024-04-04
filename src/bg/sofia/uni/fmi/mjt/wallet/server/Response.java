package bg.sofia.uni.fmi.mjt.wallet.server;

import bg.sofia.uni.fmi.mjt.wallet.server.api.dto.Asset;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;
import bg.sofia.uni.fmi.mjt.wallet.server.user.account.Order;

import java.util.Collection;

public record Response(Status status, String additionalInfo, User user) {

    private static final String AVAILABLE_LISTINGS_MESSAGE = "Here are the available listings: ";
    private static final String REMAINING_ASSETS_MESSAGE = "You have these remaining assets: ";
    private static final String DOLLARS_LEFT_MESSAGE = " dollars left in your wallet.";
    private enum Status {
        OK, DECLINED
    }

    public static Response ok(String message) {
        return new Response(Status.OK, message, null);
    }

    public static Response ok(Collection<Order> orders, double remaining) {
        StringBuilder message = new StringBuilder(REMAINING_ASSETS_MESSAGE);
        for (Order order : orders) {
            message.append(order).append(", ");
        }

        message.delete(message.length() - 2, message.length());
        message.append(".").append(" You have ").append(remaining)
            .append(DOLLARS_LEFT_MESSAGE);
        return ok(message.toString());

    }

    public static Response ok(Collection<Asset> assets) {
        StringBuilder message = new StringBuilder(AVAILABLE_LISTINGS_MESSAGE);

        for (Asset asset : assets) {
            message.append(asset.toString()).append("; ");
        }

        message.delete(message.length() - 2, message.length());
        return Response.ok(message.toString());
    }

    public static Response decline(String errorMessage) {
        return new Response(Status.DECLINED, errorMessage, null);
    }

    @Override
    public String toString() {
        return additionalInfo;
    }
}
