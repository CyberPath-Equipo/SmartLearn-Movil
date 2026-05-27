package com.cyberpath.smartlearn.data.model.estadisticas;

public class DatoHistorico {
    private final String etiqueta;
    private final float valor;

    public DatoHistorico(String etiqueta, float valor) {
        this.etiqueta = etiqueta;
        this.valor = valor;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public float getValor() {
        return valor;
    }
}

