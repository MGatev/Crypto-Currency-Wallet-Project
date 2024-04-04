package bg.sofia.uni.fmi.mjt.wallet.server.register;

import bg.sofia.uni.fmi.mjt.wallet.server.Response;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.LoginException;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static bg.sofia.uni.fmi.mjt.wallet.server.utils.ExceptionLogger.logExceptionToFile;

public class UserRegistration {

    private static final String USER_FILE = "users.txt";
    private static final String FILE_NOT_FOUND_MESSAGE = "Cannot find the file to save the registered users!";
    private static final String IO_EXCEPTION_MESSAGE = "There is a problem saving the registered users to a file!";

    private static UserRegistration instance;
    private Map<String, Map<String, User>> users;

    private UserRegistration() {
        users = new HashMap<>();
    }

    public static UserRegistration getInstance() {
        if (instance == null) {
            instance = new UserRegistration();
        }

        return instance;
    }

    public Response registerUser(String username, String password) {

        if (users.containsKey(username)) {
            return Response.decline("The username is already used. Please try again!");
        }

        String hashedPassword = hashPassword(password);

        User newUser = new User(username);
        Map<String, User> newMap = new HashMap<>();
        newMap.put(hashedPassword, newUser);

        users.put(username, newMap);

        saveUsers();

        return Response.ok("You have registered successfully!");
    }

    public User loginUser(String username, String password) throws LoginException {
        if (!users.containsKey(username)) {
            throw new LoginException("Invalid username!");
        }

        String enteredHashedPassword = hashPassword(password);
        if (enteredHashedPassword == null) {
            throw new LoginException("A problem occurred while trying to log in. Please contact an admin!");
        }

        User loggedUser = users.get(username).getOrDefault(enteredHashedPassword, null);

        if (loggedUser == null) {
            throw new LoginException("Incorrect password");
        }

        return loggedUser;
    }

    public Response loadUsers() {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            Map<String, Map<String, User>> temp = (Map<String, Map<String, User>>) input.readObject();
            users.clear();
            users = temp;
        } catch (FileNotFoundException exception) {
            logExceptionToFile(exception);
            saveUsers();
        } catch (IOException | ClassNotFoundException exception) {
            logExceptionToFile(exception);
            return Response.decline("There is a problem communicating with the server. Contact an admin.");
        }

        return null;
    }

    public void saveUsers() {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            output.writeObject(users);
        } catch (FileNotFoundException exception) {
            logExceptionToFile(exception);
            System.out.println(FILE_NOT_FOUND_MESSAGE);
        } catch (IOException exception) {
            logExceptionToFile(exception);
            System.out.println(IO_EXCEPTION_MESSAGE);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder hexStringBuilder = new StringBuilder();
            for (byte b : hashedBytes) {
                hexStringBuilder.append(String.format("%02x", b));
            }

            return hexStringBuilder.toString();
        } catch (NoSuchAlgorithmException exception) {
            logExceptionToFile(exception);
            return null;
        }
    }
}
