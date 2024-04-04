package bg.sofia.uni.fmi.mjt.wallet.server.repository;

import bg.sofia.uni.fmi.mjt.wallet.server.Response;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

public interface Repository {

    /**
     * Deposits money in the user's wallet and returns appropriate response
     * @param money the money which will be added
     * @param user the user which will get the money
     */
    Response deposit(double money, User user);

    /**
     * Returns a response with available asset listings
     * @param user the user which invokes the command
     */
    Response listOfferings(User user);

    /**
     * Buys a desired amount of an asset and returns a response
     * @param user the user which invokes the command
     * @param assetId the desired asset to be bought
     * @param amount how much of the asset to be bought
     */
    Response buyAsset(String assetId, double amount, User user);

    /**
     * Sells all possessions of an asset and returns a response
     * @param user the user which invokes the command
     * @param assetId the desired asset to be sold
     */
    Response sellAsset(String assetId, User user);

    /**
     * Returns a response with all current possessions and remaining balance
     * @param user the user which invokes the command
     */
    Response getActiveWalletSummary(User user);

    /**
     * Returns a response with the overall summary of the @user wallet
     * @param user the user which invokes the command
     */
    Response getOverallWalletSummary(User user);
}
