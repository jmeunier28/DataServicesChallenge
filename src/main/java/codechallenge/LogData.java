package codechallenge;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class just logs the data in a thread safe way bc many
 * different threads will be accessing it at once
 */
public class LogData {

    private FileHandler file;
    private Logger logger;

    LogData(String logFilePath) throws IOException {

        // Set up the logging stuff using the native logger
        this.file = new FileHandler(logFilePath);
        this.logger = Logger.getLogger("codechallenge");
        logger.addHandler(file);
    }

    // synchronized logging
    synchronized void log(String number) {
        logger.log(Level.ALL, number);
    }
}
