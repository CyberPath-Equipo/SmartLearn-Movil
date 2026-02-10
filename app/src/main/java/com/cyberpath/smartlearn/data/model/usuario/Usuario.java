package com.cyberpath.smartlearn.data.model.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    private Integer id;
    private String nombreCuenta;
    private String correo;
    private String contrasena;

    private Integer idRol;
    private Integer idConfiguracion;
    private Integer idUltimaConexion;
}
