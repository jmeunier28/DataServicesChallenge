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

    private int duplicates;
    private int uniqueNumbers; // AtomicInteger
    private int totalNumbers; // HashSet has O(1) add and already does the dedup work for me

    DataProcessor(int duplicates, int uniqueNumbers, int totalNumbers) throws IOException {
        this.duplicates = duplicates;
        this.uniqueNumbers = uniqueNumbers;
        this.totalNumbers = totalNumbers;
    }

    // gets the number of duplicates that have been sent
    int getDuplicates() {
        return duplicates;
    }

    // gets the number of unique numbers that have been sent
    int getUnique() {
        return uniqueNumbers;
    }

    // total num of unique numbers is the size of the hash set
    int getTotal() {
        return totalNumbers;
    }

}
