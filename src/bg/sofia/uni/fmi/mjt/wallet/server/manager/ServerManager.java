package bg.sofia.uni.fmi.mjt.wallet.server.manager;

import bg.sofia.uni.fmi.mjt.wallet.server.api.AssetHttpClient;
import bg.sofia.uni.fmi.mjt.wallet.server.command.factory.CommandFactory;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.wallet.server.register.UserRegistration;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static bg.sofia.uni.fmi.mjt.wallet.server.manager.AuthenticationManager.getLoginResponse;
import static bg.sofia.uni.fmi.mjt.wallet.server.manager.AuthenticationManager.handleCaseDefault;
import static bg.sofia.uni.fmi.mjt.wallet.server.manager.AuthenticationManager.handleCaseRegister;
import static bg.sofia.uni.fmi.mjt.wallet.server.manager.AuthenticationManager.parseAuthenticationCommandLine;
import static bg.sofia.uni.fmi.mjt.wallet.server.utils.ExceptionLogger.logExceptionToFile;

public class ServerManager {

    private static final int SERVER_PORT = 1910;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 4096;
    private static final String PROBLEM_WITH_SERVER_MESSAGE =
        "WARNING! There is a problem with the server! Contact an admin!";
    private static final String INVALID_COMMAND_MESSAGE = "Invalid command!";
    private static final String STOP_SERVER_COMMAND = "stop";

    boolean keepServerAlive = true;
    private Selector selector = null;
    private CommandFactory commandFactory;
    private UserRegistration registeredUsers;
    private Map<SocketChannel, User> loggedInUsers;

    public static void main(String... args) {
        ServerManager server = new ServerManager();
        server.prepare();
        try {
            server.start();
        } catch (IOException ioException) {
            logExceptionToFile(ioException);
            System.out.println(PROBLEM_WITH_SERVER_MESSAGE);
        }
    }

    private void prepare() {
        AssetHttpClient assetHttpClient = AssetHttpClient.getInstance();
        registeredUsers = UserRegistration.getInstance();
        commandFactory = CommandFactory.getInstance(assetHttpClient, registeredUsers);
        loggedInUsers = new HashMap<>();
        registeredUsers.loadUsers();

        checkWhenTerminated();
    }

    private void checkWhenTerminated() {
        Thread inputThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (keepServerAlive) {
                    String input = scanner.nextLine();
                    if (input.equalsIgnoreCase(STOP_SERVER_COMMAND)) {
                        keepServerAlive = false;
                        if (selector != null) {
                            selector.wakeup();
                        }
                        break;
                    }
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    private void start() throws IOException {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            initializeServer(serverSocketChannel);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (keepServerAlive) {
                int readyChannels = selector.select();
                if (readyChannels != 0) {

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                    handleSelectedKeys(keyIterator, buffer);
                }
            }
        } catch (IOException exception) {
            logExceptionToFile(exception);
            System.out.println(PROBLEM_WITH_SERVER_MESSAGE);
        } finally {
            if (selector != null) {
                selector.close();
            }
        }

    }

    private void initializeServer(ServerSocketChannel serverSocketChannel) throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        serverSocketChannel.configureBlocking(false);
    }

    private void handleSelectedKeys(Iterator<SelectionKey> keyIterator, ByteBuffer buffer)
        throws IOException {
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                try {
                    handleReadableKey(key, buffer);
                } catch (IOException ioException) {
                    logExceptionToFile(ioException);
                }
            } else if (key.isAcceptable()) {
                acceptNewUser((ServerSocketChannel) key.channel(), key.selector());
            }
            keyIterator.remove();
        }
    }

    private void handleReadableKey(SelectionKey key, ByteBuffer buffer)
        throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        String commandLine = readCommandFromUser(buffer, socketChannel);
        if (commandLine == null) {
            return;
        }

        String[] command;
        String response;
        try {
            command = parseAuthenticationCommandLine(commandLine);
        } catch (InvalidCommandException exception) {
            logExceptionToFile(exception);
            response = INVALID_COMMAND_MESSAGE;
            sendResponse(socketChannel, buffer, response);
            return;
        }

        response = switchCommand(commandLine, command, socketChannel);
        if (response != null) {
            sendResponse(socketChannel, buffer, response);
        }
    }

    private void acceptNewUser(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel accept = serverSocketChannel.accept();
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private void sendResponse(SocketChannel sc, ByteBuffer buffer, String response)
        throws IOException {
        buffer.clear();
        buffer.put((response + System.lineSeparator()).getBytes());

        buffer.flip();
        sc.write(buffer);

        buffer.clear();
    }

    private String readCommandFromUser(ByteBuffer buffer, SocketChannel socketChannel) throws IOException {
        buffer.clear();
        int r = socketChannel.read(buffer);
        if (r < 0) {
            socketChannel.close();
            return null;
        }
        buffer.flip();

        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    private String switchCommand(String commandLine, String[] command,
                                 SocketChannel socketChannel) {
        String commandName = command[0];
        String response;
        switch (commandName) {
            case "register" -> response = handleCaseRegister(socketChannel, command, registeredUsers, loggedInUsers);
            case "login" -> response = getLoginResponse(socketChannel, command, loggedInUsers, registeredUsers);
            default -> response = handleCaseDefault(socketChannel, commandLine, loggedInUsers, commandFactory);
        }
        return response;
    }
}

