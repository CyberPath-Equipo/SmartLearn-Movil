package com.cyberpath.smartlearn.logic.main.combo.principal.materia;

import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.ui.main.combo.principal.materia.AdaptadorMaterias;
import com.cyberpath.smartlearn.ui.main.combo.principal.materia.MateriasFragment;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;
import com.cyberpath.smartlearn.util.ui.CarruselUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MateriasNavAccesibilidad {
    private static final int MAX_POLLS_ESPERA_TTS = 25;

    private final MateriasFragment fragment;
    private final MateriasLogic materiasLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerMaterias;
    private final AdaptadorMaterias adapterMaterias;

    private final List<String> comandosNavegacion = List.of("siguiente", "anterior", "repetir", "entrar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicInteger generacionActual = new AtomicInteger(0);
    private int posicionActual = 0;
    private boolean estaHablandoMateria = false;
    private boolean navegacionActiva = false;
    private int pollsEsperaTts = 0;

    public MateriasNavAccesibilidad(MateriasFragment fragment,
                                    MateriasLogic materiasLogic,
                                    ViewPager2 viewPagerMaterias,
                                    AdaptadorMaterias adapterMaterias) {
        this.fragment = fragment;
        this.materiasLogic = materiasLogic;
        this.viewPagerMaterias = viewPagerMaterias;
        this.adapterMaterias = adapterMaterias;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        List<Materia> listaMaterias = materiasLogic.getListaMateriasFiltrada();

        if (listaMaterias == null || listaMaterias.isEmpty()) {
            salidaAudio.hablar("No hay materias inscritas en este momento.", false);
            return;
        }

        final int miGeneracion = generacionActual.incrementAndGet();
        mainHandler.removeCallbacksAndMessages(null);
        pollsEsperaTts = 0;

        try {
            entradaAudio.detenerEscucha();
            salidaAudio.detener();
        } catch (Exception ignored) {
        }

        navegacionActiva = true;

        posicionActual = materiasLogic.getPosicionActual();
        if (posicionActual < 0 || posicionActual >= listaMaterias.size()) {
            posicionActual = 0;
        }

        mainHandler.postDelayed(
                () -> leerMateriaActual(miGeneracion, true, () -> escucharComandosNavegacion(miGeneracion)),
                120
        );
    }

    public void detenerNavegacion() {
        generacionActual.incrementAndGet();
        navegacionActiva = false;
        mainHandler.removeCallbacksAndMessages(null);
        pollsEsperaTts = 0;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        try {
            salidaAudio.detener();
        } catch (Exception ignored) {
        }
    }

    private boolean esSesionVigente(int generacion) {
        return navegacionActiva && generacion == generacionActual.get();
    }

    private void leerMateriaActual(int generacion, boolean conInstrucciones, Runnable onDone) {
        if (!esSesionVigente(generacion)) {
            return;
        }

        List<Materia> listaMaterias = materiasLogic.getListaMateriasFiltrada();

        if (listaMaterias == null || listaMaterias.isEmpty()) {
            detenerNavegacion();
            salidaAudio.hablar("No hay materias disponibles.", false);
            return;
        }

        if (posicionActual < 0 || posicionActual >= listaMaterias.size()) {
            posicionActual = 0;
        }

        Materia materia = listaMaterias.get(posicionActual);
        estaHablandoMateria = true;
        pollsEsperaTts = 0;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        String texto = (conInstrucciones ? "Materia número " + (posicionActual + 1) + " de " + listaMaterias.size() + ". " : "") +
                (materia.getNombre() != null ? materia.getNombre() : "Sin nombre") + ". " +
                (materia.getDescripcion() != null ? materia.getDescripcion() : "Sin descripción.") +
                ". Di siguiente, anterior, repetir o entrar.";

        salidaAudio.hablar(texto, false, () -> {
            estaHablandoMateria = false;
            if (!esSesionVigente(generacion)) return;
            mainHandler.postDelayed(() -> {
                if (esSesionVigente(generacion) && onDone != null) onDone.run();
            }, 350);
        });
    }

    private void escucharComandosNavegacion(int generacion) {
        if (!esSesionVigente(generacion)) return;

        if (estaHablandoMateria) {
            pollsEsperaTts++;
            if (pollsEsperaTts > MAX_POLLS_ESPERA_TTS) {
                estaHablandoMateria = false;
                pollsEsperaTts = 0;
            } else {
                mainHandler.postDelayed(() -> escucharComandosNavegacion(generacion), 300);
                return;
            }
        }

        entradaAudio.seleccionarOpcion(comandosNavegacion, indice -> {
            if (!esSesionVigente(generacion)) return;

            if (indice < 0 || indice >= comandosNavegacion.size()) {
                mainHandler.postDelayed(() -> escucharComandosNavegacion(generacion), 700);
                return;
            }

            String comando = comandosNavegacion.get(indice);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            List<Materia> listaActual = materiasLogic.getListaMateriasFiltrada();
            if (listaActual == null || listaActual.isEmpty()) {
                detenerNavegacion();
                salidaAudio.hablar("No hay materias disponibles.", false);
                return;
            }

            switch (comando) {
                case "siguiente":
                    posicionActual = (posicionActual + 1) % listaActual.size();
                    moverViewPagerAPosicion(posicionActual, true);
                    leerMateriaActual(generacion, false, () -> escucharComandosNavegacion(generacion));
                    break;

                case "anterior":
                    posicionActual = (posicionActual - 1 + listaActual.size()) % listaActual.size();
                    moverViewPagerAPosicion(posicionActual, true);
                    leerMateriaActual(generacion, false, () -> escucharComandosNavegacion(generacion));
                    break;

                case "repetir":
                    leerMateriaActual(generacion, false, () -> escucharComandosNavegacion(generacion));
                    break;

                case "entrar":
                    confirmarEntrada(generacion);
                    break;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de materias.", false, this::detenerNavegacion);
                    break;

                default:
                    mainHandler.postDelayed(() -> escucharComandosNavegacion(generacion), 800);
            }
        });
    }

    private void confirmarEntrada(int generacion) {
        if (!esSesionVigente(generacion)) return;

        List<Materia> listaMaterias = materiasLogic.getListaMateriasFiltrada();

        if (listaMaterias == null || listaMaterias.isEmpty()
                || posicionActual < 0 || posicionActual >= listaMaterias.size()) {


            salidaAudio.hablar(
                    "No pude identificar la materia seleccionada. Intenta de nuevo.",
                    true,
                    () -> escucharComandosNavegacion(generacion)
            );
            return;
        }

        Materia materia = listaMaterias.get(posicionActual);
        String nombre = materia.getNombre() != null ? materia.getNombre() : "esta materia";

        salidaAudio.hablar("Deseas entrar en " + nombre + "? Di sí o no.", true, () -> {
            if (!esSesionVigente(generacion)) return;

            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!esSesionVigente(generacion)) return;

                if (esSi) {
                    detenerNavegacion();
                    fragment.entrarMateriaDesdeAccesibilidad(materia, (exito, mat) -> {
                        mainHandler.post(() -> {
                            if (!exito) {
                                iniciarNavegacion();
                                salidaAudio.hablar("Error al entrar. Intenta más tarde.", true);
                            }
                        });
                    });
                } else {
                    salidaAudio.hablar("Entrada cancelada. Continuando con la navegación.",
                            true, () -> escucharComandosNavegacion(generacion));
                }
            });
        });
    }

    private void moverViewPagerAPosicion(int posicion, boolean smooth) {
        if (viewPagerMaterias == null || adapterMaterias == null) return;

        int realSize = adapterMaterias.getRealSize();
        if (realSize <= 0) return;

        int centered = CarruselUtils.centrarPosicion(posicion, realSize);
        viewPagerMaterias.setCurrentItem(centered, smooth);
    }
}