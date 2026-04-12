package com.cyberpath.smartlearn.data.model.recurso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecursoAdjunto {
    private Integer id;
    private Integer orden;
    private String titulo;
    private String url;
    private String mimeType;
    private Long tamanoBytes;
    private String descripcion;
    private String creadoEn;

    private Integer idSubtema;
    private Integer idTipoRecurso;
}
