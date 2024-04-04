package bg.sofia.uni.fmi.mjt.wallet.server.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ExceptionLogger {

    private static final String FILE_PATH = "exception_log.txt";

    public static void logExceptionToFile(Throwable exception) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            writer.println("Exception Message:");
            writer.println(exception.getMessage());
            writer.println();

            writer.println("Stack Trace:");
            exception.printStackTrace(writer);
        } catch (IOException except) {
            System.err.println("Error writing exception to file: " + except.getMessage());
        }
    }
}
