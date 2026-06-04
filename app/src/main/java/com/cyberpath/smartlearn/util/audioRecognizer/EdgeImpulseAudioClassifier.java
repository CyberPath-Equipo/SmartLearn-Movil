package com.cyberpath.smartlearn.util.audioRecognizer;

import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EdgeImpulseAudioClassifier {

    private static final String TAG = "EdgeImpulseAudio";
    private static final EdgeImpulseAudioClassifier INSTANCIA = new EdgeImpulseAudioClassifier();

    private final boolean bibliotecaCargada;
    private volatile List<String> etiquetasCache;

    private EdgeImpulseAudioClassifier() {
        this.bibliotecaCargada = cargarBibliotecaNativa();
    }

    private boolean cargarBibliotecaNativa() {
        try {
            System.loadLibrary("smartlearn_audio");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "No se pudo cargar smartlearn_audio", e);
            return false;
        }
    }

    public static EdgeImpulseAudioClassifier obtenerInstancia() {
        return INSTANCIA;
    }

    public boolean isReady() {
        if (!bibliotecaCargada) {
            return false;
        }

        // Validar que el modelo tiene etiquetas cargadas
        List<String> etiquetas = obtenerEtiquetasModelo();
        boolean modeloTieneEtiquetas = !etiquetas.isEmpty();

        if (!modeloTieneEtiquetas) {
            Log.w(TAG, "Modelo cargado pero sin etiquetas disponibles");
        }

        return modeloTieneEtiquetas;
    }

    public String clasificar(short[] audioData) {
        if (!bibliotecaCargada || audioData == null || audioData.length == 0) {
            return null;
        }
        try {
            return classifyAudioNative(audioData);
        } catch (Throwable e) {
            Log.e(TAG, "Error al clasificar audio con Edge Impulse", e);
            return null;
        }
    }

    public List<String> obtenerEtiquetasModelo() {
        if (!bibliotecaCargada) {
            return Collections.emptyList();
        }

        List<String> cacheActual = etiquetasCache;
        if (cacheActual != null) {
            return cacheActual;
        }

        synchronized (this) {
            if (etiquetasCache != null) {
                return etiquetasCache;
            }
            try {
                String[] etiquetas = getModelLabelsNative();
                if (etiquetas == null || etiquetas.length == 0) {
                    etiquetasCache = Collections.emptyList();
                } else {
                    etiquetasCache = Collections.unmodifiableList(Arrays.asList(etiquetas));
                }
            } catch (Throwable e) {
                Log.e(TAG, "Error obteniendo etiquetas del modelo", e);
                etiquetasCache = Collections.emptyList();
            }
            return etiquetasCache;
        }
    }

    private native String classifyAudioNative(short[] audioData);

    private native String[] getModelLabelsNative();
}


