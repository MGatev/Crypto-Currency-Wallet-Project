package bg.sofia.uni.fmi.mjt.wallet.server.user.account;

import bg.sofia.uni.fmi.mjt.wallet.server.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WalletTest {

    @Test
    void testAddMoneyWithNegativeAmount() {
        Wallet wallet = new Wallet(4);
        Response expected = Response.decline("You cannot add negative amount of money.");
        Response actual = wallet.addMoney(-5);

        assertEquals(expected, actual,
            "Expected response is: " + expected.additionalInfo() + ", but was: " + actual.additionalInfo());
    }

    @Test
    void testAddMoneyWithValidAmount() {
        Wallet wallet = new Wallet(4);
        Response expected = Response.ok("You have successfully added 4.0$ to your wallet.");
        Response actual = wallet.addMoney(4);

        assertEquals(expected, actual,
            "Expected response is: " + expected.additionalInfo() + ", but was: " + actual.additionalInfo());
    }

    @Test
    void testWithdrawMoneyWithNegativeAmount() {
        Wallet wallet = new Wallet(4);
        wallet.withdrawMoney(-5);

        assertEquals(4.0, wallet.getBalance(),
            "The balance should not change when the amount is invalid");
    }

    @Test
    void testWithdrawMoneyWithValidAmount() {
        Wallet wallet = new Wallet(4);
        wallet.withdrawMoney(3);

        assertEquals(1.0, wallet.getBalance(),
            "The balance should change when the amount is valid");
    }
}
