package com.sht.bean;

public class FrameBean {
	private int width;
	private int height;
	private int rate;
	private byte[] buffer;
	private long ts;
	
	public FrameBean(int width,int height,int rate,byte[] buffer,long ts) {
		this.width = width;
		this.height = height;
		this.rate = rate;
		this.buffer = buffer;
		this.ts = ts;
	}
	
	@Override
	public String toString() {
		return "{width:"+width+",height:"+height+",rate:"+rate+",buffer:"+buffer+",ts:"+ts+"}";
	}
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getRate() {
		return rate;
	}
	public byte[] getBuffer() {
		return buffer;
	}
	public long getTs() {
		return ts;
	}
	
	
}
