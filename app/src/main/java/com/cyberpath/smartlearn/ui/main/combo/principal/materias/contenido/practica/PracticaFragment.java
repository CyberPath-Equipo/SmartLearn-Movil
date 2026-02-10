package com.cyberpath.smartlearn.ui.main.combo.principal.materias.contenido.practica;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PracticaFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView listViewEjercicios;
    private AdaptadorPractica adaptadorEjercicios;
    private final List<Ejercicio> listaEjercicios = new ArrayList<>();
    private Subtema subtema;
    private TextView tvSubtema;

    private NavAccesibilidadPractica navAccesibilidad;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_practica, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            subtema = PracticaFragmentArgs.fromBundle(getArguments()).getSubtema();
        }

        tvSubtema = view.findViewById(R.id.tv_subtema);
        tvSubtema.setText(subtema != null && subtema.getNombre() != null ? subtema.getNombre() : "");

        listViewEjercicios = view.findViewById(R.id.listViewEjercicios);
        adaptadorEjercicios = new AdaptadorPractica(getContext(), listaEjercicios);
        listViewEjercicios.setAdapter(adaptadorEjercicios);
        listViewEjercicios.setOnItemClickListener(this);

        // crear NavAccesibilidad (usa la listaEjercicios como fuente)
        navAccesibilidad = new NavAccesibilidadPractica(requireContext(), this, listViewEjercicios, adaptadorEjercicios, listaEjercicios);

        // Asegurarse de detener cualquier escucha activa al entrar
        try { EntradaAudio.obtenerInstancia().detenerEscucha(); } catch (Exception ignored) {}
        try { SalidaAudio.obtenerInstancia().detener(); } catch (Exception ignored) {}

        cargarEjercicios(subtema);
    }

    private void cargarEjercicios(Subtema subtema) {
        if (subtema == null) {
            Toast.makeText(getContext(), "Subtema no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Ejercicio>> call = apiService.getEjerciciosBySubtema(subtema.getId());
        call.enqueue(new Callback<List<Ejercicio>>() {
            @Override
            public void onResponse(Call<List<Ejercicio>> call, Response<List<Ejercicio>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Ejercicio> ejercicios = response.body();
                    requireActivity().runOnUiThread(() -> {
                        listaEjercicios.clear();
                        listaEjercicios.addAll(ejercicios);
                        adaptadorEjercicios.notifyDataSetChanged();

                        if (ejercicios.isEmpty()) {
                            Toast.makeText(requireContext(), "No hay ejercicios para este subtema", Toast.LENGTH_SHORT).show();
                        } else {
                            // posicionar al primero
                            listViewEjercicios.setSelection(0);
                        }

                        // Iniciar navegación por voz ahora que los datos están cargados
                        if (navAccesibilidad != null) {
                            // pequeña protección para que no arranque antes de tiempo
                            mainHandler.postDelayed(() -> navAccesibilidad.iniciarNavegacion(), 200);
                        }
                    });
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error al cargar ejercicios", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(Call<List<Ejercicio>> call, Throwable t) {
                getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("PRACTICA ERROR: ", "Error de conexión: " + t.getMessage());
                        }
                );
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Ejercicio ejercicioSeleccionado = listaEjercicios.get(position);

        var action = PracticaFragmentDirections
                .actionPracticaFragmentToEjercicioFragment(ejercicioSeleccionado);

        NavHostFragment.findNavController(this).navigate(action);
    }

    /**
     * Entrada por accesibilidad: navega al ejercicio igual que un click normal.
     * Devuelve resultado por callback.
     */
    public void entrarEjercicioDesdeAccesibilidad(Ejercicio ejercicio, BiConsumer<Boolean, Ejercicio> callback) {
        if (ejercicio == null) {
            if (callback != null) callback.accept(false, null);
            return;
        }

        try {
            var action = PracticaFragmentDirections.actionPracticaFragmentToEjercicioFragment(ejercicio);
            NavHostFragment.findNavController(this).navigate(action);
            if (callback != null) callback.accept(true, ejercicio);
        } catch (Exception e) {
            if (callback != null) callback.accept(false, ejercicio);
        }
    }

    /**
     * Simula presionar volver (popBackStack / onBackPressed).
     */
    public void simularRegresar() {
        requireActivity().runOnUiThread(() -> {
            try {
                if (!NavHostFragment.findNavController(PracticaFragment.this).popBackStack()) {
                    requireActivity().onBackPressed();
                }
            } catch (Exception e) {
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