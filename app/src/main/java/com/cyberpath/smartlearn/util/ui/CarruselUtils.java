package com.cyberpath.smartlearn.util.ui;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class CarruselUtils {

    public static int normalizarPosicion(int posicionVirtual, int tamanoReal) {
        if (tamanoReal <= 0) return 0;
        int realPos = posicionVirtual % tamanoReal;
        return realPos < 0 ? realPos + tamanoReal : realPos;
    }

    public static int centrarPosicion(int indiceReal, int tamanoReal) {
        if (tamanoReal <= 0) return 0;
        if (tamanoReal == 1) return indiceReal;
        int mitad = Integer.MAX_VALUE / 2;
        int base = mitad - (mitad % tamanoReal);
        return base + (indiceReal % tamanoReal);
    }
}