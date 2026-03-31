package com.cyberpath.smartlearn.data.model.usuario;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    @SerializedName(value = "id", alternate = {"id_usuario"})
    private Integer id;

    @SerializedName(value = "nombreCuenta", alternate = {"nombre_cuenta"})
    private String nombreCuenta;

    private String correo;
    private String contrasena;

    @SerializedName(value = "idRol", alternate = {"id_rol"})
    private Integer idRol;

    @SerializedName(value = "nombreCompleto", alternate = {"nombre_completo"})
    private String nombreCompleto;

    private Boolean activo;
    private Boolean verificado;

    @SerializedName(value = "creadoEn", alternate = {"creado_en"})
    private String creadoEn;

    @SerializedName(value = "actualizadoEn", alternate = {"actualizado_en"})
    private String actualizadoEn;
}
