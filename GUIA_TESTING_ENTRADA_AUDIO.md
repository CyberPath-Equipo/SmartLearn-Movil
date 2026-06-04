# 🧪 GUÍA COMPLETA DE TESTING - EntradaAudio.java

## Objetivo
Validar que el flujo de voz (modelo + STT híbrido) funciona correctamente al 100% después de las correcciones.

---

## 📱 PREREQUISITOS

### Hardware
- Dispositivo Android (mínimo API 21)
- Micrófono funcional
- Volumen de sistema activado

### Software
- Android Studio con proyecto compilado
- Logcat visible con filtro `EntradaVoz`
- WiFi o conexión 4G (para STT de Google)

### Permisos
- ✅ RECORD_AUDIO otorgado
- ✅ INTERNET otorgado

---

## 🔍 TEST 1: VALIDACIÓN DE INICIALIZACIÓN

### Procedimiento
1. Abrir la app
2. Navegar a pantalla con entrada de voz
3. Verificar logs en **Logcat**

### Criterios de Éxito ✅
```
✓ [EntradaVoz] Etiquetas del modelo cargadas: [si, no, ...]
✓ [EntradaVoz] SpeechRecognizer creado
✓ No hay errores "EdgeImpulseAudioClassifier es nulo"
```

### Criterios de Fallo ❌
```
✗ [EntradaVoz] EdgeImpulseAudioClassifier es nulo → Revisar EdgeImpulseAudioClassifier.java
✗ [EntradaVoz] Edge Impulse Classifier no está listo → Modelo no inicializado
✗ [EntradaVoz] Reconocimiento de voz no disponible → STT no disponible en dispositivo
```

---

## 🔍 TEST 2: CAPTURA DE AUDIO CON MODELO

### Escenario A: Condición Ideal (Ambiente Silencioso)

**Procedimiento:**
1. Ejecutar: `entradaAudio.confirmarAfirmacion(resultado -> {...})`
2. Esperar mensaje de voz "¿Confirma?"
3. Decir claramente: **"sí"** o **"no"**
4. Esperar respuesta

**Logs Esperados (ÉXITO):**
```
D [EntradaVoz] Etiqueta de seguridad lanzada
D [EntradaVoz] Audio capturado correctamente. Clasificando...
D [EntradaVoz] Etiqueta detectada por modelo: si
D [EntradaVoz] Evaluando confirmacion. EtiquetaToken: si
D [EntradaVoz] Confirmacion positiva detectada
```

**Logs de Fallo (FALLBACK):**
```
W [EntradaVoz] Demasiados intentos de lectura fallidos (read=0). Abortando captura modelo.
D [EntradaVoz] Modelo no resolvió, fallback a STT
D [EntradaVoz] Listo para escuchar (STT)
```

**Métricas:**
- ⏱️ Tiempo de respuesta esperado: **1.5-2.5 segundos**
- 📊 Tasa de éxito esperada: **≥95%**

---

### Escenario B: Ambiente Ruidoso

**Procedimiento:**
1. Reproducir ruido de fondo (tráfico, gente hablando)
2. Repetir TEST 2A

**Logs Esperados:**
```
D [EntradaVoz] Reintentando lectura (1/5) [aparecerá si hay retrasos]
D [EntradaVoz] Audio capturado correctamente. Clasificando...
```

**Tolerancia:**
- Puede haber hasta 3 reintentos (read=0)
- Tiempo máximo: 3 segundos

**Métricas:**
- 📊 Tasa de éxito esperada: **≥85%** (degradación aceptable en ruido)

---

### Escenario C: Modelo No Reconoce

**Procedimiento:**
1. Decir algo que NO está en las etiquetas (ej: "hola", "mundo")
2. Observar comportamiento

**Logs Esperados:**
```
D [EntradaVoz] Etiqueta detectada por modelo: hola
W [EntradaVoz] Etiqueta 'hola' (token: 'hola') no está en etiquetas del modelo. Disponibles: [si, no, ...]
D [EntradaVoz] Modelo no resolvió, fallback a STT
I [EntradaVoz] Escucha STT iniciada
```

**Comportamiento:**
- 🔄 Fallback automático a STT
- 🔊 Sistema dice: "No entendí. Repita por favor."
- 👂 Vuelve a escuchar automáticamente

---

## 🔍 TEST 3: SELECCIÓN DE OPCIONES

### Procedimiento
```java
List<String> opciones = Arrays.asList("Opción A", "Opción B", "Opción C");
entradaAudio.seleccionarOpcion(opciones, indice -> {
    Log.d("TEST", "Seleccionada: " + opciones.get(indice));
});
```

1. Ejecutar código anterior
2. Esperar prompt de voz
3. Decir uno de los nombres de opciones

**Logs Esperados (ÉXITO):**
```
D [EntradaVoz] Buscando en opciones: [opciòna, opcionb, opcionc]
D [EntradaVoz] Etiqueta detectada por modelo: opción a
D [EntradaVoz] Opcion seleccionada: index=0, token=opciòna
```

**Criterios:**
- ✅ Índice correcto retornado
- ✅ Sin delay notorio

---

## 🔍 TEST 4: RESILENCIA DEL THREAD

### Objetivo
Verificar que el thread del modelo NO se bloquea incluso si hay excepciones.

### Procedimiento (Avanzado)
1. Abrir **Android Studio → Debugger → Breakpoint**
2. Agregar breakpoint en `capturarYClasificarConModelo()`
3. Ejecutar `confirmarAfirmacion()`
4. Pausar el thread por 5 segundos
5. Continuar ejecución

**Logs Esperados:**
```
D [EntradaVoz] Resultado del modelo: null
D [EntradaVoz] Modelo no resolvió, fallback a STT
```

**Comportamiento:**
- ✅ No hay crash
- ✅ Fallback automático funciona

---

## 🔍 TEST 5: MANEJO DE ERRORES

### Test 5A: Sin Permiso de Micrófono

**Procedimiento:**
1. Ir a **Configuración → Permisos → Audio**
2. Denegar permiso RECORD_AUDIO
3. Intentar usar `confirmarAfirmacion()`

**Logs Esperados:**
```
W [EntradaVoz] Permiso RECORD_AUDIO denegado para captura con modelo
D [EntradaVoz] Modelo no resolvió, fallback a STT
I [EntradaVoz] SpeechRecognizer creado
```

**Comportamiento:**
- ✅ Modelo no intentará capturar
- ✅ Fallback a STT automático

---

### Test 5B: STT No Disponible

**Procedimiento:**
1. Desactivar conexión a Internet
2. Denegar permiso RECORD_AUDIO
3. Intentar usar `confirmarAfirmacion()`

**Logs Esperados:**
```
W [EntradaVoz] Reconocimiento de voz no disponible en este dispositivo
W [EntradaVoz] Permiso RECORD_AUDIO denegado
```

**Comportamiento:**
- 🔊 Sistema dice: "Reconocimiento no disponible o falta permiso"
- ✅ No hay crash

---

## 📊 TEST 6: TASA DE ÉXITO GLOBAL

### Protocolo de Validación
Ejecutar 20 intentos en cada escenario:

```
ESCENARIO IDEAL (Silencio):
- Modelo directo: 20/20 ✅ 100%
- Tiempo promedio: 1.8 seg

ESCENARIO NORMAL (Ruido ligero):
- Modelo directo: 18/20 ✅ 90%
- Fallback STT: 2/20
- Tiempo promedio: 2.0 seg

ESCENARIO DIFÍCIL (Ruido fuerte):
- Modelo directo: 15/20 ✅ 75%
- Fallback STT: 5/20
- Tiempo promedio: 2.5 seg
```

**Criterio Global de Éxito:**
- ✅ Tasa total ≥95% (modelo + fallback STT)
- ✅ Sin crashes
- ✅ Tiempo ≤3 segundos

---

## 🐛 CHECKLIST DE DEBUGGING

Si algo falla, verificar:

### ❌ Modelo no captura audio
- [ ] Revisar logcat: "Audio capturado correctamente"
- [ ] Si hay "Demasiados intentos fallidos" → Problema de AudioRecord
- [ ] Verificar permiso RECORD_AUDIO en tiempo de ejecución
- [ ] Probar en otro dispositivo (versión Android)

### ❌ Modelo detecta pero NO resuelve
- [ ] Verificar: "Etiqueta ... no está en etiquetas del modelo"
- [ ] Comparar etiqueta vs. etiquetasModeloTokenizadas
- [ ] Revisar que tokenización sea correcta
- [ ] Ver en EdgeImpulseAudioClassifier.obtenerEtiquetasModelo()

### ❌ Thread se bloquea
- [ ] Ver logcat: "Error no capturado en thread de modelo"
- [ ] Ver stack trace completo
- [ ] Verificar que capturarYClasificarConModelo() tiene try-catch

### ❌ STT fallback no funciona
- [ ] Verificar conexión a Internet
- [ ] Revisar si SpeechRecognizer es null
- [ ] Ver en onError() si hay ERROR_INSUFFICIENT_PERMISSIONS

### ❌ Logs vacíos
- [ ] Filtro Logcat: `EntradaVoz` (debe ser exacto)
- [ ] Nivel de log: Verbose o Debug
- [ ] Reiniciar app y ADB

---

## 📋 MATRIZ DE TESTING FINAL

| Test | Escenario | Esperado | Resultado | Observaciones |
|------|-----------|----------|-----------|---------------|
| 1 | Inicialización | Logs correctos | ☐ PASS ☐ FAIL | |
| 2A | Audio Silencio | ≥95% éxito | ☐ PASS ☐ FAIL | |
| 2B | Audio Ruido | ≥85% éxito | ☐ PASS ☐ FAIL | |
| 2C | No Reconoce | Fallback STT | ☐ PASS ☐ FAIL | |
| 3 | Seleccionar Opción | Índice correcto | ☐ PASS ☐ FAIL | |
| 4 | Resilencia Thread | Sin crash | ☐ PASS ☐ FAIL | |
| 5A | Sin Permiso | Fallback STT | ☐ PASS ☐ FAIL | |
| 5B | STT Unavailable | Error graceful | ☐ PASS ☐ FAIL | |
| 6 | Tasa Global | ≥95% total | ☐ PASS ☐ FAIL | |

---

## 🎯 CRITERIO DE ACEPTACIÓN

✅ **ACEPTADO SI:**
- Todos los tests pasan (PASS)
- Tasa de éxito global ≥95%
- Sin crashes no controlados
- Fallbacks funcionan correctamente
- Logs completos para debugging

❌ **RECHAZADO SI:**
- Thread se bloquea
- Tasa < 85% en escenarios normales
- Crash sin try-catch
- Fallback no funciona

---

## 📞 SOPORTE

Si encuentra problemas:
1. Guardar logs completos de Logcat
2. Revisar `REVISION_ENTRADA_AUDIO.md`
3. Verificar filtro logcat: `EntradaVoz`
4. Documentar: dispositivo, Android version, escenario


