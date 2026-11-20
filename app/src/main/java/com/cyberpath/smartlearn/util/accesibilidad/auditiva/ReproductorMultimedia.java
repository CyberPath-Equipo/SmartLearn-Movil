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

    private final Runnable monitorRunnable = new Runnable() {
        private boolean wasPlaying = false;

        @Override
        public void run() {
            if (player == null) return;
            boolean nowPlaying = player.isPlaying();
            if (nowPlaying && !wasPlaying) {

                if (listener != null) listener.onStarted();
            }
            if (!nowPlaying && wasPlaying) {

                if (listener != null) listener.onFinished();
            }
            wasPlaying = nowPlaying;

            mainHandler.postDelayed(this, 200);
        }
    };


    public ReproductorMultimedia(Context ctx, String baseUrl) {
        this.ctx = ctx.getApplicationContext();
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";
        this.engine = new TranslationEngine(this.ctx, baseUrl);
    }


    public void setTargetViews(ImageView imageView, PlayerView playerView) {
        this.imageView = imageView;
        this.playerView = playerView;

        if (this.player != null) {

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


    public void play(final String text) {
        if (!mappingLoaded) {

            loadMappingAsync(this.lessonId, (ok, msg) -> mainHandler.post(() -> {
                mappingLoaded = ok;
                if (!ok) {
                    if (listener != null) listener.onError("Error cargando mapping: " + msg);
                    return;
                }
                playInternal(text);
            }));
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

        List<ContenidoItem> items = engine.translate(text);
        if (items == null || items.isEmpty()) {
            if (listener != null)
                listener.onError("No se encontraron signos para el texto proporcionado.");
            return;
        }
        player.setSequence(items);
        player.play();

        mainHandler.removeCallbacks(monitorRunnable);
        mainHandler.post(monitorRunnable);
    }

    public void stop() {
        if (player != null) player.stop();
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }


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