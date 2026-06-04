package com.cyberpath.smartlearn.util.accesibilidad.visual;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cyberpath.smartlearn.util.audioRecognizer.EdgeImpulseAudioClassifier;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EntradaAudio implements RecognitionListener {

    private static final String TAG = "EntradaVoz";
    private static final int SAMPLE_RATE = 16000;
    private static final int RAW_AUDIO_SAMPLE_COUNT = 16000;

    private static final List<String> AFIRMACIONES = Arrays.asList(
            "sí", "si", "correcto", "así es", "afirmativo", "ok", "dale", "de acuerdo"
    );
    private static final List<String> NEGACIONES = Arrays.asList(
            "no", "incorrecto", "eso no", "negativo", "nop", "de ninguna manera"
    );

    private static EntradaAudio instancia;

    private final Context context;
    private final SalidaAudio salidaAudio;
    private final EdgeImpulseAudioClassifier edgeClassifier;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Object modelAudioLock = new Object();

    private SpeechRecognizer speechRecognizer;
    private AudioRecord modelAudioRecord;
    private Thread modelThread;

    private boolean estaInicializado = false;
    private volatile boolean estaEscuchando = false;
    private volatile boolean escuchaHabilitada = false;
    private volatile boolean detenerSolicitado = false;

    private final Set<String> etiquetasModeloTokenizadas = new HashSet<>();
    private final Set<String> afirmacionesTokenizadas = new HashSet<>();
    private final Set<String> negacionesTokenizadas = new HashSet<>();

    private final Runnable reintentarEscucha = () -> {
        if (!escuchaHabilitada || detenerSolicitado) {
            return;
        }
        iniciarEscucha();
    };

    private OnConfirmacionListener confirmacionListener;
    private OnOpcionSeleccionadaListener opcionListener;
    private OnTextoReconocidoListener textoReconocidoListener;
    private List<String> opcionesActuales;
    private List<String> opcionesActualesTokenizadas;
    private boolean escucharSoloAndroid;

    private EntradaAudio(Context context) {
        this.context = context.getApplicationContext();
        this.edgeClassifier = EdgeImpulseAudioClassifier.obtenerInstancia();

        if (SalidaAudio.obtenerInstancia() == null) {
            SalidaAudio.iniciarInstancia(this.context);
        }
        this.salidaAudio = SalidaAudio.obtenerInstancia();

        inicializarMapeos();
        cargarEtiquetasModelo();

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
        if (!hasRecordPermission()) {
            return false;
        }
        return modeloDisponible() || (estaInicializado && speechRecognizer != null);
    }

    public boolean isSpeechRecognizerReady() {
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

        if (estaEscuchando) {
            Log.w(TAG, "Reconocimiento ya está escuchando");
            return;
        }

        this.confirmacionListener = listener;
        this.opcionListener = null;
        this.textoReconocidoListener = null;
        this.opcionesActuales = null;
        this.opcionesActualesTokenizadas = null;
        this.escucharSoloAndroid = false;

        habilitarEscucha();
        iniciarEscuchaHibrida();
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

        if (estaEscuchando) {
            Log.w(TAG, "Reconocimiento ya está escuchando");
            return;
        }

        this.opcionesActuales = normalizarLista(opciones);
        this.opcionesActualesTokenizadas = tokenizarLista(opciones);
        this.opcionListener = listener;
        this.confirmacionListener = null;
        this.textoReconocidoListener = null;
        this.escucharSoloAndroid = false;

        habilitarEscucha();
        iniciarEscuchaHibrida();
    }

    public void escucharTextoLibreAndroid(OnTextoReconocidoListener listener) {
        if (!PreferencesManager.isAsistenciaVozActivada(context)) return;

        if (!isSpeechRecognizerReady()) {
            Log.w(TAG, "SpeechRecognizer no listo o falta permiso");
            if (salidaAudio != null) {
                salidaAudio.hablar("Reconocimiento de voz de Android no disponible o falta permiso.", true);
            }
            return;
        }

        if (estaEscuchando) {
            Log.w(TAG, "Reconocimiento ya está escuchando");
            return;
        }

        this.confirmacionListener = null;
        this.opcionListener = null;
        this.opcionesActuales = null;
        this.opcionesActualesTokenizadas = null;
        this.textoReconocidoListener = listener;
        this.escucharSoloAndroid = true;

        habilitarEscucha();
        iniciarEscucha();
    }

    private void habilitarEscucha() {
        detenerSolicitado = false;
        escuchaHabilitada = true;
        mainHandler.removeCallbacks(reintentarEscucha);
    }

    private boolean modeloDisponible() {
        return edgeClassifier != null && edgeClassifier.isReady() && !etiquetasModeloTokenizadas.isEmpty();
    }

    private void iniciarEscuchaHibrida() {
        if (modeloDisponible()) {
            iniciarEscuchaConModelo();
            return;
        }
        iniciarEscucha();
    }

     private void iniciarEscuchaConModelo() {
         if (!escuchaHabilitada || detenerSolicitado || estaEscuchando) {
             return;
         }

         estaEscuchando = true;
         modelThread = new Thread(() -> {
             try {
                 if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                     // TODO: Consider calling
                     //    ActivityCompat#requestPermissions
                     // here to request the missing permissions, and then overriding
                     //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                     //                                          int[] grantResults)
                     // to handle the case where the user grants the permission. See the documentation
                     // for ActivityCompat#requestPermissions for more details.
                     return;
                 }
                 String etiquetaDetectada = capturarYClasificarConModelo();
                 mainHandler.post(() -> procesarResultadoModelo(etiquetaDetectada));
             } catch (Exception e) {
                 Log.e(TAG, "Error no capturado en thread de modelo", e);
                 mainHandler.post(() -> procesarResultadoModelo(null));
             }
         }, "EI-Model-Recognizer");
         modelThread.start();
     }

    private void iniciarEscucha() {
        if (!escuchaHabilitada || detenerSolicitado) {
            return;
        }

        if (estaEscuchando) {
            return;
        }

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
                if (salidaAudio != null) {
                    salidaAudio.hablar("Error al iniciar escucha.", true);
                }
            }
        });
    }

    public void detenerEscucha() {
        detenerSolicitado = true;
        escuchaHabilitada = false;
        mainHandler.removeCallbacks(reintentarEscucha);
        estaEscuchando = false;
        confirmacionListener = null;
        opcionListener = null;
        textoReconocidoListener = null;
        opcionesActuales = null;
        opcionesActualesTokenizadas = null;
        escucharSoloAndroid = false;

        detenerCapturaModelo();

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

     private void cargarEtiquetasModelo() {
         etiquetasModeloTokenizadas.clear();
         if (edgeClassifier == null) {
             Log.w(TAG, "EdgeImpulseAudioClassifier es nulo");
             return;
         }

         if (!edgeClassifier.isReady()) {
             Log.w(TAG, "Edge Impulse Classifier no está listo");
             return;
         }

         try {
             List<String> etiquetas = edgeClassifier.obtenerEtiquetasModelo();
             if (etiquetas == null || etiquetas.isEmpty()) {
                 Log.w(TAG, "No se obtuvieron etiquetas del modelo");
                 return;
             }

             for (String etiqueta : etiquetas) {
                 etiquetasModeloTokenizadas.add(tokenizar(etiqueta));
             }
             Log.d(TAG, "Etiquetas del modelo cargadas: " + etiquetasModeloTokenizadas);
         } catch (Exception e) {
             Log.e(TAG, "Error cargando etiquetas del modelo", e);
         }
     }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private String capturarYClasificarConModelo() {
        if (!escuchaHabilitada || detenerSolicitado || !hasRecordPermission() || !modeloDisponible()) {
            return null;
        }

        int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Buffer invalido para captura con modelo");
            return null;
        }

        if (bufferSize < RAW_AUDIO_SAMPLE_COUNT) {
            bufferSize = RAW_AUDIO_SAMPLE_COUNT;
        }

        AudioRecord localAudioRecord = null;
        short[] audioBuffer = new short[RAW_AUDIO_SAMPLE_COUNT];
        try {

            localAudioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
            );

            if (localAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "No se pudo inicializar AudioRecord para Edge Impulse");
                localAudioRecord.release();
                return null;
            }

            synchronized (modelAudioLock) {
                modelAudioRecord = localAudioRecord;
            }

            localAudioRecord.startRecording();
            int offset = 0;

            while (offset < RAW_AUDIO_SAMPLE_COUNT && escuchaHabilitada && !detenerSolicitado) {
                int read = localAudioRecord.read(audioBuffer, offset, RAW_AUDIO_SAMPLE_COUNT - offset);
                if (read > 0) {
                    offset += read;
                } else {
                    Log.e(TAG, "Error leyendo audio para modelo: " + read);
                    return null;
                }
            }

            if (offset < RAW_AUDIO_SAMPLE_COUNT || !escuchaHabilitada || detenerSolicitado) {
                return null;
            }

            String etiqueta = edgeClassifier.clasificar(audioBuffer);
            if (etiqueta == null || etiqueta.trim().isEmpty()) {
                return null;
            }

            String etiquetaToken = tokenizar(etiqueta);
            if (!etiquetasModeloTokenizadas.contains(etiquetaToken)) {
                return null;
            }

            return etiqueta;
        } catch (Exception e) {
            Log.e(TAG, "Error en captura/clasificacion con modelo", e);
            return null;
        } finally {
            if (localAudioRecord != null) {
                try {
                    localAudioRecord.stop();
                } catch (Exception ignored) {
                }
                try {
                    localAudioRecord.release();
                } catch (Exception ignored) {
                }
            }
            synchronized (modelAudioLock) {
                if (modelAudioRecord == localAudioRecord) {
                    modelAudioRecord = null;
                }
            }
        }
    }

     private void procesarResultadoModelo(String etiquetaDetectada) {
         estaEscuchando = false;

         if (!escuchaHabilitada || detenerSolicitado) {
             return;
         }

         Log.d(TAG, "Procesando resultado del modelo: " + (etiquetaDetectada != null ? etiquetaDetectada : "nulo"));

         if (resolverResultadoModelo(etiquetaDetectada)) {
             Log.d(TAG, "Resultado del modelo resuelta exitosamente");
             return;
         }

         Log.d(TAG, "Modelo no resolvió, fallback a STT");

         if (estaInicializado && speechRecognizer != null) {
             iniciarEscucha();
             return;
         }

         if (salidaAudio != null) {
             salidaAudio.hablar("No entendí. Repita por favor.", true, () -> {
                 if (escuchaHabilitada && !detenerSolicitado) {
                     iniciarEscuchaHibrida();
                 }
             });
         } else {
             iniciarEscuchaHibrida();
         }
     }

     private boolean resolverResultadoModelo(String etiquetaDetectada) {
         if (etiquetaDetectada == null || etiquetaDetectada.trim().isEmpty()) {
             Log.d(TAG, "Etiqueta detectada es nula o vacía");
             return false;
         }

         String etiquetaToken = tokenizar(etiquetaDetectada);
         if (!etiquetasModeloTokenizadas.contains(etiquetaToken)) {
             Log.w(TAG, "Etiqueta '" + etiquetaDetectada + "' (token: '" + etiquetaToken + "') no está en etiquetas del modelo. Disponibles: " + etiquetasModeloTokenizadas);
             return false;
         }

         if (confirmacionListener != null) {
             Log.d(TAG, "Evaluando confirmacion. EtiquetaToken: " + etiquetaToken);
             if (afirmacionesTokenizadas.contains(etiquetaToken)) {
                 Log.d(TAG, "Confirmacion positiva detectada");
                 OnConfirmacionListener cb = confirmacionListener;
                 confirmacionListener = null;
                 if (cb != null) cb.onResultado(true);
                 return true;
             }

             if (negacionesTokenizadas.contains(etiquetaToken)) {
                 Log.d(TAG, "Confirmacion negativa detectada");
                 OnConfirmacionListener cb = confirmacionListener;
                 confirmacionListener = null;
                 if (cb != null) cb.onResultado(false);
                 return true;
             }
         }

         if (opcionListener != null && opcionesActualesTokenizadas != null) {
             Log.d(TAG, "Buscando en opciones: " + opcionesActualesTokenizadas);
             for (int i = 0; i < opcionesActualesTokenizadas.size(); i++) {
                 String opcionToken = opcionesActualesTokenizadas.get(i);
                 if (opcionToken.contains(etiquetaToken) || etiquetaToken.contains(opcionToken)) {
                     Log.d(TAG, "Opcion seleccionada: index=" + i + ", token=" + opcionToken);
                     OnOpcionSeleccionadaListener cb = opcionListener;
                     opcionListener = null;
                     if (cb != null) cb.onOpcionSeleccionada(i);
                     return true;
                 }
             }
             Log.w(TAG, "No se encontró coincidencia en opciones para token: " + etiquetaToken);
         }

         return false;
     }

    private void detenerCapturaModelo() {
        Thread hiloActual = modelThread;
        modelThread = null;

        synchronized (modelAudioLock) {
            if (modelAudioRecord != null) {
                try {
                    modelAudioRecord.stop();
                } catch (Exception ignored) {
                }
                try {
                    modelAudioRecord.release();
                } catch (Exception ignored) {
                }
                modelAudioRecord = null;
            }
        }

        if (hiloActual != null && hiloActual != Thread.currentThread()) {
            try {
                hiloActual.join(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

     @Override
     public void onResults(Bundle results) {
         estaEscuchando = false;
         if (!escuchaHabilitada || detenerSolicitado) {
             return;
         }

         ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
          if (textoReconocidoListener != null) {
              if (matches != null && !matches.isEmpty()) {
                  String resultado = normalizar(matches.get(0));
                  OnTextoReconocidoListener cb = textoReconocidoListener;
                  textoReconocidoListener = null;
                  escucharSoloAndroid = false;
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

         // STT no entendió - reintentar con modelo o STT nuevamente
         if (salidaAudio != null) {
             salidaAudio.hablar("No entendí. Repita por favor.", true, () -> {
                 if (escuchaHabilitada && !detenerSolicitado) {
                      if (escucharSoloAndroid) {
                          iniciarEscucha();
                      } else {
                          iniciarEscuchaHibrida(); // Reintentar con modelo primero
                      }
                 }
             });
         } else {
              if (escucharSoloAndroid) {
                  iniciarEscucha();
              } else {
                  iniciarEscuchaHibrida();
              }
         }
     }

    @Override
    public void onError(int error) {
        estaEscuchando = false;
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