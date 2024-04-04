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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class CommandFactoryTest {
    private CommandFactory factory;

    @BeforeEach
    public void setUp() {
        AssetHttpClient assetHttpClient = mock();
        UserRegistration userRegistration = mock();
        factory = CommandFactory.getInstance(assetHttpClient, userRegistration);
    }

    @Test
    void testGetInstance() {

        AssetHttpClient assetHttpClient = mock();
        UserRegistration userRegistration = mock();

        CommandFactory instance1 = CommandFactory.getInstance(assetHttpClient, userRegistration);
        CommandFactory instance2 = CommandFactory.getInstance(assetHttpClient, userRegistration);

        assertSame(instance1, instance2,
            "The CommandFactory uses the Singleton design pattern and the two instances should be the same");
    }

    @Test
    void testCommandFactoryThreadSafety() throws InterruptedException, ExecutionException {
        AssetHttpClient assetHttpClient = mock();
        UserRegistration userRegistration = mock();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<CommandFactory> future1 = executorService.submit(() -> CommandFactory.getInstance(assetHttpClient, userRegistration));
        Future<CommandFactory> future2 = executorService.submit(() -> CommandFactory.getInstance(assetHttpClient, userRegistration));

        CommandFactory instance1 = future1.get();
        CommandFactory instance2 = future2.get();

        assertSame(instance1, instance2,
            "Two instances of CommandFactory should be the same even if there is a concurrent access.");
    }

    @Test
    void testGetCommandWithEmptyLine() {
        Command emptyCommand = factory.getCommand("");

        assertEquals(emptyCommand.getClass(), IncorrectCommand.class);
    }

    @Test
    void testGetCommandWithEmptyCommand() {
        Command emptyCommand = factory.getCommand(" ");

        assertEquals(emptyCommand.getClass(), IncorrectCommand.class);
    }

    @Test
    void testGetCommandWithBlankCommand() {
        Command blankCommand = factory.getCommand("     ");

        assertEquals(IncorrectCommand.class, blankCommand.getClass());
    }

    @Test
    void testGetCommandWithNotExistingCommand() {
        Command notExistingCommand = factory.getCommand("deposit");

        assertEquals(IncorrectCommand.class, notExistingCommand.getClass());
    }

    @Test
    void testGetCommandWithCommandWithBlankParameters() {
        Command invalidCommand = factory.getCommand("buy  10");

        assertEquals(IncorrectCommand.class, invalidCommand.getClass());
    }

    @Test
    void testGetCommandWithDepositCommandWithoutParameters() {
        Command invalidDeposit = factory.getCommand("deposit-money ");

        assertEquals(IncorrectCommand.class, invalidDeposit.getClass());
    }

    @Test
    void testGetCommandWithValidDepositCommand() {
        Command validDeposit = factory.getCommand("deposit-money 32");

        assertEquals(DepositCommand.class, validDeposit.getClass());
    }

    @Test
    void testGetCommandWithValidListOfferingsCommand() {
        Command validListOfferings = factory.getCommand("list-offerings ");

        assertEquals(ListOfferingsCommand.class, validListOfferings.getClass());
    }

    @Test
    void testGetCommandWithBuyCommandWithoutParameters() {
        Command invalidBuy = factory.getCommand("buy ");

        assertEquals(IncorrectCommand.class, invalidBuy.getClass());
    }

    @Test
    void testGetCommandWithBuyCommandWithOneParameter() {
        Command invalidBuy = factory.getCommand("buy btc");

        assertEquals(IncorrectCommand.class, invalidBuy.getClass());
    }

    @Test
    void testGetCommandWithValidBuy() {
        Command validBuy = factory.getCommand("buy btc 32");

        assertEquals(BuyCommand.class, validBuy.getClass());
    }

    @Test
    void testGetCommandWithSellCommandWithoutParameters() {
        Command invalidSell = factory.getCommand("sell ");

        assertEquals(IncorrectCommand.class, invalidSell.getClass());
    }

    @Test
    void testGetCommandWithValidSell() {
        Command validSell = factory.getCommand("sell btc");

        assertEquals(SellCommand.class, validSell.getClass());
    }

    @Test
    void testGetCommandWithValidGetWalletSummary() {
        Command validWalletSummary = factory.getCommand("get-wallet-summary ");

        assertEquals(ActiveWalletSummaryCommand.class, validWalletSummary.getClass());
    }

    @Test
    void testGetCommandWithValidGetWalletOverallSummary() {
        Command validWalletOverallSummary = factory.getCommand("get-wallet-overall-summary ");

        assertEquals(OverallWalletSummaryCommand.class, validWalletOverallSummary.getClass());
    }


}
