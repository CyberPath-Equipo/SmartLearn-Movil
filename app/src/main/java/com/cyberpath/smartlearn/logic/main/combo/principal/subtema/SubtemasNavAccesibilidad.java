package com.cyberpath.smartlearn.logic.main.combo.principal.subtema;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.ui.main.combo.principal.subtema.AdaptadorSubtemas;
import com.cyberpath.smartlearn.ui.main.combo.principal.subtema.SubtemasFragment;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SubtemasNavAccesibilidad {


    private static final int MAX_POLLS_ESPERA_TTS = 25;

    private final Context context;
    private final SubtemasFragment fragment;
    private final SubtemasLogic subtemasLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerSubtemas;
    private final AdaptadorSubtemas adapterSubtemas;

    private final List<String> comandos = List.of("siguiente", "anterior", "repetir", "entrar", "regresar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicInteger generacionActual = new AtomicInteger(0);
    private int posicionActual = 0;
    private boolean estaHablando = false;
    private boolean navegacionActiva = false;
    private int pollsEsperaTts = 0;

    public SubtemasNavAccesibilidad(Context context, SubtemasFragment fragment, SubtemasLogic subtemasLogic,
                                    ViewPager2 viewPagerSubtemas, AdaptadorSubtemas adapterSubtemas) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.subtemasLogic = subtemasLogic;
        this.viewPagerSubtemas = viewPagerSubtemas;
        this.adapterSubtemas = adapterSubtemas;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        List<Subtema> listaSubtemas = subtemasLogic.getListaSubtemas();

        if (listaSubtemas == null || listaSubtemas.isEmpty()) {
            salidaAudio.hablar("No hay subtemas disponibles en este tema.", false);
            return;
        }

        final int miGeneracion = generacionActual.incrementAndGet();
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

        navegacionActiva = true;

        posicionActual = subtemasLogic.getPosicionActual();
        if (posicionActual < 0 || posicionActual >= listaSubtemas.size()) {
            posicionActual = 0;
        }

        mainHandler.postDelayed(
                () -> leerSubtemaActual(miGeneracion, true, () -> escucharComandos(miGeneracion)),
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

    private void leerSubtemaActual(int generacion, boolean conInstrucciones, Runnable onDone) {
        if (!esSesionVigente(generacion)) return;

        List<Subtema> listaSubtemas = subtemasLogic.getListaSubtemas();

        if (listaSubtemas == null || listaSubtemas.isEmpty()) {
            detenerNavegacion();
            salidaAudio.hablar("No hay subtemas disponibles.", false);
            return;
        }

        if (posicionActual < 0 || posicionActual >= listaSubtemas.size()) {
            posicionActual = 0;
        }

        Subtema subtema = listaSubtemas.get(posicionActual);
        estaHablando = true;
        pollsEsperaTts = 0;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        String nombre = subtema.getNombre() != null ? subtema.getNombre() : "Sin nombre";
        String texto = (conInstrucciones ? "Subtema número " + (posicionActual + 1) + " de " + listaSubtemas.size() + ". " : "")
                + nombre + ". Di siguiente, anterior, repetir, entrar o regresar.";

        salidaAudio.hablar(texto, false, () -> {
            estaHablando = false;
            if (!esSesionVigente(generacion)) return;
            mainHandler.postDelayed(() -> {
                if (esSesionVigente(generacion) && onDone != null) onDone.run();
            }, 350);
        });
    }

    private void escucharComandos(int generacion) {
        if (!esSesionVigente(generacion)) return;

        if (estaHablando) {
            pollsEsperaTts++;
            if (pollsEsperaTts > MAX_POLLS_ESPERA_TTS) {
                estaHablando = false;
                pollsEsperaTts = 0;
            } else {
                mainHandler.postDelayed(() -> escucharComandos(generacion), 300);
                return;
            }
        }

        entradaAudio.seleccionarOpcion(comandos, indice -> {
            if (!esSesionVigente(generacion)) return;

            if (indice < 0 || indice >= comandos.size()) {
                mainHandler.postDelayed(() -> escucharComandos(generacion), 700);
                return;
            }

            String comando = comandos.get(indice);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            List<Subtema> listaActual = subtemasLogic.getListaSubtemas();
            if (listaActual == null || listaActual.isEmpty()) {
                detenerNavegacion();
                salidaAudio.hablar("No hay subtemas disponibles.", false);
                return;
            }

            switch (comando) {
                case "siguiente":
                    posicionActual = (posicionActual + 1) % listaActual.size();
                    moverViewPager(posicionActual, true);
                    leerSubtemaActual(generacion, false, () -> escucharComandos(generacion));
                    break;

                case "anterior":
                    posicionActual = (posicionActual - 1 + listaActual.size()) % listaActual.size();
                    moverViewPager(posicionActual, true);
                    leerSubtemaActual(generacion, false, () -> escucharComandos(generacion));
                    break;

                case "repetir":
                    leerSubtemaActual(generacion, false, () -> escucharComandos(generacion));
                    break;

                case "entrar":
                    confirmarEntrada(generacion);
                    break;

                case "regresar":
                    detenerNavegacion();
                    fragment.simularRegresar();
                    break;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de subtemas.", false, this::detenerNavegacion);
                    break;

                default:
                    mainHandler.postDelayed(() -> escucharComandos(generacion), 700);
            }
        });
    }

    private void confirmarEntrada(int generacion) {
        if (!esSesionVigente(generacion)) return;

        List<Subtema> listaSubtemas = subtemasLogic.getListaSubtemas();

        if (listaSubtemas == null || listaSubtemas.isEmpty()
                || posicionActual < 0 || posicionActual >= listaSubtemas.size()) {
            salidaAudio.hablar("Subtema inválido. Intenta de nuevo.", true,
                    () -> escucharComandos(generacion));
            return;
        }

        Subtema subtema = listaSubtemas.get(posicionActual);
        String nombre = subtema.getNombre() != null ? subtema.getNombre() : "este subtema";

        salidaAudio.hablar("Deseas entrar en " + nombre + "? Di sí o no.", true, () -> {
            if (!esSesionVigente(generacion)) return;

            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!esSesionVigente(generacion)) return;

                if (esSi) {
                    detenerNavegacion();
                    fragment.entrarSubtemaDesdeAccesibilidad(subtema, (exito, st) -> {
                        mainHandler.post(() -> {
                            if (!exito) {
                                iniciarNavegacion();
                                salidaAudio.hablar("No fue posible abrir el subtema. Intenta más tarde.", true);
                            }
                        });
                    });
                } else {
                    salidaAudio.hablar("Entrada cancelada. Continuando con la navegación.",
                            true, () -> escucharComandos(generacion));
                }
            });
        });
    }

    private void moverViewPager(int posicionReal, boolean smooth) {
        if (viewPagerSubtemas == null || subtemasLogic == null) return;

        int realSize = subtemasLogic.getRealSize();
        if (realSize == 0) return;

        int target = Math.max(0, posicionReal + 1);
        viewPagerSubtemas.setCurrentItem(target, smooth);
    }
}