package bg.sofia.uni.fmi.mjt.wallet.server.command;

import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

public class IncorrectCommand implements Command {

    @Override
    public String execute(User user) {
        return "Invalid command entered. Please try again!";
    }
}
