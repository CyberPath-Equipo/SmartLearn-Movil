package com.cyberpath.smartlearn.data.model.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rol {
    private Integer id;
    private String tipo;
}
