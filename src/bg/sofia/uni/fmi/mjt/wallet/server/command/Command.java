package bg.sofia.uni.fmi.mjt.wallet.server.command;

import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

public interface Command {

    /**
     * Returns the response of a command.
     */
    String execute(User user);
}
