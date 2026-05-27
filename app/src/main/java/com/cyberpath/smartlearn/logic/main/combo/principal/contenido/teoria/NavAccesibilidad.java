package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.media3.ui.PlayerView;

import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.teoria.TeoriaFragment;
import com.cyberpath.smartlearn.util.accesibilidad.auditiva.ReproductorMultimedia;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

public class NavAccesibilidad {

    private final Context context;
    private final TeoriaFragment fragment;
    private final ReproductorMultimedia reproductorMultimedia;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private PlayerView playerView;
    private ImageView imageView;
    private final boolean accesibilidadAuditivaActivada;
    //IP local - Efrén
    private String baseUrlLenguajeSenas = "http://192.168.1.110:8080/smartlearn/api/lsm/";

    public NavAccesibilidad(Context context, TeoriaFragment fragment) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.accesibilidadAuditivaActivada = PreferencesManager.isAccesibilidadAuditivaActivada(context);

        if (accesibilidadAuditivaActivada) {
            this.reproductorMultimedia = new ReproductorMultimedia(context, baseUrlLenguajeSenas);
            this.reproductorMultimedia.setPlaybackListener(new ReproductorMultimedia.PlaybackListener() {
                @Override
                public void onStarted() {
                    // Reproducción iniciada
                }

                @Override
                public void onFinished() {
                    // Reproducción finalizada
                }

                @Override
                public void onError(String message) {
                    // Error en reproducción
                }

                @Override
                public void onMappingLoaded(boolean ok, String message) {
                    // Mapping cargado
                }
            });
        } else {
            this.reproductorMultimedia = null;
        }
    }

    /**
     * Configura las vistas para el reproductor multimedia
     *
     * @param imageView  ImageView para mostrar las imágenes del lenguaje de señas
     * @param playerView PlayerView para reproducción de videos
     */
    public void setTargetViews(ImageView imageView, PlayerView playerView) {
        if (!accesibilidadAuditivaActivada || reproductorMultimedia == null) {
            return;
        }

        this.imageView = imageView;
        this.playerView = playerView;

        // Mostrar las vistas solo si accesibilidad auditiva está activada
        if (imageView != null) {
            imageView.setVisibility(android.view.View.VISIBLE);
        }
        if (playerView != null) {
            playerView.setVisibility(android.view.View.VISIBLE);
        }

        // Configurar el reproductor con las vistas
        reproductorMultimedia.setTargetViews(imageView, playerView);
    }

    /**
     * Reproduce el contenido de teoría en lenguaje de señas
     *
     * @param textoTeoria Texto a traducir y reproducir
     * @param lessonId    ID de la lección para cargar el mapping correcto
     */
    public void reproducirContenido(String textoTeoria, String lessonId) {
        if (!accesibilidadAuditivaActivada || reproductorMultimedia == null || playerView == null || imageView == null) {
            return;
        }

        if (textoTeoria == null || textoTeoria.trim().isEmpty()) {
            return;
        }

        // Configurar el ID de la lección
        if (lessonId != null && !lessonId.trim().isEmpty()) {
            reproductorMultimedia.setLessonId(lessonId);
        }

        // Reproducir el contenido
        reproductorMultimedia.play(textoTeoria);
    }

    /**
     * Detiene la reproducción actual
     */
    public void detenerReproduccion() {
        if (reproductorMultimedia != null) {
            reproductorMultimedia.stop();
        }
    }

    /**
     * Verifica si accesibilidad auditiva está activada
     */
    public boolean isAccesibilidadAuditivaActivada() {
        return accesibilidadAuditivaActivada;
    }

    /**
     * Verifica si se está reproduciendo algo
     */
    public boolean isPlaying() {
        return accesibilidadAuditivaActivada && reproductorMultimedia != null && reproductorMultimedia.isPlaying();
    }

    /**
     * Libera recursos del reproductor
     */
    public void release() {
        if (reproductorMultimedia != null) {
            reproductorMultimedia.release();
        }
        mainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Establece la URL base del API de lenguaje de señas
     */
    public void setBaseUrlLenguajeSenas(String baseUrl) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            this.baseUrlLenguajeSenas = baseUrl;
        }
    }
}