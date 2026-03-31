package com.cyberpath.smartlearn.data.model.recurso;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoRecurso {
    @SerializedName(value = "id", alternate = {"id_tipo_recurso"})
    private Integer id;

    private String nombre;
    private String descripcion;
}
