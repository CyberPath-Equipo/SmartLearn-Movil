package com.cyberpath.smartlearn.data.model.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    private Integer id;
    private String nombreCuenta;
    private String correo;
    private String contrasena;
    private String nombreCompleto;
    private Boolean activo;
    private Boolean verificado;
    private String creadoEn;
    private String actualizadoEn;
    private String trustedDeviceToken;
    private String deviceInfo;

    private Integer idRol;
    private Integer idConfiguracion;
    private Integer idUltimaConexion;
}
