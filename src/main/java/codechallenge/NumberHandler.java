package codechallenge;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**

 The number handler is a Netty handler object which handles all the
 I/O events for this application it writes all recved numbas to a
 thread safe Logger

 */

@ChannelHandler.Sharable
public class NumberHandler extends ChannelInboundHandlerAdapter {

    private Server server;
    private Logger logger;
    private FileHandler file;


    // Constructor for Handler
    NumberHandler(Server server, String logFilePath) throws IOException {
        this.server = server;
        this.file = new FileHandler(logFilePath);

        // Use the built in java logger bc i dont like log4j and this is fine
        this.logger = Logger.getLogger("codechallenge");
        logger.addHandler(file);
        logger.info("Starting App..\n");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            String inputStream = in.toString(io.netty.util.CharsetUtil.US_ASCII);
            if (isValidData(inputStream)) {
                // now process the data

            } else {
                if (isTerminated(inputStream)) {
                    // Shut that sucker down
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

    // Log the data to numbers.log
    synchronized void log(String inputStream) {
        logger.info(inputStream);
    }
}

