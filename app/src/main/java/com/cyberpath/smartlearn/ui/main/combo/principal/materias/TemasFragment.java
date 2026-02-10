package com.cyberpath.smartlearn.ui.main.combo.principal.materias;

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
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemasFragment extends Fragment {
    private final List<Tema> listaTemas = new ArrayList<>();
    private TextView textoMateria;
    private ViewPager2 viewPagerTemas;
    private AdaptadorTemas adapterTemas;
    private Materia materia;

    private NavAccesibilidadTemas navAccesibilidad;
    EntradaAudio entradaAudio = EntradaAudio.obtenerInstancia();

    private int ultimaPosicionReal = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        entradaAudio.detenerEscucha();

        textoMateria = view.findViewById(R.id.tvNombreMateria);
        crearCarrusel(view);

        materia = TemasFragmentArgs.fromBundle(getArguments()).getMateria();
        int materiaId = materia.getId();
        textoMateria.setText(materia.getNombre());

        // crear instancia de navegación por voz (usa listaTemas como fuente de verdad)
        navAccesibilidad = new NavAccesibilidadTemas(requireContext(), this, viewPagerTemas, adapterTemas, listaTemas);

        cargarTemas(materiaId);
    }

    private void cargarTemas(int materiaId) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Tema>> call = apiService.getTemasByMateria(materiaId);

        call.enqueue(new Callback<List<Tema>>() {
            @Override
            public void onResponse(Call<List<Tema>> call, Response<List<Tema>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Tema> temas = response.body();
                    requireActivity().runOnUiThread(() -> {
                        listaTemas.clear();
                        listaTemas.addAll(temas);
                        adapterTemas.actualizarLista(new ArrayList<>(listaTemas));
                        adapterTemas.notifyDataSetChanged();

                        if (temas.size() > 0) {
                            // posicion inicial centrada en el primer real (índice 1 en duplicado)
                            viewPagerTemas.setCurrentItem(1, false);
                        }

                        if (temas.isEmpty()) {
                            Toast.makeText(requireContext(), "No hay temas para esta materia", Toast.LENGTH_SHORT).show();
                        }

                        int posicionMostrar = ultimaPosicionReal + 1; // +1 por el duplicado inicial
                        viewPagerTemas.setCurrentItem(posicionMostrar, false);

                        viewPagerTemas.post(() -> viewPagerTemas.requestTransform());

                        // Iniciar la navegación por voz ahora que los datos ya están cargados y el adapter actualizado
                        if (navAccesibilidad != null) navAccesibilidad.iniciarNavegacion();
                    });
                } else {
                    mostrarError("Error al cargar temas");
                }
            }

            @Override
            public void onFailure(Call<List<Tema>> call, Throwable t) {
                mostrarError("Error de red: " + t.getMessage());
            }
        });
    }

    private void mostrarError(String mensaje) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }

    private void onTemaClick(Tema tema) {
        var action = TemasFragmentDirections.actionTemasFragmentToSubtemasFragment(tema);
        NavHostFragment.findNavController(this).navigate(action);
    }

    public void crearCarrusel(View view) {
        viewPagerTemas = view.findViewById(R.id.viewPagerTemas);
        adapterTemas = new AdaptadorTemas(listaTemas, this::onTemaClick);
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
                int realSize = listaTemas.size();
                if (realSize > 0) {
                    ultimaPosicionReal = (position - 1 + realSize) % realSize;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE && !isResetting) {
                    int position = viewPagerTemas.getCurrentItem();
                    int realSize = listaTemas.size();

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

    /**
     * Permite que NavAccesibilidad entre a un tema exactamente igual que onTemaClick.
     * Devuelve true/false por callback cuando la navegación se realizó.
     */
    public void entrarTemaDesdeAccesibilidad(Tema tema, BiConsumer<Boolean, Tema> callback) {
        if (tema == null) {
            if (callback != null) callback.accept(false, null);
            return;
        }
        try {
            var action = TemasFragmentDirections.actionTemasFragmentToSubtemasFragment(tema);
            NavHostFragment.findNavController(this).navigate(action);
            if (callback != null) callback.accept(true, tema);
        } catch (Exception e) {
            if (callback != null) callback.accept(false, tema);
        }
    }

    /**
     * Simula que se presionó el botón de regresar (popBackStack / onBackPressed).
     * NavAccesibilidad puede usar esto para volver a la sección anterior.
     */
    public void simularRegresar() {
        requireActivity().runOnUiThread(() -> {
            try {
                // Intentamos popBackStack primero
                if (!NavHostFragment.findNavController(TemasFragment.this).popBackStack()) {
                    // Si no hay nada que pop, caemos a onBackPressed
                    requireActivity().onBackPressed();
                }
            } catch (Exception e) {
                // fallback
                requireActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (navAccesibilidad != null) navAccesibilidad.detenerNavegacion();
    }
}