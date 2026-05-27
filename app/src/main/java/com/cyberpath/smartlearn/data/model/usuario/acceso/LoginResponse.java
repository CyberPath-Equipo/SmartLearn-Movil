package com.cyberpath.smartlearn.data.model.usuario.acceso;

public class LoginResponse {
    private String token;
    private Integer idUsuario;
    private String nombreCuenta;
    private String labelNombre;
    private Integer idRol;
    private Boolean requires2fa;
    private String twoFactorTransactionId;
    private String twoFactorChannel;
    private String trustedDeviceToken;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public String getLabelNombre() {
        return labelNombre;
    }

    public void setLabelNombre(String labelNombre) {
        this.labelNombre = labelNombre;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public Boolean getRequires2fa() {
        return requires2fa;
    }

    public void setRequires2fa(Boolean requires2fa) {
        this.requires2fa = requires2fa;
    }

    public String getTwoFactorTransactionId() {
        return twoFactorTransactionId;
    }

    public void setTwoFactorTransactionId(String twoFactorTransactionId) {
        this.twoFactorTransactionId = twoFactorTransactionId;
    }

    public String getTwoFactorChannel() {
        return twoFactorChannel;
    }

    public void setTwoFactorChannel(String twoFactorChannel) {
        this.twoFactorChannel = twoFactorChannel;
    }

    public String getTrustedDeviceToken() {
        return trustedDeviceToken;
    }

    public void setTrustedDeviceToken(String trustedDeviceToken) {
        this.trustedDeviceToken = trustedDeviceToken;
    }
}