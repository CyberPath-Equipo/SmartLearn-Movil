package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria;
public final class TeoriaBloqueContenido {

    public enum Tipo { TEXTO, IMAGEN }

    public final Tipo tipo;
    public final String valor; // texto del párrafo, o URL de la imagen

    public TeoriaBloqueContenido(Tipo tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }
}
