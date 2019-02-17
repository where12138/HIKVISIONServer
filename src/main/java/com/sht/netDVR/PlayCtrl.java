package com.sht.netDVR;


import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API.HWND;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface PlayCtrl extends StdCallLibrary {
    PlayCtrl INSTANCE = (PlayCtrl) Native.loadLibrary("hcsdk\\PlayCtrl",
            PlayCtrl.class);
    //流类型
    public static final int STREAME_REALTIME = 0;
    public static final int STREAME_FILE = 1;
    //帧类型
    public static final int T_AUDIO16 = 101;
    public static final int T_AUDIO8 = 100;
    public static final int T_UTVY = 1;
    public static final int T_YV12 = 3;
    public static final int T_RGB32 = 7;
    //缓存大小
    public static final int SOURCE_BUF_MAX = 1024*100000;
    public static final int SOURCE_BUF_MIN = 1024*50;
    

    boolean PlayM4_GetPort(NativeLongByReference nPort);
    boolean PlayM4_OpenStream(NativeLong nPort, ByteByReference pFileHeadBuf, int nSize, int nBufPoolSize);
    boolean PlayM4_InputData(NativeLong nPort, ByteByReference pBuf, int nSize);
    boolean PlayM4_CloseStream(NativeLong nPort);
    boolean PlayM4_SetStreamOpenMode(NativeLong nPort, int nMode);
    boolean PlayM4_Play(NativeLong nPort, HWND hWnd);
    boolean PlayM4_Stop(NativeLong nPort);
    boolean PlayM4_SetSecretKey(NativeLong nPort, NativeLong lKeyType, String pSecretKey, NativeLong lKeyLen);
    boolean PlayM4_SetDecCallBackExMend(NativeLong nPort,DecCBFun fDecCBFun,ByteByReference pDest,NativeLong nDestSize,NativeLong nUser);
    
    boolean PlayM4_FreePort(NativeLong nPort);
    boolean PlayM4_GetLastError(NativeLong nPort);
    
  //解码帧，图像和声音信息结构体。
    public static class FRAME_INFO extends Structure {
		public NativeLong nWidth;
		public NativeLong nHeight;
		public NativeLong nStamp;
		public NativeLong nType;
		public NativeLong nFrameRate;
		public int dwFrameNum;
		
	}
    public static interface DecCBFun extends StdCallCallback {
        public boolean invoke(NativeLong nPort, ByteByReference pBuffer,NativeLong nSize,FRAME_INFO pFrameInfo,NativeLong nUser,NativeLong nReserved2);
      }
   
}