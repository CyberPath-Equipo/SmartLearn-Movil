package com.cyberpath.smartlearn.data.model.usuario.autenticacion;

import java.util.List;

public class TwoFactorSetupResponse {
    private String secret;
    private String provisioningUri;
    private String transactionId;
    private List<String> recoveryCodes;
    private String channel;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getProvisioningUri() {
        return provisioningUri;
    }

    public void setProvisioningUri(String provisioningUri) {
        this.provisioningUri = provisioningUri;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<String> getRecoveryCodes() {
        return recoveryCodes;
    }

    public void setRecoveryCodes(List<String> recoveryCodes) {
        this.recoveryCodes = recoveryCodes;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}

