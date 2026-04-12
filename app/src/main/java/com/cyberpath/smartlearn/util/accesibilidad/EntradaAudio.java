package com.cyberpath.smartlearn.util.accesibilidad;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EntradaAudio implements RecognitionListener {

    private static final String TAG = "EntradaVoz";
    private static final List<String> AFIRMACIONES = Arrays.asList(
            "sí", "si", "correcto", "así es", "afirmativo", "ok", "dale", "de acuerdo"
    );
    private static final List<String> NEGACIONES = Arrays.asList(
            "no", "incorrecto", "eso no", "negativo", "nop", "de ninguna manera"
    );
    private static EntradaAudio instancia;
    private final Context context;
    private final SalidaAudio salidaAudio;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SpeechRecognizer speechRecognizer;
    private boolean estaInicializado = false;
    private volatile boolean estaEscuchando = false;
    private OnConfirmacionListener confirmacionListener;
    private OnOpcionSeleccionadaListener opcionListener;
    private List<String> opcionesActuales;

    private EntradaAudio(Context context) {
        this.context = context.getApplicationContext();
        if (SalidaAudio.obtenerInstancia() == null) {
            SalidaAudio.iniciarInstancia(this.context);
        }
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        try {
            if (SpeechRecognizer.isRecognitionAvailable(this.context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.context);
                speechRecognizer.setRecognitionListener(this);
                estaInicializado = true;
            } else {
                estaInicializado = false;
                Log.e(TAG, "Reconocimiento de voz no disponible en este dispositivo");
            }
        } catch (Exception e) {
            estaInicializado = false;
            Log.e(TAG, "Error creando SpeechRecognizer: " + e.getMessage(), e);
        }
    }

    public static synchronized void iniciarInstancia(Context context) {
        if (instancia == null) {
            instancia = new EntradaAudio(context.getApplicationContext());
        }
    }

    public static synchronized EntradaAudio obtenerInstancia() {
        return instancia;
    }

    private Intent getRecognizerIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        return intent;
    }

    public boolean hasRecordPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isReady() {
        return estaInicializado && hasRecordPermission() && speechRecognizer != null;
    }

    public void confirmarAfirmacion(OnConfirmacionListener listener) {
        if (!PreferencesManager.isModoAudioActivado(context)) return;

        if (!isReady()) {
            Log.w(TAG, "STT no listo o falta permiso");
            if (salidaAudio != null)
                salidaAudio.hablar("Reconocimiento de voz no disponible o falta permiso.", true);
            return;
        }
        if (estaEscuchando) {
            Log.w(TAG, "STT ya está escuchando");
            return;
        }
        this.confirmacionListener = listener;
        this.opcionListener = null;


        iniciarEscucha();
    }

    public void seleccionarOpcion(List<String> opciones, OnOpcionSeleccionadaListener listener) {
        if (!PreferencesManager.isModoAudioActivado(context)) return;

        if (!isReady()) {
            Log.w(TAG, "STT no listo o falta permiso");
            if (salidaAudio != null)
                salidaAudio.hablar("Reconocimiento de voz no disponible o falta permiso.", true);
            return;
        }
        if (estaEscuchando) {
            Log.w(TAG, "STT ya está escuchando");
            return;
        }
        this.opcionesActuales = normalizarLista(opciones);
        this.opcionListener = listener;
        this.confirmacionListener = null;
        iniciarEscucha();
    }

    private void iniciarEscucha() {
        if (speechRecognizer == null) {
            Log.e(TAG, "SpeechRecognizer nulo, no se puede iniciar escucha");
            return;
        }

        mainHandler.post(() -> {
            try {
                estaEscuchando = true;
                speechRecognizer.startListening(getRecognizerIntent());
            } catch (Exception e) {
                estaEscuchando = false;
                Log.e(TAG, "Error startListening: " + e.getMessage(), e);
                if (salidaAudio != null) salidaAudio.hablar("Error al iniciar escucha.", true);
            }
        });
    }

    public void detenerEscucha() {
        if (speechRecognizer != null && estaEscuchando) {
            try {

                mainHandler.post(() -> {
                    try {
                        speechRecognizer.stopListening();
                    } catch (Exception ex) {
                        Log.w(TAG, "stopListening ex: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.w(TAG, "Error al detener escucha: " + e.getMessage());
            }
            estaEscuchando = false;
            Log.d(TAG, "Escucha detenida");
        }
    }

    public void liberar() {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.destroy();
            } catch (Exception ignored) {
            }
            speechRecognizer = null;
        }
        instancia = null;
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        return texto.toLowerCase(Locale.ROOT)
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("ñ", "n");
    }

    private List<String> normalizarLista(List<String> lista) {
        List<String> normalizada = new ArrayList<>();
        for (String item : lista) {
            normalizada.add(normalizar(item));
        }
        return normalizada;
    }

    @Override
    public void onResults(Bundle results) {
        estaEscuchando = false;
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String resultado = normalizar(matches.get(0));
            Log.d(TAG, "Resultado reconocido: " + resultado);

            if (confirmacionListener != null) {
                if (AFIRMACIONES.contains(resultado)) {
                    OnConfirmacionListener cb = confirmacionListener;
                    confirmacionListener = null;
                    if (cb != null) cb.onResultado(true);
                    return;
                } else if (NEGACIONES.contains(resultado)) {
                    OnConfirmacionListener cb = confirmacionListener;
                    confirmacionListener = null;
                    if (cb != null) cb.onResultado(false);
                    return;
                }
            }

            if (opcionListener != null && opcionesActuales != null) {
                for (int i = 0; i < opcionesActuales.size(); i++) {
                    if (resultado.contains(opcionesActuales.get(i))) {
                        OnOpcionSeleccionadaListener cb = opcionListener;
                        opcionListener = null;
                        if (cb != null) cb.onOpcionSeleccionada(i);
                        return;
                    }
                }
            }
        }


        if (salidaAudio != null) {
            salidaAudio.hablar("No entendí. Repita por favor.", true, () -> iniciarEscucha());
        } else {
            iniciarEscucha();
        }
    }

    @Override
    public void onError(int error) {
        estaEscuchando = false;
        Log.e(TAG, "Error en STT: " + error);
        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            if (salidaAudio != null)
                salidaAudio.hablar("No tengo permiso para usar el micrófono.", true);
            opcionListener = null;
            confirmacionListener = null;
            return;
        }

        mainHandler.postDelayed(this::iniciarEscucha, 400);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "Listo para escuchar");
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    public interface OnConfirmacionListener {
        void onResultado(boolean esAfirmacion);
    }

    public interface OnOpcionSeleccionadaListener {
        void onOpcionSeleccionada(int indice);
    }
}