# 📋 ANÁLISIS COMPLETO: FLUJO DE VOZ CON MODELO EDGE IMPULSE
**Proyecto:** SmartLearn Móvil  
**Fecha:** Junio 2026  
**Enfoque:** Verificación de funcionamiento del modelo y integración nativa

---

## 🎯 RESUMEN EJECUTIVO

El proyecto tiene implementado un **sistema híbrido de reconocimiento de voz**:
1. **Sistema Nativo (C++)**: Modelo Edge Impulse compilado en JNI
2. **Sistema Fallback**: Google Speech Recognition (STT nativo de Android)

**ESTADO**: Sistema **correctamente implementado** pero con **áreas de mejora críticas**.

---

## 📊 ARQUITECTURA DEL FLUJO DE VOZ

```
┌─────────────────────────────────────────────────────────────┐
│              FLUJO DE RECONOCIMIENTO DE VOZ                 │
└─────────────────────────────────────────────────────────────┘

    ┌─────────────────────────────────────────────────────┐
    │      EntradaAudio (Orquestador Principal)          │
    │  - Singleton pattern                               │
    │  - Maneja ambos sistemas de reconocimiento        │
    └─────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
   ┌──────────────┐  ┌──────────────┐  ┌──────────┐
   │  EdgeImpulse │  │ SpeechRecog- │  │ SalidaAudio
   │   Classifier │  │   nizer(STT) │  │   (TTS)
   │  (C++/JNI)   │  │   (Android)  │  │
   └──────────────┘  └──────────────┘  └──────────┘
         │                   │              │
         └─────────┬─────────┴──────────────┘
                   │
                   ▼
          ┌────────────────┐
          │   Respuesta    │
          │   Detectada    │
          └────────────────┘
```

---

## 🔧 COMPONENTES PRINCIPALES

### 1️⃣ **EdgeImpulseAudioClassifier.java**
**Ubicación:** `util/audioRecognizer/EdgeImpulseAudioClassifier.java`

#### Características:
```java
✅ Singleton pattern implementado correctamente
✅ Carga biblioteca nativa (smartlearn_audio)
✅ Cache de etiquetas del modelo
✅ Métodos nativos JNI:
   - classifyAudioNative(short[])
   - getModelLabelsNative()
```

#### Potencial Problema:
```java
// Línea 35-37: isReady() solo verifica carga de librería
public boolean isReady() {
    return bibliotecaCargada;
}
// ⚠️ NO verifica si el modelo fue inicializado correctamente
// ⚠️ NO valida si el modelo tiene etiquetas
```

**RECOMENDACIÓN**: Mejorar validación de estado

---

### 2️⃣ **EntradaAudio.java**
**Ubicación:** `util/accesibilidad/visual/EntradaAudio.java`

#### Características Principales:
- ✅ Implementa `RecognitionListener` para STT
- ✅ Sistema híbrido: intenta modelo primero, fallback a STT
- ✅ Tokenización inteligente de palabras (normalización de acentos)
- ✅ Manejo de confirmaciones y selección de opciones

#### Lógica Crítica:
```
iniciarEscucha() 
  ↓
¿Modelo disponible?
  ├─ SÍ → capturarYClasificarConModelo() (16000 muestras = 1s)
  └─ NO → Google STT fallback
```

#### Problemas Identificados:

**PROBLEMA #1: Inicio de sesión del modelo sin verificación adecuada**
```java
// Línea 203-214: iniciarEscuchaConModelo()
private void iniciarEscuchaConModelo() {
    if (!escuchaHabilitada || detenerSolicitado || estaEscuchando) {
        return;
    }
    
    estaEscuchando = true;
    modelThread = new Thread(() -> {
        String etiquetaDetectada = capturarYClasificarConModelo();
        mainHandler.post(() -> procesarResultadoModelo(etiquetaDetectada));
    }, "EI-Model-Recognizer");
    modelThread.start();
}

⚠️ PROBLEMA: No hay catch de excepciones en el thread
⚠️ Si hay crash en el modelo, estaEscuchando queda en TRUE
⚠️ Impedirá escuchas futuras
```

**PROBLEMA #2: Captura de audio sin validación de buffer completo**
```java
// Línea 348-437: capturarYClasificarConModelo()

while (offset < RAW_AUDIO_SAMPLE_COUNT && escuchaHabilitada && !detenerSolicitado) {
    int read = localAudioRecord.read(audioBuffer, offset, RAW_AUDIO_SAMPLE_COUNT - offset);
    if (read > 0) {
        offset += read;
    } else {
        Log.e(TAG, "Error leyendo audio para modelo: " + read);
        return null;  // ⚠️ PROBLEMA: Sale sin limpiar AudioRecord
    }
}

// Línea 402-404: Valida buffer incompleto pero NO reinicia
if (offset < RAW_AUDIO_SAMPLE_COUNT || !escuchaHabilitada || !detenerSolicitado) {
    return null;  // ⚠️ PROBLEMA: Retorna null sin explicar por qué
}
```

---

### 3️⃣ **native-lib.cpp** (C++ - Crítico)
**Ubicación:** `src/main/cpp/native-lib.cpp`

#### Función Clave:
```cpp
static jstring classify_audio(JNIEnv *env, jshortArray audioData) {
    // 1. Obtener datos de audio
    jshort* buffer = env->GetShortArrayElements(audioData, nullptr);
    jsize length = env->GetArrayLength(audioData);
    
    // 2. Convertir a float
    float* float_buffer = new float[length];
    for (jsize i = 0; i < length; i++) {
        float_buffer[i] = static_cast<float>(buffer[i]);  // ⚠️ SIN NORMALIZACIÓN
    }
    
    // 3. Crear señal para Edge Impulse
    signal_t signal;
    int err = numpy::signal_from_buffer(float_buffer, length, &signal);
    
    // 4. Ejecutar clasificador
    ei_impulse_result_t result = { 0 };
    err = run_classifier(&signal, &result, false);
    
    // 5. Encontrar label con máxima confianza
    int max_idx = 0;
    float max_val = 0.0;
    for (size_t ix = 0; ix < EI_CLASSIFIER_LABEL_COUNT; ix++) {
        if (result.classification[ix].value > max_val) {
            max_val = result.classification[ix].value;
            max_idx = ix;
        }
    }
    
    // 6. Verificar threshold
    if (max_val < EI_CLASSIFIER_THRESHOLD) {
        return env->NewStringUTF("");  // Clase "unknown"
    }
    
    return env->NewStringUTF(result.classification[max_idx].label);
}
```

#### Problemas Encontrados:

**PROBLEMA #3: Conversión de short a float SIN normalización**
```cpp
// Línea 10-13: Convertidor INCORRECTO
float_buffer[i] = static_cast<float>(buffer[i]);

// ⚠️ CRÍTICO: El rango es:
//    short: [-32768, 32767]
//    float (sin normalizar): [-32768.0f, 32767.0f]
//
// El modelo espera valores en rango [-1.0, 1.0] típicamente
// SOLUCIÓN: Normalizar dividiendo por 32768.0f

// CORRECTO:
float_buffer[i] = (float)buffer[i] / 32768.0f;
```

**PROBLEMA #4: Sin validación de entrada**
```cpp
// Línea 7-8: No valida que el array sea válido
jshort* buffer = env->GetShortArrayElements(audioData, nullptr);
jsize length = env->GetArrayLength(audioData);

// ⚠️ Si audioData es NULL, GetShortArrayElements devuelve NULL
// ⚠️ Si length != 16000, el modelo puede fallar
// ⚠️ Sin manejo de excepciones JNI
```

**PROBLEMA #5: Sin manejo de errores en el modelo**
```cpp
// Línea 15-16: Sin validación
signal_t signal;
int err = numpy::signal_from_buffer(float_buffer, length, &signal);

// ⚠️ No verifica si length es exactamente 16000
// ⚠️ No valida que signal se creó correctamente
// ⚠️ Si hay error, sigue adelante
```

---

### 4️⃣ **Configuración del Modelo**
**Ubicación:** `src/main/cpp/model-parameters/model_variables.h`

#### Parámetros del Modelo:
```cpp
34 etiquetas detectables:
- Números: uno, dos, tres, ..., diez
- Comandos: opciones, repetir, seleccionar, regresar, salir, etc.
- Confirmaciones: si, no, correcto, incorrecto, ok, etc.

DSP Configuración (MFE - Mel Frequency Cepstral Coefficients):
- Frame length: 0.02s (20ms)
- Frame stride: 0.01s (10ms)
- Filters: 40
- FFT length: 256
- Output features: 3960

Modelo:
- Entrada: 16000 samples @ 16kHz (1 segundo)
- Frecuencia esperada: 16000 Hz
- Arena TFLite: 162284 bytes (~158 KB)
```

---

## 🐛 PROBLEMAS CRÍTICOS ENCONTRADOS

| Prioridad | Problema | Ubicación | Impacto | Solución |
|-----------|----------|-----------|---------|----------|
| 🔴 CRÍTICA | Conversión short→float SIN normalización | native-lib.cpp:12 | Modelo recibe valores fuera de rango, baja precisión | Normalizar dividiendo por 32768 |
| 🔴 CRÍTICA | Sin validación de tamaño de buffer | native-lib.cpp:8 | Crash si no es exactamente 16000 muestras | Validar length == 16000 |
| 🟠 ALTA | Thread del modelo puede quedar bloqueado | EntradaAudio.java:203 | Escucha futura impedida | Try-catch en modelThread |
| 🟠 ALTA | Sin reintentos tras fallo del modelo | EntradaAudio.java:402 | Usuario ve "sin respuesta" | Implementar reintentos |
| 🟡 MEDIA | isReady() no valida modelo correctamente | EdgeImpulseAudioClassifier.java:35 | Falsa positive de disponibilidad | Validar etiquetas + librería |
| 🟡 MEDIA | Sin logging detallado de errores JNI | native-lib.cpp | Difícil debuggear fallos | Agregar logs con __android_log_print |

---

## ✅ ÁREAS QUE FUNCIONAN BIEN

1. **Inicialización del Audio**
   - ✅ Permisos correctamente solicitados
   - ✅ AudioRecord configurado con parámetros correctos
   - ✅ Buffer size válido (16000 muestras)

2. **Flujo Híbrido**
   - ✅ Intenta modelo primero (más rápido, más privado)
   - ✅ Fallback a Google STT (más preciso si modelo falla)
   - ✅ Tokenización inteligente para matching

3. **Interfaz TTS (Text-to-Speech)**
   - ✅ Correctamente configurado con español (es-MX)
   - ✅ Callbacks para sincronización
   - ✅ Queue management (FLUSH/ADD)

4. **Integración en UI**
   - ✅ NavAccesibilidad.java implementa flujos de voz correctamente
   - ✅ Callbacks bien estructurados
   - ✅ Estados manejados apropiadamente

---

## 📝 RECOMENDACIONES URGENTES

### 1. Arreglar native-lib.cpp (CRÍTICA)

```cpp
#include <jni.h>
#include <string>
#include <android/log.h>
#include "edge-impulse-sdk/classifier/ei_run_classifier.h"

#define LOG_TAG "SmartLearn-Audio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Constantes esperadas del modelo
static constexpr jsize EXPECTED_AUDIO_LENGTH = 16000;
static constexpr float NORMALIZATION_FACTOR = 32768.0f;

static jstring classify_audio(JNIEnv *env, jshortArray audioData) {
    // 1. VALIDAR ENTRADA
    if (audioData == nullptr) {
        LOGE("Error: audioData es NULL");
        return env->NewStringUTF("Error: datos de audio vacíos");
    }
    
    jshort* buffer = env->GetShortArrayElements(audioData, nullptr);
    if (buffer == nullptr) {
        LOGE("Error: No se pudo acceder al buffer de audio");
        return env->NewStringUTF("Error: No se pudo acceder al audio");
    }
    
    jsize length = env->GetArrayLength(audioData);
    
    // VALIDACIÓN CRÍTICA
    if (length != EXPECTED_AUDIO_LENGTH) {
        LOGE("Error: Audio length %ld != %d", (long)length, EXPECTED_AUDIO_LENGTH);
        env->ReleaseShortArrayElements(audioData, buffer, JNI_ABORT);
        return env->NewStringUTF("Error: Tamaño de audio incorrecto");
    }
    
    // 2. CONVERTIR A FLOAT CON NORMALIZACIÓN
    float* float_buffer = new float[length];
    try {
        for (jsize i = 0; i < length; i++) {
            // ✅ NORMALIZAR al rango [-1.0, 1.0]
            float_buffer[i] = (float)buffer[i] / NORMALIZATION_FACTOR;
        }
    } catch (...) {
        LOGE("Error: Excepción durante conversión de audio");
        delete[] float_buffer;
        env->ReleaseShortArrayElements(audioData, buffer, JNI_ABORT);
        return env->NewStringUTF("Error: Conversión de audio fallida");
    }
    
    env->ReleaseShortArrayElements(audioData, buffer, JNI_ABORT);
    
    // 3. CREAR SEÑAL
    signal_t signal;
    int err = numpy::signal_from_buffer(float_buffer, length, &signal);
    
    if (err != 0) {
        LOGE("Error creando señal: %d", err);
        delete[] float_buffer;
        return env->NewStringUTF("Error creando señal");
    }
    
    // 4. EJECUTAR CLASIFICADOR
    ei_impulse_result_t result = { 0 };
    err = run_classifier(&signal, &result, false);
    
    delete[] float_buffer;
    
    if (err != EI_IMPULSE_OK) {
        LOGE("Error en clasificación: %d", err);
        return env->NewStringUTF("Error en clasificación");
    }
    
    // 5. ENCONTRAR LABEL CON MÁXIMA CONFIANZA
    int max_idx = 0;
    float max_val = 0.0f;
    for (size_t ix = 0; ix < EI_CLASSIFIER_LABEL_COUNT; ix++) {
        if (result.classification[ix].value > max_val) {
            max_val = result.classification[ix].value;
            max_idx = ix;
        }
    }
    
    // 6. VERIFICAR THRESHOLD
    if (max_val < EI_CLASSIFIER_THRESHOLD) {
        LOGI("No clasificación: max_val=%f < threshold=%f", max_val, EI_CLASSIFIER_THRESHOLD);
        return env->NewStringUTF("");
    }
    
    const char* detected_label = result.classification[max_idx].label;
    LOGI("Detectado: %s (confianza: %f)", detected_label, max_val);
    
    return env->NewStringUTF(detected_label);
}

// [Mantener el resto de funciones JNI igual]
```

### 2. Mejorar EntradaAudio.java

```java
private void iniciarEscuchaConModelo() {
    if (!escuchaHabilitada || detenerSolicitado || estaEscuchando) {
        return;
    }
    
    estaEscuchando = true;
    modelThread = new Thread(() -> {
        try {
            String etiquetaDetectada = capturarYClasificarConModelo();
            mainHandler.post(() -> procesarResultadoModelo(etiquetaDetectada));
        } catch (Exception e) {
            Log.e(TAG, "Error en modelo de voz", e);
            mainHandler.post(() -> {
                estaEscuchando = false;
                if (escuchaHabilitada && !detenerSolicitado) {
                    // Fallback a STT
                    iniciarEscucha();
                }
            });
        }
    }, "EI-Model-Recognizer");
    modelThread.start();
}

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
        Log.e(TAG, "Buffer inválido para captura con modelo");
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
            Log.e(TAG, "AudioRecord no inicializado");
            localAudioRecord.release();
            return null;
        }
        
        synchronized (modelAudioLock) {
            modelAudioRecord = localAudioRecord;
        }
        
        localAudioRecord.startRecording();
        int offset = 0;
        int retries = 0;
        final int MAX_RETRIES = 3;
        
        while (offset < RAW_AUDIO_SAMPLE_COUNT && escuchaHabilitada && !detenerSolicitado) {
            int read = localAudioRecord.read(audioBuffer, offset, RAW_AUDIO_SAMPLE_COUNT - offset);
            if (read > 0) {
                offset += read;
                retries = 0;  // Reset retries on success
            } else if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                Log.w(TAG, "AudioRecord en estado inválido");
                return null;
            } else if (read == AudioRecord.ERROR_BAD_VALUE) {
                Log.w(TAG, "Parámetros inválidos en AudioRecord");
                return null;
            } else if (read == 0) {
                retries++;
                if (retries >= MAX_RETRIES) {
                    Log.e(TAG, "Timeout leyendo audio");
                    return null;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        
        if (offset < RAW_AUDIO_SAMPLE_COUNT) {
            Log.w(TAG, "Buffer incompleto: %d/%d muestras", offset, RAW_AUDIO_SAMPLE_COUNT);
            return null;
        }
        
        if (!escuchaHabilitada || detenerSolicitado) {
            Log.d(TAG, "Escucha detenida durante captura");
            return null;
        }
        
        String etiqueta = edgeClassifier.clasificar(audioBuffer);
        if (etiqueta == null || etiqueta.trim().isEmpty()) {
            Log.d(TAG, "Modelo no detectó ninguna clase");
            return null;
        }
        
        String etiquetaToken = tokenizar(etiqueta);
        if (!etiquetasModeloTokenizadas.contains(etiquetaToken)) {
            Log.d(TAG, "Etiqueta '%s' no está en el modelo", etiqueta);
            return null;
        }
        
        Log.d(TAG, "Detectado correctamente: %s", etiqueta);
        return etiqueta;
    } catch (Exception e) {
        Log.e(TAG, "Error en captura/clasificación", e);
        return null;
    } finally {
        if (localAudioRecord != null) {
            try {
                localAudioRecord.stop();
            } catch (Exception ignored) {}
            try {
                localAudioRecord.release();
            } catch (Exception ignored) {}
        }
        synchronized (modelAudioLock) {
            if (modelAudioRecord == localAudioRecord) {
                modelAudioRecord = null;
            }
        }
    }
}
```

### 3. Mejorar EdgeImpulseAudioClassifier.java

```java
public boolean isReady() {
    if (!bibliotecaCargada) {
        return false;
    }
    
    // Validar que el modelo tiene etiquetas
    List<String> etiquetas = obtenerEtiquetasModelo();
    return !etiquetas.isEmpty();
}
```

---

## 🧪 TESTING RECOMENDADO

### Test 1: Validar Carga de Modelo
```java
@Test
public void testModelLoadsCorrectly() {
    EdgeImpulseAudioClassifier classifier = EdgeImpulseAudioClassifier.obtenerInstancia();
    
    assertTrue("Modelo no cargó", classifier.isReady());
    
    List<String> labels = classifier.obtenerEtiquetasModelo();
    assertEquals("Debe haber 34 etiquetas", 34, labels.size());
    assertTrue("Debe tener 'si'", labels.contains("si"));
    assertTrue("Debe tener 'no'", labels.contains("no"));
}
```

### Test 2: Validar Normalización de Audio
```cpp
@Test
void testAudioNormalization() {
    // Input: short[] con valores conocidos
    short[] audio = new short[16000];
    audio[0] = 32767;  // Máximo positivo
    audio[1] = -32768; // Mínimo
    
    String result = classifier.clasificar(audio);
    // Debe procesar sin crash
}
```

### Test 3: Validar Flujo Completo
```java
@Test
void testVoiceFlowEnd2End() {
    EntradaAudio.iniciarInstancia(context);
    SalidaAudio.iniciarInstancia(context);
    
    EntradaAudio entrada = EntradaAudio.obtenerInstancia();
    assertTrue("EntradaAudio no listo", entrada.isReady());
    
    // Simular "sí"
    entrada.confirmarAfirmacion(resultado -> {
        assertTrue("Debería detectar sí", resultado);
    });
}
```

---

## 📦 CHECKLIST FINAL ANTES DE PRODUCCIÓN

- [ ] Aplicar normalización float en native-lib.cpp
- [ ] Agregar validaciones de entrada en C++
- [ ] Implementar try-catch en modelThread
- [ ] Agregar logging detallado con __android_log_print
- [ ] Mejorar reintentos en capturación de audio
- [ ] Validar que isReady() verifica etiquetas
- [ ] Testear con 34 clases del modelo
- [ ] Verificar threshold del modelo (EI_CLASSIFIER_THRESHOLD)
- [ ] Probar fallback a STT
- [ ] Validar tiempo de respuesta (<500ms para modelo)
- [ ] Comprobar consumo de memoria (162KB arena TFLite)
- [ ] Testear en dispositivos con bajo nivel de batería

---

## 🚀 PRÓXIMOS PASOS

1. **INMEDIATO** (Hoy): Aplicar fix de normalización en native-lib.cpp
2. **HOY** (Máximo): Agregar validaciones y try-catch
3. **ESTA SEMANA**: Testing completo del flujo
4. **SIGUIENTE SEMANA**: Validación en dispositivos reales

---

## 📞 CONTACTO Y REFERENCIAS

- **Edge Impulse Docs**: https://docs.edgeimpulse.com/
- **Android TFLite**: https://www.tensorflow.org/lite/android
- **Android Audio**: https://developer.android.com/guide/topics/media/audio-capture
- **JNI Best Practices**: https://developer.android.com/training/articles/perf-jni

---

**Generado:** 2026-06-03  
**Revisor:** Sistema de Análisis Automático

