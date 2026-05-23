package com.cyberpath.smartlearn.logic.main.combo.principal.materia;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.ui.main.combo.principal.materia.AdaptadorMaterias;
import com.cyberpath.smartlearn.ui.main.combo.principal.materia.MateriasFragment;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;

import java.util.List;

public class NavAccesibilidad {

    private final Context context;
    private final MateriasFragment fragment;
    private final MateriasLogic materiasLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerMaterias;
    private final AdaptadorMaterias adapterMaterias;

    private final List<String> comandosNavegacion = List.of("siguiente", "anterior", "repetir", "entrar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int posicionActual = 0;
    private boolean estaHablandoMateria = false;
    private boolean navegacionActiva = false;

    public NavAccesibilidad(Context context, MateriasFragment fragment,
                            MateriasLogic materiasLogic,
                            ViewPager2 viewPagerMaterias,
                            AdaptadorMaterias adapterMaterias) {
        this.context = context.getApplicationContext();
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

        List<Materia> listaMaterias = materiasLogic.getListaMateriasFiltrada();

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
                ". Di siguiente, anterior, repetir o entrar.";

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
                    posicionActual = (posicionActual + 1) % materiasLogic.getListaMateriasFiltrada().size();
                    moverViewPagerAPosicion(posicionActual, true);
                    leerMateriaActual(false, this::escucharComandosNavegacion);
                    break;

                case "anterior":
                    posicionActual = (posicionActual - 1 + materiasLogic.getListaMateriasFiltrada().size())
                            % materiasLogic.getListaMateriasFiltrada().size();
                    moverViewPagerAPosicion(posicionActual, true);
                    leerMateriaActual(false, this::escucharComandosNavegacion);
                    break;

                case "repetir":
                    leerMateriaActual(false, this::escucharComandosNavegacion);
                    break;

                case "entrar":
                    confirmarEntrada();
                    break;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de materias.", false, this::detenerNavegacion);
                    break;

                default:
                    mainHandler.postDelayed(this::escucharComandosNavegacion, 800);
            }
        });
    }

    private void pararTodo() {
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}
        try { salidaAudio.detener(); } catch (Exception ignored) {}
        navegacionActiva = false;
    }

    private void confirmarEntrada() {
        List<Materia> listaMaterias = materiasLogic.getListaMateriasFiltrada();

        if (posicionActual < 0 || posicionActual >= listaMaterias.size()) {
            return;
        }

        Materia materia = listaMaterias.get(posicionActual);
        String nombre = materia.getNombre() != null ? materia.getNombre() : "esta materia";

        salidaAudio.hablar("Deseas entrar en " + nombre + "? Di sí o no.", true, () -> {
            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!navegacionActiva) return;

                if (esSi) {
                    pararTodo();
                    fragment.entrarMateriaDesdeAccesibilidad(materia, (exito, mat) -> {
                        mainHandler.post(() -> {
                            if (!exito) {
                                navegacionActiva = true;
                                salidaAudio.hablar("Error al entrar. Intenta más tarde.",
                                        true, this::escucharComandosNavegacion);
                            }
                        });
                    });
                } else {
                    salidaAudio.hablar("Entrada cancelada. Continuando con la navegación.",
                            true, this::escucharComandosNavegacion);
                }
            });
        });
    }

    private void moverViewPagerAPosicion(int posicion, boolean smooth) {
        if (viewPagerMaterias == null || adapterMaterias == null) return;

        int realSize = adapterMaterias.getRealSize();
        if (realSize <= 0) return;

        int centered = materiasLogic.centeredPositionForIndex(posicion, realSize);
        viewPagerMaterias.setCurrentItem(centered, smooth);
    }
}