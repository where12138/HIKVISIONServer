package com.sht.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.sht.bean.Camera;
import com.sht.bean.Command;

public class CameraConfig {
	
	public static Map<String, Camera> getCameras() {
		Map<String, Camera> cameras = new HashMap<>();
		
		try {
			JSONReader reader = new JSONReader(new FileReader(new File("src\\\\main\\\\resources\\\\camera.json")));
			reader.startObject();
			while (reader.hasNext()) {
				String key = reader.readString();
				JSONObject jsonObject = (JSONObject) reader.readObject();
				BlockingQueue<Command> bQueue = new ArrayBlockingQueue<>(10);
				Camera camera = new Camera(key,jsonObject.getString("rtmp"), jsonObject.getString("ip"), jsonObject.getString("user"), jsonObject.getString("pwd"),
						jsonObject.getIntValue("port"),bQueue, jsonObject.getIntValue("channel"), jsonObject.getIntValue("linkmode"));
				cameras.put(key, camera);
			}
			reader.endObject();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getStackTrace());
		}	catch (Exception e) {
			// TODO: handle exception
			System.err.println(e.getStackTrace());
		}
		return cameras;
	}
	
	public static JSONObject getConfigs() {
		JSONObject jsonObject=null;
		try {
			JSONReader reader = new JSONReader(new FileReader(new File("src\\main\\resources\\camera.json")));
			jsonObject = (JSONObject) reader.readObject();
			//reader.endObject();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO: handle exception
			System.err.println(e.getStackTrace());
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println(e.getStackTrace());
		}
		return jsonObject;
	}
}
