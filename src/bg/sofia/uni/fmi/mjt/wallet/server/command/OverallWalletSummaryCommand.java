package bg.sofia.uni.fmi.mjt.wallet.server.command;

import bg.sofia.uni.fmi.mjt.wallet.server.repository.AssetRepository;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

public class OverallWalletSummaryCommand implements Command {
    private final AssetRepository repository;

    public OverallWalletSummaryCommand(AssetRepository repository) {
        this.repository = repository;
    }

    @Override
    public String execute(User user) {
        return repository.getOverallWalletSummary(user).toString();
    }
}
