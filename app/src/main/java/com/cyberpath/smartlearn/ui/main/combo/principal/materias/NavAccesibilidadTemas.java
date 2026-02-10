package com.cyberpath.smartlearn.ui.main.combo.principal.materias;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;
import androidx.navigation.fragment.NavHostFragment;

import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Navegación por voz para TemasFragment.
 * Comandos: "siguiente", "anterior", "repetir", "entrar", "regresar", "salir"/"detente".
 */
public class NavAccesibilidadTemas {

    private final Context context;
    private final TemasFragment fragment;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerTemas;
    private final AdaptadorTemas adapterTemas;
    private final List<Tema> listaTemasDisponibles;

    private int posicionActual = 0;
    private boolean estaHablando = false;
    private boolean navegacionActiva = false;

    private final List<String> comandos = List.of("siguiente", "anterior", "repetir", "entrar", "regresar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public NavAccesibilidadTemas(Context context, TemasFragment fragment,
                            ViewPager2 viewPagerTemas, AdaptadorTemas adapterTemas,
                            List<Tema> listaTemasDisponibles) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.viewPagerTemas = viewPagerTemas;
        this.adapterTemas = adapterTemas;
        this.listaTemasDisponibles = listaTemasDisponibles;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        if (listaTemasDisponibles == null || listaTemasDisponibles.isEmpty()) {
            salidaAudio.hablar("No hay temas disponibles en esta materia.", false);
            return;
        }

        // evitar escucha concurrente
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}
        try { salidaAudio.detener(); } catch (Exception ignored) {}

        navegacionActiva = true;
        posicionActual = 0;

        // dejar un pequeño margen para que el adapter/app actualice UI si se lanzó justo después de cargar datos
        mainHandler.postDelayed(() -> leerTemaActual(true, this::escucharComandos), 120);
    }

    public void detenerNavegacion() {
        navegacionActiva = false;
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}
        try { salidaAudio.detener(); } catch (Exception ignored) {}
    }

    private void leerTemaActual(boolean conInstrucciones, Runnable onDone) {
        if (!navegacionActiva) {
            if (onDone != null) onDone.run();
            return;
        }
        if (listaTemasDisponibles == null || listaTemasDisponibles.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        if (posicionActual < 0 || posicionActual >= listaTemasDisponibles.size()) posicionActual = 0;

        Tema tema = listaTemasDisponibles.get(posicionActual);
        estaHablando = true;

        // aseguramos que no esté escuchando antes de hablar
        try { entradaAudio.detenerEscucha(); } catch (Exception ignored) {}

        String nombre = tema.getNombre() != null ? tema.getNombre() : "Sin nombre";
        String texto = (conInstrucciones ? "Tema número " + (posicionActual + 1) + " de " + listaTemasDisponibles.size() + ". " : "")
                + nombre + ". Di siguiente, anterior, repetir, entrar o regresar.";

        salidaAudio.hablar(texto, false, () -> {
            estaHablando = false;
            // espera breve para liberar audio
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
                    posicionActual = (posicionActual + 1) % listaTemasDisponibles.size();
                    moverViewPager(posicionActual, true);
                    leerTemaActual(false, this::escucharComandos);
                    return;

                case "anterior":
                    posicionActual = (posicionActual - 1 + listaTemasDisponibles.size()) % listaTemasDisponibles.size();
                    moverViewPager(posicionActual, true);
                    leerTemaActual(false, this::escucharComandos);
                    return;

                case "repetir":
                    leerTemaActual(false, this::escucharComandos);
                    return;

                case "entrar":
                    confirmarEntrada();
                    return;

                case "regresar":
                    salidaAudio.hablar("Regresando a la sección anterior.", false, () -> {
                        fragment.simularRegresar();
                        detenerNavegacion();
                    });
                    return;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de temas.", false, this::detenerNavegacion);
                    return;

                default:
                    mainHandler.postDelayed(this::escucharComandos, 700);
                    return;
            }
        });
    }

    private void confirmarEntrada() {
        if (posicionActual < 0 || posicionActual >= listaTemasDisponibles.size()) {
            salidaAudio.hablar("Tema inválido. Intenta de nuevo.", true, this::escucharComandos);
            return;
        }
        Tema tema = listaTemasDisponibles.get(posicionActual);
        String nombre = tema.getNombre() != null ? tema.getNombre() : "este tema";
        salidaAudio.hablar("¿Deseas entrar en " + nombre + "? Di sí o no.", true, () -> {
            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!navegacionActiva) return;
                if (esSi) {
                    // pedir al fragmento que navegue por accesibilidad
                    fragment.entrarTemaDesdeAccesibilidad(tema, this::onAccesoCompletado);
                } else {
                    salidaAudio.hablar("Entrada cancelada. Continuando con la navegación.", true, this::escucharComandos);
                }
            });
        });
    }

    private void onAccesoCompletado(Boolean exito, Tema tema) {
        if (!navegacionActiva) return;
        mainHandler.post(() -> {
            if (!navegacionActiva) return;
            if (exito) {
                salidaAudio.hablar("Entrando a " + (tema.getNombre() != null ? tema.getNombre() : "el tema") + ".", false, this::detenerNavegacion);
            } else {
                salidaAudio.hablar("No fue posible entrar. Intenta más tarde.", true, this::escucharComandos);
            }
        });
    }

    private void moverViewPager(int posicionReal, boolean smooth) {
        if (viewPagerTemas == null || adapterTemas == null || listaTemasDisponibles == null) return;
        int realSize = listaTemasDisponibles.size();
        if (realSize == 0) return;
        // Adaptador duplica con un elemento al inicio: la posición visual correcta es posicionReal + 1
        int target = Math.max(0, posicionReal + 1);
        viewPagerTemas.setCurrentItem(target, smooth);
    }
}