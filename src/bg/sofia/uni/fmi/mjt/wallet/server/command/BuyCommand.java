package bg.sofia.uni.fmi.mjt.wallet.server.command;

import bg.sofia.uni.fmi.mjt.wallet.server.repository.AssetRepository;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

public class BuyCommand implements Command {
    private final AssetRepository repository;
    private final String assetId;
    private final double amount;

    public BuyCommand(String assetId, double amount, AssetRepository repository) {
        this.repository = repository;
        this.assetId = assetId;
        this.amount = amount;
    }

    @Override
    public String execute(User user) {
        return repository.buyAsset(assetId, amount, user).toString();
    }
}
