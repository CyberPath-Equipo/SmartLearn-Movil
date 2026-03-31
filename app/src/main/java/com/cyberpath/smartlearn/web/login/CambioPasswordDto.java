package com.cyberpath.smartlearn.web.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CambioPasswordDto {
    private String passwordActual;
    private String passwordNueva;
}

