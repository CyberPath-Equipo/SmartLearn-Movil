package com.cyberpath.smartlearn.util.accesibilidad.auditiva;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.media3.ui.PlayerView;

import java.util.List;

public class ReproductorMultimedia {

    private final Context ctx;
    private final TranslationEngine engine;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ReproductorContenido player;
    private ImageView imageView;
    private PlayerView playerView;
    private String lessonId = "lesson_1";
    private boolean mappingLoaded = false;
    private PlaybackListener listener;
    // Monitor para detectar fin de reproducción (polling ligero)
    private final Runnable monitorRunnable = new Runnable() {
        private boolean wasPlaying = false;

        @Override
        public void run() {
            if (player == null) return;
            boolean nowPlaying = player.isPlaying();
            if (nowPlaying && !wasPlaying) {
                // empezó
                if (listener != null) listener.onStarted();
            }
            if (!nowPlaying && wasPlaying) {
                // terminó
                if (listener != null) listener.onFinished();
            }
            wasPlaying = nowPlaying;
            // seguir monitoreando si sigue existiendo el player
            mainHandler.postDelayed(this, 200);
        }
    };

    /**
     * @param ctx     Context (preferiblemente Activity)
     * @param baseUrl Base URL del TranslationEngine (ej: "http://.../smartlearn/api/lsm/")
     */
    public ReproductorMultimedia(Context ctx, String baseUrl) {
        this.ctx = ctx.getApplicationContext();
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";
        this.engine = new TranslationEngine(baseUrl);
    }

    /**
     * Debes llamar esto para indicarle dónde debe mostrar imágenes/videos.
     * Se puede llamar en cualquier Activity/Fragment antes de play().
     */
    public void setTargetViews(ImageView imageView, PlayerView playerView) {
        this.imageView = imageView;
        this.playerView = playerView;
        // (re)crear el SignPlayer ligado a estas vistas
        if (this.player != null) {
            // liberar el antiguo player (evita fugas)
            this.player.release();
        }
        this.player = new ReproductorContenido(ctx, imageView, playerView);
    }

    public void setLessonId(String lessonId) {
        if (lessonId != null && !lessonId.trim().isEmpty()) this.lessonId = lessonId;
    }

    public void setPlaybackListener(PlaybackListener listener) {
        this.listener = listener;
    }

    /**
     * Carga mapping explicitamente. Callback se invoca en el hilo de Retrofit (normalmente background);
     * se reenvía al hilo principal.
     */
    public void loadMappingAsync(String lessonId, TranslationEngine.MappingCallback cb) {
        if (lessonId != null) setLessonId(lessonId);
        engine.loadMappingAsync(this.lessonId, (ok, msg) -> {
            mappingLoaded = ok;
            mainHandler.post(() -> {
                if (listener != null) listener.onMappingLoaded(ok, msg);
                if (cb != null) cb.onLoaded(ok, msg);
            });
        });
    }

    /**
     * Versión sin callback: carga mapping si hace falta y luego reproduce.
     */
    public void play(final String text) {
        if (!mappingLoaded) {
            // cargar y luego reproducir
            loadMappingAsync(this.lessonId, (ok, msg) -> {
                mainHandler.post(() -> {
                    mappingLoaded = ok;
                    if (!ok) {
                        if (listener != null) listener.onError("Error cargando mapping: " + msg);
                        return;
                    }
                    playInternal(text);
                });
            });
        } else {
            playInternal(text);
        }
    }

    private void playInternal(String text) {
        if (player == null || imageView == null || playerView == null) {
            if (listener != null)
                listener.onError("Target views no configuradas. Llama setTargetViews(...) antes de play().");
            return;
        }
        // Translate (rápido, en memoria). Si fuera costoso, mover a background.
        List<ContenidoItem> items = engine.translate(text);
        if (items == null || items.isEmpty()) {
            if (listener != null)
                listener.onError("No se encontraron signos para el texto proporcionado.");
            return;
        }
        player.setSequence(items);
        player.play();
        // iniciar monitor para notificar start/finish
        mainHandler.removeCallbacks(monitorRunnable);
        mainHandler.post(monitorRunnable);
    }

    public void stop() {
        if (player != null) player.stop();
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    /**
     * Libera recursos (llamar desde onStop()/onDestroy() del Activity/Fragment)
     */
    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
        mainHandler.removeCallbacksAndMessages(null);
    }

    public interface PlaybackListener {
        @MainThread
        void onStarted();

        @MainThread
        void onFinished();

        @MainThread
        void onError(String message);

        @MainThread
        void onMappingLoaded(boolean ok, @Nullable String message);
    }
}