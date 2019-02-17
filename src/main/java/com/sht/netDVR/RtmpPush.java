package com.sht.netDVR;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgproc;

import static org.bytedeco.javacpp.opencv_core.*;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;
import org.bytedeco.javacv.FrameRecorder.Exception;

import com.sht.bean.FrameBean;

public class RtmpPush extends Thread {
	private BlockingQueue<FrameBean> frameQueue;
	private String outputFile;
	private FFmpegFrameRecorder recorder;
	private FrameBean frameBean;
	private int width,height;
	private boolean flag = true;
	private Map<String, String> videoOption = new HashMap<>();
	private ToIplImage converter;
	private ToMat matConverter;
	private int bitrate = 2500000;
	private Frame matFrame;
	private long lastts=0;
	private BytePointer bPointer;
	private Mat yv12Mat,rgbMat;
	
	public RtmpPush(String outputFile,BlockingQueue<FrameBean> frameQueue) {
		// TODO Auto-generated constructor stub
		//Loader.load(opencv_core.class);
		//Loader.load(opencv_objdetect.class);
		this.frameQueue = frameQueue;
		this.outputFile = outputFile;
		//视频设置
		this.videoOption.put("tune", "zerolatency");
		this.videoOption.put("preset", "medium");
		this.videoOption.put("crf", "28");
		
	}
	
	public void quit() {
		flag = false;
		//System.out.println("quit:"+this.getName());
		System.err.println("quit:"+this.getName());
		this.interrupt();
	}
	
	
	@Override
	public void run() {
		// TODO 监听帧队列然后向rtmp服务器推流
		//Loader.load(opencv_objdetect.class);
		while (flag) {
			try {
				frameBean = this.frameQueue.take();
				synchronized (this) {
					if (this.recorder==null) {
						//初始化recorder
						System.err.println("init recoder"+this.getName());
						this.width = frameBean.getWidth();
						this.height = frameBean.getHeight();
						int rate = frameBean.getRate();
						this.recorder = new FFmpegFrameRecorder(this.outputFile, this.width, this.height);
						//this.recorder = FrameRecorder.createDefault(this.outputFile, this.width, this.height);
						this.recorder.setInterleaved(true);
						this.recorder.setVideoOptions(this.videoOption);
						this.recorder.setVideoBitrate(bitrate);
						this.recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
						this.recorder.setFormat("flv");
						this.recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
						this.recorder.setFrameRate(rate);
						this.recorder.setGopSize(rate*2);
						this.recorder.start();
						this.bPointer = new BytePointer(frameBean.getBuffer().length);
						//this.bPointer = new BytePointer(1382400);
						this.yv12Mat = new Mat(height + height/2,width,CV_8UC1);
						this.rgbMat = new Mat(height,width,CV_8UC3);
						if (this.converter==null) {
							this.converter = new ToIplImage();
						}
						if (this.matConverter==null) {
							this.matConverter = new ToMat();
						}
					}
					
					//时间戳-----↓
					long ts = frameBean.getTs();
					if (ts<this.lastts) {
						ts=this.lastts+40;
					}
					this.lastts = ts;
					this.recorder.setTimestamp(ts*1000);
					//时间戳-----↑
					//图像转码-----↓
					//填充指针
					//this.bPointer.put(frameBean.getBuffer());
					this.bPointer.put(frameBean.getBuffer());
					//mat填充
					this.yv12Mat.data(this.bPointer);
					//转码opencv实现方式
					opencv_imgproc.cvtColor(this.yv12Mat, this.rgbMat, opencv_imgproc.COLOR_YUV2BGR_YV12);
					//转换为帧
					this.matFrame = this.converter.convert(this.rgbMat);
					//this.matFrame = this.matConverter.convert(this.yv12Mat);
					//图像转码-----↑
				    try {
						this.recorder.record(this.matFrame);
					} catch (Exception e) {
						e.printStackTrace();
						this.recorder.stop();
						this.recorder.release();
						if (this.bPointer!=null) {
							this.bPointer.deallocate();
						}
						this.recorder=null;
						continue;
					}
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				if(!flag) {
					if (this.bPointer!=null) {
						this.bPointer.deallocate();
					}
					break;
				}
				e.printStackTrace();
			} catch (Exception e) {
				// TODO 
				try {
					this.recorder.stop();
					this.recorder.release();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					//System.out.println("lastts:"+lastts);
				}
				this.recorder=null;
				
				if (this.bPointer!=null) {
					this.bPointer.deallocate();
				}
				e.printStackTrace();
			}
		}
		
		if (this.bPointer!=null) {
			this.bPointer.deallocate();
		}
		if (this.recorder!=null) {
			try {
				this.recorder.stop();
				this.recorder.release();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		this.recorder=null;
		}
		if (this.bPointer!=null) {
			this.bPointer.deallocate();
		}
		this.yv12Mat.release();
		this.rgbMat.release();
	}
	
	public byte[] swapYV12ToI420( byte [] yv12bytes, int width , int height) { 
		byte[] Varray = new byte[width *height/ 4];
		//将UV数据对换
		int Vposition = width*height-1;
		//缓存V
		System.arraycopy(yv12bytes, Vposition, Varray, 0, Varray.length);
		//将U移动到V
		System.arraycopy(yv12bytes, Vposition+Varray.length, yv12bytes, Vposition, Varray.length);
		//将缓存的V放到U
		System.arraycopy(Varray, 0, yv12bytes, Vposition+Varray.length, Varray.length);
		return yv12bytes;
	} 
	
	public byte[] YV12ToYUV420P(byte[] yv12,int width,int height) {
		for(int i=0;i<width*height/4;i++) {
			
		}
		
		return yv12;
	}
	
	
	/**
	 * yv12数据图像转换为IpImage
	 * @param yv12
	 * @param width
	 * @param height
	 * @return
	 */
	public IplImage YV12_ToIplImage(byte[] yv12, int width, int height,IplImage image,BytePointer pointer) {
        if (yv12 == null) {
            return null;
        }
 
        byte[] rgb24 = YV12_To_RGB24(yv12, width, height);
        if (rgb24 == null) {
            return null;
        }
        pointer.zero();
        pointer.put(rgb24);
        image.imageData(pointer);
        return image;
    }
	
	/**
	 * yv12编码图像数据转换为RGB24格式原始算法实现
	 * @param yv12
	 * @param width
	 * @param height
	 * @return
	 */
	private byte[] YV12_To_RGB24(byte[] yv12, int width, int height) {
        if (yv12 == null) {
            return null;
        }
 
        int nYLen = (int) width * height;
        int halfWidth = width >> 1;
 
        if (nYLen < 1 || halfWidth < 1) {
            return null;
        }

        // Convert YV12 to RGB24
        byte[] rgb24 = new byte[width * height * 3];
        int[] rgb = new int[3];
        int i, j, m, n, x, y;
        m = -width;
        n = -halfWidth;
        for (y = 0; y < height; y++) {
            m += width;
            if (y % 2 != 0) {
                n += halfWidth;
            }
 
            for (x = 0; x < width; x++) {
                i = m + x;
                j = n + (x >> 1);
                rgb[2] = (int) ((int) (yv12[i] & 0xFF) + 1.370705 * ((int) (yv12[nYLen + j] & 0xFF) - 128)); // r
                rgb[1] = (int) ((int) (yv12[i] & 0xFF) - 0.698001 * ((int) (yv12[nYLen + (nYLen >> 2) + j] & 0xFF) - 128) - 0.703125 * ((int) (yv12[nYLen + j] & 0xFF) - 128));   // g
                rgb[0] = (int) ((int) (yv12[i] & 0xFF) + 1.732446 * ((int) (yv12[nYLen + (nYLen >> 2) + j] & 0xFF) - 128)); // b
 
                //j = nYLen - iWidth - m + x;
                //i = (j<<1) + j;    //图像是上下颠倒的
 
                j = m + x;
                i = (j << 1) + j;
 
                for (j = 0; j < 3; j++) {
                    if (rgb[j] >= 0 && rgb[j] <= 255) {
                        rgb24[i + j] = (byte) rgb[j];
                    } else {
                        rgb24[i + j] = (byte) ((rgb[j] < 0) ? 0 : 255);
                    }
                }
            }
        }
        return rgb24;
    }
	
	
	
}
