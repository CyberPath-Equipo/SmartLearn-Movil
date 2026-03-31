package com.cyberpath.smartlearn.data.model.usuario;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UltimaConexion {
    private Integer id;

    @SerializedName(value = "idUsuario", alternate = {"id_usuario"})
    private Integer idUsuario;

    @SerializedName(value = "ultimaConexion", alternate = {"ultima_conexion"})
    private String ultimaConexion;

    private String dispositivo;

    @SerializedName(value = "idSubtema", alternate = {"id_subtema"})
    private Integer idSubtema;
}
