package com.chestnut.common.volc;

public class VolcEngineException extends RuntimeException {

    public VolcEngineException(String message) {
        super(message);
    }

    public VolcEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
