package com.cyberpath.smartlearn.data.model.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Configuracion {
    private Integer id;
    private boolean modoAudio;
    private boolean notificacionesActivadas;
    private Configuracion.TamanoFuente tamanoFuente = TamanoFuente.MEDIO;
    private boolean modoOffline;
    private boolean cuentaCreada;

    private Integer idUsuario;

    public enum TamanoFuente {
        PEQUENO(0), MEDIO(1), GRANDE(2);

        Integer valor;

        TamanoFuente(Integer valor) {
            this.valor = valor;
        }

        public Integer getValor() {
            return valor;
        }
    }
}
