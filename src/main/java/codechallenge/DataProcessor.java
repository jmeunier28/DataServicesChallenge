package codechallenge;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * This class will process all of the data and log it to the
 * default java logger in a thread safe way
 *
 */
public class DataProcessor {

    String inputStream;
    private Logger logger;

    private AtomicInteger duplicates;
    private AtomicInteger uniqueNumbers; // AtomicInteger
    private HashSet<Integer> numbers; // HashSet has O(1) add and already does the dedup work for me


    DataProcessor(String inputStream, String logFilePath) throws IOException {
        this.inputStream = inputStream;
        this.logger = Logger.getLogger("codechallenge");
        FileHandler file = new FileHandler(logFilePath);
        logger.addHandler(file);
        logger.info("Starting App..\n");
        this.duplicates.getAndSet(0);
        this.uniqueNumbers.getAndSet(0);
    }

    DataProcessor parseData() {
        Integer input = Integer.parseInt(inputStream);
        // if this can be added to the set then we are good to log it
        if (numbers.add(input)) {
            logData();
            incrementNumbers();
        } else {
            incrementDuplicates();
        }
        return null;
    }

    synchronized void logData() {
        logger.info(inputStream);
    }

    // method to increment
    void incrementDuplicates() {
        duplicates.getAndIncrement();
    }

    // method to increment unique numbers
    void incrementNumbers() {
        uniqueNumbers.getAndIncrement();
    }

    // gets the number of duplicates that have been sent
    int getDuplicates() {
        return duplicates.get();
    }

    // gets the number of unique numbers that have been sent
    protected int getUnique() {
        return uniqueNumbers.get();
    }

    // total num of unique numbers is the size of the hash set
    protected int getTotal() {
        return numbers.size();
    }
}
