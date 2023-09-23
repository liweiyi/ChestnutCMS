package com.chestnut.common.async.enums;

public enum TaskStatus {

	/**
	 * 已提交，准备执行
	 */
    READY(1, "READY"),
    
    /**
     * 进行中
     */
    RUNNING(0, "RUNNING"),
    
    /**
     * 执行完成
     */
    SUCCESS(2, "SUCCESS"),
    
    /**
     * 异常中断
     */
    FAILED(-2, "FAILED"),
    
    /**
     * 取消执行的任务状态设置为中断
     */
    INTERRUPTED(-10, "INTERRUPTED");
	
    private int state;
    
    private String stateInfo;

    TaskStatus(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }
}