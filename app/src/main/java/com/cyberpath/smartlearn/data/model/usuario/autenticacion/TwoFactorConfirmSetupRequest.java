package com.cyberpath.smartlearn.data.model.usuario.autenticacion;

public class TwoFactorConfirmSetupRequest {
    private String transactionId;
    private String code;
    private String tempSecret;

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

    public String getTempSecret() {
        return tempSecret;
    }

    public void setTempSecret(String tempSecret) {
        this.tempSecret = tempSecret;
    }
}

