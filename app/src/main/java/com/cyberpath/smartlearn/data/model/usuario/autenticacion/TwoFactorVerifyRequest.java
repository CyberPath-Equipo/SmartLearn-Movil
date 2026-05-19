package com.cyberpath.smartlearn.data.model.usuario.autenticacion;

public class TwoFactorVerifyRequest {
    private String transactionId;
    private String code;
    private Boolean rememberDevice;
    private String deviceInfo;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getRememberDevice() {
        return rememberDevice;
    }

    public void setRememberDevice(Boolean rememberDevice) {
        this.rememberDevice = rememberDevice;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}

