package com.cyberpath.smartlearn.data.model.usuario;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rol {
    @SerializedName(value = "id", alternate = {"id_rol"})
    private Integer id;

    private String tipo;
    private String descripcion;
}
