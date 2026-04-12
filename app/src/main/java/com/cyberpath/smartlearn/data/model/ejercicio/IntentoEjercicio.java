package com.cyberpath.smartlearn.data.model.ejercicio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntentoEjercicio {
    private Integer id;
    private double puntaje;
    private Integer duracionSeg;
    private String fecha;
    private String estado;

    private Integer idUsuario;
    private Integer idEjercicio;
}
