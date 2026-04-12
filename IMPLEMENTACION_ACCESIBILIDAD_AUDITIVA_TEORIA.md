# Implementación de Accesibilidad Auditiva (Lenguaje de Señas) - Módulo Teoría

## Descripción General

Se ha implementado la accesibilidad auditiva (traducción de texto a lenguaje de señas) en el módulo de teoría de SmartLearn Mobile. Esta funcionalidad permite que los usuarios con discapacidad auditiva vean imágenes y videos en lenguaje de señas mientras leen el contenido de teoría.

## Cambios Realizados

### 1. **Clase NavAccesibilidad.java** (Lógica de Accesibilidad)
**Ubicación:** `app/src/main/java/com/cyberpath/smartlearn/logic/main/combo/principal/contenido/teoria/`

**Funcionalidad Principal:**
- Gestiona la reproducción de contenido en lenguaje de señas
- Verifica si la accesibilidad auditiva está activada en las preferencias del usuario
- Inicializa y configura el `ReproductorMultimedia` solo si está habilitada

**Métodos Clave:**
```java
// Constructor - Inicializa NavAccesibilidad e instancia ReproductorMultimedia si está activado
public NavAccesibilidad(Context context, TeoriaFragment fragment)

// Configura las vistas para mostrar imágenes y videos del lenguaje de señas
public void setTargetViews(ImageView imageView, PlayerView playerView)

// Reproduce el contenido de teoría en lenguaje de señas
public void reproducirContenido(String textoTeoria, String lessonId)

// Detiene la reproducción actual
public void detenerReproduccion()

// Verifica si accesibilidad auditiva está activada
public boolean isAccesibilidadAuditivaActivada()

// Libera recursos del reproductor
public void release()
```

**Características Importante:**
- Solo inicializa `ReproductorMultimedia` si `PreferencesManager.isAccesibilidadAuditivaActivada()` retorna `true`
- Si accesibilidad auditiva no está activada, todas las operaciones retornan sin hacer nada
- Maneja listeners para eventos de reproducción (inicio, finalización, errores, mapeo cargado)

### 2. **TeoriaFragment.java** (Interfaz de Usuario)
**Ubicación:** `app/src/main/java/com/cyberpath/smartlearn/ui/main/combo/principal/contenido/teoria/`

**Cambios Realizados:**
- Agregadas nuevas vistas privadas para ImageView, PlayerView y contenedor del reproductor
- Instancia `NavAccesibilidad` en `onViewCreated()`
- Configura las vistas del reproductor llamando a `setTargetViews()` si accesibilidad está activada
- Detiene la reproducción cuando el usuario regresa (botón volver)
- Libera recursos en `onDestroyView()` para evitar fugas de memoria

**Vistas Nuevas:**
```java
private ImageView imageViewLSM;
private PlayerView playerViewLSM;
private FrameLayout containerReproductorLSM;
private NavAccesibilidad navAccesibilidad;
```

**Ciclo de Vida:**
- `onViewCreated()`: Inicializa NavAccesibilidad y configura vistas
- `onDestroyView()`: Libera recursos del reproductor

### 3. **TeoriaLogic.java** (Lógica de Negocio)
**Ubicación:** `app/src/main/java/com/cyberpath/smartlearn/logic/main/combo/principal/contenido/teoria/`

**Cambios Realizados:**
- Aceptar `NavAccesibilidad` como parámetro en el constructor (opcional para compatibilidad)
- Invocar reproducción de contenido en lenguaje de señas después de mostrar el contenido
- Pasar el ID del subtema como `lessonId` para cargar el mapping correcto

**Constructores:**
```java
// Constructor original (mantiene compatibilidad)
public TeoriaLogic(TeoriaFragment fragment, Subtema subtema, ArrayList<String> preguntas)

// Nuevo constructor con soporte para NavAccesibilidad
public TeoriaLogic(TeoriaFragment fragment, Subtema subtema, ArrayList<String> preguntas, NavAccesibilidad navAccesibilidad)
```

**Lógica de Reproducción:**
Cuando se carga el contenido de teoría, si `NavAccesibilidad` está disponible y la accesibilidad auditiva está activada:
```java
if (navAccesibilidad != null && navAccesibilidad.isAccesibilidadAuditivaActivada()) {
    String lessonId = subtema != null ? String.valueOf(subtema.getId()) : "lesson_1";
    navAccesibilidad.reproducirContenido(contenido, lessonId);
}
```

### 4. **Layout: fragment_teoria.xml** (Interfaz)
**Ubicación:** `app/src/main/res/layout/`

**Cambios Realizados:**
- Agregado contenedor `FrameLayout` para el reproductor multimedia
- ImageView para mostrar imágenes del lenguaje de señas
- PlayerView (androidx.media3) para reproducción de videos
- Posicionado en la esquina inferior derecha
- Inicialmente oculto (`android:visibility="gone"`) se muestra solo cuando accesibilidad está activada

**Dimensiones:**
- Ancho: 180dp
- Alto: 240dp
- Margen derecho: 10dp
- Margen inferior: 75dp (para no solapar con botón "Volver")

```xml
<!-- Contenedor para reproductor multimedia de lenguaje de señas -->
<FrameLayout
    android:id="@+id/container_reproductor_lsm"
    android:layout_width="180dp"
    android:layout_height="240dp"
    android:layout_gravity="bottom|end"
    android:layout_marginEnd="10dp"
    android:layout_marginBottom="75dp"
    android:visibility="gone">
    
    <ImageView
        android:id="@+id/image_view_lsm"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerInside" />
    
    <androidx.media3.ui.PlayerView
        android:id="@+id/player_view_lsm"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone" />
</FrameLayout>
```

## Flujo de Funcionamiento

### Cuando el usuario accede a teoría:

1. **TeoriaFragment.onViewCreated()**
   - Crea instancia de `NavAccesibilidad`
   - Verifica si accesibilidad auditiva está activada
   - Si está activada, configura las vistas y muestra el contenedor del reproductor

2. **TeoriaLogic.mostrarContenido()**
   - Muestra el contenido de teoría en el TextView
   - Si `NavAccesibilidad` existe y está activada, reproduce el contenido en lenguaje de señas

3. **ReproductorMultimedia.play()**
   - Carga el mapping de palabras a signos desde el servidor
   - Traduce el texto a secuencia de imágenes/videos
   - Reproduce los signos en el ImageView/PlayerView

4. **Cuando el usuario regresa**
   - Se detiene la reproducción del reproductor multimedia
   - Se liberan recursos en `onDestroyView()`

## Dependencias

- **androidx.media3:media3-ui** - Para PlayerView (video playback)
- **Clase PreferencesManager** - Para verificar si accesibilidad está activada
- **Clase ReproductorMultimedia** - Maneja la traducción y reproducción

## Configuración Necesaria

### URL del API de Lenguaje de Señas
La URL base está configurada en `NavAccesibilidad.java`:
```java
private String baseUrlLenguajeSenas = "http://192.168.1.1:8000/smartlearn/api/lsm/";
```

Puede modificarse usando el método:
```java
navAccesibilidad.setBaseUrlLenguajeSenas(String baseUrl);
```

### Preferencias de Usuario
La accesibilidad auditiva se gestiona a través de `PreferencesManager`:
```java
// Activar/desactivar accesibilidad auditiva
PreferencesManager.setAccesibilidadAuditivaActivada(context, true);

// Verificar si está activada
boolean activada = PreferencesManager.isAccesibilidadAuditivaActivada(context);
```

## Comportamiento Condicional

### Si Accesibilidad Auditiva está **ACTIVADA**:
✅ Se muestra el contenedor del reproductor multimedia en la esquina inferior derecha  
✅ Se reproduce automáticamente el contenido en lenguaje de señas  
✅ El usuario puede ver imágenes/videos mientras lee el texto  
✅ Se detiene al regresar de la pantalla  

### Si Accesibilidad Auditiva está **DESACTIVADA**:
❌ El contenedor del reproductor permanece oculto  
❌ No se inicializa `ReproductorMultimedia`  
❌ El usuario solo ve el contenido de texto normal  
❌ No hay impacto en rendimiento  

## Validación de Cumplimiento

✅ **La lógica se maneja en NavAccesibilidad** - Toda la lógica de reproducción está centralizada  
✅ **Usa ReproductorMultimedia** - Integración completa con la clase existente  
✅ **Visible solo si accesibilidad auditiva está activada** - Verificación en PreferencesManager  
✅ **PlayerView en esquina inferior derecha** - Layout posicionado correctamente  
✅ **ImageView para imágenes de lenguaje de señas** - Vista agregada para mostrar signos  
✅ **Manejo de ciclo de vida** - Recursos liberados en onDestroyView()  

## Próximas Mejoras (Opcionales)

1. **Controles de Reproducción**: Agregar botones para play/pause/stop
2. **Velocidad de Reproducción**: Permitir ajustar la velocidad de los signos
3. **Tamaño Configurable**: Permitir al usuario cambiar el tamaño del reproductor
4. **Persistencia**: Guardar preferencias de tamaño y posición
5. **Extensión a Otras Secciones**: Implementar en práctica y ejercicios

## Testing

Para probar la funcionalidad:

1. **Activar accesibilidad auditiva:**
   - Ir a Cuenta → Configuración → Accesibilidad Auditiva → Activar

2. **Acceder a cualquier teoría:**
   - El reproductor multimedia debe aparecer en la esquina inferior derecha

3. **Verificar contenido:**
   - El texto debe mostrarse en lenguaje de señas (imágenes/videos)

4. **Desactivar y reactivar:**
   - El reproductor debe desaparecer/aparecer según la preferencia

