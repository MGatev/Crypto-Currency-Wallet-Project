package bg.sofia.uni.fmi.mjt.wallet.server.command.factory;

import bg.sofia.uni.fmi.mjt.wallet.server.api.AssetHttpClient;
import bg.sofia.uni.fmi.mjt.wallet.server.command.ActiveWalletSummaryCommand;
import bg.sofia.uni.fmi.mjt.wallet.server.command.BuyCommand;
import bg.sofia.uni.fmi.mjt.wallet.server.command.Command;
import bg.sofia.uni.fmi.mjt.wallet.server.command.DepositCommand;
import bg.sofia.uni.fmi.mjt.wallet.server.command.IncorrectCommand;
import bg.sofia.uni.fmi.mjt.wallet.server.command.ListOfferingsCommand;
import bg.sofia.uni.fmi.mjt.wallet.server.command.OverallWalletSummaryCommand;
import bg.sofia.uni.fmi.mjt.wallet.server.command.SellCommand;
import bg.sofia.uni.fmi.mjt.wallet.server.register.UserRegistration;
import bg.sofia.uni.fmi.mjt.wallet.server.repository.AssetRepository;

public class CommandFactory {

    private static final int THREE_PARAMS_COUNT = 3;
    private static final int TWO_PARAM_COUNT = 2;

    private static CommandFactory instance;
    private final AssetRepository repository;

    private CommandFactory(AssetHttpClient httpClient, UserRegistration users) {
        repository = new AssetRepository(httpClient, users);
    }

    public static synchronized CommandFactory getInstance(AssetHttpClient httpClient, UserRegistration users) {
        if (instance == null) {
            instance = new CommandFactory(httpClient, users);
        }
        return instance;
    }

    public Command getCommand(String commandLine) {
        if (commandLine == null || commandLine.isEmpty()) {
            return new IncorrectCommand();
        }

        String commandName;
        String[] tokens = new String[0];
        if (commandLine.contains(" ")) {
            tokens = commandLine.split(" ");

            if (tokens.length == 0) {
                return new IncorrectCommand();
            }
            for (String string : tokens) {
                if (string.isBlank() || string.isEmpty()) {
                    return new IncorrectCommand();
                }
            }
            commandName = tokens[0];
        } else {
            commandName = commandLine.substring(0, commandLine.length() - 2);
        }

        return switchCommandName(commandName, tokens);
    }

    private Command switchCommandName(String commandName, String[] tokens) {
        return switch (commandName) {
            case "deposit-money" -> handleCaseDeposit(tokens);
            case "list-offerings" -> new ListOfferingsCommand(repository);
            case "buy" -> handleCaseBuy(tokens);
            case "sell" -> handleCaseSell(tokens);
            case "get-wallet-summary" -> new ActiveWalletSummaryCommand(repository);
            case "get-wallet-overall-summary" -> new OverallWalletSummaryCommand(repository);
            default -> new IncorrectCommand();
        };
    }

    private Command handleCaseSell(String[] tokens) {
        if (tokens.length != TWO_PARAM_COUNT) {
            return new IncorrectCommand();
        }

        String assetId = tokens[1].substring(0, tokens[1].length() - 2);
        return new SellCommand(assetId, repository);
    }

    private Command handleCaseBuy(String[] tokens) {
        if (tokens.length != THREE_PARAMS_COUNT) {
            return new IncorrectCommand();
        }

        String assetId = tokens[1];
        String strAmount = tokens[2];
        double amount = Double.parseDouble(strAmount);

        return new BuyCommand(assetId, amount, repository);
    }

    private Command handleCaseDeposit(String[] tokens) {
        if (tokens.length != TWO_PARAM_COUNT) {
            return new IncorrectCommand();
        }

        String strAmount = tokens[1];
        double amount = Double.parseDouble(strAmount);

        return new DepositCommand(amount, repository);
    }
}
