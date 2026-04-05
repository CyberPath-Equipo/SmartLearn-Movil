package com.cyberpath.smartlearn.ui.main.combo.principal.subtema;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        viewPagerSubtemas.setCurrentItem(1, false);
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
                        });
                    } else if (position == realSize + 1) {
                        isResetting = true;
                        viewPagerSubtemas.setCurrentItem(1, false);
                        viewPagerSubtemas.post(() -> {
                            viewPagerSubtemas.requestTransform();
                            isResetting = false;
                        });
                    }
                }
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
}