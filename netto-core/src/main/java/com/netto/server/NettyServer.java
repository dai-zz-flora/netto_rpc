package com.netto.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.netto.filter.InvokeMethodFilter;
import com.netto.server.bean.NettoServiceBean;
import com.netto.server.bean.ServiceBean;

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

import io.netty.handler.codec.bytes.ByteArrayDecoder;

public class NettyServer implements InitializingBean , ApplicationContextAware {
	private static Logger logger = Logger.getLogger(NettyServer.class);
	private int port = 12345;

	private List<InvokeMethodFilter> filters;
	private int numWorkerThreads = 16;
	private int maxRequestSize = 1024*1024;
    private ApplicationContext applicationContext;
    private Map<String, Object> refBeans;
    private Map<String, NettoServiceBean> serviceBeans;
    
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

    public NettyServer(int port) {
		this.port = port;
	
	}



	public List<InvokeMethodFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<InvokeMethodFilter> filters) {
		this.filters = filters;
	}

	public void afterPropertiesSet() throws Exception {
	    this.serviceBeans = new HashMap<String, NettoServiceBean>();
        if (this.refBeans == null) {
            Map<String, ServiceBean> temps = this.applicationContext.getBeansOfType(ServiceBean.class);
            for (String key : temps.keySet()) {
                ServiceBean bean = temps.get(key);
                NettoServiceBean factoryBean = new NettoServiceBean(bean,
                        this.applicationContext.getBean(bean.getRef()));
                this.serviceBeans.put(bean.getRef(), factoryBean);
            }

        } else {
            for (String key : this.refBeans.keySet()) {
                ServiceBean bean = new ServiceBean();
                bean.setRef(key);
                NettoServiceBean factoryBean = new NettoServiceBean(bean, this.refBeans.get(key));
                this.serviceBeans.put(key, factoryBean);
            }

        }

        
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
                            p.addLast("decoder",new ByteArrayDecoder());                            
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


    
    
    public void setRefBeans(Map<String, Object> refBeans) {
               this.refBeans = refBeans;
       }
        
           @Override
           public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
               this.applicationContext = applicationContext;
           }

}
