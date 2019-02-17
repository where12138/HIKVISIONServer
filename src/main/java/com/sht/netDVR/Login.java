package com.sht.netDVR;

import com.sht.bean.Camera;
import com.sht.netDVR.HCNetSDK.NET_DVR_DEVICEINFO_V30;
import com.sun.jna.NativeLong;


public class Login {
	private NativeLong lUserID;
	private NET_DVR_DEVICEINFO_V30 lpDeviceinfo_V30;
	private String sDVRIP;
	private int port;
	private String sUserName;
	private String sPassword;
	private int error = 0;
	private boolean isLogin;
	
	/**
	 * 登录构造方法
	 * @param sDVRIP 摄像头IP地址
	 * @param port 摄像头端口号
	 * @param sUserName 登录用户名
	 * @param sPassword 登录密码
	 */
	public Login(String sDVRIP,int port,String sUserName,String sPassword) {
		this.lUserID = new NativeLong(-1);
		this.sDVRIP = sDVRIP;
		this.port = port;
		this.sUserName = sUserName;
		this.sPassword = sPassword;
	}
	
	/**
	 * 登录构造函数
	 * @param camera 摄像头相关信息
	 */
	public Login(Camera camera) {
		this.lUserID = new NativeLong(-1);
		this.sDVRIP = camera.getIp();
		this.sUserName = camera.getUser();
		this.port = camera.getPort();
		this.sPassword = camera.getPwd();
	}
	
	/**
	 * 退出登录
	 * @return
	 */
	public boolean logout() {
		if (HCNetSDK.INSTANCE.NET_DVR_Logout(this.lUserID)) {
			this.isLogin = false;
			return true;
		}else {
			this.error = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
			return false;
		}
	}
	
	
	/**
	 * @deprecated
	 * 退出摄像头登录
	 * @param lUserID 登录句柄（登录摄像头后返回的摄像头ID号）
	 * @return
	 */
	public boolean logout(NativeLong lUserID) {
		if (HCNetSDK.INSTANCE.NET_DVR_Logout(lUserID)) {
			this.isLogin=false;
			return true;
		}else {
			this.error = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
			return false;
		}
	}
	
	/**
	 * 登录摄像头
	 * @return
	 */
	public boolean login() {
		if (!this.isLogin) {
			this.lUserID = HCNetSDK.INSTANCE.NET_DVR_Login_V30(this.sDVRIP, (short)this.port, this.sUserName, this.sPassword,this.lpDeviceinfo_V30);
		}
		if (this.lUserID.longValue()>-1) {
			this.isLogin=true;
			return true;
		}else {
			this.error = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
			this.isLogin = false;
			return false;
		}
	}

	/**
	 * 登陆后返回的摄像头信息
	 * @return
	 */
	public NET_DVR_DEVICEINFO_V30 getLpDeviceinfo_V30() {
		return lpDeviceinfo_V30;
	}

	/**
	 * 登陆后的用户句柄（摄像头ID）
	 * @return
	 */
	public NativeLong getlUserID() {
		return lUserID;
	}

	/**
	 * 发生失败时返回的失败码
	 * @return
	 */
	public int getError() {
		return error;
	}
}
