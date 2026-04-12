package com.cyberpath.smartlearn.data.model.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rol {
    private Integer id;
    private String tipo;
    private String descripcion;
}
