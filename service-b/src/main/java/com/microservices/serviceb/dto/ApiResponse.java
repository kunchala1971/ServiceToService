package com.microservices.serviceb.dto;

import java.time.LocalDateTime;

/**
 * Standard Unified REST API Response Wrapper for Service B.
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String traceId;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data, String traceId) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(String message, T data, String traceId) {
        return new ApiResponse<>(true, message, data, traceId);
    }

    public static <T> ApiResponse<T> error(String message, String traceId) {
        return new ApiResponse<>(false, message, null, traceId);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
