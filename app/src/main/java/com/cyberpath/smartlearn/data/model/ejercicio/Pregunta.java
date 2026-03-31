package com.cyberpath.smartlearn.data.model.ejercicio;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pregunta {
    @SerializedName(value = "id", alternate = {"id_pregunta"})
    private Integer id;

    private String enunciado;

    @SerializedName(value = "idEjercicio", alternate = {"id_ejercicio"})
    private Integer idEjercicio;

    private String tipo;
    private Integer orden;
    private Double puntos;

    private List<Opcion> opciones = new ArrayList<>();
}