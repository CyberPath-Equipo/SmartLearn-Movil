package com.cyberpath.smartlearn.logic.main.combo.principal.tema;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.ui.main.combo.principal.tema.AdaptadorTemas;
import com.cyberpath.smartlearn.ui.main.combo.principal.tema.TemasFragment;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TemasNavAccesibilidad {


    private static final int MAX_POLLS_ESPERA_TTS = 25;

    private final Context context;
    private final TemasFragment fragment;
    private final TemasLogic temasLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerTemas;
    private final AdaptadorTemas adapterTemas;

    private final List<String> comandos = List.of("siguiente", "anterior", "repetir", "entrar", "regresar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicInteger generacionActual = new AtomicInteger(0);
    private int posicionActual = 0;
    private boolean estaHablando = false;
    private boolean navegacionActiva = false;
    private int pollsEsperaTts = 0;

    public TemasNavAccesibilidad(Context context, TemasFragment fragment, TemasLogic temasLogic,
                                 ViewPager2 viewPagerTemas, AdaptadorTemas adapterTemas) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.temasLogic = temasLogic;
        this.viewPagerTemas = viewPagerTemas;
        this.adapterTemas = adapterTemas;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        List<Tema> listaTemas = temasLogic.getListaTemas();

        if (listaTemas == null || listaTemas.isEmpty()) {
            salidaAudio.hablar("No hay temas disponibles en esta materia.", false);
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

        posicionActual = temasLogic.getPosicionActual();
        if (posicionActual < 0 || posicionActual >= listaTemas.size()) {
            posicionActual = 0;
        }

        mainHandler.postDelayed(
                () -> leerTemaActual(miGeneracion, true, () -> escucharComandos(miGeneracion)),
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

    private void leerTemaActual(int generacion, boolean conInstrucciones, Runnable onDone) {
        if (!esSesionVigente(generacion)) return;

        List<Tema> listaTemas = temasLogic.getListaTemas();

        if (listaTemas == null || listaTemas.isEmpty()) {
            detenerNavegacion();
            salidaAudio.hablar("No hay temas disponibles.", false);
            return;
        }

        if (posicionActual < 0 || posicionActual >= listaTemas.size()) {
            posicionActual = 0;
        }

        Tema tema = listaTemas.get(posicionActual);
        estaHablando = true;
        pollsEsperaTts = 0;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        String nombre = tema.getNombre() != null ? tema.getNombre() : "Sin nombre";
        String texto = (conInstrucciones ? "Tema número " + (posicionActual + 1) + " de " + listaTemas.size() + ". " : "")
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

            List<Tema> listaActual = temasLogic.getListaTemas();
            if (listaActual == null || listaActual.isEmpty()) {
                detenerNavegacion();
                salidaAudio.hablar("No hay temas disponibles.", false);
                return;
            }

            switch (comando) {
                case "siguiente":
                    posicionActual = (posicionActual + 1) % listaActual.size();
                    moverViewPager(posicionActual, true);
                    leerTemaActual(generacion, false, () -> escucharComandos(generacion));
                    break;

                case "anterior":
                    posicionActual = (posicionActual - 1 + listaActual.size()) % listaActual.size();
                    moverViewPager(posicionActual, true);
                    leerTemaActual(generacion, false, () -> escucharComandos(generacion));
                    break;

                case "repetir":
                    leerTemaActual(generacion, false, () -> escucharComandos(generacion));
                    break;

                case "entrar":
                    confirmarEntrada(generacion);
                    break;

                case "regresar":
                    detenerNavegacion();
                    fragment.regresarPantallaAnterior();
                    break;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de temas.", false, this::detenerNavegacion);
                    break;

                default:


                    mainHandler.postDelayed(() -> escucharComandos(generacion), 700);
            }
        });
    }

    private void confirmarEntrada(int generacion) {
        if (!esSesionVigente(generacion)) return;

        List<Tema> listaTemas = temasLogic.getListaTemas();

        if (listaTemas == null || listaTemas.isEmpty()
                || posicionActual < 0 || posicionActual >= listaTemas.size()) {


            salidaAudio.hablar("Tema inválido. Intenta de nuevo.", true,
                    () -> escucharComandos(generacion));
            return;
        }

        Tema tema = listaTemas.get(posicionActual);
        String nombre = tema.getNombre() != null ? tema.getNombre() : "este tema";

        salidaAudio.hablar("Deseas entrar en " + nombre + "? Di sí o no.", true, () -> {
            if (!esSesionVigente(generacion)) return;

            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!esSesionVigente(generacion)) return;

                if (esSi) {
                    detenerNavegacion();
                    fragment.entrarTemaDesdeAccesibilidad(tema, (exito, t) -> {
                        mainHandler.post(() -> {
                            if (!exito) {


                                iniciarNavegacion();
                                salidaAudio.hablar("No fue posible entrar. Intenta más tarde.", true);
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
        if (viewPagerTemas == null || temasLogic == null) return;

        int realSize = temasLogic.getRealSize();
        if (realSize == 0) return;

        int target = Math.max(0, posicionReal + 1);
        viewPagerTemas.setCurrentItem(target, smooth);
    }
}