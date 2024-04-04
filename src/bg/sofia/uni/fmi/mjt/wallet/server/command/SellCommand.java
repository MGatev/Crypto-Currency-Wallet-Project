package bg.sofia.uni.fmi.mjt.wallet.server.command;

import bg.sofia.uni.fmi.mjt.wallet.server.repository.AssetRepository;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

public class SellCommand implements Command {
    private final AssetRepository repository;
    private final String assetId;

    public SellCommand(String assetId, AssetRepository repository) {
        this.repository = repository;
        this.assetId = assetId;
    }

    @Override
    public String execute(User user) {
        return repository.sellAsset(assetId, user).toString();
    }
}
