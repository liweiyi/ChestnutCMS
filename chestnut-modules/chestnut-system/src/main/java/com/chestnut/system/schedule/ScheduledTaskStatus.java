/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.system.schedule;

public enum ScheduledTaskStatus {

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

    ScheduledTaskStatus(int state, String stateInfo) {
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