package bg.sofia.uni.fmi.mjt.wallet.server.register;

import bg.sofia.uni.fmi.mjt.wallet.server.Response;
import bg.sofia.uni.fmi.mjt.wallet.server.exception.LoginException;
import bg.sofia.uni.fmi.mjt.wallet.server.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserRegistrationTest {
    private UserRegistration userRegistration;

    @BeforeEach
    void setUp() {
        userRegistration = UserRegistration.getInstance();
    }

    @Test
    void testGetInstance() {
        UserRegistration instance1 = UserRegistration.getInstance();
        UserRegistration instance2 = UserRegistration.getInstance();

        assertSame(instance1, instance2,
            "The UserRegistration uses the Singleton design pattern and the two instances should be the same");
    }

    @Test
    void testUserRegistrationThreadSafety() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<UserRegistration> future1 = executorService.submit(UserRegistration::getInstance);
        Future<UserRegistration> future2 = executorService.submit(UserRegistration::getInstance);

        UserRegistration instance1 = future1.get();
        UserRegistration instance2 = future2.get();

        assertSame(instance1, instance2,
            "Two instances of UserRegistration should be the same even if there is a concurrent access.");
    }

    @Test
    void testRegisterUserWithUsedUsername() {
        userRegistration.registerUser("marti", "123");

        Response expected = Response.decline("The username is already used. Please try again!");
        Response actual = userRegistration.registerUser("marti", "321");

        assertEquals(expected, actual,
            "Expected response is: " + expected.additionalInfo() + ", but was: " + actual.additionalInfo());

    }

    @Test
    void testRegisterUserWithValidUsernameAndPassword() {
        Response expected = Response.ok("You have registered successfully!");
        Response actual = userRegistration.registerUser("petar", "pepi123");

        assertEquals(expected, actual,
            "Expected response is: " + expected.additionalInfo() + ", but was: " + actual.additionalInfo());
    }

    @Test
    void testLoginUserWithWrongUsername() {
        assertThrows(LoginException.class, () -> userRegistration.loginUser("marti", "123") ,
            "LoginException is expected to be thrown!");
    }

    @Test
    void testLoginUserWithWrongPassword() {
        userRegistration.registerUser("marti", "1111");
        assertThrows(LoginException.class, () -> userRegistration.loginUser("marti", "123") ,
            "LoginException is expected to be thrown!");
    }

    @Test
    void testLoginUserWithValidData() throws LoginException {
        userRegistration.registerUser("marti", "1111");
        User expected = new User("marti");
        User actual = userRegistration.loginUser("marti", "1111");
        assertEquals(expected, actual, "Expected user to login.");
    }

    @Test
    void testLoadUsersSuccessfulLoad() {
        Response actual = userRegistration.loadUsers();

        assertNull(actual, "Expected null as Response when loadUsers is successful.");
    }
}
