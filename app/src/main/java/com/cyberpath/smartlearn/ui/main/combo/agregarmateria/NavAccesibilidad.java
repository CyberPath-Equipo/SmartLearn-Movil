package com.cyberpath.smartlearn.ui.main.combo.agregarmateria;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.List;

/**
 * Navegación por voz para el fragmento de agregar materias.
 * Protecciones añadidas para evitar que el micrófono se active antes de que termine el TTS.
 */
public class NavAccesibilidad {

    private final Context context;
    private final AgregarMateriaFragment fragment;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerMaterias;
    private final AdaptadorAgregarMaterias adapterMaterias;
    private final List<Materia> listaMateriasDisponibles;

    private int posicionActual = 0;
    private boolean estaHablandoMateria = false;
    private boolean navegacionActiva = false;

    // Comandos soportados
    private final List<String> comandosNavegacion = List.of("siguiente", "anterior", "repetir", "inscribir", "salir", "detente");

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public NavAccesibilidad(Context context, AgregarMateriaFragment fragment,
                            ViewPager2 viewPagerMaterias, AdaptadorAgregarMaterias adapterMaterias,
                            List<Materia> listaMateriasDisponibles) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.viewPagerMaterias = viewPagerMaterias;
        this.adapterMaterias = adapterMaterias;
        this.listaMateriasDisponibles = listaMateriasDisponibles;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        if (listaMateriasDisponibles == null || listaMateriasDisponibles.isEmpty()) {
            salidaAudio.hablar("No hay materias disponibles para inscribirse en este momento.", false);
            return;
        }

        // Asegurarnos de detener cualquier escucha previa antes de empezar a hablar
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}
        try { salidaAudio.detener(); } catch (Exception ignored) {}

        navegacionActiva = true;
        posicionActual = 0;

        // Da un pequeño margen para que la UI haya aplicado el adapter/ViewPager si viene justo después de actualizar la lista.
        mainHandler.postDelayed(() -> leerMateriaActual(true, this::escucharComandosNavegacion), 120);
    }

    public void detenerNavegacion() {
        navegacionActiva = false;
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}
        try { salidaAudio.detener(); } catch (Exception ignored) {}
    }

    private void leerMateriaActual(boolean conInstrucciones, Runnable onDone) {
        if (!navegacionActiva) {
            if (onDone != null) onDone.run();
            return;
        }

        if (listaMateriasDisponibles == null || listaMateriasDisponibles.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        if (posicionActual < 0 || posicionActual >= listaMateriasDisponibles.size()) posicionActual = 0;

        Materia materia = listaMateriasDisponibles.get(posicionActual);
        estaHablandoMateria = true;

        // Aseguramos de nuevo que no haya escucha activa justo antes de hablar
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}

        String texto = (conInstrucciones ? "Materia número " + (posicionActual + 1) + " de " + listaMateriasDisponibles.size() + ". " : "") +
                (materia.getNombre() != null ? materia.getNombre() : "Sin nombre") + ". " +
                (materia.getDescripcion() != null ? materia.getDescripcion() : "Sin descripción.") +
                ". Di siguiente, anterior, repetir o inscribir.";

        // Hablamos y en el callback esperamos un pequeño margen antes de iniciar la escucha.
        salidaAudio.hablar(texto, false, () -> {
            // Indicamos que ya terminó de hablar (o casi): limpiezas y pequeño delay antes de escuchar.
            estaHablandoMateria = false;
            // Delay breve para asegurar que el TTS haya liberado el audio hardware.
            mainHandler.postDelayed(() -> {
                if (onDone != null) onDone.run();
            }, 350);
        });
    }

    private void escucharComandosNavegacion() {
        if (!navegacionActiva) return;

        // Si aún está "hablando" no arrancar la escucha
        if (estaHablandoMateria) {
            mainHandler.postDelayed(this::escucharComandosNavegacion, 300);
            return;
        }

        // Llamada a entradaAudio para escuchar comandos; la implementación recibirá el índice elegido.
        entradaAudio.seleccionarOpcion(comandosNavegacion, indice -> {
            if (!navegacionActiva) return;

            if (indice < 0 || indice >= comandosNavegacion.size()) {
                // si no entendió, reintentar
                mainHandler.postDelayed(this::escucharComandosNavegacion, 700);
                return;
            }

            String comando = comandosNavegacion.get(indice);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (comando) {
                case "siguiente":
                    posicionActual = (posicionActual + 1) % listaMateriasDisponibles.size();
                    moverViewPagerAPosicion(posicionActual, true);
                    leerMateriaActual(false, this::escucharComandosNavegacion);
                    return;

                case "anterior":
                    posicionActual = (posicionActual - 1 + listaMateriasDisponibles.size()) % listaMateriasDisponibles.size();
                    moverViewPagerAPosicion(posicionActual, true);
                    leerMateriaActual(false, this::escucharComandosNavegacion);
                    return;

                case "repetir":
                    leerMateriaActual(false, this::escucharComandosNavegacion);
                    return;

                case "inscribir":
                    confirmarInscripcion();
                    return;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de inscripción de materias.", false, this::detenerNavegacion);
                    return;

                default:
                    mainHandler.postDelayed(this::escucharComandosNavegacion, 800);
                    return;
            }
        });
    }

    private void confirmarInscripcion() {
        if (posicionActual < 0 || posicionActual >= listaMateriasDisponibles.size()) {
            salidaAudio.hablar("Materia inválida. Intenta de nuevo.", true, this::escucharComandosNavegacion);
            return;
        }
        Materia materia = listaMateriasDisponibles.get(posicionActual);
        String nombre = materia.getNombre() != null ? materia.getNombre() : "esta materia";
        salidaAudio.hablar("¿Deseas inscribirte en " + nombre + "? Di sí o no.", true, () -> {
            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!navegacionActiva) return;
                if (esSi) {
                    fragment.inscribirMateriaDesdeAccesibilidad(materia, (exito, mat) -> {
                        mainHandler.post(() -> {
                            if (!navegacionActiva) return;
                            if (exito) {
                                fragment.actualizarListasDespuesInscripcion(mat);
                                salidaAudio.hablar("¡Inscripción exitosa en " + (mat.getNombre() != null ? mat.getNombre() : "la materia") + "!", false, this::detenerNavegacion);
                            } else {
                                salidaAudio.hablar("No fue posible inscribirte. Intenta de nuevo más tarde.", true, this::escucharComandosNavegacion);
                            }
                        });
                    });
                } else {
                    salidaAudio.hablar("Inscripción cancelada. Continuando con la navegación.", true, this::escucharComandosNavegacion);
                }
            });
        });
    }

    private void moverViewPagerAPosicion(int posicion, boolean smooth) {
        if (viewPagerMaterias == null || adapterMaterias == null) return;
        int realSize = adapterMaterias.getRealSize();
        if (realSize <= 0) return;
        int centered = centeredPositionForIndex(posicion, realSize);
        viewPagerMaterias.setCurrentItem(centered, smooth);
    }

    private int centeredPositionForIndex(int index, int realSize) {
        if (realSize <= 1) return index;
        int half = Integer.MAX_VALUE / 2;
        int base = half - (half % realSize);
        return base + (index % realSize);
    }
}