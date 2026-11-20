package com.cyberpath.smartlearn.util.accesibilidad.visual;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntradaAudio implements RecognitionListener {

    private static final String TAG = "EntradaVoz";

    private static final long TIMEOUT_ESCUCHA_MS = 8000L;

    private static final int MAX_REINTENTOS_CONSECUTIVOS = 3;

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
    private final AtomicBoolean estaEscuchando = new AtomicBoolean(false);
    private final Set<String> afirmacionesTokenizadas = new HashSet<>();
    private final Set<String> negacionesTokenizadas = new HashSet<>();
    private SpeechRecognizer speechRecognizer;
    private boolean estaInicializado = false;
    private volatile boolean escuchaHabilitada = false;
    private volatile boolean detenerSolicitado = false;
    private int reintentosConsecutivos = 0;
    private OnConfirmacionListener confirmacionListener;
    private OnOpcionSeleccionadaListener opcionListener;    private final Runnable reintentarEscucha = () -> {
        if (!escuchaHabilitada || detenerSolicitado) {
            return;
        }
        iniciarEscucha();
    };
    private OnTextoReconocidoListener textoReconocidoListener;
    private List<String> opcionesActuales;    private final Runnable watchdogTimeout = this::manejarTimeoutEscucha;
    private List<String> opcionesActualesTokenizadas;
    private EntradaAudio(Context context) {
        this.context = context.getApplicationContext();

        if (SalidaAudio.obtenerInstancia() == null) {
            SalidaAudio.iniciarInstancia(this.context);
        }
        this.salidaAudio = SalidaAudio.obtenerInstancia();

        inicializarMapeos();

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
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isReady() {
        return hasRecordPermission() && estaInicializado && speechRecognizer != null;
    }

    public void confirmarAfirmacion(OnConfirmacionListener listener) {
        if (!PreferencesManager.isAsistenciaVozActivada(context)) return;

        if (!isReady()) {
            Log.w(TAG, "Reconocimiento no listo o falta permiso");
            if (salidaAudio != null) {
                salidaAudio.hablar("Reconocimiento de voz no disponible o falta permiso.", true);
            }
            return;
        }

        if (estaEscuchando.get()) {
            Log.w(TAG, "Reconocimiento ya está escuchando");
            return;
        }

        this.confirmacionListener = listener;
        this.opcionListener = null;
        this.textoReconocidoListener = null;
        this.opcionesActuales = null;
        this.opcionesActualesTokenizadas = null;

        habilitarEscucha();
        iniciarEscucha();
    }

    public void seleccionarOpcion(List<String> opciones, OnOpcionSeleccionadaListener listener) {
        if (!PreferencesManager.isAsistenciaVozActivada(context)) return;

        if (!isReady()) {
            Log.w(TAG, "Reconocimiento no listo o falta permiso");
            if (salidaAudio != null) {
                salidaAudio.hablar("Reconocimiento de voz no disponible o falta permiso.", true);
            }
            return;
        }

        if (estaEscuchando.get()) {
            Log.w(TAG, "Reconocimiento ya está escuchando");
            return;
        }

        this.opcionesActuales = normalizarLista(opciones);
        this.opcionesActualesTokenizadas = tokenizarLista(opciones);
        this.opcionListener = listener;
        this.confirmacionListener = null;
        this.textoReconocidoListener = null;

        habilitarEscucha();
        iniciarEscucha();
    }

    public void escucharTextoLibreAndroid(OnTextoReconocidoListener listener) {
        if (!PreferencesManager.isAsistenciaVozActivada(context)) return;

        if (!isReady()) {
            Log.w(TAG, "SpeechRecognizer no listo o falta permiso");
            if (salidaAudio != null) {
                salidaAudio.hablar("Reconocimiento de voz de Android no disponible o falta permiso.", true);
            }
            return;
        }

        if (estaEscuchando.get()) {
            Log.w(TAG, "Reconocimiento ya está escuchando");
            return;
        }

        this.confirmacionListener = null;
        this.opcionListener = null;
        this.opcionesActuales = null;
        this.opcionesActualesTokenizadas = null;
        this.textoReconocidoListener = listener;

        habilitarEscucha();
        iniciarEscucha();
    }

    private void habilitarEscucha() {
        detenerSolicitado = false;
        escuchaHabilitada = true;
        reintentosConsecutivos = 0;
        mainHandler.removeCallbacks(reintentarEscucha);
        mainHandler.removeCallbacks(watchdogTimeout);
    }

    private void iniciarEscucha() {
        if (!escuchaHabilitada || detenerSolicitado) {
            return;
        }


        if (!estaEscuchando.compareAndSet(false, true)) {
            Log.w(TAG, "iniciarEscucha() ignorado: ya hay una sesión en curso");
            return;
        }

        if (speechRecognizer == null) {
            Log.e(TAG, "SpeechRecognizer nulo, no se puede iniciar escucha");
            estaEscuchando.set(false);
            return;
        }

        mainHandler.post(() -> {
            try {
                speechRecognizer.startListening(getRecognizerIntent());


                mainHandler.removeCallbacks(watchdogTimeout);
                mainHandler.postDelayed(watchdogTimeout, TIMEOUT_ESCUCHA_MS);
            } catch (Exception e) {
                estaEscuchando.set(false);
                Log.e(TAG, "Error startListening: " + e.getMessage(), e);
                if (salidaAudio != null) {
                    salidaAudio.hablar("Error al iniciar escucha.", true);
                }
            }
        });
    }

    private void manejarTimeoutEscucha() {
        if (!estaEscuchando.get()) {
            return;
        }
        Log.e(TAG, "Timeout de escucha alcanzado, forzando cancelación");
        estaEscuchando.set(false);

        try {
            if (speechRecognizer != null) {
                speechRecognizer.cancel();
            }
        } catch (Exception ignored) {
        }

        notificarFalloYDetener("No se recibió respuesta del micrófono.");
    }

    public void detenerEscucha() {
        detenerSolicitado = true;
        escuchaHabilitada = false;
        mainHandler.removeCallbacks(reintentarEscucha);
        mainHandler.removeCallbacks(watchdogTimeout);
        estaEscuchando.set(false);
        confirmacionListener = null;
        opcionListener = null;
        textoReconocidoListener = null;
        opcionesActuales = null;
        opcionesActualesTokenizadas = null;
        reintentosConsecutivos = 0;

        if (speechRecognizer != null) {
            try {
                mainHandler.post(() -> {
                    try {
                        speechRecognizer.stopListening();
                    } catch (Exception ex) {
                        Log.w(TAG, "stopListening ex: " + ex.getMessage());
                    }
                    try {
                        speechRecognizer.cancel();
                    } catch (Exception ex) {
                        Log.w(TAG, "cancel ex: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.w(TAG, "Error al detener escucha: " + e.getMessage());
            }
        }
        Log.d(TAG, "Escucha detenida");
    }

    public void liberar() {
        detenerEscucha();
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
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n");
    }

    private String tokenizar(String texto) {
        return normalizar(texto)
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");
    }

    private List<String> normalizarLista(List<String> lista) {
        List<String> normalizada = new ArrayList<>();
        for (String item : lista) {
            normalizada.add(normalizar(item));
        }
        return normalizada;
    }

    private List<String> tokenizarLista(List<String> lista) {
        if (lista == null) {
            return Collections.emptyList();
        }
        List<String> tokens = new ArrayList<>();
        for (String item : lista) {
            tokens.add(tokenizar(item));
        }
        return tokens;
    }

    private void inicializarMapeos() {
        afirmacionesTokenizadas.clear();
        negacionesTokenizadas.clear();

        for (String afirmacion : AFIRMACIONES) {
            afirmacionesTokenizadas.add(tokenizar(afirmacion));
        }
        for (String negacion : NEGACIONES) {
            negacionesTokenizadas.add(tokenizar(negacion));
        }
    }

    private void limpiarEstadoResultado() {
        mainHandler.removeCallbacks(watchdogTimeout);
        reintentosConsecutivos = 0;
    }

    private void notificarFalloYDetener(String mensajeHablado) {
        escuchaHabilitada = false;
        mainHandler.removeCallbacks(reintentarEscucha);
        mainHandler.removeCallbacks(watchdogTimeout);

        OnConfirmacionListener cbConfirmacion = confirmacionListener;
        OnOpcionSeleccionadaListener cbOpcion = opcionListener;
        OnTextoReconocidoListener cbTexto = textoReconocidoListener;

        confirmacionListener = null;
        opcionListener = null;
        textoReconocidoListener = null;
        opcionesActuales = null;
        opcionesActualesTokenizadas = null;
        reintentosConsecutivos = 0;

        if (salidaAudio != null) {
            salidaAudio.hablar(mensajeHablado, true);
        }


        if (cbConfirmacion != null) cbConfirmacion.onResultado(false);
        if (cbOpcion != null) cbOpcion.onOpcionSeleccionada(-1);
        if (cbTexto != null) cbTexto.onTextoReconocido(null);
    }

    @Override
    public void onResults(Bundle results) {
        estaEscuchando.set(false);
        limpiarEstadoResultado();
        if (!escuchaHabilitada || detenerSolicitado) {
            return;
        }

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (textoReconocidoListener != null) {
            if (matches != null && !matches.isEmpty()) {
                String resultado = normalizar(matches.get(0));
                OnTextoReconocidoListener cb = textoReconocidoListener;
                textoReconocidoListener = null;
                if (cb != null) cb.onTextoReconocido(resultado);
                return;
            }
        }

        if (matches != null && !matches.isEmpty()) {
            String resultado = normalizar(matches.get(0));
            Log.d(TAG, "Resultado reconocido por STT: " + resultado);

            if (confirmacionListener != null) {
                String tokenResultado = tokenizar(resultado);
                if (afirmacionesTokenizadas.contains(tokenResultado)) {
                    OnConfirmacionListener cb = confirmacionListener;
                    confirmacionListener = null;
                    if (cb != null) cb.onResultado(true);
                    return;
                } else if (negacionesTokenizadas.contains(tokenResultado)) {
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


        reintentosConsecutivos++;
        if (reintentosConsecutivos > MAX_REINTENTOS_CONSECUTIVOS) {
            notificarFalloYDetener("No logré entenderte. Intenta de nuevo más tarde.");
            return;
        }

        if (salidaAudio != null) {
            salidaAudio.hablar("No entendí. Repita por favor.", true, () -> {
                if (escuchaHabilitada && !detenerSolicitado) {
                    iniciarEscucha();
                }
            });
        } else {
            iniciarEscucha();
        }
    }

    @Override
    public void onError(int error) {
        estaEscuchando.set(false);
        mainHandler.removeCallbacks(watchdogTimeout);
        if (!escuchaHabilitada || detenerSolicitado) {
            return;
        }
        Log.e(TAG, "Error en STT: " + error);
        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            if (salidaAudio != null)
                salidaAudio.hablar("No tengo permiso para usar el micrófono.", true);
            opcionListener = null;
            confirmacionListener = null;
            opcionesActuales = null;
            opcionesActualesTokenizadas = null;
            escuchaHabilitada = false;
            reintentosConsecutivos = 0;
            return;
        }


        reintentosConsecutivos++;
        if (reintentosConsecutivos > MAX_REINTENTOS_CONSECUTIVOS) {
            notificarFalloYDetener("Hubo un problema con el reconocimiento de voz.");
            return;
        }

        mainHandler.removeCallbacks(reintentarEscucha);
        mainHandler.postDelayed(reintentarEscucha, 400);
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

    public interface OnTextoReconocidoListener {
        void onTextoReconocido(String textoReconocido);
    }






}