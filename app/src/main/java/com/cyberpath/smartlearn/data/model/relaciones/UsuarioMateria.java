package com.cyberpath.smartlearn.data.model.relaciones;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioMateria {
    @SerializedName(value = "idMateria", alternate = {"id_materia"})
    private Integer idMateria;

    @SerializedName(value = "idUsuario", alternate = {"id_usuario"})
    private Integer idUsuario;

    @SerializedName(value = "suscritoEn", alternate = {"suscrito_en"})
    private String suscritoEn;
}
