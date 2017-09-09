package codechallenge;

import java.io.IOException;

public class Server {

    /** Port on which server will listen */
    private final int port;

    /** Number of worker threads used to handle input */
    private final int workerThreads;

    /**
     * The Netty handler object which implements the input handling logic.
     *
     * Need to store a reference to it in this field so that it can be queried
     * for stats by the periodic reporting thread - see startReporter().
     */
    //private final SocketInputHandler inputHandler;

    // constructor
    Server(int port, int workerThreads, String logFilePath) throws IOException {
        this.port = port;
        this.workerThreads = workerThreads;
        //inputHandler = new SocketInputHandler(this, logFilePath);
        //reporterExecutor = startReporter();
    }
    void run() {
        System.out.println("I am running now");
    }
}
