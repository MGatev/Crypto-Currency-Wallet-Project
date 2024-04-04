package bg.sofia.uni.fmi.mjt.wallet.server.command;

import bg.sofia.uni.fmi.mjt.wallet.server.repository.AssetRepository;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

public class ListOfferingsCommand implements Command {
    private final  AssetRepository repository;

    public ListOfferingsCommand(AssetRepository repository) {
        this.repository = repository;
    }

    @Override
    public String execute(User user) {
        return repository.listOfferings(user).toString();
    }
}
