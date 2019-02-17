package com.sht.netDVR;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.sht.bean.Camera;
import com.sht.bean.Command;

public class CameraDispatcher extends Thread {

	private BlockingQueue<JSONObject> jsonObjects;
	private List<Realplay> realplays;
	private volatile boolean runs = true;
	private Map<String, Camera> cameras;
	public CameraDispatcher(BlockingQueue<JSONObject> jsonObjects,Map<String, Camera> cameras) {
		this.jsonObjects = jsonObjects;
		this.realplays = new ArrayList<>();
		this.cameras = cameras;
	}
	
	public void quit() {
		runs = false;
		interrupt();
	}
	
	
	@Override
	public void run() {
		// TODO 调度
		while (runs) {
			try {
				JSONObject jsonObject = this.jsonObjects.poll(60, TimeUnit.SECONDS);
				long timeStamp = (long)System.currentTimeMillis()/1000;
				boolean flag = true;
				Iterator<Realplay> iterator = this.realplays.iterator();
				while (iterator.hasNext()) {
					Realplay realplay = (Realplay) iterator.next();
					//将已经结束的线程丢弃
					if (!realplay.isAlive()) {
						realplay.getLogin().logout();
						iterator.remove();
						continue;
					}
					if (jsonObject!=null) {
						if (realplay.getCamera().getAlias().equals(jsonObject.getString("alias"))) {
							realplay.setTimeTag(timeStamp);
							if (jsonObject.containsKey("command")) {
								realplay.getCamera().getBlockingQueue().put(jsonObject.getObject("command", Command.class));
							}
							flag=false;
						}
					}
					if (timeStamp-realplay.getTimeTag()>=300) {
						//超时未收到心跳包==》结束推送线程
						realplay.getCamera().getBlockingQueue().put(new Command(0, 0));
						realplay.getLogin().logout();
						iterator.remove();
					}
				}
				//在检查完所有线程后新建线程
				if (flag) {
					if (jsonObject!=null) {
						Camera camera = this.cameras.get(jsonObject.getString("alias"));
						Login login = new Login(camera);
						if (login.login()) {
							//成功登陆
							Realplay play = new Realplay(camera, login);
							play.start();
							this.realplays.add(play);
						}
					}
				}
			}catch (InterruptedException e) {
				// TODO: handle exception
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
}
