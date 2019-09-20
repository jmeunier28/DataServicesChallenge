package codechallenge;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The number handler is a Netty handler object which handles all the
 * I/O events for this application it writes all valid messages to the Data Processor
 *
 * Keeping in mind:
 * 1) Any data that does not conform to a valid line of input should
 * be discarded and the client connection terminated immediately and without comment
 * AND
 * 2) If any connected client writes a single line with only the word "terminate" followed
 * by a server-native newline sequence, the Application must disconnect all clients
 * and perform a clean shutdown as quickly as possible
 */

@ChannelHandler.Sharable
public class NumberHandler extends ChannelInboundHandlerAdapter {

    private Server server;
    private StringBuilder inputStream = new StringBuilder(10);
    private String blah;
    private LogData logData;

    private AtomicInteger duplicates = new AtomicInteger(0);
    private AtomicInteger uniqueNumbers = new AtomicInteger(0); // AtomicInteger
    private BitSet numbers = new BitSet(1_000_000_000);
    private AtomicInteger totalNum = new AtomicInteger(0);
    private RingBuffer<ValueEvent> ringBuffer;
    private static ByteBuffer TERMINATE_STRING = ByteBuffer.wrap("terminate\n".getBytes());
//    private HashSet<Integer> numbers = new HashSet<>(); // HashSet has O(1) add and already does the dedup work for me


    // Constructor for Handler
    NumberHandler(Server server, LogData logData) throws IOException {
        this.server = server;
        this.logData = logData;

        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;

        WaitStrategy waitStrategy = new BusySpinWaitStrategy();
        Disruptor<ValueEvent> disruptor
                = new Disruptor<>(
                ValueEvent.EVENT_FACTORY,
                1048576,
                threadFactory,
                ProducerType.SINGLE,
                waitStrategy);

        disruptor.handleEventsWith(new NumberProcessor());
        this.ringBuffer = disruptor.start();

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
//            inputStream.append(in.toString(io.netty.util.CharsetUtil.US_ASCII).trim());
//            int input = Integer.parseInt(inputStream.getChars(););
            blah = in.toString(CharsetUtil.US_ASCII).trim();

            // check if term:
            maybeTerminated(blah);

            int input = Integer.parseInt(blah);
            // put it in the ring buffer to be processed
            long sequenceId = ringBuffer.next();
            ValueEvent valueEvent = ringBuffer.get(sequenceId);
            valueEvent.setValue(input);
            ringBuffer.publish(sequenceId);
        }
        catch (NumberFormatException numExe) {
            // If the data is not valid then just junk that client thread
            ctx.close();
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void maybeTerminated(String input) {
        if (input.equals("terminate")) {
            server.shutDown();
        }
    }

    DataProcessor processData() throws IOException {
        // return the stats from this reporting period
        return new DataProcessor(
                // every reporting period we want to see the counts for
                // dups and unique numbers back to 0
                duplicates.getAndSet(0),
                uniqueNumbers.getAndSet(0),
                totalNum.get());
    }

    // method to increment
    private void incrementDuplicates() {
        duplicates.getAndIncrement();
    }

    // method to increment unique numbers
    private void incrementNumbers() {
        uniqueNumbers.getAndIncrement();
    }

    // method to increment unique numbers
    private void incrementTotal() {
        totalNum.getAndIncrement();
    }

    public class NumberProcessor implements EventHandler<ValueEvent> {
        @Override
        public void onEvent(ValueEvent event, long sequence, boolean endOfBatch) throws Exception {
            final int value = event.getValue();
            // wrap in a synch block to ensure thread safety
            synchronized (this) {
                try {
                    if (numbers.get(value)) {
                        // else we should increment duplicate count
                        incrementDuplicates();
                    }
                    else {
                        // if the numbers could be added they are unique so we should log and increment unique
                        numbers.set(value, true);
                        incrementNumbers();
                        incrementTotal();
                        // log dat data
//                    logData.log(inputStream);
                    }
                }
                catch (Exception exe) {
                    System.out.println(exe);
                }
            }

        }

    }

}

