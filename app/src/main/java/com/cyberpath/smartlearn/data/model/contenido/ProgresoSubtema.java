package com.cyberpath.smartlearn.data.model.contenido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgresoSubtema {
    private Integer id;
    private boolean teoriaLeida;
    private Integer ejerciciosCompletados;
    private Integer ejerciciosTotales;
    private double porcentaje;
    private String ultimoAcceso;

    private Integer idUsuario;
    private Integer idSubtema;
}
