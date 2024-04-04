package bg.sofia.uni.fmi.mjt.wallet.server.manager;

import bg.sofia.uni.fmi.mjt.wallet.server.command.factory.CommandFactory;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.wallet.server.register.UserRegistration;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;
import org.junit.jupiter.api.Test;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class AuthenticationManagerTest {

    @Test
    void testParseAuthenticationCommandLineWithCommandWithoutWhiteSpaces() throws InvalidCommandException {
        String[] expectedTokens = new String[1];
        expectedTokens[0] = "test-command";

        String[] actualTokens = AuthenticationManager.parseAuthenticationCommandLine("test-command\r\n");

        assertEquals(expectedTokens[0], actualTokens[0],
            "Expected a String array with size 1 and command: " + expectedTokens[0] + ", but was: " + actualTokens[0]);
    }

    @Test
    void testParseAuthenticationCommandLineWithCommandWithWhiteSpacesBetween() throws InvalidCommandException {
        String[] expectedTokens = new String[2];
        expectedTokens[0] = "test-command";
        expectedTokens[1] = "marti";

        String[] actualTokens = AuthenticationManager.parseAuthenticationCommandLine("test-command marti\r\n");

        assertArrayEquals(expectedTokens, actualTokens,
            "Expected a String array with size 2 and command: " + expectedTokens[0] + ", but was: " + actualTokens[0]);

    }

    @Test
    void testParseAuthenticationCommandLineWithLoginWithTwoParams() {
        assertThrows(InvalidCommandException.class,
            () -> AuthenticationManager.parseAuthenticationCommandLine("login marti\r\n"),
            "InvalidCommandException is expected!");
    }

    @Test
    void testParseAuthenticationCommandLineWithEmptyLine() {
        assertThrows(InvalidCommandException.class,
            () -> AuthenticationManager.parseAuthenticationCommandLine("\r\n"),
            "InvalidCommandException is expected!");
    }

    @Test
    void testParseAuthenticationCommandLineWithBlankLine() {
        assertThrows(InvalidCommandException.class,
            () -> AuthenticationManager.parseAuthenticationCommandLine(" \r\n"),
            "InvalidCommandException is expected!");
    }

    @Test
    void testParseAuthenticationCommandLineWithLineWithBlankTokens() {
        assertThrows(InvalidCommandException.class,
            () -> AuthenticationManager.parseAuthenticationCommandLine("login     user\r\n"),
            "InvalidCommandException is expected!");
    }

    @Test
    void testGetLoginResponseWithAlreadyLoggedInUser() {
        SocketChannel socketChannel = mock();
        User loggedUser = new User("marti");
        Map<SocketChannel, User> loggedInUsers = new HashMap<>();
        loggedInUsers.put(socketChannel, loggedUser);

        String expected = "You are already logged in.";

        String[] command = new String[1];
        UserRegistration userRegistration = UserRegistration.getInstance();

        String actual = AuthenticationManager.getLoginResponse(socketChannel, command, loggedInUsers, userRegistration);

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testGetLoginResponseWithInvalidUsername() {
        SocketChannel socketChannel = mock();
        Map<SocketChannel, User> loggedInUsers = new HashMap<>();

        String expected = "Invalid username!";

        String[] command = new String[3];
        command[0] = "login";
        command[1] = "test";
        command[2] = "9999";

        UserRegistration userRegistration = UserRegistration.getInstance();

        String actual = AuthenticationManager.getLoginResponse(socketChannel, command, loggedInUsers, userRegistration);

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testGetLoginResponseWithInvalidPassword() {
        SocketChannel socketChannel = mock();
        Map<SocketChannel, User> loggedInUsers = new HashMap<>();

        String expected = "Incorrect password";

        String[] command = new String[3];
        command[0] = "login";
        command[1] = "invalidPassword";
        command[2] = "9999";

        UserRegistration userRegistration = UserRegistration.getInstance();

        userRegistration.registerUser("invalidPassword", "8888");

        String actual = AuthenticationManager.getLoginResponse(socketChannel, command, loggedInUsers, userRegistration);

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testGetLoginResponseWithValidData() {
        SocketChannel socketChannel = mock();
        Map<SocketChannel, User> loggedInUsers = new HashMap<>();

        String expected = "Login successful";

        String[] command = new String[3];
        command[0] = "login";
        command[1] = "validTest";
        command[2] = "9999";

        UserRegistration userRegistration = UserRegistration.getInstance();

        userRegistration.registerUser("validTest", "9999");

        String actual = AuthenticationManager.getLoginResponse(socketChannel, command, loggedInUsers, userRegistration);

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testGetLoginResponseWithInvalidCommand() {
        SocketChannel socketChannel = mock();
        Map<SocketChannel, User> loggedInUsers = new HashMap<>();

        String expected = "Invalid command!";

        String[] command = new String[2];
        command[0] = "login";
        command[1] = "invalidTest";

        UserRegistration userRegistration = UserRegistration.getInstance();
        String actual = AuthenticationManager.getLoginResponse(socketChannel, command, loggedInUsers, userRegistration);

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testHandleCaseRegister() {
        String expected = "You are already logged in.";

        SocketChannel socketChannel = mock();
        User loggedUser = new User("marti");
        Map<SocketChannel, User> loggedInUsers = new HashMap<>();
        loggedInUsers.put(socketChannel, loggedUser);

        String[] command = new String[1];
        UserRegistration userRegistration = UserRegistration.getInstance();

        String actual =
            AuthenticationManager.handleCaseRegister(socketChannel, command, userRegistration, loggedInUsers);

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testHandleCaseRegisterWithInvalidCommand() {
        SocketChannel socketChannel = mock();
        Map<SocketChannel, User> loggedInUsers = new HashMap<>();

        String expected = "Invalid command!";

        String[] command = new String[2];
        command[0] = "register";
        command[1] = "invalidTest";

        UserRegistration userRegistration = UserRegistration.getInstance();
        String actual =
            AuthenticationManager.handleCaseRegister(socketChannel, command, userRegistration, loggedInUsers);

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testHandleCaseRegisterWithValidData() {
        SocketChannel socketChannel = mock();
        Map<SocketChannel, User> loggedInUsers = new HashMap<>();

        String expected = "You have registered successfully!";

        String[] command = new String[3];
        command[0] = "register";
        command[1] = "registerTest";
        command[2] = "1234";

        UserRegistration userRegistration = UserRegistration.getInstance();

        String actual =
            AuthenticationManager.handleCaseRegister(socketChannel, command, userRegistration, loggedInUsers);

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }

    @Test
    void testHandleCaseDefaultWithNotLoggedInUser() {
        String expected = "You are not logged in. Log in and try again.";

        SocketChannel socketChannel = mock();
        Map<SocketChannel, User> loggedInUsers = new HashMap<>();
        String commandLine = "command";
        CommandFactory commandFactory = mock();

        String actual =
            AuthenticationManager.handleCaseDefault(socketChannel, commandLine, loggedInUsers, commandFactory);

        assertEquals(expected, actual, "Expected response is: " + expected + ", but it was: " + actual);
    }
}
