package com.sht;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;
import com.sht.bean.Camera;
import com.sht.config.CameraConfig;
import com.sht.netDVR.CameraDispatcher;
import com.sht.netDVR.InitHCSDK;
import com.sht.web.ServerInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class Service {

	public static final boolean SSL = System.getProperty("ssl") !=null;
	public static final int PORT = Integer.parseInt(System.getProperty("port",SSL? "8443":"12346"));
	public static final Map<String, Camera> cameras = CameraConfig.getCameras();
	public static final BlockingQueue<JSONObject> jsonObjects = new ArrayBlockingQueue<>(50);
	
	public static void main(String[] args) throws Exception{
		//Map<String, Camera> cameras = CameraConfig.getCameras();
		//BlockingQueue<JSONObject> jsonObjects = new ArrayBlockingQueue<>(50);
		
		InitHCSDK initHCSDK = new InitHCSDK();
		
		if (!initHCSDK.init()) {
			System.out.println("init hcsdk failed,code:"+initHCSDK.getErrorCode());
		}else {
			//TODO 这里实现推送总线程
			CameraDispatcher dispatcher = new CameraDispatcher(jsonObjects, cameras);
			dispatcher.setName("CameraDispatcher");
			dispatcher.start();
		}
		
		//SSL 设置
		final SslContext sslContext;
		if (SSL) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslContext = SslContextBuilder.forServer(ssc.certificate(),ssc.privateKey()).build();
		}else {
			sslContext = null;
		}
		
		//server 设置
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);//单线程
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
			bootstrap.group(bossGroup,workGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ServerInitializer(sslContext));
			
			Channel channel = bootstrap.bind(PORT).channel();
			System.err.println("server at "+(SSL? "https":"http"+"://127.0.0.1:"+PORT+"/"));
			channel.closeFuture().sync();
			System.err.println("after service service shutdown");
		} finally {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
		
		if (!initHCSDK.cleanup()) {
			System.err.println("cleanup hcsdk failed,code:"+initHCSDK.getErrorCode());
		}else {
			System.err.println("cleanup success");
			
		}
		
	}

}
