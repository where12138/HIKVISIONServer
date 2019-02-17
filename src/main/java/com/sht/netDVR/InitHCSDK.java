package com.sht.netDVR;

/**
 * 
 * @author 王天宝
 * 初始化、注销视频sdk
 * 初始化、注销均可多次调用
 *
 */
public class InitHCSDK {
	private int errorCode = 0;
	private boolean isInit,isCleanup;
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		if(!HCNetSDK.INSTANCE.NET_DVR_Init()) {
			this.errorCode = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
			isInit = false;
			isCleanup = true;
			return false;
		}else {
			this.isInit = true;
			this.isCleanup = false;
			return true;
		}
		
	}
	
	/**
	 * 注销
	 * @return
	 */
	public boolean cleanup() {
		if(!HCNetSDK.INSTANCE.NET_DVR_Cleanup()) {
			this.errorCode = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
			if (this.errorCode==3) {
				this.isCleanup = false;
				this.isInit = false;
			}else {
				this.isCleanup = false;
				this.isInit = true;
			}
			return false;
		}else {
			this.isCleanup = true;
			this.isInit = false;
			return true;
		}
		
	}
	
	public boolean isInit() {
		return this.isInit;
	}
	public boolean isCleanup() {
		return this.isCleanup;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
