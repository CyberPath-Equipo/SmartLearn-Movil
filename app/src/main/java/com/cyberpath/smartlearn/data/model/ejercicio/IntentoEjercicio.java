package com.cyberpath.smartlearn.data.model.ejercicio;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntentoEjercicio {
    @SerializedName(value = "id", alternate = {"id_intento_ejercicio"})
    private Integer id;

    private double puntaje;
    private String fecha;

    @SerializedName(value = "duracionSeg", alternate = {"duracion_seg"})
    private Integer duracionSeg;

    private String estado;

    @SerializedName(value = "idUsuario", alternate = {"id_usuario"})
    private Integer idUsuario;

    @SerializedName(value = "idEjercicio", alternate = {"id_ejercicio"})
    private Integer idEjercicio;
}
