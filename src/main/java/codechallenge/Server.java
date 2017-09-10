package codechallenge;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    /** Port on which server will listen */
    private final int port;

    /** Number of worker threads used to handle input */
    private final int workerThreads;

    //private final ExecutorService reporterExecutor;
    private final NumberHandler numberHandler;

    // Server Constructor
    Server(int port, int workerThreads, String logFilePath) throws IOException {
        this.port = port;
        this.workerThreads = workerThreads;
        numberHandler = new NumberHandler(this, logFilePath);
        //reporterExecutor = startReporter();
    }

    /**
     * Starts the periodic reporting thread (to STDOUT)
     *
     * @return The ExecutorService used to run the reporting thread.
     */
    private ExecutorService startReporter() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable scheduledTask = () -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Hello " + threadName);
//            PeriodStats stats = inputHandler.getAndResetStats();
//            System.out.printf(
//                    "Received %s unique numbers, %s duplicates.  Unique total: %s\n",
//                    stats.getUnique(), stats.getDuplicates(), stats.getUniqueTotal());

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
        EventLoopGroup workerGroup = new NioEventLoopGroup();
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

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.err.println("sync() was interrupted");
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
