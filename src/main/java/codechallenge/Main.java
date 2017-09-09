package codechallenge;

import java.io.IOException;

public class Main {

    private static final int PORT = 4000;
    private static final int MAX_CONCURRENT_CLIENTS = 5;
    private static final String LOG_FILE_PATH = "numbers.log";

    public static void main(String args[]) throws IOException {
        System.out.println("Starting up server...");
         new Server(PORT, MAX_CONCURRENT_CLIENTS,LOG_FILE_PATH).run();
    }
}
