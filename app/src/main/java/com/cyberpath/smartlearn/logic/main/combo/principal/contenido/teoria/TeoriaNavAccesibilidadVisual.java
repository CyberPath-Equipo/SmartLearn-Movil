package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.teoria.TeoriaFragment;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class TeoriaNavAccesibilidadVisual {


    private static final int MAX_POLLS_ESPERA_TTS = 25;

    private final Context context;
    private final TeoriaFragment fragment;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final List<String> comandos = List.of("repetir", "regresar", "salir", "detente");
    private final AtomicInteger generacionActual = new AtomicInteger(0);
    private boolean navegacionActiva = false;
    private boolean estaHablando = false;
    private int pollsEsperaTts = 0;
    private String contenidoActual;

    public TeoriaNavAccesibilidadVisual(Context context, TeoriaFragment fragment) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }


    public void iniciarNavegacion(String contenidoTeoria) {
        if (contenidoTeoria == null || contenidoTeoria.trim().isEmpty()) {
            salidaAudio.hablar("No hay contenido de teoría para leer.", false);
            return;
        }

        final int miGeneracion = generacionActual.incrementAndGet();
        mainHandler.removeCallbacksAndMessages(null);
        pollsEsperaTts = 0;
        contenidoActual = contenidoTeoria;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }
        try {
            salidaAudio.detener();
        } catch (Exception ignored) {
        }

        navegacionActiva = true;

        mainHandler.postDelayed(() -> leerContenido(miGeneracion, true), 150);
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

    private void leerContenido(int generacion, boolean conInstrucciones) {
        if (!esSesionVigente(generacion)) return;
        if (contenidoActual == null) return;

        estaHablando = true;
        pollsEsperaTts = 0;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        String texto = contenidoActual
                + (conInstrucciones ? ". Di repetir, regresar o salir." : "");

        salidaAudio.hablar(texto, false, () -> {
            estaHablando = false;
            if (!esSesionVigente(generacion)) return;
            mainHandler.postDelayed(() -> escucharComandos(generacion), 300);
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

        entradaAudio.seleccionarOpcion(comandos, idx -> {
            if (!esSesionVigente(generacion)) return;

            if (idx < 0 || idx >= comandos.size()) {
                mainHandler.postDelayed(() -> escucharComandos(generacion), 700);
                return;
            }

            String comando = comandos.get(idx);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (comando) {
                case "repetir":
                    leerContenido(generacion, false);
                    break;

                case "regresar":
                    detenerNavegacion();
                    fragment.regresarPantallaAnterior();
                    break;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo de la teoría.", false, this::detenerNavegacion);
                    break;

                default:
                    mainHandler.postDelayed(() -> escucharComandos(generacion), 700);
            }
        });
    }

    public void release() {
        detenerNavegacion();
    }
}