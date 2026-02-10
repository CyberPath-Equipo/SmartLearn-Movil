package com.cyberpath.smartlearn.ui.main.combo.principal.materias;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.viewpager2.widget.ViewPager2;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;
import java.util.List;

public class NavAccesibilidadMaterias {

    private final Context context;
    private final MateriasFragment fragment;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerMaterias;
    private final AdaptadorMaterias adapterMaterias;
    private final List<Materia> listaMateriasDisponibles;

    private int posicionActual = 0;
    private boolean estaHablandoMateria = false;
    private boolean navegacionActiva = false;

    private final List<String> comandosNavegacion = List.of("siguiente", "anterior", "repetir", "entrar", "salir", "detente");

    public NavAccesibilidadMaterias(Context context, MateriasFragment fragment, ViewPager2 viewPagerMaterias,
                                    AdaptadorMaterias adapterMaterias, List<Materia> listaMateriasDisponibles) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.viewPagerMaterias = viewPagerMaterias;
        this.adapterMaterias = adapterMaterias;
        this.listaMateriasDisponibles = listaMateriasDisponibles;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        if (listaMateriasDisponibles.isEmpty()) {
            salidaAudio.hablar("No hay materias inscritas en este momento.", false);
            return;
        }

        navegacionActiva = true;
        posicionActual = 0;
        // Leer materia y luego iniciar escucha de comandos
        leerMateriaActual(true, () -> escucharComandosNavegacion());
    }

    public void detenerNavegacion() {
        navegacionActiva = false;
        entradaAudio.detenerEscucha();
        salidaAudio.detener();
    }

    private void leerMateriaActual(boolean conInstrucciones, Runnable onDone) {
        if (posicionActual < 0 || posicionActual >= listaMateriasDisponibles.size()) {
            if (onDone != null) onDone.run();
            return;
        }

        Materia materia = listaMateriasDisponibles.get(posicionActual);
        estaHablandoMateria = true;

        String texto = (conInstrucciones ? "Materia número " + (posicionActual + 1) + " de " + listaMateriasDisponibles.size() + ". " : "") +
                (materia.getNombre() != null ? materia.getNombre() : "Sin nombre") + ". " +
                (materia.getDescripcion() != null ? materia.getDescripcion() : "Sin descripción.") +
                ". Di siguiente, anterior, repetir o entrar.";

        salidaAudio.hablar(texto, false, () -> {
            if (onDone != null) onDone.run();
        });
    }

    private void escucharComandosNavegacion() {
        if (!navegacionActiva) return;

        entradaAudio.seleccionarOpcion(comandosNavegacion, indice -> {
            if (!navegacionActiva) return;

            String comando = comandosNavegacion.get(indice);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (comando) {
                case "siguiente":
                    posicionActual = (posicionActual + 1) % listaMateriasDisponibles.size();
                    leerMateriaActual(false, () -> escucharComandosNavegacion());
                    if (viewPagerMaterias != null && adapterMaterias != null && adapterMaterias.getRealSize() > 0) {
                        int centered = centeredPositionForIndex(posicionActual, adapterMaterias.getRealSize());
                        viewPagerMaterias.setCurrentItem(centered, true);
                    }
                    return;

                case "anterior":
                    posicionActual = (posicionActual - 1 + listaMateriasDisponibles.size()) % listaMateriasDisponibles.size();
                    leerMateriaActual(false, () -> escucharComandosNavegacion());
                    if (viewPagerMaterias != null && adapterMaterias != null && adapterMaterias.getRealSize() > 0) {
                        int centered = centeredPositionForIndex(posicionActual, adapterMaterias.getRealSize());
                        viewPagerMaterias.setCurrentItem(centered, true);
                    }
                    return;

                case "repetir":
                    leerMateriaActual(false, () -> escucharComandosNavegacion());
                    return;

                case "entrar":
                    confirmarEntrada();
                    return;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de materias.", false);
                    detenerNavegacion();
                    return;
            }

            new Handler(Looper.getMainLooper()).postDelayed(this::escucharComandosNavegacion, 800);
        });
    }

    private void confirmarEntrada() {
        if (posicionActual < 0 || posicionActual >= listaMateriasDisponibles.size()) return;

        Materia materia = listaMateriasDisponibles.get(posicionActual);
        salidaAudio.hablar("¿Deseas entrar en " + (materia.getNombre() != null ? materia.getNombre() : "esta materia") + "? Di sí o no.", true, () -> {
            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!navegacionActiva) return;

                if (esSi) {
                    fragment.entrarMateriaDesdeAccesibilidad(materia, this::onAccesoCompletado);
                } else {
                    salidaAudio.hablar("Entrada cancelada. Continuando con la navegación.", true, () -> escucharComandosNavegacion());
                }
            });
        });
    }

    private void onAccesoCompletado(boolean exito, Materia materia) {
        if (!navegacionActiva) return;

        if (exito) {
            salidaAudio.hablar("¡Navegando hacia " + (materia.getNombre() != null ? materia.getNombre() : "la materia") + "!", false, () -> {
                detenerNavegacion();
            });
        } else {
            salidaAudio.hablar("Error al entrar. Intenta más tarde.", true, () -> escucharComandosNavegacion());
        }
    }

    private int centeredPositionForIndex(int index, int realSize) {
        if (realSize <= 1) return index;
        int half = Integer.MAX_VALUE / 2;
        int base = half - (half % realSize);
        return base + (index % realSize);
    }
}