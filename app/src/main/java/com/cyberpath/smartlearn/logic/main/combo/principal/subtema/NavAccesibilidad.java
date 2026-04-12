package com.cyberpath.smartlearn.logic.main.combo.principal.subtema;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.ui.main.combo.principal.subtema.AdaptadorSubtemas;
import com.cyberpath.smartlearn.ui.main.combo.principal.subtema.SubtemasFragment;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.List;

public class NavAccesibilidad {
    private final Context context;
    private final SubtemasFragment fragment;
    private final SubtemasLogic subtemasLogic;
    private final SalidaAudio salidaAudio;
    private final EntradaAudio entradaAudio;
    private final ViewPager2 viewPagerSubtemas;
    private final AdaptadorSubtemas adapterSubtemas;

    private final List<String> comandos = List.of("siguiente", "anterior", "repetir", "entrar", "regresar", "salir", "detente");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int posicionActual = 0;
    private boolean estaHablando = false;
    private boolean navegacionActiva = false;

    public NavAccesibilidad(Context context, SubtemasFragment fragment, SubtemasLogic subtemasLogic,
                            ViewPager2 viewPagerSubtemas, AdaptadorSubtemas adapterSubtemas) {
        this.context = context.getApplicationContext();
        this.fragment = fragment;
        this.subtemasLogic = subtemasLogic;
        this.viewPagerSubtemas = viewPagerSubtemas;
        this.adapterSubtemas = adapterSubtemas;
        this.salidaAudio = SalidaAudio.obtenerInstancia();
        this.entradaAudio = EntradaAudio.obtenerInstancia();
    }

    public void iniciarNavegacion() {
        List<Subtema> listaSubtemas = subtemasLogic.getListaSubtemas();

        if (listaSubtemas == null || listaSubtemas.isEmpty()) {
            salidaAudio.hablar("No hay subtemas disponibles en este tema.", false);
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

        mainHandler.postDelayed(() -> leerSubtemaActual(true, this::escucharComandos), 120);
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

    private void leerSubtemaActual(boolean conInstrucciones, Runnable onDone) {
        if (!navegacionActiva) {
            if (onDone != null) onDone.run();
            return;
        }

        List<Subtema> listaSubtemas = subtemasLogic.getListaSubtemas();

        if (listaSubtemas == null || listaSubtemas.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        if (posicionActual < 0 || posicionActual >= listaSubtemas.size()) {
            posicionActual = 0;
        }

        Subtema subtema = listaSubtemas.get(posicionActual);
        estaHablando = true;

        try {
            entradaAudio.detenerEscucha();
        } catch (Exception ignored) {
        }

        String nombre = subtema.getNombre() != null ? subtema.getNombre() : "Sin nombre";
        String texto = (conInstrucciones ? "Subtema número " + (posicionActual + 1) + " de " + listaSubtemas.size() + ". " : "")
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
                    posicionActual = (posicionActual + 1) % subtemasLogic.getListaSubtemas().size();
                    moverViewPager(posicionActual, true);
                    leerSubtemaActual(false, this::escucharComandos);
                    break;

                case "anterior":
                    posicionActual = (posicionActual - 1 + subtemasLogic.getListaSubtemas().size())
                            % subtemasLogic.getListaSubtemas().size();
                    moverViewPager(posicionActual, true);
                    leerSubtemaActual(false, this::escucharComandos);
                    break;

                case "repetir":
                    leerSubtemaActual(false, this::escucharComandos);
                    break;

                case "entrar":
                    confirmarEntrada();
                    break;

                case "regresar":
                    salidaAudio.hablar("Regresando a la sección de temas.", false, () -> {
                        fragment.simularRegresar();
                        detenerNavegacion();
                    });
                    break;

                case "salir":
                case "detente":
                    salidaAudio.hablar("Saliendo del módulo de subtemas.", false, this::detenerNavegacion);
                    break;

                default:
                    mainHandler.postDelayed(this::escucharComandos, 700);
            }
        });
    }

    private void confirmarEntrada() {
        List<Subtema> listaSubtemas = subtemasLogic.getListaSubtemas();

        if (posicionActual < 0 || posicionActual >= listaSubtemas.size()) {
            salidaAudio.hablar("Subtema inválido. Intenta de nuevo.", true, this::escucharComandos);
            return;
        }

        Subtema subtema = listaSubtemas.get(posicionActual);
        String nombre = subtema.getNombre() != null ? subtema.getNombre() : "este subtema";

        salidaAudio.hablar("¿Deseas entrar en " + nombre + "? Di sí o no.", true, () -> {
            entradaAudio.confirmarAfirmacion(esSi -> {
                if (!navegacionActiva) return;

                if (esSi) {
                    fragment.entrarSubtemaDesdeAccesibilidad(subtema, (exito, st) -> {
                        mainHandler.post(() -> {
                            if (!navegacionActiva) return;
                            if (exito) {
                                salidaAudio.hablar("Abriendo " +
                                                (st.getNombre() != null ? st.getNombre() : "el subtema") + ".",
                                        false, this::detenerNavegacion);
                            } else {
                                salidaAudio.hablar("No fue posible abrir el subtema. Intenta más tarde.",
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
        if (viewPagerSubtemas == null || subtemasLogic == null) return;

        int realSize = subtemasLogic.getRealSize();
        if (realSize == 0) return;

        int target = Math.max(0, posicionReal + 1);
        viewPagerSubtemas.setCurrentItem(target, smooth);
    }
}