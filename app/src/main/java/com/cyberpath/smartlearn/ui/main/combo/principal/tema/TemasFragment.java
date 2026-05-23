package com.cyberpath.smartlearn.ui.main.combo.principal.tema;

import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.logic.main.combo.principal.tema.NavAccesibilidad;
import com.cyberpath.smartlearn.logic.main.combo.principal.tema.TemasLogic;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TemasFragment extends Fragment {

    private TemasLogic temasLogic;
    private NavAccesibilidad navAccesibilidad;
    private EntradaAudio entradaAudio;
    private Materia materia;

    private TextView textoMateria;
    private ViewPager2 viewPagerTemas;
    private AdaptadorTemas adapterTemas;
    private FloatingActionButton btnPrevTema;
    private FloatingActionButton btnNextTema;
    private MaterialButton btnRegresar;
    private LinearLayout indicadoresContainerTemas;
    private ImageView[] indicadores;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        crearTextoMateria(view);
        crearCarrusel(view);
        crearBotonesFlotantes(view);
        crearBotonRegresar(view);

        entradaAudio = EntradaAudio.obtenerInstancia();
        if (entradaAudio != null) {
            entradaAudio.detenerEscucha();
        }

        temasLogic = new TemasLogic(this, materia);
        navAccesibilidad = new NavAccesibilidad(requireContext(), this, temasLogic, viewPagerTemas, adapterTemas);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (navAccesibilidad != null) {
            navAccesibilidad.detenerNavegacion();
        }

        if (temasLogic != null) {
            temasLogic.limpiarDatos();
        }

        viewPagerTemas = null;
        adapterTemas = null;
        navAccesibilidad = null;
        temasLogic = null;
    }

    private void crearCarrusel(View view) {
        viewPagerTemas = view.findViewById(R.id.viewPagerTemas);
        adapterTemas = new AdaptadorTemas(new ArrayList<>(), this::onTemaClick);
        viewPagerTemas.setAdapter(adapterTemas);

        viewPagerTemas.setOffscreenPageLimit(3);
        viewPagerTemas.setClipToPadding(false);
        viewPagerTemas.setClipChildren(false);

        viewPagerTemas.setPageTransformer((page, position) -> {
            float scaleFactor = Math.max(0.85f, 1 - Math.abs(position) * 0.15f);
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setAlpha(0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * 0.5f);
        });

        viewPagerTemas.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private boolean isResetting = false;

            @Override
            public void onPageSelected(int position) {
                actualizarIndicadores(position);
                actualizarBotones(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE && !isResetting) {
                    int position = viewPagerTemas.getCurrentItem();
                    int realSize = temasLogic != null ? temasLogic.getRealSize() : 0;

                    if (realSize <= 1) return;

                    if (position == 0) {
                        isResetting = true;
                        viewPagerTemas.setCurrentItem(realSize, false);
                        viewPagerTemas.post(() -> {
                            viewPagerTemas.requestTransform();
                            isResetting = false;
                            actualizarIndicadores(viewPagerTemas.getCurrentItem());
                            actualizarBotones(viewPagerTemas.getCurrentItem());
                        });
                    } else if (position == realSize + 1) {
                        isResetting = true;
                        viewPagerTemas.setCurrentItem(1, false);
                        viewPagerTemas.post(() -> {
                            viewPagerTemas.requestTransform();
                            isResetting = false;
                            actualizarIndicadores(viewPagerTemas.getCurrentItem());
                            actualizarBotones(viewPagerTemas.getCurrentItem());
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (navAccesibilidad != null) {
            navAccesibilidad.detenerNavegacion();
        }
        try {
            EntradaAudio.obtenerInstancia().detenerEscucha();
        } catch (Exception ignored) {
        }
        try {
            SalidaAudio.obtenerInstancia().detener();
        } catch (Exception ignored) {
        }
    }

    private void crearBotonesFlotantes(View view) {
        btnPrevTema = view.findViewById(R.id.btn_prev_tema);
        btnNextTema = view.findViewById(R.id.btn_next_tema);
        indicadoresContainerTemas = view.findViewById(R.id.indicadores_container_temas);

        btnPrevTema.setScaleX(0.86f);
        btnPrevTema.setScaleY(0.86f);
        btnNextTema.setScaleX(0.86f);
        btnNextTema.setScaleY(0.86f);

        btnPrevTema.setOnClickListener(v -> {
            if (adapterTemas == null || temasLogic == null || temasLogic.getRealSize() == 0) return;
            int current = viewPagerTemas.getCurrentItem();
            viewPagerTemas.setCurrentItem(current - 1, true);
        });

        btnNextTema.setOnClickListener(v -> {
            if (adapterTemas == null || temasLogic == null || temasLogic.getRealSize() == 0) return;
            int current = viewPagerTemas.getCurrentItem();
            viewPagerTemas.setCurrentItem(current + 1, true);
        });
    }

    private void crearBotonRegresar(View view) {
        btnRegresar = view.findViewById(R.id.btn_volver);

        btnRegresar.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().finish();
            }
        });
    }

    private void crearTextoMateria(View view) {
        textoMateria = view.findViewById(R.id.tvNombreMateria);
        materia = TemasFragmentArgs.fromBundle(getArguments()).getMateria();
        if (materia != null) {
            textoMateria.setText(materia.getNombre());
        }
    }

    private void onTemaClick(Tema tema) {
        var action = TemasFragmentDirections.actionTemasFragmentToSubtemasFragment(tema);
        NavHostFragment.findNavController(this).navigate(action);
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

    public void actualizarAdapter(List<Tema> temas) {
        if (adapterTemas != null) {
            adapterTemas.actualizarLista(new ArrayList<>(temas));
            viewPagerTemas.post(() -> {
                int realSize = temasLogic != null ? temasLogic.getRealSize() : 0;
                if (realSize <= 0) {
                    actualizarIndicadores(viewPagerTemas.getCurrentItem());
                    actualizarBotones(viewPagerTemas.getCurrentItem());
                    return;
                }
                if (realSize == 1) {
                    viewPagerTemas.setCurrentItem(0, false);
                } else {
                    viewPagerTemas.setCurrentItem(1, false);
                }
                actualizarIndicadores(viewPagerTemas.getCurrentItem());
                actualizarBotones(viewPagerTemas.getCurrentItem());
            });
        }
    }

    public void moverViewPagerAPosicion(int posicion, boolean smooth) {
        if (viewPagerTemas != null) {
            viewPagerTemas.setCurrentItem(posicion, smooth);
        }
    }

    public void iniciarNavegacionPorVoz() {
        if (!PreferencesManager.isAsistenciaVozActivada(requireContext())) {
            return;
        }
        if (navAccesibilidad != null) {
            navAccesibilidad.iniciarNavegacion();
        }
    }

    public int getPosicionActualViewPager() {
        if (viewPagerTemas == null) return 0;
        return viewPagerTemas.getCurrentItem();
    }

    public void entrarTemaDesdeAccesibilidad(Tema tema, BiConsumer<Boolean, Tema> callback) {
        try {
            var action = TemasFragmentDirections.actionTemasFragmentToSubtemasFragment(tema);
            NavHostFragment.findNavController(this).navigate(action);
            callback.accept(true, tema);
        } catch (Exception e) {
            callback.accept(false, tema);
        }
    }

    private void actualizarIndicadores(int posicionActualPagina) {
        if (adapterTemas == null || temasLogic == null) return;
        int total = temasLogic.getRealSize();
        indicadoresContainerTemas.removeAllViews();
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
            indicadoresContainerTemas.addView(indicadores[i]);
        }
    }

    private void actualizarBotones(int posicionPagina) {
        int total = temasLogic != null ? temasLogic.getRealSize() : 0;
        if (total <= 1) {
            btnPrevTema.setAlpha(0.4f);
            btnNextTema.setAlpha(0.4f);
            return;
        }
        int realPos = getRealPositionFromPageIndex(posicionPagina);
        btnPrevTema.setAlpha(realPos > 0 ? 1.0f : 0.4f);
        btnNextTema.setAlpha(realPos < total - 1 ? 1.0f : 0.4f);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int getRealPositionFromPageIndex(int pageIndex) {
        int realSize = temasLogic != null ? temasLogic.getRealSize() : 0;
        if (realSize <= 1) return 0;
        int realPos = (pageIndex - 1) % realSize;
        if (realPos < 0) realPos += realSize;
        return realPos;
    }
}