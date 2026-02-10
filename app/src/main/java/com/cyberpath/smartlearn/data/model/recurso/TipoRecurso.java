package com.cyberpath.smartlearn.data.model.recurso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoRecurso {
    private Integer id;
    private String nombre;
    private String descripcion;
}
