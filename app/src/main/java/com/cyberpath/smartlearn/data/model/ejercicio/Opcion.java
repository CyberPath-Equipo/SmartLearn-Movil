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

    private Boolean correcta;
    private Integer orden;

    private Integer idPregunta;


    public boolean isCorrecta() {
        return Boolean.TRUE.equals(correcta);
    }
}
