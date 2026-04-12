# GUÍA DE USO - ACCESIBILIDAD AUDITIVA EN TEORÍA

## 🎯 Descripción General

Se ha implementado exitosamente la **accesibilidad auditiva** en el módulo de **Teoría** de SmartLearn Mobile. Esta funcionalidad traduce automáticamente el contenido de texto a **lenguaje de señas** usando imágenes y videos, permitiendo que usuarios con discapacidad auditiva accedan al contenido educativo de manera inclusiva.

---

## 🚀 Cómo Funciona

### Para el Usuario Final:

**1. Activar Accesibilidad Auditiva:**
- Abrir la aplicación
- Ir a **Perfil/Cuenta** → **Configuración**
- Buscar **"Accesibilidad Auditiva"**
- Activar el interruptor
- La configuración se guarda automáticamente

**2. Acceder a Teoría:**
- Navegar a cualquier **Materia** → **Tema** → **Subtema**
- Hacer clic en **"Teoría"**
- Si accesibilidad auditiva está activada, verá:
  - ✅ El contenido de texto en el centro
  - ✅ Un reproductor multimedia en la **esquina inferior derecha**
  - ✅ Imágenes/videos del lenguaje de señas reproduciéndose automáticamente

**3. Ver Lenguaje de Señas:**
- Las imágenes/videos se sincronizarán con el texto mostrado
- El reproductor es independiente y no interfiere con el resto de la pantalla
- El usuario puede seguir leyendo mientras ve los signos

**4. Regresar:**
- Al presionar el botón "Volver", la reproducción se detiene automáticamente
- Al cambiar de teoría, el reproductor ajusta el contenido automáticamente

---

## 📱 Interfaz de Usuario

### Layout del Reproductor Multimedia:
```
┌─────────────────────────────────────────────────────┐
│  CONTENIDO DE TEORÍA                                │
│                                                     │
│  Lorem ipsum dolor sit amet...                      │
│  [Texto contenido de teoría                    ┌────┐
│   continúa aquí...]                            │ 📱 │
│                                                │ LSM│
│  Más texto...                                  │    │
│                                                └────┘
│  [Botón Volver]                               (180x240dp)
└─────────────────────────────────────────────────────┘
```

**Características del Reproductor:**
- Posición: Esquina inferior derecha
- Tamaño: 180dp × 240dp
- Margen derecho: 10dp
- Margen inferior: 75dp (para no solapar con botón)
- Oculto si accesibilidad auditiva está desactivada

---

## 🔧 Detalles Técnicos de Implementación

### Archivos Modificados: 4

#### 1️⃣ **NavAccesibilidad.java** (Nueva Lógica)
**Responsabilidades:**
- Verificar si accesibilidad auditiva está activada
- Instanciar `ReproductorMultimedia` solo si está habilitada
- Configurar vistas de imagen y video
- Reproducir contenido automáticamente
- Manejar ciclo de vida y liberación de recursos

**Métodos Principales:**
```java
// Constructor - Crea instancia del reproductor
public NavAccesibilidad(Context context, TeoriaFragment fragment)

// Configurar vistas
public void setTargetViews(ImageView imageView, PlayerView playerView)

// Reproducir contenido
public void reproducirContenido(String textoTeoria, String lessonId)

// Detener reproducción
public void detenerReproduccion()

// Liberar recursos
public void release()

// Verificar estado
public boolean isAccesibilidadAuditivaActivada()
public boolean isPlaying()
```

#### 2️⃣ **TeoriaFragment.java** (Integración UI)
**Cambios:**
- ✅ Instancia `NavAccesibilidad` en `onViewCreated()`
- ✅ Obtiene referencias a las vistas del reproductor
- ✅ Configura vistas si accesibilidad está activada
- ✅ Muestra/oculta contenedor del reproductor
- ✅ Detiene reproducción al regresar
- ✅ Libera recursos en `onDestroyView()`

**Nuevas Vistas:**
```java
private NavAccesibilidad navAccesibilidad;
private ImageView imageViewLSM;           // Muestra imágenes de signos
private PlayerView playerViewLSM;         // Reproduce videos de signos
private FrameLayout containerReproductorLSM; // Contenedor general
```

#### 3️⃣ **TeoriaLogic.java** (Orquestación)
**Cambios:**
- ✅ Acepta `NavAccesibilidad` como parámetro (constructor sobrecargado)
- ✅ Mantiene compatibilidad con constructor original
- ✅ Invoca reproducción automática cuando carga contenido

**Constructor Original (Compatible):**
```java
public TeoriaLogic(TeoriaFragment fragment, Subtema subtema, ArrayList<String> preguntas)
```

**Nuevo Constructor (Con Accesibilidad):**
```java
public TeoriaLogic(TeoriaFragment fragment, Subtema subtema, 
                   ArrayList<String> preguntas, NavAccesibilidad navAccesibilidad)
```

#### 4️⃣ **fragment_teoria.xml** (Diseño)
**Nuevos Elementos:**
```xml
<!-- Contenedor principal (180×240dp) -->
<FrameLayout
    android:id="@+id/container_reproductor_lsm"
    android:layout_width="180dp"
    android:layout_height="240dp"
    android:layout_gravity="bottom|end"
    android:layout_marginEnd="10dp"
    android:layout_marginBottom="75dp"
    android:visibility="gone">
    
    <!-- ImageView: Muestra imágenes del lenguaje de señas -->
    <ImageView
        android:id="@+id/image_view_lsm"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerInside" />
    
    <!-- PlayerView: Reproduce videos del lenguaje de señas -->
    <androidx.media3.ui.PlayerView
        android:id="@+id/player_view_lsm"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone" />
</FrameLayout>
```

---

## 🔄 Flujo de Ejecución

```
1. Usuario abre Teoría
        ↓
2. TeoriaFragment.onViewCreated()
   - Crea NavAccesibilidad
   - Verifica PreferencesManager.isAccesibilidadAuditivaActivada()
        ↓
3. Si está ACTIVADA:
   - Obtiene referencias a vistas
   - Llama navAccesibilidad.setTargetViews()
   - Muestra containerReproductorLSM
        ↓
4. TeoriaLogic.cargarTeoria()
   - Obtiene contenido de teoría
   - Llama mostrarContenido()
        ↓
5. NavAccesibilidad.reproducirContenido()
   - Inicia ReproductorMultimedia
   - Carga mapping de signos desde servidor
   - Traduce texto a secuencia de imágenes/videos
        ↓
6. ReproductorMultimedia.play()
   - Muestra signos en ImageView/PlayerView
   - Usuario ve lenguaje de señas mientras lee el texto
        ↓
7. Usuario regresa o cambia de pantalla
   - detenerReproduccion() es llamado
   - Recursos se liberan en onDestroyView()
```

---

## 🛡️ Validaciones de Seguridad

✅ **1. Verificación de Preferencias:**
```java
if (PreferencesManager.isAccesibilidadAuditivaActivada(context)) {
    // Solo entonces se inicializa el reproductor
}
```

✅ **2. Validación de Nulidad:**
```java
if (navAccesibilidad != null && navAccesibilidad.isAccesibilidadAuditivaActivada()) {
    // Seguro de no tener NullPointerException
}
```

✅ **3. Validación de Contenido:**
```java
if (textoTeoria == null || textoTeoria.trim().isEmpty()) {
    return; // No reproducir si está vacío
}
```

✅ **4. Manejo del Ciclo de Vida:**
```java
@Override
public void onDestroyView() {
    if (navAccesibilidad != null) {
        navAccesibilidad.release(); // Liberar recursos
    }
}
```

---

## 🎯 Requisitos Cumplidos

| Requisito | Estado | Detalles |
|-----------|--------|----------|
| Accesibilidad auditiva en teoría | ✅ | Completamente implementada |
| Traducción de texto a lenguaje de señas | ✅ | Integración con `ReproductorMultimedia` |
| Uso de `androidx.media3.ui.PlayerView` | ✅ | Para reproducción de videos de signos |
| `ImageView` para imágenes de signos | ✅ | Para mostrar imágenes estáticas |
| Posición en esquina inferior derecha | ✅ | `layout_gravity="bottom\|end"` |
| Lógica en `NavAccesibilidad` | ✅ | Centralizada y modular |
| Uso de `ReproductorMultimedia` | ✅ | Integración completa |
| Solo si accesibilidad auditiva activada | ✅ | Verificación condicional |
| Exclusivo para módulo de teoría | ✅ | Implementación específica |

---

## 📊 Comparativa: Con vs Sin Accesibilidad Auditiva

### SIN Accesibilidad Auditiva (Desactivada):
```
┌──────────────────────────────────────┐
│   CONTENIDO DE TEORÍA                │
│                                      │
│   Lorem ipsum dolor sit amet...      │
│   [Texto contenido de teoría]        │
│   continúa aquí...                   │
│                                      │
│   [Botón Volver]                     │
└──────────────────────────────────────┘
```

### CON Accesibilidad Auditiva (Activada):
```
┌──────────────────────────────────────────────┐
│   CONTENIDO DE TEORÍA                   ┌────┐
│                                         │ LSM│
│   Lorem ipsum dolor sit amet...         │📱  │
│   [Texto contenido de teoría]           │(V) │
│   continúa aquí...                      └────┘
│                                    (Imágenes/Videos
│   [Botón Volver]                 Lenguaje de Señas)
└──────────────────────────────────────────────┘
```

---

## 🔌 Configuración de API

### URL del Servidor de Lenguaje de Señas

La URL está configurada en `NavAccesibilidad.java`:

```java
private String baseUrlLenguajeSenas = "http://192.168.1.1:8000/smartlearn/api/lsm/";
```

**Para cambiar la URL en tiempo de ejecución:**
```java
navAccesibilidad.setBaseUrlLenguajeSenas("https://nuevo-servidor.com/api/lsm/");
```

**Esperado del servidor:**
- Endpoint: `/mapping/{lessonId}`
- Método: GET
- Respuesta: JSON con mapeo de palabras a signos
- Videos/Imágenes: Accesibles desde URLs devueltas

---

## 📝 Pruebas Recomendadas

### Test 1: Desactivado
```
1. Desactivar accesibilidad auditiva
2. Abrir cualquier teoría
3. ✓ El reproductor NO debe aparecer
4. ✓ El contenido de texto se muestra normalmente
```

### Test 2: Activado
```
1. Activar accesibilidad auditiva
2. Cerrar y reabrior la aplicación
3. Abrir cualquier teoría
4. ✓ El reproductor aparece en esquina inferior derecha
5. ✓ Las imágenes/videos de LSM se reproducen
```

### Test 3: Cambio de Teoría
```
1. Con accesibilidad activada
2. Abrir una teoría y observar reproducción
3. Volver atrás
4. Abrir otra teoría
5. ✓ El reproductor debe ajustarse al nuevo contenido
```

### Test 4: Rendimiento
```
1. Monitorear con Android Studio Profiler
2. Navegar entre teorías varias veces
3. ✓ No debe haber memory leaks
4. ✓ Recursos deben liberarse correctamente
```

---

## 🚀 Integración Futura

Esta implementación puede extenderse fácilmente a otros módulos:

### Próximos Pasos:
- [ ] Implementar en módulo **Práctica**
- [ ] Implementar en módulo **Ejercicios**
- [ ] Agregar controles de reproducción (play/pause/stop)
- [ ] Permitir cambiar velocidad de reproducción
- [ ] Agregar subtítulos en lenguaje de señas
- [ ] Hacer reproductor redimensionable

### Código para Extensión:
```java
// En cualquier otra pantalla:
NavAccesibilidad navAccesibilidad = new NavAccesibilidad(context, fragment);

if (navAccesibilidad.isAccesibilidadAuditivaActivada()) {
    navAccesibilidad.setTargetViews(imageView, playerView);
    navAccesibilidad.reproducirContenido(contenido, lessonId);
}
```

---

## ✅ Resumen Final

✨ **Se ha implementado exitosamente:**

1. ✅ Accesibilidad auditiva completa en módulo de Teoría
2. ✅ Integración con `ReproductorMultimedia` existente
3. ✅ Lógica centralizada en `NavAccesibilidad`
4. ✅ Interfaz de usuario intuitiva en esquina inferior derecha
5. ✅ Gestión automática del ciclo de vida
6. ✅ Validaciones de seguridad y nulidad
7. ✅ Comportamiento condicional basado en preferencias
8. ✅ Liberación correcta de recursos
9. ✅ Compatibilidad hacia atrás mantenida
10. ✅ Preparado para extensión futura

---

## 📞 Soporte y Preguntas

**En caso de problemas:**
- Verificar que `PreferencesManager` esté correctamente guardando la preferencia
- Asegurar que la URL del servidor LSM sea correcta y accesible
- Revisar logs de Android Studio para errores de red
- Validar que `ReproductorMultimedia` esté inicializado correctamente

---

**Documento generado:** 11/04/2026  
**Versión:** 1.0  
**Estado:** Implementación Completada ✅

