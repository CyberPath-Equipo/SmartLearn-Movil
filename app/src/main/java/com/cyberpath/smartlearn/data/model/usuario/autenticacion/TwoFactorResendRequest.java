package com.cyberpath.smartlearn.data.model.usuario.autenticacion;

public class TwoFactorResendRequest {
    private String transactionId;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}

