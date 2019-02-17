package com.sht.bean;

import java.util.concurrent.BlockingQueue;

import com.sht.netDVR.HCNetSDK.NET_DVR_CLIENTINFO;
import com.sun.jna.NativeLong;

public class Camera {
	private String alias;
	private String output;
	private String ip;
	private String user;
	private String pwd;
	private int port;
	private BlockingQueue<Command> blockingQueue;
	private NET_DVR_CLIENTINFO clientinfo;
	
	/**
	 * 摄像头相关信息构造函数
	 * @param alias 摄像头名
	 * @param output 输出地址或文件
	 * @param ip 摄像头ip地址
	 * @param user 登录用户名
	 * @param pwd 登录密码
	 * @param port 摄像头端口
	 * @param blockingQueue 阻塞消息队列
	 * @param clientinfo 摄像头信息
	 */
	public Camera(String alias,String output,String ip,String user,String pwd,int port,BlockingQueue<Command> blockingQueue,NET_DVR_CLIENTINFO clientinfo) {
		this.alias = alias;
		this.output = output;
		this.ip = ip;
		this.user = user;
		this.pwd = pwd;
		this.port = port;
		this.blockingQueue = blockingQueue;
		this.clientinfo = clientinfo;
	}
	
	
	/**
	 * 摄像头相关信息构造函数
	 * @param alias 摄像头名
	 * @param output 输出文件或地址
	 * @param ip 摄像头IP地址
	 * @param user 登录用户名
	 * @param pwd 登录密码
	 * @param port 摄像头端口号
	 * @param blockingQueue 阻塞队列
	 * @param channel 摄像头播放通道
	 * @param linkmode 摄像头连接模式最高位(31)为0表示主码流，为1表示子码流；0～30位表示连接方式：0－TCP方式，1－UDP方式，2－多播方式
	 */
	public Camera(String alias,String output,String ip,String user,String pwd,int port,BlockingQueue<Command> blockingQueue,int channel,long linkmode) {
		this.alias = alias;
		this.output = output;
		this.ip = ip;
		this.user = user;
		this.pwd = pwd;
		this.port = port;
		this.blockingQueue = blockingQueue;
		this.clientinfo = new NET_DVR_CLIENTINFO();
		this.clientinfo.hPlayWnd = null;
		this.clientinfo.lChannel = new NativeLong(channel);
		this.clientinfo.lLinkMode = new NativeLong(linkmode);
		this.clientinfo.sMultiCastIP = null;
	}
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public BlockingQueue<Command> getBlockingQueue() {
		return blockingQueue;
	}
	public void setBlockingQueue(BlockingQueue<Command> blockingQueue) {
		this.blockingQueue = blockingQueue;
	}
	public NET_DVR_CLIENTINFO getClientinfo() {
		return clientinfo;
	}
	public void setClientinfo(NET_DVR_CLIENTINFO clientinfo) {
		this.clientinfo = clientinfo;
	}
	
	
}
