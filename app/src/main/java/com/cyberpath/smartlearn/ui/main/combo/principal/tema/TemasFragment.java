package com.cyberpath.smartlearn.ui.main.combo.principal.tema;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TemasFragment extends Fragment {

    private TemasLogic temasLogic;
    private NavAccesibilidad navAccesibilidad;
    private EntradaAudio entradaAudio;

    private TextView textoMateria;
    private ViewPager2 viewPagerTemas;
    private AdaptadorTemas adapterTemas;
    private Materia materia;

    public TemasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        entradaAudio = EntradaAudio.obtenerInstancia();
        if (entradaAudio != null) {
            entradaAudio.detenerEscucha();
        }

        textoMateria = view.findViewById(R.id.tvNombreMateria);

        materia = TemasFragmentArgs.fromBundle(getArguments()).getMateria();
        if (materia != null) {
            textoMateria.setText(materia.getNombre());
        }

        crearCarrusel(view);

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
        viewPagerTemas.setPadding(80, 0, 80, 0);

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
                        });
                    } else if (position == realSize + 1) {
                        isResetting = true;
                        viewPagerTemas.setCurrentItem(1, false);
                        viewPagerTemas.post(() -> {
                            viewPagerTemas.requestTransform();
                            isResetting = false;
                        });
                    }
                }
            }
        });
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
        }
    }

    public void moverViewPagerAPosicion(int posicion, boolean smooth) {
        if (viewPagerTemas != null) {
            viewPagerTemas.setCurrentItem(posicion, smooth);
        }
    }

    public void iniciarNavegacionPorVoz() {
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
}