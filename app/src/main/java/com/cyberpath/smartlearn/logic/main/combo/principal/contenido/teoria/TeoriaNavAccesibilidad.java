package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.media3.ui.PlayerView;

import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.teoria.TeoriaFragment;
import com.cyberpath.smartlearn.util.accesibilidad.auditiva.ReproductorMultimedia;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

public class TeoriaNavAccesibilidad {

    private final Context context;
    private final TeoriaFragment fragment;
    private final ReproductorMultimedia reproductorMultimedia;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final boolean accesibilidadAuditivaActivada;
    private PlayerView playerView;
    private ImageView imageView;
    //IP local - Efrén
    private String baseUrlLenguajeSenas = "http://192.168.1.110:8080/smartlearn/api/lsm/";

    public TeoriaNavAccesibilidad(Context context, TeoriaFragment fragment) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.accesibilidadAuditivaActivada = PreferencesManager.isAccesibilidadAuditivaActivada(context);

        if (accesibilidadAuditivaActivada) {
            this.reproductorMultimedia = new ReproductorMultimedia(context, baseUrlLenguajeSenas);
            this.reproductorMultimedia.setPlaybackListener(new ReproductorMultimedia.PlaybackListener() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onFinished() {

                }

                @Override
                public void onError(String message) {

                }

                @Override
                public void onMappingLoaded(boolean ok, String message) {

                }
            });
        } else {
            this.reproductorMultimedia = null;
        }
    }


    public void setTargetViews(ImageView imageView, PlayerView playerView) {
        if (!accesibilidadAuditivaActivada || reproductorMultimedia == null) {
            return;
        }

        this.imageView = imageView;
        this.playerView = playerView;


        if (imageView != null) {
            imageView.setVisibility(android.view.View.VISIBLE);
        }
        if (playerView != null) {
            playerView.setVisibility(android.view.View.VISIBLE);
        }


        reproductorMultimedia.setTargetViews(imageView, playerView);
    }


    public void reproducirContenido(String textoTeoria, String lessonId) {
        if (!accesibilidadAuditivaActivada || reproductorMultimedia == null || playerView == null || imageView == null) {
            return;
        }

        if (textoTeoria == null || textoTeoria.trim().isEmpty()) {
            return;
        }


        if (lessonId != null && !lessonId.trim().isEmpty()) {
            reproductorMultimedia.setLessonId(lessonId);
        }


        reproductorMultimedia.play(textoTeoria);
    }


    public void detenerReproduccion() {
        if (reproductorMultimedia != null) {
            reproductorMultimedia.stop();
        }
    }


    public boolean isAccesibilidadAuditivaActivada() {
        return accesibilidadAuditivaActivada;
    }


    public boolean isPlaying() {
        return accesibilidadAuditivaActivada && reproductorMultimedia != null && reproductorMultimedia.isPlaying();
    }


    public void release() {
        if (reproductorMultimedia != null) {
            reproductorMultimedia.release();
        }
        mainHandler.removeCallbacksAndMessages(null);
    }


    public void setBaseUrlLenguajeSenas(String baseUrl) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            this.baseUrlLenguajeSenas = baseUrl;
        }
    }
}