package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;

import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.AdaptadorPractica;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.PracticaFragment;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.List;

public class NavAccesibilidad {

    private final Context context;
    private final PracticaFragment fragment;
    private final PracticaLogic practicaLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ListView listViewEjercicios;
    private final AdaptadorPractica adaptador;

    private final List<String> comandos = List.of("siguiente", "anterior", "repetir", "entrar", "regresar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int posicionActual = 0;
    private boolean estaHablando = false;
    private boolean navegacionActiva = false;

    public NavAccesibilidad(Context context, PracticaFragment fragment, PracticaLogic practicaLogic,
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

        mainHandler.postDelayed(() -> leerEjercicioActual(true, this::escucharComandos), 120);
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

    private void leerEjercicioActual(boolean conInstrucciones, Runnable onDone) {
        if (!navegacionActiva) {
            if (onDone != null) onDone.run();
            return;
        }

        List<Ejercicio> listaEjercicios = practicaLogic.getListaEjercicios();

        if (listaEjercicios == null || listaEjercicios.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        if (posicionActual < 0) posicionActual = 0;
        if (posicionActual >= listaEjercicios.size()) posicionActual = listaEjercicios.size() - 1;

        Ejercicio ejercicio = listaEjercicios.get(posicionActual);
        estaHablando = true;

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
            mainHandler.postDelayed(() -> {
                if (onDone != null) onDone.run();
            }, 350);
        });
    }

    private void escucharComandos() {
        if (!navegacionActiva) return;

        if (estaHablando) {
            mainHandler.postDelayed(this::escucharComandos, 300);
            return;
        }

        entradaAudio.seleccionarOpcion(comandos, indice -> {
            if (!navegacionActiva) return;

            if (indice < 0 || indice >= comandos.size()) {
                mainHandler.postDelayed(this::escucharComandos, 700);
                return;
            }

            String comando = comandos.get(indice);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (comando) {
                case "siguiente":
                    posicionActual = (posicionActual + 1) % Math.max(1, practicaLogic.getListaEjercicios().size());
                    moverListViewAPosicion(posicionActual);
                    leerEjercicioActual(false, this::escucharComandos);
                    break;

                case "anterior":
                    posicionActual = (posicionActual - 1 + practicaLogic.getListaEjercicios().size())
                            % practicaLogic.getListaEjercicios().size();
                    moverListViewAPosicion(posicionActual);
                    leerEjercicioActual(false, this::escucharComandos);
                    break;

                case "repetir":
                    leerEjercicioActual(false, this::escucharComandos);
                    break;

                case "entrar":
                    confirmarEntrada();
                    break;

                case "regresar":
                    salidaAudio.hablar("Regresando a la sección de subtemas.", false, () -> {
                        fragment.simularRegresar();
                        detenerNavegacion();
                    });
                    break;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de práctica.", false, this::detenerNavegacion);
                    break;

                default:
                    mainHandler.postDelayed(this::escucharComandos, 700);
            }
        });
    }

    private void confirmarEntrada() {
        List<Ejercicio> listaEjercicios = practicaLogic.getListaEjercicios();

        if (listaEjercicios == null || listaEjercicios.isEmpty()
                || posicionActual < 0 || posicionActual >= listaEjercicios.size()) {
            salidaAudio.hablar("Ejercicio inválido. Intenta de nuevo.", true, this::escucharComandos);
            return;
        }

        Ejercicio ejercicio = listaEjercicios.get(posicionActual);
        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre() : "este ejercicio";

        salidaAudio.hablar("¿Deseas abrir " + nombre + "? Di sí o no.", true, () -> {
            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!navegacionActiva) return;

                if (esSi) {
                    fragment.entrarEjercicioDesdeAccesibilidad(ejercicio, (exito, ej) -> {
                        mainHandler.post(() -> {
                            if (!navegacionActiva) return;
                            if (exito) {
                                salidaAudio.hablar("Abriendo " +
                                                (ej.getNombre() != null ? ej.getNombre() : "el ejercicio") + ".",
                                        false, this::detenerNavegacion);
                            } else {
                                salidaAudio.hablar("No fue posible abrir el ejercicio. Intenta más tarde.",
                                        true, this::escucharComandos);
                            }
                        });
                    });
                } else {
                    salidaAudio.hablar("Operación cancelada. Continuando.",
                            true, this::escucharComandos);
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