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
    /** Wrapper Boolean para coincidir con el DTO backend (puede ser null). */
    private Boolean correcta;
    private Integer orden;

    private Integer idPregunta;

    /** Método de conveniencia para evitar NullPointerException al evaluar respuestas. */
    public boolean isCorrecta() {
        return Boolean.TRUE.equals(correcta);
    }
}
