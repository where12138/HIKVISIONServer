package com.sht.netDVR;

import com.sht.netDVR.HCNetSDK.FRealDataCallBack_V30;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.NativeLongByReference;

public class RealDataCallBack implements FRealDataCallBack_V30 {

	private NativeLongByReference m_lPort;
	private DecodeCallBack fDecCBFun;
	
	/**
	 * 预览回调
	 * @param fDecCBFun 解码回调函数
	 * @param m_lPort 播放通道号
	 */
	public RealDataCallBack(DecodeCallBack fDecCBFun,NativeLongByReference m_lPort) {
		this.fDecCBFun = fDecCBFun;
		this.m_lPort = m_lPort;
	}
	
	@Override
	public void invoke(NativeLong lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
		// TODO Auto-generated method stub
		switch (dwDataType) {
		case HCNetSDK.NET_DVR_SYSHEAD:
			if (!PlayCtrl.INSTANCE.PlayM4_GetPort(this.m_lPort)) //获取播放库未使用的通道号
            {
                break;
            }
			if (dwBufSize > 0)
            {
                if (!PlayCtrl.INSTANCE.PlayM4_SetStreamOpenMode(this.m_lPort.getValue(), PlayCtrl.STREAME_REALTIME))  //设置实时流播放模式
                {
                    break;
                }

                if (!PlayCtrl.INSTANCE.PlayM4_OpenStream(this.m_lPort.getValue(), pBuffer, dwBufSize, 1024 * 1024)) //打开流接口
                {
                    break;
                }
                
                if(!PlayCtrl.INSTANCE.PlayM4_SetDecCallBackExMend(this.m_lPort.getValue(), this.fDecCBFun, null, new NativeLong(0), null))
                {
                	break;
                }

                if (!PlayCtrl.INSTANCE.PlayM4_Play(this.m_lPort.getValue(), null)) //播放开始
                {
                    break;
                }
            }
			break;
		case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
            if ((dwBufSize > 0) && (this.m_lPort.getValue().longValue() != -1))
            {
                if (!PlayCtrl.INSTANCE.PlayM4_InputData(this.m_lPort.getValue(), pBuffer, dwBufSize))  //输入流数据
                {
                    break;
                }
            }
		default:
			break;
		}
	}

}
