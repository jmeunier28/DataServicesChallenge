package codechallenge;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    /** Port on which server will listen */
    private final int port;

    /** Number of worker threads used to handle input */
    private final int workerThreads;

    /** Executor Service to get stats on processed data */
    private final ExecutorService reporterExecutor;

    /** Netty Handler */
    private final NumberHandler numberHandler;


    /** LogData is a thread safe logger **/
    private final LogData logData;

    /** This is the channel that all the things listen to **/
    private Channel channel;

    private boolean isBound;


    // Server Constructor
    Server(int port, int workerThreads, String logFilePath) throws IOException {
        this.port = port;
        this.workerThreads = workerThreads;
        logData = new LogData(logFilePath);
        this.numberHandler = new NumberHandler(this, logData);
        this.reporterExecutor = startReporter();
    }

    private ExecutorService startReporter() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable scheduledTask = () -> {
            try {
                DataProcessor data = numberHandler.processData();
                System.out.printf(
                        "Received %s unique numbers, %s duplicates.  Unique total: %s\n", data.getUnique()
                        , data.getDuplicates(), data.getTotal());

            } catch (IOException exe) {
                System.out.println(exe);
            }

            System.out.flush();
        };
        scheduler.scheduleAtFixedRate(scheduledTask, 0, 10, TimeUnit.SECONDS);
        return scheduler;
    }


    void run() {

        // All the Netty business
        /** Accepts an incoming connection */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        /**
            Handles the traffic of the accepted connection
            once the boss accepts the connection and registers
            the accepted connection to the worker
        */
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(numberHandler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();
            isBound = true;
            channel = f.channel();

            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            System.err.println("sync() was interrupted");
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    boolean isBound() {
        return isBound;
    }

    void shutDown() {
        // shut this guy down now and close the boss channel
        reporterExecutor.shutdown();
        channel.close();
        isBound = false;
    }

}
