package com.cyberpath.smartlearn.ui.main.combo.principal.materias;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Navegación por voz para SubtemasFragment.
 * Comandos: "siguiente", "anterior", "repetir", "entrar", "regresar", "salir"/"detente".
 *
 * Protecciones:
 * - Detiene la escucha antes de hablar.
 * - Espera un pequeño delay después del TTS antes de arrancar la escucha.
 */
public class NavAccesibilidadSubtemas {

    private final Context context;
    private final SubtemasFragment fragment;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerSubtemas;
    private final AdaptadorSubtemas adapterSubtemas;
    private final List<Subtema> listaSubtemasDisponibles;

    private int posicionActual = 0;
    private boolean estaHablando = false;
    private boolean navegacionActiva = false;

    private final List<String> comandos = List.of("siguiente", "anterior", "repetir", "entrar", "regresar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public NavAccesibilidadSubtemas(Context context, SubtemasFragment fragment,
                            ViewPager2 viewPagerSubtemas, AdaptadorSubtemas adapterSubtemas,
                            List<Subtema> listaSubtemasDisponibles) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.viewPagerSubtemas = viewPagerSubtemas;
        this.adapterSubtemas = adapterSubtemas;
        this.listaSubtemasDisponibles = listaSubtemasDisponibles;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        if (listaSubtemasDisponibles == null || listaSubtemasDisponibles.isEmpty()) {
            salidaAudio.hablar("No hay subtemas disponibles en este tema.", false);
            return;
        }

        // Asegurar que no haya escucha activa antes de comenzar a hablar
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}
        try { salidaAudio.detener(); } catch (Exception ignored) {}

        navegacionActiva = true;
        posicionActual = 0;

        // Pequeño delay para asegurar que el adapter/viewpager ya esté listo
        mainHandler.postDelayed(() -> leerSubtemaActual(true, this::escucharComandos), 120);
    }

    public void detenerNavegacion() {
        navegacionActiva = false;
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}
        try { salidaAudio.detener(); } catch (Exception ignored) {}
    }

    private void leerSubtemaActual(boolean conInstrucciones, Runnable onDone) {
        if (!navegacionActiva) {
            if (onDone != null) onDone.run();
            return;
        }
        if (listaSubtemasDisponibles == null || listaSubtemasDisponibles.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        if (posicionActual < 0 || posicionActual >= listaSubtemasDisponibles.size()) posicionActual = 0;

        Subtema subtema = listaSubtemasDisponibles.get(posicionActual);
        estaHablando = true;

        // Garantizar que no haya escucha activa
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}

        String nombre = subtema.getNombre() != null ? subtema.getNombre() : "Sin nombre";
        String texto = (conInstrucciones ? "Subtema número " + (posicionActual + 1) + " de " + listaSubtemasDisponibles.size() + ". " : "")
                + nombre + ". Di siguiente, anterior, repetir, entrar o regresar.";

        salidaAudio.hablar(texto, false, () -> {
            estaHablando = false;
            // Delay para asegurar liberación del audio hardware antes de iniciar escucha
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
                    posicionActual = (posicionActual + 1) % listaSubtemasDisponibles.size();
                    moverViewPager(posicionActual, true);
                    leerSubtemaActual(false, this::escucharComandos);
                    return;

                case "anterior":
                    posicionActual = (posicionActual - 1 + listaSubtemasDisponibles.size()) % listaSubtemasDisponibles.size();
                    moverViewPager(posicionActual, true);
                    leerSubtemaActual(false, this::escucharComandos);
                    return;

                case "repetir":
                    leerSubtemaActual(false, this::escucharComandos);
                    return;

                case "entrar":
                    confirmarEntrada();
                    return;

                case "regresar":
                    salidaAudio.hablar("Regresando a la sección de temas.", false, () -> {
                        fragment.simularRegresar();
                        detenerNavegacion();
                    });
                    return;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del m��dulo de subtemas.", false, this::detenerNavegacion);
                    return;

                default:
                    mainHandler.postDelayed(this::escucharComandos, 700);
                    return;
            }
        });
    }

    private void confirmarEntrada() {
        if (posicionActual < 0 || posicionActual >= listaSubtemasDisponibles.size()) {
            salidaAudio.hablar("Subtema inválido. Intenta de nuevo.", true, this::escucharComandos);
            return;
        }

        Subtema subtema = listaSubtemasDisponibles.get(posicionActual);
        String nombre = subtema.getNombre() != null ? subtema.getNombre() : "este subtema";
        salidaAudio.hablar("¿Deseas entrar en " + nombre + "? Di sí o no.", true, () -> {
            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!navegacionActiva) return;
                if (esSi) {
                    // Pedimos al fragmento que maneje la entrada (guardado de última conexión y elección teoría/práctica)
                    fragment.entrarSubtemaDesdeAccesibilidad(subtema, (exito, st) -> {
                        mainHandler.post(() -> {
                            if (!navegacionActiva) return;
                            if (exito) {
                                salidaAudio.hablar("Abriendo " + (st.getNombre() != null ? st.getNombre() : "el subtema") + ".", false, this::detenerNavegacion);
                            } else {
                                salidaAudio.hablar("No fue posible abrir el subtema. Intenta más tarde.", true, this::escucharComandos);
                            }
                        });
                    });
                } else {
                    salidaAudio.hablar("Entrada cancelada. Continuando con la navegación.", true, this::escucharComandos);
                }
            });
        });
    }

    private void moverViewPager(int posicionReal, boolean smooth) {
        if (viewPagerSubtemas == null || adapterSubtemas == null || listaSubtemasDisponibles == null) return;
        int realSize = listaSubtemasDisponibles.size();
        if (realSize == 0) return;
        // El adaptador duplica con un elemento al inicio: la posición visual correcta es posicionReal + 1
        int target = Math.max(0, posicionReal + 1);
        viewPagerSubtemas.setCurrentItem(target, smooth);
    }
}