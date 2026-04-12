package com.cyberpath.smartlearn.data.model.relaciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioEjercicio {
    private Integer id;

    private Integer idUsuario;
    private Integer idEjercicio;
    private boolean hecho;
}
