package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica.ejercicio;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.ejercicio.EjercicioFragment;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.ArrayList;
import java.util.List;

public class NavAccesibilidad {

    private final Context context;
    private final EjercicioFragment fragment;
    private final EjercicioLogic ejercicioLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean navegacionActiva = false;
    private boolean estaHablando = false;

    public NavAccesibilidad(Context context, EjercicioFragment fragment, EjercicioLogic ejercicioLogic) {
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

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        try {
            salidaAudio.detener();
        } catch (Exception ignored) {
        }

        navegacionActiva = true;
        mainHandler.postDelayed(this::leerPreguntaYPreguntarOpciones, 150);
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

    private void leerPreguntaYPreguntarOpciones() {
        if (!navegacionActiva) return;

        Pregunta pregunta = ejercicioLogic.getPreguntaActual();

        if (pregunta == null) {
            if (ejercicioLogic.estaEjercicioFinalizado()) {
                hablarResumenYPreguntarSiguiente();
            }
            return;
        }

        estaHablando = true;

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
            mainHandler.postDelayed(this::escucharOpcionesOMenu, 300);
        });
    }

    private void escucharOpcionesOMenu() {
        if (!navegacionActiva) return;

        if (estaHablando) {
            mainHandler.postDelayed(this::escucharOpcionesOMenu, 250);
            return;
        }

        List<String> comandos = List.of("opciones", "repetir", "regresar", "salir");

        entradaAudio.seleccionarOpcion(comandos, idx -> {
            if (!navegacionActiva) return;

            if (idx < 0 || idx >= comandos.size()) {
                mainHandler.postDelayed(this::escucharOpcionesOMenu, 500);
                return;
            }

            String cmd = comandos.get(idx);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (cmd) {
                case "opciones":
                    leerOpcionesYPreguntarAccion();
                    break;
                case "repetir":
                    leerPreguntaYPreguntarOpciones();
                    break;
                case "regresar":
                    salidaAudio.hablar("Regresando.", false, () -> {
                        fragment.simularRegresar();
                        detenerNavegacion();
                    });
                    break;
                case "salir":
                    salidaAudio.hablar("Saliendo del ejercicio.", false, this::detenerNavegacion);
                    break;
                default:
                    mainHandler.postDelayed(this::escucharOpcionesOMenu, 500);
            }
        });
    }

    private void leerOpcionesYPreguntarAccion() {
        if (!navegacionActiva) return;

        Pregunta pregunta = ejercicioLogic.getPreguntaActual();

        if (pregunta == null) {
            mainHandler.postDelayed(this::leerPreguntaYPreguntarOpciones, 200);
            return;
        }

        List<Opcion> opciones = pregunta.getOpciones();

        if (opciones == null || opciones.isEmpty()) {
            salidaAudio.hablar("No hay opciones para esta pregunta.", true, this::leerPreguntaYPreguntarOpciones);
            return;
        }

        estaHablando = true;

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
            mainHandler.postDelayed(this::escucharSeleccionOModaRepetir, 300);
        });
    }

    private void escucharSeleccionOModaRepetir() {
        if (!navegacionActiva) return;

        if (estaHablando) {
            mainHandler.postDelayed(this::escucharSeleccionOModaRepetir, 250);
            return;
        }

        List<String> opciones = List.of("seleccionar", "repetir", "regresar", "salir");

        entradaAudio.seleccionarOpcion(opciones, idx -> {
            if (!navegacionActiva) return;

            if (idx < 0 || idx >= opciones.size()) {
                mainHandler.postDelayed(this::escucharSeleccionOModaRepetir, 500);
                return;
            }

            String cmd = opciones.get(idx);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (cmd) {
                case "seleccionar":
                    pedirNumeroDeOpcion();
                    break;
                case "repetir":
                    leerOpcionesYPreguntarAccion();
                    break;
                case "regresar":
                    salidaAudio.hablar("Regresando.", false, () -> {
                        fragment.simularRegresar();
                        detenerNavegacion();
                    });
                    break;
                case "salir":
                    salidaAudio.hablar("Saliendo del ejercicio.", false, this::detenerNavegacion);
                    break;
                default:
                    mainHandler.postDelayed(this::escucharSeleccionOModaRepetir, 500);
            }
        });
    }

    private void pedirNumeroDeOpcion() {
        if (!navegacionActiva) return;

        Pregunta pregunta = ejercicioLogic.getPreguntaActual();

        if (pregunta == null) {
            salidaAudio.hablar("No hay pregunta actual.", true, this::leerPreguntaYPreguntarOpciones);
            return;
        }

        int opcionesCount = ejercicioLogic.getNumeroOpcionesPreguntaActual();

        if (opcionesCount <= 0) {
            salidaAudio.hablar("No hay opciones para esta pregunta.", true, this::leerPreguntaYPreguntarOpciones);
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
            mainHandler.postDelayed(() -> {
                entradaAudio.seleccionarOpcion(numeros, idx -> {
                    if (!navegacionActiva) return;

                    if (idx < 0 || idx >= numeros.size()) {
                        salidaAudio.hablar("No entendí el número. Intenta de nuevo.", true, this::pedirNumeroDeOpcion);
                        return;
                    }

                    int opcionIdx = idx;
                    fragment.seleccionarOpcionPorIndice(opcionIdx);

                    String textoOpcion = ejercicioLogic.getTextoOpcionActual(opcionIdx);
                    salidaAudio.hablar("Seleccionaste la opción " + numeroEnPalabras(opcionIdx + 1) + ": " + textoOpcion + ". ¿Deseas comprobar? Di sí o no.", true, () -> {
                        entradaAudio.confirmarAfirmacion(esSi -> {
                            if (!navegacionActiva) return;

                            if (esSi) {
                                fragment.comprobarRespuestaDesdeAccesibilidad((exito, nuevaPos) -> {
                                    mainHandler.post(() -> {
                                        if (!navegacionActiva) return;

                                        if (ejercicioLogic.estaEjercicioFinalizado()) {
                                            hablarResumenYPreguntarSiguiente();
                                        } else {
                                            mainHandler.postDelayed(() -> leerPreguntaYPreguntarOpciones(), 250);
                                        }
                                    });
                                });
                            } else {
                                salidaAudio.hablar("Selección guardada. Si quieres comprobar dilo seleccionar y luego comprobar.", true, this::leerPreguntaYPreguntarOpciones);
                            }
                        });
                    });
                });
            }, 300);
        });
    }

    private void hablarResumenYPreguntarSiguiente() {
        if (!navegacionActiva) return;

        int total = ejercicioLogic.getTotalPreguntas();
        int correctas = ejercicioLogic.getScore();
        int incorrectas = Math.max(0, total - correctas);

        String texto = "Resumen: " + correctas + " correctas, " + incorrectas + " incorrectas. "
                + "¿Quieres regresar a la lista de ejercicios o pasar a la teoría? Di lista o teoría.";

        salidaAudio.hablar(texto, false, () -> {
            mainHandler.postDelayed(this::escucharResumenOpciones, 300);
        });
    }

    private void escucharResumenOpciones() {
        if (!navegacionActiva) return;

        List<String> opciones = List.of("lista", "teoria", "salir");

        entradaAudio.seleccionarOpcion(opciones, idx -> {
            if (!navegacionActiva) return;

            if (idx < 0 || idx >= opciones.size()) {
                mainHandler.postDelayed(this::escucharResumenOpciones, 700);
                return;
            }

            String cmd = opciones.get(idx);

            if (salidaAudio.estaHablando()) {
                salidaAudio.detener();
            }

            switch (cmd) {
                case "lista":
                    salidaAudio.hablar("Regresando a la lista de ejercicios.", false, () -> {
                        fragment.simularRegresar();
                        detenerNavegacion();
                    });
                    break;
                case "teoria":
                    salidaAudio.hablar("Abriendo la teoría.", false, () -> {
                        fragment.irATeoriaDesdeAccesibilidad();
                        detenerNavegacion();
                    });
                    break;
                case "salir":
                    salidaAudio.hablar("Saliendo.", false, this::detenerNavegacion);
                    break;
                default:
                    mainHandler.postDelayed(this::escucharResumenOpciones, 500);
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


