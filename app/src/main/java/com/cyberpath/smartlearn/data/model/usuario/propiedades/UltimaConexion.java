package com.cyberpath.smartlearn.data.model.usuario.propiedades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UltimaConexion {
    private Integer id;
    private String ultimaConexion;
    private String dispositivo;

    private Integer idUsuario;
    private Integer idSubtema;
}
