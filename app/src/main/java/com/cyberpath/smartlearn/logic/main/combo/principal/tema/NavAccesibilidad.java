package com.cyberpath.smartlearn.logic.main.combo.principal.tema;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.ui.main.combo.principal.tema.AdaptadorTemas;
import com.cyberpath.smartlearn.ui.main.combo.principal.tema.TemasFragment;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.List;

public class NavAccesibilidad {

    private final Context context;
    private final TemasFragment fragment;
    private final TemasLogic temasLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerTemas;
    private final AdaptadorTemas adapterTemas;

    private final List<String> comandos = List.of("siguiente", "anterior", "repetir", "entrar", "regresar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int posicionActual = 0;
    private boolean estaHablando = false;
    private boolean navegacionActiva = false;

    public NavAccesibilidad(Context context, TemasFragment fragment, TemasLogic temasLogic,
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

        mainHandler.postDelayed(() -> leerTemaActual(true, this::escucharComandos), 120);
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

    private void leerTemaActual(boolean conInstrucciones, Runnable onDone) {
        if (!navegacionActiva) {
            if (onDone != null) onDone.run();
            return;
        }

        List<Tema> listaTemas = temasLogic.getListaTemas();

        if (listaTemas == null || listaTemas.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        if (posicionActual < 0 || posicionActual >= listaTemas.size()) {
            posicionActual = 0;
        }

        Tema tema = listaTemas.get(posicionActual);
        estaHablando = true;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        String nombre = tema.getNombre() != null ? tema.getNombre() : "Sin nombre";
        String texto = (conInstrucciones ? "Tema número " + (posicionActual + 1) + " de " + listaTemas.size() + ". " : "")
                + nombre + ". Di siguiente, anterior, repetir, entrar o regresar.";

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
                    posicionActual = (posicionActual + 1) % temasLogic.getListaTemas().size();
                    moverViewPager(posicionActual, true);
                    leerTemaActual(false, this::escucharComandos);
                    break;

                case "anterior":
                    posicionActual = (posicionActual - 1 + temasLogic.getListaTemas().size())
                            % temasLogic.getListaTemas().size();
                    moverViewPager(posicionActual, true);
                    leerTemaActual(false, this::escucharComandos);
                    break;

                case "repetir":
                    leerTemaActual(false, this::escucharComandos);
                    break;

                case "entrar":
                    confirmarEntrada();
                    break;

                case "regresar":
                    salidaAudio.hablar("Regresando a la sección anterior.", false, () -> {
                        fragment.simularRegresar();
                        detenerNavegacion();
                    });
                    break;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de temas.", false, this::detenerNavegacion);
                    break;

                default:
                    mainHandler.postDelayed(this::escucharComandos, 700);
            }
        });
    }

    private void confirmarEntrada() {
        List<Tema> listaTemas = temasLogic.getListaTemas();

        if (posicionActual < 0 || posicionActual >= listaTemas.size()) {
            salidaAudio.hablar("Tema inválido. Intenta de nuevo.", true, this::escucharComandos);
            return;
        }

        Tema tema = listaTemas.get(posicionActual);
        String nombre = tema.getNombre() != null ? tema.getNombre() : "este tema";

        salidaAudio.hablar("¿Deseas entrar en " + nombre + "? Di sí o no.", true, () -> {
            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!navegacionActiva) return;

                if (esSi) {
                    fragment.entrarTemaDesdeAccesibilidad(tema, (exito, t) -> {
                        mainHandler.post(() -> {
                            if (!navegacionActiva) return;
                            if (exito) {
                                salidaAudio.hablar("Entrando a " +
                                                (t.getNombre() != null ? t.getNombre() : "el tema") + ".",
                                        false, this::detenerNavegacion);
                            } else {
                                salidaAudio.hablar("No fue posible entrar. Intenta más tarde.",
                                        true, this::escucharComandos);
                            }
                        });
                    });
                } else {
                    salidaAudio.hablar("Entrada cancelada. Continuando con la navegación.",
                            true, this::escucharComandos);
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