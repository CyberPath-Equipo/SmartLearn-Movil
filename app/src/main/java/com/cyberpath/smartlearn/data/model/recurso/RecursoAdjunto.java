package com.cyberpath.smartlearn.data.model.recurso;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecursoAdjunto {
    @SerializedName(value = "id", alternate = {"id_recurso"})
    private Integer id;

    private Integer orden;
    private String titulo;
    private String url;

    @SerializedName(value = "mimeType", alternate = {"mime_type"})
    private String mimeType;

    @SerializedName(value = "tamanoBytes", alternate = {"tamano_bytes"})
    private Long tamanoBytes;

    private String descripcion;

    @SerializedName(value = "creadoEn", alternate = {"creado_en"})
    private String creadoEn;

    @SerializedName(value = "idSubtema", alternate = {"id_subtema"})
    private Integer idSubtema;

    @SerializedName(value = "idTipoRecurso", alternate = {"id_tipo_recurso"})
    private Integer idTipoRecurso;
}
