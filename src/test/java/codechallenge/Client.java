package codechallenge;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Will use this to test the server code
 */
public class Client {

    private static int n = 100000000;
    private final static Random random = new Random(); // random numba gen
    private static final int PORT = 4000;
    private static final String HOST = "localhost";


    private static void startClients() {
        // using 100 threads in my pool to start with
        System.out.println("starting...");
        ExecutorService executor = Executors.newFixedThreadPool(100);
        Runnable task = () -> {
          // open a socket and send some data to it
            try {
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(HOST, PORT));
                while (true) {
                    // create ByteBuffer by wrapping the random number generator
                    socketChannel.write(ByteBuffer.wrap(generateNumbers().getBytes()));
                    Thread.sleep(75);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i< 100; i++) {
            // submit each task to the executor service
            executor.submit(task);
        }
    }

    public static void main(String args[]) throws IOException {
        startClients();
    }

    private static String generateNumbers() {
        int input =  random.nextInt(n);
        return String.format("%09d\n", input);
    }

}
