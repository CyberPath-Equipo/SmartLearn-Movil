package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica.ejercicio;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.ejercicio.EjercicioFragment;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EjercicioNavAccesibilidad {


    private static final int MAX_POLLS_ESPERA_TTS = 25;


    private static final int MAX_REINTENTOS_NUMERO = 3;

    private final Context context;
    private final EjercicioFragment fragment;
    private final EjercicioLogic ejercicioLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicInteger generacionActual = new AtomicInteger(0);
    private boolean navegacionActiva = false;
    private boolean estaHablando = false;
    private int pollsEsperaTts = 0;
    private int reintentosNumero = 0;

    public EjercicioNavAccesibilidad(Context context, EjercicioFragment fragment, EjercicioLogic ejercicioLogic) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.ejercicioLogic = ejercicioLogic;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        if (ejercicioLogic.getTotalPreguntas() == 0) {
            salidaAudio.hablar("No hay preguntas en este ejercicio.", false);
            return;
        }

        final int miGeneracion = generacionActual.incrementAndGet();
        mainHandler.removeCallbacksAndMessages(null);
        pollsEsperaTts = 0;
        reintentosNumero = 0;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }
        try {
            salidaAudio.detener();
        } catch (Exception ignored) {
        }

        navegacionActiva = true;
        mainHandler.postDelayed(() -> leerPreguntaYPreguntarOpciones(miGeneracion), 150);
    }

    public void detenerNavegacion() {
        generacionActual.incrementAndGet();
        navegacionActiva = false;
        mainHandler.removeCallbacksAndMessages(null);
        pollsEsperaTts = 0;
        reintentosNumero = 0;

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

    private void leerPreguntaYPreguntarOpciones(int generacion) {
        if (!esSesionVigente(generacion)) return;

        Pregunta pregunta = ejercicioLogic.getPreguntaActual();

        if (pregunta == null) {
            if (ejercicioLogic.estaEjercicioFinalizado()) {
                hablarResumenYPreguntarSiguiente(generacion);
            } else {


                detenerNavegacion();
                salidaAudio.hablar("No se pudo cargar la pregunta actual.", false);
            }
            return;
        }

        estaHablando = true;
        pollsEsperaTts = 0;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        String texto = "Pregunta " + (ejercicioLogic.getCurrentQuestionIndex() + 1) + " de "
                + ejercicioLogic.getTotalPreguntas() + ". "
                + pregunta.getEnunciado() + ". "
                + "¿Quieres pasar a las opciones o repetir la pregunta? Di opciones o repetir.";

        salidaAudio.hablar(texto, false, () -> {
            estaHablando = false;
            if (!esSesionVigente(generacion)) return;
            mainHandler.postDelayed(() -> escucharOpcionesOMenu(generacion), 300);
        });
    }

    private void escucharOpcionesOMenu(int generacion) {
        if (!esSesionVigente(generacion)) return;

        if (estaHablando) {
            pollsEsperaTts++;
            if (pollsEsperaTts > MAX_POLLS_ESPERA_TTS) {
                estaHablando = false;
                pollsEsperaTts = 0;
            } else {
                mainHandler.postDelayed(() -> escucharOpcionesOMenu(generacion), 250);
                return;
            }
        }

        List<String> comandos = List.of("opciones", "repetir", "regresar", "salir");

        entradaAudio.seleccionarOpcion(comandos, idx -> {
            if (!esSesionVigente(generacion)) return;

            if (idx < 0 || idx >= comandos.size()) {
                mainHandler.postDelayed(() -> escucharOpcionesOMenu(generacion), 500);
                return;
            }

            String cmd = comandos.get(idx);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (cmd) {
                case "opciones":
                    leerOpcionesYPreguntarAccion(generacion);
                    break;
                case "repetir":
                    leerPreguntaYPreguntarOpciones(generacion);
                    break;
                case "regresar":
                    detenerNavegacion();
                    fragment.simularRegresar();
                    break;
                case "salir":
                    salidaAudio.hablar("Saliendo del ejercicio.", false, this::detenerNavegacion);
                    break;
                default:
                    mainHandler.postDelayed(() -> escucharOpcionesOMenu(generacion), 500);
            }
        });
    }

    private void leerOpcionesYPreguntarAccion(int generacion) {
        if (!esSesionVigente(generacion)) return;

        Pregunta pregunta = ejercicioLogic.getPreguntaActual();

        if (pregunta == null) {
            mainHandler.postDelayed(() -> leerPreguntaYPreguntarOpciones(generacion), 200);
            return;
        }

        List<Opcion> opciones = pregunta.getOpciones();

        if (opciones == null || opciones.isEmpty()) {
            salidaAudio.hablar("No hay opciones para esta pregunta.", true,
                    () -> leerPreguntaYPreguntarOpciones(generacion));
            return;
        }

        estaHablando = true;
        pollsEsperaTts = 0;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < opciones.size(); i++) {
            sb.append("Opción ").append(numeroEnPalabras(i + 1)).append(": ").append(opciones.get(i).getTexto()).append(". ");
        }
        sb.append("¿Quieres seleccionar una opción o repetir todas las opciones? Di seleccionar o repetir.");

        salidaAudio.hablar(sb.toString(), false, () -> {
            estaHablando = false;
            if (!esSesionVigente(generacion)) return;
            mainHandler.postDelayed(() -> escucharSeleccionOModaRepetir(generacion), 300);
        });
    }

    private void escucharSeleccionOModaRepetir(int generacion) {
        if (!esSesionVigente(generacion)) return;

        if (estaHablando) {
            pollsEsperaTts++;
            if (pollsEsperaTts > MAX_POLLS_ESPERA_TTS) {
                estaHablando = false;
                pollsEsperaTts = 0;
            } else {
                mainHandler.postDelayed(() -> escucharSeleccionOModaRepetir(generacion), 250);
                return;
            }
        }

        List<String> opciones = List.of("seleccionar", "repetir", "regresar", "salir");

        entradaAudio.seleccionarOpcion(opciones, idx -> {
            if (!esSesionVigente(generacion)) return;

            if (idx < 0 || idx >= opciones.size()) {
                mainHandler.postDelayed(() -> escucharSeleccionOModaRepetir(generacion), 500);
                return;
            }

            String cmd = opciones.get(idx);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (cmd) {
                case "seleccionar":
                    reintentosNumero = 0;
                    pedirNumeroDeOpcion(generacion);
                    break;
                case "repetir":
                    leerOpcionesYPreguntarAccion(generacion);
                    break;
                case "regresar":
                    detenerNavegacion();
                    fragment.simularRegresar();
                    break;
                case "salir":
                    salidaAudio.hablar("Saliendo del ejercicio.", false, this::detenerNavegacion);
                    break;
                default:
                    mainHandler.postDelayed(() -> escucharSeleccionOModaRepetir(generacion), 500);
            }
        });
    }

    private void pedirNumeroDeOpcion(int generacion) {
        if (!esSesionVigente(generacion)) return;

        Pregunta pregunta = ejercicioLogic.getPreguntaActual();

        if (pregunta == null) {
            salidaAudio.hablar("No hay pregunta actual.", true, () -> leerPreguntaYPreguntarOpciones(generacion));
            return;
        }

        int opcionesCount = ejercicioLogic.getNumeroOpcionesPreguntaActual();

        if (opcionesCount <= 0) {
            salidaAudio.hablar("No hay opciones para esta pregunta.", true,
                    () -> leerPreguntaYPreguntarOpciones(generacion));
            return;
        }

        List<String> numeros = new ArrayList<>();
        for (int i = 1; i <= opcionesCount; i++) {
            numeros.add(numeroEnPalabras(i));
        }

        StringBuilder instruccion = new StringBuilder("Di el número de la opción que quieres seleccionar: ");
        for (int i = 0; i < numeros.size(); i++) {
            instruccion.append(numeros.get(i));
            if (i < numeros.size() - 1) instruccion.append(", ");
        }
        instruccion.append(". También puedes decir regresar o salir.");

        salidaAudio.hablar(instruccion.toString(), false, () -> {
            if (!esSesionVigente(generacion)) return;

            mainHandler.postDelayed(() -> {
                if (!esSesionVigente(generacion)) return;

                entradaAudio.seleccionarOpcion(numeros, idx -> {
                    if (!esSesionVigente(generacion)) return;

                    if (idx < 0 || idx >= numeros.size()) {


                        reintentosNumero++;
                        if (reintentosNumero > MAX_REINTENTOS_NUMERO) {
                            reintentosNumero = 0;
                            salidaAudio.hablar(
                                    "No logré identificar tu selección. Volviendo a las opciones.",
                                    true,
                                    () -> leerOpcionesYPreguntarAccion(generacion)
                            );
                            return;
                        }
                        salidaAudio.hablar("No entendí el número. Intenta de nuevo.", true,
                                () -> pedirNumeroDeOpcion(generacion));
                        return;
                    }

                    reintentosNumero = 0;
                    int opcionIdx = idx;
                    fragment.seleccionarOpcionPorIndice(opcionIdx);

                    String textoOpcion = ejercicioLogic.getTextoOpcionActual(opcionIdx);
                    salidaAudio.hablar("Seleccionaste la opción " + numeroEnPalabras(opcionIdx + 1) + ": " + textoOpcion
                            + ". ¿Deseas comprobar? Di sí o no.", true, () -> {
                        if (!esSesionVigente(generacion)) return;

                        entradaAudio.confirmarAfirmacion(esSi -> {
                            if (!esSesionVigente(generacion)) return;

                            if (esSi) {
                                fragment.comprobarRespuestaDesdeAccesibilidad((exito, nuevaPos) -> {
                                    mainHandler.post(() -> {
                                        if (!esSesionVigente(generacion)) return;

                                        if (ejercicioLogic.estaEjercicioFinalizado()) {
                                            hablarResumenYPreguntarSiguiente(generacion);
                                        } else {
                                            mainHandler.postDelayed(
                                                    () -> leerPreguntaYPreguntarOpciones(generacion), 250);
                                        }
                                    });
                                });
                            } else {
                                salidaAudio.hablar(
                                        "Selección guardada. Si quieres comprobar dilo seleccionar y luego comprobar.",
                                        true, () -> leerPreguntaYPreguntarOpciones(generacion));
                            }
                        });
                    });
                });
            }, 300);
        });
    }

    private void hablarResumenYPreguntarSiguiente(int generacion) {
        if (!esSesionVigente(generacion)) return;

        int total = ejercicioLogic.getTotalPreguntas();
        int correctas = ejercicioLogic.getScore();
        int incorrectas = Math.max(0, total - correctas);

        String texto = "Resumen: " + correctas + " correctas, " + incorrectas + " incorrectas. "
                + "¿Quieres regresar a la lista de ejercicios o pasar a la teoría? Di lista o teoría.";

        salidaAudio.hablar(texto, false, () -> {
            if (!esSesionVigente(generacion)) return;
            mainHandler.postDelayed(() -> escucharResumenOpciones(generacion), 300);
        });
    }

    private void escucharResumenOpciones(int generacion) {
        if (!esSesionVigente(generacion)) return;

        List<String> opciones = List.of("lista", "teoria", "salir");

        entradaAudio.seleccionarOpcion(opciones, idx -> {
            if (!esSesionVigente(generacion)) return;

            if (idx < 0 || idx >= opciones.size()) {
                mainHandler.postDelayed(() -> escucharResumenOpciones(generacion), 700);
                return;
            }

            String cmd = opciones.get(idx);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (cmd) {
                case "lista":
                    detenerNavegacion();
                    fragment.simularRegresar();
                    break;
                case "teoria":
                    detenerNavegacion();
                    fragment.irATeoriaDesdeAccesibilidad();
                    break;
                case "salir":
                    salidaAudio.hablar("Saliendo.", false, this::detenerNavegacion);
                    break;
                default:
                    mainHandler.postDelayed(() -> escucharResumenOpciones(generacion), 500);
            }
        });
    }

    private String numeroEnPalabras(int n) {
        switch (n) {
            case 1:
                return "uno";
            case 2:
                return "dos";
            case 3:
                return "tres";
            case 4:
                return "cuatro";
            case 5:
                return "cinco";
            case 6:
                return "seis";
            case 7:
                return "siete";
            case 8:
                return "ocho";
            case 9:
                return "nueve";
            case 10:
                return "diez";
            default:
                return String.valueOf(n);
        }
    }
}