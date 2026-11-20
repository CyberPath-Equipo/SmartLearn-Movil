package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;

import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.AdaptadorPractica;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.PracticaFragment;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PracticaNavAccesibilidad {


    private static final int MAX_POLLS_ESPERA_TTS = 25;

    private final Context context;
    private final PracticaFragment fragment;
    private final PracticaLogic practicaLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ListView listViewEjercicios;
    private final AdaptadorPractica adaptador;

    private final List<String> comandos = List.of("siguiente", "anterior", "repetir", "entrar", "regresar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicInteger generacionActual = new AtomicInteger(0);
    private int posicionActual = 0;
    private boolean estaHablando = false;
    private boolean navegacionActiva = false;
    private int pollsEsperaTts = 0;

    public PracticaNavAccesibilidad(Context context, PracticaFragment fragment, PracticaLogic practicaLogic,
                                    ListView listViewEjercicios, AdaptadorPractica adaptador) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.practicaLogic = practicaLogic;
        this.listViewEjercicios = listViewEjercicios;
        this.adaptador = adaptador;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        List<Ejercicio> listaEjercicios = practicaLogic.getListaEjercicios();

        if (listaEjercicios == null || listaEjercicios.isEmpty()) {
            salidaAudio.hablar("No hay ejercicios disponibles en este subtema.", false);
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
        posicionActual = 0;

        mainHandler.postDelayed(
                () -> leerEjercicioActual(miGeneracion, true, () -> escucharComandos(miGeneracion)),
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

    private void leerEjercicioActual(int generacion, boolean conInstrucciones, Runnable onDone) {
        if (!esSesionVigente(generacion)) return;

        List<Ejercicio> listaEjercicios = practicaLogic.getListaEjercicios();

        if (listaEjercicios == null || listaEjercicios.isEmpty()) {
            detenerNavegacion();
            salidaAudio.hablar("No hay ejercicios disponibles.", false);
            return;
        }

        if (posicionActual < 0) posicionActual = 0;
        if (posicionActual >= listaEjercicios.size()) posicionActual = listaEjercicios.size() - 1;

        Ejercicio ejercicio = listaEjercicios.get(posicionActual);
        estaHablando = true;
        pollsEsperaTts = 0;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre() : "Ejercicio sin nombre";
        String texto = (conInstrucciones ? "Ejercicio número " + (posicionActual + 1) + " de " + listaEjercicios.size() + ". " : "")
                + nombre + ". Di siguiente, anterior, repetir o entrar.";

        moverListViewAPosicion(posicionActual);

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


            List<Ejercicio> listaActual = practicaLogic.getListaEjercicios();
            if (listaActual == null || listaActual.isEmpty()) {
                detenerNavegacion();
                salidaAudio.hablar("No hay ejercicios disponibles.", false);
                return;
            }

            switch (comando) {
                case "siguiente":
                    posicionActual = (posicionActual + 1) % listaActual.size();
                    moverListViewAPosicion(posicionActual);
                    leerEjercicioActual(generacion, false, () -> escucharComandos(generacion));
                    break;

                case "anterior":
                    posicionActual = (posicionActual - 1 + listaActual.size()) % listaActual.size();
                    moverListViewAPosicion(posicionActual);
                    leerEjercicioActual(generacion, false, () -> escucharComandos(generacion));
                    break;

                case "repetir":
                    leerEjercicioActual(generacion, false, () -> escucharComandos(generacion));
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
                    salidaAudio.hablar("Saliendo del módulo de práctica.", false, this::detenerNavegacion);
                    break;

                default:
                    mainHandler.postDelayed(() -> escucharComandos(generacion), 700);
            }
        });
    }

    private void confirmarEntrada(int generacion) {
        if (!esSesionVigente(generacion)) return;

        List<Ejercicio> listaEjercicios = practicaLogic.getListaEjercicios();

        if (listaEjercicios == null || listaEjercicios.isEmpty()
                || posicionActual < 0 || posicionActual >= listaEjercicios.size()) {
            salidaAudio.hablar("Ejercicio inválido. Intenta de nuevo.", true,
                    () -> escucharComandos(generacion));
            return;
        }

        Ejercicio ejercicio = listaEjercicios.get(posicionActual);
        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre() : "este ejercicio";

        salidaAudio.hablar("Deseas abrir " + nombre + "? Di sí o no.", true, () -> {
            if (!esSesionVigente(generacion)) return;

            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!esSesionVigente(generacion)) return;

                if (esSi) {
                    detenerNavegacion();
                    fragment.entrarEjercicioDesdeAccesibilidad(ejercicio, (exito, ej) -> {
                        mainHandler.post(() -> {
                            if (!exito) {
                                iniciarNavegacion();
                                salidaAudio.hablar("No fue posible abrir el ejercicio. Intenta más tarde.", true);
                            }
                        });
                    });
                } else {
                    salidaAudio.hablar("Operación cancelada. Continuando.",
                            true, () -> escucharComandos(generacion));
                }
            });
        });
    }

    private void moverListViewAPosicion(int posicion) {
        if (listViewEjercicios == null || practicaLogic == null) return;

        int count = practicaLogic.getRealSize();
        if (count == 0) return;

        int p = Math.max(0, Math.min(posicion, count - 1));

        try {
            listViewEjercicios.post(() -> {
                listViewEjercicios.setSelection(p);
            });
        } catch (Exception ignored) {
        }
    }
}