package com.sht.netDVR;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sht.bean.Camera;
import com.sht.bean.Command;
import com.sht.bean.FrameBean;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;


public class Realplay extends Thread{
	
	
	private Camera camera;
	private NativeLong lRealPlayHandle;
	private NativeLong lUserID;
	private RealDataCallBack realDataCallBack;
	private DecodeCallBack fDecCBFun;
	private NativeLongByReference m_lPort;
	private BlockingQueue<FrameBean> beans;
	private Login login;
	private int error = 0;
	private long timeTag;
	private volatile boolean realplayRuns;
	
	public Realplay(Camera camera,Login login) {
		// TODO Auto-generated constructor stub
		this.camera = camera;
		this.beans = new ArrayBlockingQueue<>(250);
		this.login = login;
		this.timeTag = (long) System.currentTimeMillis()/1000;
	}
	
	public void quit() {
		this.realplayRuns = false;
		System.err.println("quit realplay:"+this.getName());
		interrupt();
	}
	
	public long getTimeTag() {
		return timeTag;
	}


	public void setTimeTag(long timestamp) {
		if(this.timeTag>timestamp) {
			long tempTag = (long) System.currentTimeMillis()/1000;
			if(tempTag>this.timeTag) {
				this.timeTag = tempTag;
			}
		}else {
			this.timeTag = timestamp;
		}
	}


	public NativeLong getlRealPlayHandle() {
		return lRealPlayHandle;
	}

	public Login getLogin() {
		return login;
	}
	
	public int getError() {
		return error;
	}

	public Camera getCamera() {
		return this.camera;
	}


	public boolean play() {
		
		if (this.login.login()) {
			this.lUserID = this.login.getlUserID();
		}else {
			this.error = this.login.getError();
			return false;
		}
		
		this.m_lPort = new NativeLongByReference();
		this.fDecCBFun = new DecodeCallBack(beans);
		this.realDataCallBack = new RealDataCallBack(this.fDecCBFun, this.m_lPort);
		this.lRealPlayHandle = HCNetSDK.INSTANCE.NET_DVR_RealPlay_V30(this.lUserID, this.camera.getClientinfo(), this.realDataCallBack, null, true);
		//System.out.println("NET_DVR_RealPlay_V30 error,code:"+HCNetSDK.INSTANCE.NET_DVR_GetLastError());
		if (this.lRealPlayHandle.longValue()<0) {
			this.error = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
			return false;
		}else {
			return true;
		}
	}
	
	
	@Override
	public void run() {
		// TODO 实现预览、推流、操纵
		try {
			//推送线程
			RtmpPush push = new RtmpPush(camera.getOutput(), this.beans);
			push.start();
			boolean isplay = play();
			if(isplay) {
				while (true) {
					Command command = this.camera.getBlockingQueue().poll(30, TimeUnit.SECONDS);
					if(command==null) {
						if (!push.isAlive()) {
							//出了某种异常导致推送线程已经结束
							push.join();
							break;
						}
					}else if (command.getCode()==0) {
						push.quit();
						push.join();
						break;
					}else {
						//TODO 操纵摄像头
						HCNetSDK.INSTANCE.NET_DVR_PTZControl(this.lRealPlayHandle, command.getCode(),command.getOperation());
					}
				}
			}
			
		}catch (InterruptedException e) {
			// TODO: handle exception
			if (!this.realplayRuns) {
				System.err.println("interrupt:"+this.getName());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			//TODO: 释放播放库资源、停止预览、注销设备异常未处理。
			if (this.m_lPort!=null) {
				try {
					if(!HCNetSDK.INSTANCE.NET_DVR_StopRealPlay(this.lRealPlayHandle)) {
						System.err.println("NET_DVR_StopRealPlay error,code:"+HCNetSDK.INSTANCE.NET_DVR_GetLastError());
						this.error = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
					}
					if(!PlayCtrl.INSTANCE.PlayM4_Stop(this.m_lPort.getValue())) {
						System.err.println("PLAYM4_Stop error,code:"+PlayCtrl.INSTANCE.PlayM4_GetLastError(this.m_lPort.getValue()));
						this.error = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
					}
					if(!PlayCtrl.INSTANCE.PlayM4_CloseStream(this.m_lPort.getValue())) {
						System.err.println("PlayM4_CloseStream error,code:"+PlayCtrl.INSTANCE.PlayM4_GetLastError(this.m_lPort.getValue()));
						this.error = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
					}
					if(!PlayCtrl.INSTANCE.PlayM4_FreePort(this.m_lPort.getValue())) {
						System.err.println("PlayM4_FreePort error,code:"+PlayCtrl.INSTANCE.PlayM4_GetLastError(this.m_lPort.getValue()));
						this.error = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
					}
				}catch (Exception e) {
					// TODO: handle exception
					System.err.println("NET_DVR or PlayM4 errors");
				}
			}
			this.login.logout();
		}
	}
}
