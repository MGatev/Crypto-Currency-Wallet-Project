package bg.sofia.uni.fmi.mjt.wallet.server.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ExceptionLoggerTest {
    private static final String LOG_FILE = "exception_log.txt";

    @Test
    void testLog() throws IOException {
        Throwable exception = new NullPointerException("Test Exception!");
        ExceptionLogger.logExceptionToFile(exception);
        assertTrue(checkLogFileForMessage(exception.getMessage()), "Exception message not found in log file");
    }

    private boolean checkLogFileForMessage(String message) throws IOException {
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(message)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }
}
