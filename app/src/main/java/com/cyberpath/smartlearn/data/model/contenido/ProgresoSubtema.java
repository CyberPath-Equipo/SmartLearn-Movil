package com.cyberpath.smartlearn.data.model.contenido;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgresoSubtema {
    @SerializedName(value = "id", alternate = {"id_progreso"})
    private Integer id;

    @SerializedName(value = "teoriaLeida", alternate = {"teoria_leida"})
    private boolean teoriaLeida;

    @SerializedName(value = "ejerciciosCompletados", alternate = {"ejercicios_completados"})
    private Integer ejerciciosCompletados;

    @SerializedName(value = "ejerciciosTotales", alternate = {"ejercicios_totales"})
    private Integer ejerciciosTotales;

    private double porcentaje;

    @SerializedName(value = "ultimoAcceso", alternate = {"ultimo_acceso"})
    private String ultimoAcceso;

    @SerializedName(value = "idUsuario", alternate = {"id_usuario"})
    private Integer idUsuario;

    @SerializedName(value = "idSubtema", alternate = {"id_subtema"})
    private Integer idSubtema;
}
