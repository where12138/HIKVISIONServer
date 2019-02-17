package com.sht.bean;

public class Command {
	private int code;
	private int operation;
	
	
	/**
	 * 操作指令
	 * @param code
	 * @param operation
	 */
	public Command(int code,int operation) {
		this.code = code;
		this.operation = operation;
	}
	
	
	public int getCode() {
		return code;
	}
	public int getOperation() {
		return operation;
	}
	
}
