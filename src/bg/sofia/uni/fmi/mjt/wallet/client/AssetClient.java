package bg.sofia.uni.fmi.mjt.wallet.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class AssetClient {
    private static final int SERVER_PORT = 1910;
    private static final String HOST_NAME = "localhost";
    private static final String ENTER_COMMAND_MESSAGE = "Enter message: ";
    private static final String QUIT_COMMAND_MESSAGE = "quit";
    private static final String SERVER_CONNECTION_LOST_MESSAGE = "Cannot make contact with the server! " +
        "Contact an admin and provide him with \"exception_log.txt\" file.";
    private static final String AVAILABLE_COMMANDS_MESSAGE =
        "Here are the available commands which you can use to navigate in your wallet: ";
    private static final String LOGIN_COMMAND_MESSAGE = " - login <username> <password> - login in your wallet.";
    private static final String REGISTER_COMMAND_MESSAGE =
        " - register <username> <password> - register if you do not have a registration yet.";
    private static final String DEPOSIT_COMMAND_MESSAGE = " - deposit-money <amount> - deposit money in you wallet.";
    private static final String LIST_OFFERINGS_COMMAND_MESSAGE =
        " - list-offerings - view current available assets and the information for them.";
    private static final String BUY_COMMAND_MESSAGE =
        " - buy <asset id> <amount> - buy the desired asset with the specified amount of it.";
    private static final String SELL_COMMAND_MESSAGE =
        " - sell <asset id> - sell all your possessions of the desired asset.";
    private static final String GET_SUMMARY_COMMAND_MESSAGE =
        " - get-wallet-summary - view your current possessions and the remaining balance in your wallet.";
    private static final String GET_SUMMARY_OVERALL_COMMAND_MESSAGE =
        " - get-wallet-overall-summary - view your money history, did you make profit or lost money.";

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             BufferedReader reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(HOST_NAME, SERVER_PORT));

            printInstructions();

            while (true) {
                System.out.print(ENTER_COMMAND_MESSAGE);
                String message = scanner.nextLine();

                if (QUIT_COMMAND_MESSAGE.equals(message)) {
                    break;
                }

                writer.println(message);

                String reply = reader.readLine();
                System.out.println(reply);
            }
        } catch (IOException exception) {
            System.out.println(SERVER_CONNECTION_LOST_MESSAGE);
        }
    }

    private static void printInstructions() {
        System.out.println(System.lineSeparator() + AVAILABLE_COMMANDS_MESSAGE + System.lineSeparator());
        System.out.println(REGISTER_COMMAND_MESSAGE);
        System.out.println(LOGIN_COMMAND_MESSAGE);
        System.out.println(DEPOSIT_COMMAND_MESSAGE);
        System.out.println(LIST_OFFERINGS_COMMAND_MESSAGE);
        System.out.println(BUY_COMMAND_MESSAGE);
        System.out.println(SELL_COMMAND_MESSAGE);
        System.out.println(GET_SUMMARY_COMMAND_MESSAGE);
        System.out.println(GET_SUMMARY_OVERALL_COMMAND_MESSAGE + System.lineSeparator());
    }
}
