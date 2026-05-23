package com.cyberpath.smartlearn.util.accesibilidad.visual;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.util.Locale;

public class SalidaAudio implements TextToSpeech.OnInitListener {

    private static final String TAG = "SalidaAudio";
    private static SalidaAudio instancia;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextToSpeech tts;
    private boolean estaInicializado = false;
    private Runnable onSpeechDone;
    private String utteranceActivaId;

    private SalidaAudio(Context context) {
        this.context = context.getApplicationContext();
        tts = new TextToSpeech(this.context, this);
    }

    public static synchronized void iniciarInstancia(Context context) {
        if (instancia == null) {
            instancia = new SalidaAudio(context.getApplicationContext());
        }
    }

    public static synchronized SalidaAudio obtenerInstancia() {
        return instancia;
    }

    @Override
    public void onInit(int status) {
        TextToSpeech motorTts = tts;
        if (motorTts == null) {
            Log.w(TAG, "Se recibio onInit pero TextToSpeech ya fue liberado");
            estaInicializado = false;
            return;
        }

        if (status == TextToSpeech.SUCCESS) {
            int resultado = motorTts.setLanguage(new Locale("es", "MX"));

            if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                resultado = motorTts.setLanguage(Locale.forLanguageTag("es_US"));
            }

            if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "El idioma español no está soportado en este dispositivo");
            } else {
                Log.i(TAG, "TTS inicializado correctamente con voz en español");
            }

            motorTts.setSpeechRate(0.95f);
            motorTts.setPitch(1.0f);

            motorTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.d(TAG, "Comenzó a reproducir: " + utteranceId);
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d(TAG, "Terminó de reproducir: " + utteranceId);
                    ejecutarCallbackSiCorresponde(utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                    Log.e(TAG, "Error al reproducir: " + utteranceId);
                    ejecutarCallbackSiCorresponde(utteranceId);
                }
            });

            estaInicializado = true;

        } else {
            Log.e(TAG, "Error al inicializar TextToSpeech. Código: " + status);
            estaInicializado = false;
        }
    }


    public void hablar(String texto, boolean interrumpir) {
        hablar(texto, interrumpir, null);
    }


    public void hablar(String texto, boolean interrumpir, Runnable onDone) {
        if (!PreferencesManager.isAsistenciaVozActivada(context)) {
            if (onDone != null) mainHandler.post(onDone);
            return;
        }
        if (!estaInicializado || tts == null) {
            Log.w(TAG, "TTS no está inicializado aún");
            if (onDone != null) mainHandler.post(onDone);
            return;
        }

        if (texto == null || texto.trim().isEmpty()) {
            if (onDone != null) mainHandler.post(onDone);
            return;
        }

        String utteranceId = "mensaje_" + System.currentTimeMillis();
        this.onSpeechDone = onDone;
        this.utteranceActivaId = utteranceId;

        if (interrumpir) {
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            tts.speak(texto, TextToSpeech.QUEUE_ADD, null, utteranceId);
        }
    }

    public void hablar(String texto) {
        hablar(texto, false, null);
    }

    public void detener() {
        onSpeechDone = null;
        utteranceActivaId = null;
        if (tts != null) {
            tts.stop();
            Log.d(TAG, "Reproducción de audio detenida");
        }
    }

    public void liberar() {
        estaInicializado = false;
        onSpeechDone = null;
        utteranceActivaId = null;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            Log.d(TAG, "TTS liberado");
        }
        instancia = null;
    }

    public boolean estaHablando() {
        return tts != null && tts.isSpeaking();
    }


    public boolean isReady() {
        return estaInicializado && tts != null;
    }

    private void ejecutarCallbackSiCorresponde(String utteranceId) {
        if (utteranceActivaId == null || !utteranceActivaId.equals(utteranceId)) {
            return;
        }

        final Runnable cb = onSpeechDone;
        onSpeechDone = null;
        utteranceActivaId = null;
        if (cb != null) {
            mainHandler.post(() -> {
                try {
                    cb.run();
                } catch (Exception e) {
                    Log.e(TAG, "Error ejecutando callback de voz: " + e.getMessage(), e);
                }
            });
        }
    }
}