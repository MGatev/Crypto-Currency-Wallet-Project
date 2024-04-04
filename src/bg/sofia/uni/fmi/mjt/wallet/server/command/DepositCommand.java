package bg.sofia.uni.fmi.mjt.wallet.server.command;

import bg.sofia.uni.fmi.mjt.wallet.server.repository.AssetRepository;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

public class DepositCommand implements Command {
    private final AssetRepository repository;
    private final double amount;

    public DepositCommand(double amount, AssetRepository repository) {
        this.repository = repository;
        this.amount = amount;
    }

    @Override
    public String execute(User user) {
        return repository.deposit(amount, user).toString();
    }
}
