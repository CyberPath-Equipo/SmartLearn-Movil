package com.cyberpath.smartlearn.util.accesibilidad.auditiva;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.PlaybackException;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ReproductorContenido {
    private final ImageView imageView;
    private final PlayerView playerView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Context ctx;
    private ExoPlayer exoPlayer;
    private List<ContenidoItem> items;
    private int index = 0;
    private boolean playing = false;

    public ReproductorContenido(Context ctx, ImageView imageView, PlayerView playerView) {
        this.ctx = ctx;
        this.imageView = imageView;
        this.playerView = playerView;
    }

    public void setSequence(List<ContenidoItem> items) {
        stop();
        this.items = items;
        this.index = 0;
    }

    public void play() {
        if (items == null || items.isEmpty()) return;
        if (playing) return;
        playing = true;
        index = Math.max(0, index);
        handler.post(stepImage);
    }    private final Runnable stepImage = new Runnable() {
        @Override
        public void run() {
            if (!playing || items == null || index >= items.size()) {
                playing = false;
                return;
            }
            ContenidoItem it = items.get(index);
            if (it.type == ContenidoItem.Type.IMAGE) {
                showImage(it);
            } else {
                playVideo(it);
            }
        }
    };

    public void stop() {
        playing = false;
        handler.removeCallbacksAndMessages(null);
        stopVideo();
    }

    public void release() {
        stop();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void showImage(ContenidoItem it) {
        playerView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);

        Glide.with(ctx)
                .load(it.url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(imageView);

        int duration = it.durationMs > 0 ? it.durationMs : 800;
        handler.postDelayed(this::runNext, duration);
    }

    private void playVideo(ContenidoItem it) {
        imageView.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);

        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(ctx).build();
            playerView.setPlayer(exoPlayer);
            exoPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                            .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE)
                            .build(),
                    true
            );
            exoPlayer.setVolume(0f);

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_ENDED) runNext();
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    runNext();
                }
            });
        } else {
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
        }

        String fileName = it.url.substring(it.url.lastIndexOf('/') + 1).toLowerCase();
        if (!fileName.matches(".*\\.(mp4|avi|mkv|mov|webm)$")) {
            runNext();
            return;
        }

        MediaItem mediaItem = MediaItem.fromUri(it.url);
        exoPlayer.setVolume(0f);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    private void runNext() {
        if (!playing) return;

        index++;

        if (items == null || index >= items.size()) {
            playing = false;
            return;
        }

        handler.post(stepImage);
    }

    private void stopVideo() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
        }
        playerView.setVisibility(View.GONE);
    }

    public boolean isPlaying() {
        boolean exoIsPlaying = exoPlayer != null && exoPlayer.isPlaying();
        return playing || exoIsPlaying;
    }


}