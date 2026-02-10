package com.cyberpath.smartlearn.data.model.ejercicio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Opcion {
    private Integer id;
    private String texto;
    private boolean correcta;

    private Integer idPregunta;
}
