package codechallenge;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;

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
    private String logFilePath;
    private String inputStream;


    // Constructor for Handler
    NumberHandler(Server server, String logFilePath) throws IOException {
        this.server = server;
        this.logFilePath = logFilePath;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            this.inputStream = in.toString(io.netty.util.CharsetUtil.US_ASCII);
            if (isValidData(inputStream)) {
                // now process the data
                try {
                    processData();
                } catch (IOException exe) {
                    System.out.println(exe);
                }
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

    void processData() throws IOException {
        new DataProcessor(inputStream, logFilePath).parseData();
    }
}

