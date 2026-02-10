package com.cyberpath.smartlearn.ui.main.combo.principal.materias.contenido.practica;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;

import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Navegación por voz para PracticaFragment (lista de ejercicios).
 * Comandos: "siguiente", "anterior", "repetir", "entrar", "regresar", "salir"/"detente".
 *
 * Protecciones:
 * - Detiene la escucha antes de hablar.
 * - Espera un pequeño delay después del TTS antes de arrancar la escucha.
 */
public class NavAccesibilidadPractica {

    private final Context context;
    private final PracticaFragment fragment;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ListView listViewEjercicios;
    private final AdaptadorPractica adaptador;
    private final List<Ejercicio> listaEjercicios;

    private int posicionActual = 0;
    private boolean estaHablando = false;
    private boolean navegacionActiva = false;

    private final List<String> comandos = List.of("siguiente", "anterior", "repetir", "entrar", "regresar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public NavAccesibilidadPractica(Context context,
                            PracticaFragment fragment,
                            ListView listViewEjercicios,
                            AdaptadorPractica adaptador,
                            List<Ejercicio> listaEjercicios) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.listViewEjercicios = listViewEjercicios;
        this.adaptador = adaptador;
        this.listaEjercicios = listaEjercicios;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        if (listaEjercicios == null || listaEjercicios.isEmpty()) {
            salidaAudio.hablar("No hay ejercicios disponibles en este subtema.", false);
            return;
        }

        // Asegurar que no haya escucha activa
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}
        try { salidaAudio.detener(); } catch (Exception ignored) {}

        navegacionActiva = true;
        posicionActual = 0;

        // Pequeño delay para que el ListView/adapter puedan estabilizarse
        mainHandler.postDelayed(() -> leerEjercicioActual(true, this::escucharComandos), 120);
    }

    public void detenerNavegacion() {
        navegacionActiva = false;
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}
        try { salidaAudio.detener(); } catch (Exception ignored) {}
    }

    private void leerEjercicioActual(boolean conInstrucciones, Runnable onDone) {
        if (!navegacionActiva) {
            if (onDone != null) onDone.run();
            return;
        }
        if (listaEjercicios == null || listaEjercicios.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        if (posicionActual < 0) posicionActual = 0;
        if (posicionActual >= listaEjercicios.size()) posicionActual = listaEjercicios.size() - 1;

        Ejercicio ejercicio = listaEjercicios.get(posicionActual);
        estaHablando = true;

        // Asegurar que no se esté escuchando justo antes de hablar
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}

        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre() : "Ejercicio sin nombre";
        String texto = (conInstrucciones ? "Ejercicio número " + (posicionActual + 1) + " de " + listaEjercicios.size() + ". " : "")
                + nombre + ". Di siguiente, anterior, repetir o entrar.";

        // Actualizar selección visual
        moverListViewAPosicion(posicionActual);

        salidaAudio.hablar(texto, false, () -> {
            estaHablando = false;
            // esperar un poco antes de arrancar la escucha para evitar que el mic capture el TTS
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

            if (salidaAudio.estaHablando()) salidaAudio.detener();

            switch (comando) {
                case "siguiente":
                    posicionActual = (posicionActual + 1) % Math.max(1, listaEjercicios.size());
                    moverListViewAPosicion(posicionActual);
                    leerEjercicioActual(false, this::escucharComandos);
                    return;

                case "anterior":
                    posicionActual = (posicionActual - 1 + listaEjercicios.size()) % listaEjercicios.size();
                    moverListViewAPosicion(posicionActual);
                    leerEjercicioActual(false, this::escucharComandos);
                    return;

                case "repetir":
                    leerEjercicioActual(false, this::escucharComandos);
                    return;

                case "entrar":
                    confirmarEntrada();
                    return;

                case "regresar":
                    salidaAudio.hablar("Regresando a la sección de subtemas.", false, () -> {
                        fragment.simularRegresar();
                        detenerNavegacion();
                    });
                    return;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de práctica.", false, this::detenerNavegacion);
                    return;

                default:
                    mainHandler.postDelayed(this::escucharComandos, 700);
                    return;
            }
        });
    }

    private void confirmarEntrada() {
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
                                salidaAudio.hablar("Abriendo " + (ej.getNombre() != null ? ej.getNombre() : "el ejercicio") + ".", false, this::detenerNavegacion);
                            } else {
                                salidaAudio.hablar("No fue posible abrir el ejercicio. Intenta más tarde.", true, this::escucharComandos);
                            }
                        });
                    });
                } else {
                    salidaAudio.hablar("Operación cancelada. Continuando.", true, this::escucharComandos);
                }
            });
        });
    }

    private void moverListViewAPosicion(int posicion) {
        if (listViewEjercicios == null || adaptador == null) return;
        int count = adaptador.getCount();
        if (count == 0) return;
        int p = Math.max(0, Math.min(posicion, count - 1));
        try {
            listViewEjercicios.post(() -> {
                listViewEjercicios.setSelection(p);
            });
        } catch (Exception ignored) {}
    }
}