package codechallenge;

import java.io.IOException;

/**
 * This is the main class where the server will be run from
 * it was built with the following requirement in mind:
 * The Application must accept input from at most 5 concurrent clients on TCP/IP port 4000
 */

public class Main {

    private static final int PORT = 4000;
    private static final int MAX_CONCURRENT_CLIENTS = 5;
    private static final String LOG_FILE_PATH = "numbers.log";

    public static void main(String args[]) throws IOException {
        System.out.println("Starting up server...");
         new Server(PORT, MAX_CONCURRENT_CLIENTS,LOG_FILE_PATH).run();
    }
}
