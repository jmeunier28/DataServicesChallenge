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

    private final int port;
    private final int numThreads;
    private final ExecutorService executor;

    /**
     * Netty Handler
     */
    private final NumberHandler numberHandler;
    private final LogData logData;
    private Channel channel;


    // Server Constructor
    Server(int port, int numThreads, String logFilePath) throws IOException {
        this.port = port;
        this.numThreads = numThreads;
        logData = new LogData(logFilePath);
        this.numberHandler = new NumberHandler(this, logData);
        this.executor = startExecutor();
    }

    private ExecutorService startExecutor() {
        // getting updates on the data intake by running on a dead simple scheduled executor service
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable scheduledTask = () -> {
            try {
                DataProcessor data = numberHandler.processData();
                System.out.printf(
                        "Received %s unique numbers, %s duplicates.  Unique total: %s\n", data.getUnique()
                        , data.getDuplicates(), data.getTotal());

            }
            catch (IOException exe) {
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
        EventLoopGroup workerGroup = new NioEventLoopGroup(numThreads);
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
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();
            channel = f.channel();

            channel.closeFuture().sync();
        }
        catch (InterruptedException e) {
            System.err.println("sync() was interrupted");
            e.printStackTrace();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    void shutDown() {
        // shut this guy down now and close the boss channel
        executor.shutdown();
        channel.close();
    }

}
