package com.cyberpath.smartlearn.web.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private Integer idUsuario;
    private String nombreCuenta;
    private Integer idRol;
}

