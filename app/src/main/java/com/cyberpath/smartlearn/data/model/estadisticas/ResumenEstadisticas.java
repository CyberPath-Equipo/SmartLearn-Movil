package com.cyberpath.smartlearn.data.model.estadisticas;

public class ResumenEstadisticas {
    private int ejerciciosCompletados;
    private int ejerciciosTotales;
    private double promedioAcierto;
    private long minutosEstudio;
    private int sesionesEstudio;
    private long minutosUltimos7Dias;

    public int getEjerciciosCompletados() {
        return ejerciciosCompletados;
    }

    public void setEjerciciosCompletados(int ejerciciosCompletados) {
        this.ejerciciosCompletados = ejerciciosCompletados;
    }

    public int getEjerciciosTotales() {
        return ejerciciosTotales;
    }

    public void setEjerciciosTotales(int ejerciciosTotales) {
        this.ejerciciosTotales = ejerciciosTotales;
    }

    public double getPromedioAcierto() {
        return promedioAcierto;
    }

    public void setPromedioAcierto(double promedioAcierto) {
        this.promedioAcierto = promedioAcierto;
    }

    public long getMinutosEstudio() {
        return minutosEstudio;
    }

    public void setMinutosEstudio(long minutosEstudio) {
        this.minutosEstudio = minutosEstudio;
    }

    public int getSesionesEstudio() {
        return sesionesEstudio;
    }

    public void setSesionesEstudio(int sesionesEstudio) {
        this.sesionesEstudio = sesionesEstudio;
    }

    public long getMinutosUltimos7Dias() {
        return minutosUltimos7Dias;
    }

    public void setMinutosUltimos7Dias(long minutosUltimos7Dias) {
        this.minutosUltimos7Dias = minutosUltimos7Dias;
    }
}

