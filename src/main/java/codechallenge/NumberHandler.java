package codechallenge;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**

 The number handler is a Netty handler object which handles all the
 I/O events for this application it writes all valid messages to the Data Processor

 Keeping in mind:
 1) Any data that does not conform to a valid line of input should
 be discarded and the client connection terminated immediately and without comment
 AND
 2) If any connected client writes a single line with only the word "terminate" followed
 by a server-native newline sequence, the Application must disconnect all clients
 and perform a clean shutdown as quickly as possible

 */

@ChannelHandler.Sharable
public class NumberHandler extends ChannelInboundHandlerAdapter {

    private Server server;
    private String inputStream;
    private LogData logData;

    private AtomicInteger duplicates = new AtomicInteger(0);
    private AtomicInteger uniqueNumbers = new AtomicInteger(0); // AtomicInteger
    private HashSet<Integer> numbers = new HashSet<>(); // HashSet has O(1) add and already does the dedup work for me


    // Constructor for Handler
    NumberHandler(Server server, LogData logData) throws IOException {
        this.server = server;
        this.logData = logData;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            inputStream = in.toString(io.netty.util.CharsetUtil.US_ASCII);
            if (isValidData(inputStream)) {
                // now process the data
                incrementSet(inputStream);
            } else {
                if (isTerminated(inputStream)) {
                    // Shut that sucker down
                    server.shutDown();
                } else {
                    // If the data is not valid then just junk that client thread
                    ctx.close();
                }
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    // isTerminated checks if the msg sent was meant to close a conn
    static boolean isTerminated(String input) {
        // Easy Regex to tell if terminate string has been recv
        return input.matches("^terminate\\r?\\n$");
    }

    // isValidData Checks
    static boolean isValidData(String input) {
        // Easy Regex to make sure that the data sent was valid
        return input.matches("^\\d{9}\\r?\\n$");
    }

    DataProcessor processData() throws IOException {
        // return the stats from this reporting period
        return new DataProcessor(
                // every reporting period we want to se the counts for
                // dups and unique numbers back to 0
                duplicates.getAndSet(0),
                uniqueNumbers.getAndSet(0),
                numbers.size());
    }

    void incrementSet(String inputStream) {
        int input = Integer.parseInt(inputStream.trim());
        if (numbers.add(input)) {
            // if the numbers could be added they are unique so we should log and increment unique
            incrementNumbers();
            // log dat data
            logData.log(inputStream);
        } else {
            // else we should increment duplicate count
            incrementDuplicates();
        }
    }

    // method to increment
    void incrementDuplicates() {
        duplicates.getAndIncrement();
    }

    // method to increment unique numbers
    void incrementNumbers() {
        uniqueNumbers.getAndIncrement();
    }

}

