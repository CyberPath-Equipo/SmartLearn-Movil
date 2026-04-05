package com.cyberpath.smartlearn.ui.main.combo.principal.subtema;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.logic.main.combo.principal.subtema.NavAccesibilidad;
import com.cyberpath.smartlearn.logic.main.combo.principal.subtema.SubtemasLogic;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SubtemasFragment extends Fragment {
    private SubtemasLogic subtemasLogic;
    private NavAccesibilidad navAccesibilidad;
    private EntradaAudio entradaAudio;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ViewPager2 viewPagerSubtemas;
    private AdaptadorSubtemas adapterSubtemas;
    private TextView textoTema;
    private Tema tema;
    private ImageView[] indicadores;
    private LinearLayout indicadoresContainer;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnPrev;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnNext;
    private MaterialButton btnVolver;

    public SubtemasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subtemas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        entradaAudio = EntradaAudio.obtenerInstancia();
        if (entradaAudio != null) {
            entradaAudio.detenerEscucha();
        }

        textoTema = view.findViewById(R.id.tvNombreTema);

        tema = SubtemasFragmentArgs.fromBundle(getArguments()).getTema();
        if (tema != null) {
            textoTema.setText(tema.getNombre());
        }

        crearCarrusel(view);
        crearBotonesFlotantes(view);
        crearBotonVolver(view);

        subtemasLogic = new SubtemasLogic(this, tema);
        navAccesibilidad = new NavAccesibilidad(requireContext(), this, subtemasLogic, viewPagerSubtemas, adapterSubtemas);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (navAccesibilidad != null) {
            navAccesibilidad.detenerNavegacion();
        }

        if (subtemasLogic != null) {
            subtemasLogic.limpiarDatos();
        }

        viewPagerSubtemas = null;
        adapterSubtemas = null;
        navAccesibilidad = null;
        subtemasLogic = null;
    }

    private void crearCarrusel(View view) {
        viewPagerSubtemas = view.findViewById(R.id.viewPagerSubtemas);
        adapterSubtemas = new AdaptadorSubtemas(new ArrayList<>(), this::onSubtemaClick);
        viewPagerSubtemas.setAdapter(adapterSubtemas);
        viewPagerSubtemas.setOffscreenPageLimit(3);
        viewPagerSubtemas.setClipToPadding(false);
        viewPagerSubtemas.setClipChildren(false);
        viewPagerSubtemas.setPadding(80, 0, 80, 0);

        viewPagerSubtemas.setPageTransformer((page, position) -> {
            float scaleFactor = Math.max(0.85f, 1 - Math.abs(position) * 0.15f);
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setAlpha(0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * 0.5f);
        });

        viewPagerSubtemas.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private boolean isResetting = false;

            @Override
            public void onPageSelected(int position) {
                actualizarIndicadores(position);
                actualizarBotones(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE && !isResetting) {
                    int position = viewPagerSubtemas.getCurrentItem();
                    int realSize = subtemasLogic != null ? subtemasLogic.getRealSize() : 0;

                    if (realSize <= 1) return;

                    if (position == 0) {
                        isResetting = true;
                        viewPagerSubtemas.setCurrentItem(realSize, false);
                        viewPagerSubtemas.post(() -> {
                            viewPagerSubtemas.requestTransform();
                            isResetting = false;
                            actualizarIndicadores(viewPagerSubtemas.getCurrentItem());
                            actualizarBotones(viewPagerSubtemas.getCurrentItem());
                        });
                    } else if (position == realSize + 1) {
                        isResetting = true;
                        viewPagerSubtemas.setCurrentItem(1, false);
                        viewPagerSubtemas.post(() -> {
                            viewPagerSubtemas.requestTransform();
                            isResetting = false;
                            actualizarIndicadores(viewPagerSubtemas.getCurrentItem());
                            actualizarBotones(viewPagerSubtemas.getCurrentItem());
                        });
                    }
                }
            }
        });
    }

    private void crearBotonesFlotantes(View view) {
        btnPrev = view.findViewById(R.id.btn_prev_subtema);
        btnNext = view.findViewById(R.id.btn_next_subtema);
        indicadoresContainer = view.findViewById(R.id.indicadores_container_subtemas);

        btnPrev.setScaleX(0.86f);
        btnPrev.setScaleY(0.86f);
        btnNext.setScaleX(0.86f);
        btnNext.setScaleY(0.86f);

        btnPrev.setOnClickListener(v -> {
            if (adapterSubtemas == null || subtemasLogic == null || subtemasLogic.getRealSize() == 0) return;
            int current = viewPagerSubtemas.getCurrentItem();
            viewPagerSubtemas.setCurrentItem(current - 1, true);
        });

        btnNext.setOnClickListener(v -> {
            if (adapterSubtemas == null || subtemasLogic == null || subtemasLogic.getRealSize() == 0) return;
            int current = viewPagerSubtemas.getCurrentItem();
            viewPagerSubtemas.setCurrentItem(current + 1, true);
        });
    }

    private void crearBotonVolver(View view) {
        btnVolver = view.findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });
    }

    private void onSubtemaClick(Subtema subtema) {
        subtemasLogic.guardarUltimaConexion(subtema);
        navegarContenido(subtema);
    }

    private void navegarContenido(Subtema subtema) {
        View vista = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_teoria_practica, null);
        TextView tvMensaje = vista.findViewById(R.id.tv_titulo_subtema);
        tvMensaje.setText(subtema.getNombre());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(vista)
                .setCancelable(true)
                .show();

        vista.findViewById(R.id.btn_teoria).setOnClickListener(v -> {
            var action = SubtemasFragmentDirections.actionSubtemasFragmentToTeoriaFragment(subtema, null);
            NavHostFragment.findNavController(this).navigate(action);
            dialog.dismiss();
        });

        vista.findViewById(R.id.btn_practica).setOnClickListener(v -> {
            var action = SubtemasFragmentDirections.actionSubtemasFragmentToPracticaFragment(subtema);
            NavHostFragment.findNavController(this).navigate(action);
            dialog.dismiss();
        });

        vista.findViewById(R.id.btn_cancelar).setOnClickListener(v -> dialog.dismiss());
    }

    public void simularRegresar() {
        requireActivity().runOnUiThread(() -> {
            try {
                if (!NavHostFragment.findNavController(this).popBackStack()) {
                    requireActivity().onBackPressed();
                }
            } catch (Exception e) {
                requireActivity().onBackPressed();
            }
        });
    }

    public void showToast(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    public void showToastLong(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        }
    }

    public void actualizarAdapter(List<Subtema> subtemas) {
        if (adapterSubtemas != null) {
            adapterSubtemas.actualizarLista(new ArrayList<>(subtemas));
            viewPagerSubtemas.post(() -> {
                int realSize = subtemasLogic != null ? subtemasLogic.getRealSize() : 0;
                if (realSize <= 0) {
                    actualizarIndicadores(viewPagerSubtemas.getCurrentItem());
                    actualizarBotones(viewPagerSubtemas.getCurrentItem());
                    return;
                }
                if (realSize == 1) {
                    viewPagerSubtemas.setCurrentItem(0, false);
                } else {
                    viewPagerSubtemas.setCurrentItem(1, false);
                }
                actualizarIndicadores(viewPagerSubtemas.getCurrentItem());
                actualizarBotones(viewPagerSubtemas.getCurrentItem());
            });
        }
    }

    public void moverViewPagerAPosicion(int posicion, boolean smooth) {
        if (viewPagerSubtemas != null) {
            viewPagerSubtemas.setCurrentItem(posicion, smooth);
        }
    }

    public void iniciarNavegacionPorVoz() {
        if (navAccesibilidad != null) {
            navAccesibilidad.iniciarNavegacion();
        }
    }

    public int getPosicionActualViewPager() {
        if (viewPagerSubtemas == null) return 0;
        return viewPagerSubtemas.getCurrentItem();
    }

    public void entrarSubtemaDesdeAccesibilidad(Subtema subtema, BiConsumer<Boolean, Subtema> callback) {
        if (subtema == null) {
            if (callback != null) callback.accept(false, null);
            return;
        }
        subtemasLogic.guardarUltimaConexion(subtema);

        SalidaAudio salida = SalidaAudio.obtenerInstancia();
        EntradaAudio entrada = EntradaAudio.obtenerInstancia();

        List<String> opciones = List.of("teoría", "práctica", "cancelar");

        try {
            entrada.detenerEscucha();
        } catch (Exception ignored) {}

        salida.hablar("¿Deseas ver teoría o práctica? Di teoría o práctica.", true, () -> {
            mainHandler.postDelayed(() -> entrada.seleccionarOpcion(opciones, indice -> {
                if (indice < 0 || indice >= opciones.size()) {
                    salida.hablar("No te entendí. Cancelando.", true, () -> {
                        if (callback != null) callback.accept(false, subtema);
                    });
                    return;
                }

                String opcion = opciones.get(indice);

                if ("teoría".equals(opcion)) {
                    mainHandler.post(() -> {
                        try {
                            var action = SubtemasFragmentDirections.actionSubtemasFragmentToTeoriaFragment(subtema, null);
                            NavHostFragment.findNavController(this).navigate(action);
                            if (callback != null) callback.accept(true, subtema);
                        } catch (Exception e) {
                            if (callback != null) callback.accept(false, subtema);
                        }
                    });
                    return;
                }

                if ("práctica".equals(opcion)) {
                    mainHandler.post(() -> {
                        try {
                            var action = SubtemasFragmentDirections.actionSubtemasFragmentToPracticaFragment(subtema);
                            NavHostFragment.findNavController(this).navigate(action);
                            if (callback != null) callback.accept(true, subtema);
                        } catch (Exception e) {
                            if (callback != null) callback.accept(false, subtema);
                        }
                    });
                    return;
                }

                salida.hablar("Operación cancelada.", true, () -> {
                    if (callback != null) callback.accept(false, subtema);
                });

            }), 350);
        });
    }

    private void actualizarIndicadores(int posicionActualPagina) {
        if (adapterSubtemas == null || subtemasLogic == null || indicadoresContainer == null) return;
        int total = subtemasLogic.getRealSize();
        indicadoresContainer.removeAllViews();
        if (total <= 0) return;
        indicadores = new ImageView[total];

        int sizePx = dpToPx(10);
        int marginPx = dpToPx(4);
        int posicionReal = getRealPositionFromPageIndex(posicionActualPagina);

        for (int i = 0; i < total; i++) {
            indicadores[i] = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMargins(marginPx, 0, marginPx, 0);
            indicadores[i].setLayoutParams(params);
            indicadores[i].setImageResource(i == posicionReal ? R.drawable.ic_dot_active : R.drawable.ic_dot_inactive);

            final int indexForClick = i;
            indicadores[i].setOnClickListener(v -> {
                int realSize = subtemasLogic != null ? subtemasLogic.getRealSize() : 0;
                if (realSize <= 0) return;
                int targetPage = (realSize == 1) ? 0 : (1 + indexForClick);
                viewPagerSubtemas.setCurrentItem(targetPage, true);
            });

            indicadoresContainer.addView(indicadores[i]);
        }
    }

    private void actualizarBotones(int posicionPagina) {
        int total = subtemasLogic != null ? subtemasLogic.getRealSize() : 0;
        if (total <= 1) {
            btnPrev.setAlpha(0.4f);
            btnNext.setAlpha(0.4f);
            return;
        }
        int realPos = getRealPositionFromPageIndex(posicionPagina);
        btnPrev.setAlpha(realPos > 0 ? 1.0f : 0.4f);
        btnNext.setAlpha(realPos < total - 1 ? 1.0f : 0.4f);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int getRealPositionFromPageIndex(int pageIndex) {
        int realSize = subtemasLogic != null ? subtemasLogic.getRealSize() : 0;
        if (realSize <= 1) return 0;
        int realPos = (pageIndex - 1) % realSize;
        if (realPos < 0) realPos += realSize;
        return realPos;
    }
}