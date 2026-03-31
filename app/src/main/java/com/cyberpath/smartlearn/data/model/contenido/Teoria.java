package com.cyberpath.smartlearn.data.model.contenido;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teoria {
    @SerializedName(value = "id", alternate = {"id_subtema"})
    private Integer id;

    private String contenido;
    private boolean revisado;
    private String fuente;

    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    private String updatedAt;

    @SerializedName(value = "idSubtema", alternate = {"id_subtema"})
    private Integer idSubtema;
}
