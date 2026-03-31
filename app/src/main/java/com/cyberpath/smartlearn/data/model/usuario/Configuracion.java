package com.cyberpath.smartlearn.data.model.usuario;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Configuracion {
    private Integer id;

    @SerializedName(value = "idUsuario", alternate = {"id_usuario"})
    private Integer idUsuario;

    @SerializedName(value = "modoAudio", alternate = {"modo_audio"})
    private boolean modoAudio;

    @SerializedName(value = "cuentaCreada", alternate = {"cuenta_creada"})
    private boolean cuentaCreada;

    @SerializedName(value = "notificacionesActivadas", alternate = {"notificaciones_activadas"})
    private boolean notificacionesActivadas;

    @SerializedName(value = "tamanoFuente", alternate = {"tamano_fuente"})
    private Configuracion.TamanoFuente tamanoFuente = TamanoFuente.MEDIO;

    @SerializedName(value = "modoOffline", alternate = {"modo_offline"})
    private boolean modoOffline;

    public enum TamanoFuente {
        @SerializedName("pequeno") PEQUENO(0),
        @SerializedName("medio") MEDIO(1),
        @SerializedName("grande") GRANDE(2);

        Integer valor;

        TamanoFuente(Integer valor) {
            this.valor = valor;
        }

        public Integer getValor() {
            return valor;
        }
    }
}
