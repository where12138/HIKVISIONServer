package com.sht.web;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslContext;
	
	public ServerInitializer(SslContext sslContext) {
		this.sslContext = sslContext;
	}
	
	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		ChannelPipeline pipeline = arg0.pipeline();
		if (this.sslContext!=null) {
			pipeline.addLast(this.sslContext.newHandler(arg0.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());//HTTP 服务解码器
		pipeline.addLast(new HttpObjectAggregator(2048));//HTTP 消息合并处理
		pipeline.addLast(new CameraHandler());//自己的业务控制器
	}

}
