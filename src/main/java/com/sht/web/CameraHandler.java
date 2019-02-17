package com.sht.web;


import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sht.Service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
public class CameraHandler extends ChannelInboundHandlerAdapter {
	private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
	private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");
    private static final AsciiString ACCESS_CONTROL_ALLOW_ORIGIN = new AsciiString("Access-Control-Allow-Origin");
    private static final AsciiString ACCESS_CONTROL_ALLOW_METHODS = new AsciiString("Access-Control-Allow-Methods");
    private static final AsciiString ACCESS_CONTROL_ALLOW_HEADERS = new AsciiString("Access-ControlAAllow-Headers");
    private static final AsciiString ACCESS_CONTROL_MAX_AGE = new AsciiString("Access-Control-Max-Age");
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    	ctx.flush();
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	if (msg instanceof FullHttpRequest) {
			FullHttpRequest request = (FullHttpRequest) msg;
			JSONObject response = new JSONObject();
			
			JSONObject requestJson =null;
			try {
				requestJson = JSONObject.parseObject(parseJsonRequest(request));
				//System.out.println("test,json:"+requestJson);
			} catch (Exception e) {
				responseJson(ctx, request, "error json");
				return;
			}
			
			if (request.method() == HttpMethod.POST) {
				if (request.uri().equals("/showAll")) {
					//TODO 显示所有配置摄像头
					Map<String, Object> map = new HashMap<>();
					map.putAll(Service.cameras);
					response.put("all", new JSONObject(map));
				}else if (request.uri().equals("/camera")) {
					//TODO 向控制队列发送命令
					String cameraName = requestJson.getString("camera");
					if (Service.cameras.get(cameraName)!=null) {
						try {
							JSONObject jObject = new JSONObject();
							jObject.put("alias", cameraName);
							if (requestJson.containsKey("command")) {
								jObject.put("command", requestJson.getJSONObject("command"));
							}
							Service.jsonObjects.put(jObject);
							response.put("success", "success to send command");
						} catch (Exception e) {
							System.err.println(e.getStackTrace());
						}
					}else {
						response.put("error", "403,Camera not found");
					}
					
				}else if (request.uri().equals("/jsonTest")) {
					
					responseJson(ctx, request, requestJson.toString());
				}else {
					response.put("error", "404,Page not found");
				}
			}else {
				response.put("error", "405,Method not allowed");
			}
			
			responseJson(ctx, request, response.toString());
    	}
    }
    
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	cause.printStackTrace();
    	ctx.close();
    }
    
    
    private void responseJson(ChannelHandlerContext ctx,FullHttpRequest req,String jsonStr)
    {
    	boolean keepAlive = HttpUtil.isKeepAlive(req);
    	byte[] jsonByteByte = jsonStr.getBytes();
    	FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,Unpooled.wrappedBuffer(jsonByteByte));
    	response.headers().set(CONTENT_TYPE,"text/json");
    	response.headers().setInt(CONTENT_LENGTH,response.content().readableBytes());
    	//允许跨域访问
    	response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS,"Content-Type");
    	response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
    	response.headers().set(ACCESS_CONTROL_ALLOW_METHODS,"GET, POST, PUT,DELETE");
    	response.headers().set(ACCESS_CONTROL_MAX_AGE,590);
    	
    	if (!keepAlive) {
			ctx.write(response).addListener(ChannelFutureListener.CLOSE);
		}else {
			response.headers().set(CONNECTION,KEEP_ALIVE);
			ctx.write(response);
		}
    }
    
    /**
     * 将request对象转为json对象
     * @param request
     * @return
     */
    private String parseJsonRequest(FullHttpRequest request) {
    	ByteBuf jsonBuf = request.content();
    	String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
    	return jsonStr;
    }
    
}
