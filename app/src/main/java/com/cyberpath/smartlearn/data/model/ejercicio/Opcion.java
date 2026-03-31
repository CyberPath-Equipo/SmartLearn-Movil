package com.cyberpath.smartlearn.data.model.ejercicio;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Opcion {
    @SerializedName(value = "id", alternate = {"id_opcion"})
    private Integer id;

    private String texto;

    @SerializedName(value = "correcta", alternate = {"es_correcta"})
    private boolean correcta;

    @SerializedName(value = "idPregunta", alternate = {"id_pregunta"})
    private Integer idPregunta;

    private Integer orden;
}
