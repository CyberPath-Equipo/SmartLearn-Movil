package com.cyberpath.smartlearn.data.model.estadisticas;

public class InteresItem {
    private final String titulo;
    private final long minutos;

    public InteresItem(String titulo, long minutos) {
        this.titulo = titulo;
        this.minutos = minutos;
    }

    public String getTitulo() {
        return titulo;
    }

    public long getMinutos() {
        return minutos;
    }
}

