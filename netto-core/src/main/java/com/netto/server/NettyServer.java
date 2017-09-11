package com.netto.server;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.netto.filter.InvokeMethodFilter;
import com.netto.server.handler.NettyServerHandler;
import com.netto.server.handler.NettyServerJsonHandler;
import com.netto.util.Constants;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;

public class NettyServer implements InitializingBean {
	private static Logger logger = Logger.getLogger(NettyServer.class);
	private int port = 12345;
	private Map<String, Object> serviceBeans;
	private List<InvokeMethodFilter> filters;
	private int numWorkerThreads = 16;
	private int maxRequestSize = 1024*1024;

	public int getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(int maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public int getNumWorkerThreads() {
        return numWorkerThreads;
    }

    public void setNumWorkerThreads(int numWorkerThreads) {
        this.numWorkerThreads = numWorkerThreads;
    }

    public NettyServer(int port, Map<String, Object> serviceBeans) {
		this.port = port;
		this.serviceBeans = serviceBeans;
	}

	public Map<String, Object> getServiceBeans() {
		return serviceBeans;
	}

	public List<InvokeMethodFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<InvokeMethodFilter> filters) {
		this.filters = filters;
	}

	public void afterPropertiesSet() throws Exception {
		this.run();
	}

	private void run() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1); // (1)
		EventLoopGroup workerGroup = new NioEventLoopGroup(numWorkerThreads);
		try {
			ServerBootstrap b = new ServerBootstrap(); // (2)
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) // (3)
					.option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChannelInitializer<SocketChannel>() { // (4)
						@Override
						public void initChannel(SocketChannel ch) throws Exception {

							ChannelPipeline p = ch.pipeline();
                            p.addLast("framer",
                                    new DelimiterBasedFrameDecoder(maxRequestSize, Constants.delimiterAsByteBufArray()));							
							p.addLast("handler",new NettyServerJsonHandler(serviceBeans, filters));
//							p.addLast("handler",new NettyServerJsonHandler(serviceBeans, filters));
						}
					});

			// Bind and start to accept incoming connections.
			ChannelFuture f = b.bind(this.port).sync(); // (7)

			logger.info("server bind port:" + this.port);

			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();
		} finally {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}
