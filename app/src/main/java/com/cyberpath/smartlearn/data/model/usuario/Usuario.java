package com.cyberpath.smartlearn.data.model.usuario;

import com.google.gson.annotations.SerializedName;

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

    private Integer idRol;
    private Integer idConfiguracion;
    private Integer idUltimaConexion;
}
