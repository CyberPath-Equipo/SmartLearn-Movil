package com.cyberpath.smartlearn.data.model.contenido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teoria {
    private Integer id;
    private String contenido;
    private boolean revisado;

    private Integer idSubtema;
}
