package com.sht.netDVR;

import java.util.concurrent.BlockingQueue;

import com.sht.bean.FrameBean;
import com.sht.netDVR.PlayCtrl.DecCBFun;
import com.sht.netDVR.PlayCtrl.FRAME_INFO;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.ByteByReference;

public class DecodeCallBack implements DecCBFun {

	private BlockingQueue<FrameBean> beans;
	public DecodeCallBack(BlockingQueue<FrameBean> beans) {
		this.beans = beans;
	}
	
	@Override
	public boolean invoke(NativeLong nPort, ByteByReference pBuffer, NativeLong nSize, FRAME_INFO pFrameInfo,
			NativeLong nUser, NativeLong nReserved2) {
		// TODO Auto-generated method stub
		byte[] buffer = pBuffer.getPointer().getByteArray(0, (int)nSize.longValue());
		int width = (int) pFrameInfo.nWidth.longValue();
		int height = (int) pFrameInfo.nHeight.longValue();
		int rate = (int) pFrameInfo.nFrameRate.longValue();
		long ts = pFrameInfo.nStamp.longValue();
		
		FrameBean bean = new FrameBean(width, height, rate, buffer, ts);
		try {
			this.beans.put(bean);
			//System.out.println(bean);
			pFrameInfo.clear();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
