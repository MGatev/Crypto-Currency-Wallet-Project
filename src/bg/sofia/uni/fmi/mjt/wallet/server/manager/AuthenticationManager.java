package bg.sofia.uni.fmi.mjt.wallet.server.manager;

import bg.sofia.uni.fmi.mjt.wallet.server.command.factory.CommandFactory;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.LoginException;
import bg.sofia.uni.fmi.mjt.wallet.server.register.UserRegistration;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class AuthenticationManager {

    private static final String ALREADY_LOGGED_IN_USER = "You are already logged in.";
    private static final String SUCCESSFUL_LOGIN_MESSAGE = "Login successful";
    private static final String INVALID_COMMAND_MESSAGE = "Invalid command!";
    private static final int THREE_PARAMS_COUNT = 3;
    private static final String LOGIN_COMMAND = "login";
    private static final String REGISTER_COMMAND = "register";
    private static final String NOT_LOGGED_IN_USER = "You are not logged in. Log in and try again.";

    //All functions are package private and are specific only for this package
    static String[] parseAuthenticationCommandLine(String commandLine)
        throws InvalidCommandException {
        if (commandLine.isEmpty() || commandLine.isBlank()) {
            throw new InvalidCommandException("User tries to use an invalid command!");
        }

        commandLine = commandLine.substring(0, commandLine.length() - 2);

        String[] tokens;
        if (commandLine.contains(" ")) {
            tokens = commandLine.split(" ");
            for (String string : tokens) {
                if (string.isBlank() || string.isEmpty()) {
                    throw new InvalidCommandException("User tries to use an invalid command!");
                }
            }
        } else {
            tokens = new String[1];
            tokens[0] = commandLine;
            return tokens;
        }

        if (tokens.length != THREE_PARAMS_COUNT &&
            (tokens[0].equals(LOGIN_COMMAND) || tokens[0].equals(REGISTER_COMMAND))) {
            throw new InvalidCommandException("User tries to use an invalid command");
        }
        return tokens;
    }

    static String getLoginResponse(SocketChannel socketChannel, String[] command,
                                   Map<SocketChannel, User> loggedInUsers, UserRegistration registeredUsers) {
        String response;
        if (loggedInUsers.get(socketChannel) != null) {
            return ALREADY_LOGGED_IN_USER;
        }
        User user;
        try {
            if (command.length == THREE_PARAMS_COUNT) {
                user = registeredUsers.loginUser(command[1], command[2]);
            } else {
                user = null;
            }
        } catch (LoginException exception) {
            return exception.getMessage();
        }
        if (user != null) {
            loggedInUsers.put(socketChannel, user);
            response = SUCCESSFUL_LOGIN_MESSAGE;
        } else {
            response = INVALID_COMMAND_MESSAGE;
        }

        return response;
    }

    static String handleCaseRegister(SocketChannel socketChannel, String[] command, UserRegistration registeredUsers,
                                     Map<SocketChannel, User> loggedInUsers) {
        String response;
        if (loggedInUsers.get(socketChannel) == null) {
            if (command.length == THREE_PARAMS_COUNT) {
                response = registeredUsers.registerUser(command[1], command[2]).toString();
            } else {
                response = INVALID_COMMAND_MESSAGE;
            }
        } else {
            response = ALREADY_LOGGED_IN_USER;
        }

        return response;
    }

    static String handleCaseDefault(SocketChannel socketChannel, String commandLine,
                                    Map<SocketChannel, User> loggedInUsers, CommandFactory commandFactory) {
        String response;
        User currentUser = loggedInUsers.get(socketChannel);
        if (currentUser == null) {
            response = NOT_LOGGED_IN_USER;
        } else {
            response = commandFactory.getCommand(commandLine).execute(currentUser);
        }

        return response;
    }
}
