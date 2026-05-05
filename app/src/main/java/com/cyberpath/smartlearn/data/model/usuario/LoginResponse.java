package com.cyberpath.smartlearn.data.model.usuario;

public class LoginResponse {
    private String token;
    private Integer idUsuario;
    private String nombreCuenta;
    private Integer idRol;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreCuenta() { return nombreCuenta; }
    public void setNombreCuenta(String nombreCuenta) { this.nombreCuenta = nombreCuenta; }
    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
}