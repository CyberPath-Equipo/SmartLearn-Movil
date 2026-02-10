package com.cyberpath.smartlearn.data.model.relaciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioMateria {
    private Integer id;

    private Integer idMateria;
    private Integer idUsuario;
}
