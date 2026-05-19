package com.cyberpath.smartlearn.data.model.usuario.acceso;

public class RegistroVerificacionRequest {
    private String transactionId;
    private String code;

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
}

