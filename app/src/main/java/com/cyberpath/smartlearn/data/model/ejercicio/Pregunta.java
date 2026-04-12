package com.cyberpath.smartlearn.data.model.ejercicio;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pregunta {
    private Integer id;
    private String enunciado;
    private String tipo;
    private Integer orden;
    private Double puntos;

    private Integer idEjercicio;
    private List<Opcion> opciones = new ArrayList<>();
}