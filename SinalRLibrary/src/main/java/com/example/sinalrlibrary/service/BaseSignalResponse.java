package com.example.sinalrlibrary.service;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BaseSignalResponse<T> {
    @Expose
    @SerializedName("Message")
    private String message;
    @Expose
    @SerializedName("IsSuccess")
    private boolean isSuccess;
    @Expose
    @SerializedName("Dto")
    private T dto;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public T getDto() {
        return dto;
    }

    public void setDto(T dto) {
        this.dto = dto;
    }
}
