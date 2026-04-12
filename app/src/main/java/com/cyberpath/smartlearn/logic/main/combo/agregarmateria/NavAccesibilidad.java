package com.cyberpath.smartlearn.logic.main.combo.agregarmateria;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.ui.main.combo.agregarmateria.AdaptadorAgregarMaterias;
import com.cyberpath.smartlearn.ui.main.combo.agregarmateria.AgregarMateriaFragment;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;

import java.util.List;

public class NavAccesibilidad {
    private final Context context;
    private final AgregarMateriaFragment fragment;
    private final AgregarMateriaLogic agregarMateriaLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerMaterias;
    private final AdaptadorAgregarMaterias adapterMaterias;

    private final List<String> comandosNavegacion = List.of("siguiente", "anterior", "repetir", "inscribir", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int posicionActual = 0;
    private boolean estaHablandoMateria = false;
    private boolean navegacionActiva = false;

    public NavAccesibilidad(Context context, AgregarMateriaFragment fragment,
                            AgregarMateriaLogic agregarMateriaLogic,
                            ViewPager2 viewPagerMaterias,
                            AdaptadorAgregarMaterias adapterMaterias) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.agregarMateriaLogic = agregarMateriaLogic;
        this.viewPagerMaterias = viewPagerMaterias;
        this.adapterMaterias = adapterMaterias;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        List<Materia> listaMaterias = agregarMateriaLogic.getListaMateriasFiltrada();

        if (listaMaterias == null || listaMaterias.isEmpty()) {
            salidaAudio.hablar("No hay materias disponibles para inscribirse en este momento.", false);
            return;
        }

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        try {
            salidaAudio.detener();
        } catch (Exception ignored) {
        }

        navegacionActiva = true;
        posicionActual = 0;

        mainHandler.postDelayed(() -> leerMateriaActual(true, this::escucharComandosNavegacion), 120);
    }

    public void detenerNavegacion() {
        navegacionActiva = false;
        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        try {
            salidaAudio.detener();
        } catch (Exception ignored) {
        }
    }

    private void leerMateriaActual(boolean conInstrucciones, Runnable onDone) {
        if (!navegacionActiva) {
            if (onDone != null) onDone.run();
            return;
        }

        List<Materia> listaMaterias = agregarMateriaLogic.getListaMateriasFiltrada();

        if (listaMaterias == null || listaMaterias.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        if (posicionActual < 0 || posicionActual >= listaMaterias.size()) {
            posicionActual = 0;
        }

        Materia materia = listaMaterias.get(posicionActual);
        estaHablandoMateria = true;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        String texto = (conInstrucciones ? "Materia número " + (posicionActual + 1) + " de " + listaMaterias.size() + ". " : "") +
                (materia.getNombre() != null ? materia.getNombre() : "Sin nombre") + ". " +
                (materia.getDescripcion() != null ? materia.getDescripcion() : "Sin descripción.") +
                ". Di siguiente, anterior, repetir o inscribir.";

        salidaAudio.hablar(texto, false, () -> {
            estaHablandoMateria = false;
            mainHandler.postDelayed(() -> {
                if (onDone != null) onDone.run();
            }, 350);
        });
    }

    private void escucharComandosNavegacion() {
        if (!navegacionActiva) return;

        if (estaHablandoMateria) {
            mainHandler.postDelayed(this::escucharComandosNavegacion, 300);
            return;
        }

        entradaAudio.seleccionarOpcion(comandosNavegacion, indice -> {
            if (!navegacionActiva) return;

            if (indice < 0 || indice >= comandosNavegacion.size()) {
                mainHandler.postDelayed(this::escucharComandosNavegacion, 700);
                return;
            }

            String comando = comandosNavegacion.get(indice);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (comando) {
                case "siguiente":
                    posicionActual = (posicionActual + 1) % agregarMateriaLogic.getListaMateriasFiltrada().size();
                    moverViewPagerAPosicion(posicionActual, true);
                    leerMateriaActual(false, this::escucharComandosNavegacion);
                    break;

                case "anterior":
                    posicionActual = (posicionActual - 1 + agregarMateriaLogic.getListaMateriasFiltrada().size())
                            % agregarMateriaLogic.getListaMateriasFiltrada().size();
                    moverViewPagerAPosicion(posicionActual, true);
                    leerMateriaActual(false, this::escucharComandosNavegacion);
                    break;

                case "repetir":
                    leerMateriaActual(false, this::escucharComandosNavegacion);
                    break;

                case "inscribir":
                    confirmarInscripcion();
                    break;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de inscripción de materias.", false, this::detenerNavegacion);
                    break;

                default:
                    mainHandler.postDelayed(this::escucharComandosNavegacion, 800);
            }
        });
    }

    private void confirmarInscripcion() {
        List<Materia> listaMaterias = agregarMateriaLogic.getListaMateriasFiltrada();

        if (posicionActual < 0 || posicionActual >= listaMaterias.size()) {
            salidaAudio.hablar("Materia inválida. Intenta de nuevo.", true, this::escucharComandosNavegacion);
            return;
        }

        Materia materia = listaMaterias.get(posicionActual);
        String nombre = materia.getNombre() != null ? materia.getNombre() : "esta materia";

        salidaAudio.hablar("¿Deseas inscribirte en " + nombre + "? Di sí o no.", true, () -> {
            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!navegacionActiva) return;

                if (esSi) {
                    agregarMateriaLogic.inscribirMateria(materia, mat -> {
                        mainHandler.post(() -> {
                            if (!navegacionActiva) return;
                            fragment.actualizarListasDespuesInscripcion(mat);
                            salidaAudio.hablar("¡Inscripción exitosa en " +
                                            (mat.getNombre() != null ? mat.getNombre() : "la materia") + "!",
                                    false, this::detenerNavegacion);
                        });
                    });
                } else {
                    salidaAudio.hablar("Inscripción cancelada. Continuando con la navegación.",
                            true, this::escucharComandosNavegacion);
                }
            });
        });
    }

    private void moverViewPagerAPosicion(int posicion, boolean smooth) {
        if (viewPagerMaterias == null || adapterMaterias == null) return;

        int realSize = adapterMaterias.getRealSize();
        if (realSize <= 0) return;

        int centered = agregarMateriaLogic.centeredPositionForIndex(posicion, realSize);
        viewPagerMaterias.setCurrentItem(centered, smooth);
    }
}