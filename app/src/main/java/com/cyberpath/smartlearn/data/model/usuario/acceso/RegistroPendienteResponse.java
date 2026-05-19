package com.cyberpath.smartlearn.data.model.usuario.acceso;

public class RegistroPendienteResponse {
    private String message;
    private Boolean requiresVerification;
    private String transactionId;
    private String correo;
    private String nombreCuenta;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getRequiresVerification() {
        return requiresVerification;
    }

    public void setRequiresVerification(Boolean requiresVerification) {
        this.requiresVerification = requiresVerification;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }
}

