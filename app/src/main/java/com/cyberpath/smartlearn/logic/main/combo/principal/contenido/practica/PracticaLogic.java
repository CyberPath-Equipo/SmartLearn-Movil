package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica;

import android.content.Context;
import android.util.Log;
import android.widget.AdapterView;

import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.PracticaFragment;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PracticaLogic {

    private final PracticaFragment fragment;
    private final Context context;
    private final Subtema subtema;

    @Getter
    private final List<Ejercicio> listaEjercicios = new ArrayList<>();

    public PracticaLogic(PracticaFragment fragment, Subtema subtema) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        this.subtema = subtema;

        cargarEjercicios();
    }

    public void cargarEjercicios() {
        if (subtema == null || subtema.getId() == null) {
            fragment.showToast("Subtema no identificado");
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Ejercicio>> call = apiService.getEjerciciosBySubtema(subtema.getId());

        call.enqueue(new Callback<List<Ejercicio>>() {
            @Override
            public void onResponse(Call<List<Ejercicio>> call, Response<List<Ejercicio>> response) {
                if (fragment == null || !fragment.isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    listaEjercicios.clear();
                    listaEjercicios.addAll(response.body());

                    fragment.actualizarAdapter(listaEjercicios);

                    if (listaEjercicios.isEmpty()) {
                        fragment.showToast("No hay ejercicios para este subtema");
                    } else {

                        fragment.seleccionarItemListView(0);
                    }

                    fragment.iniciarNavegacionPorVoz();
                } else {
                    fragment.showToast("Error al cargar ejercicios");
                }
            }

            @Override
            public void onFailure(Call<List<Ejercicio>> call, Throwable t) {
                if (fragment == null || !fragment.isAdded()) return;
                fragment.showToast("Error de conexión: " + t.getMessage());
                Log.d("PRACTICA ERROR: ", "Error de conexión: " + t.getMessage());
            }
        });
    }

    public void limpiarDatos() {
        listaEjercicios.clear();
    }

    public int getRealSize() {
        return listaEjercicios != null ? listaEjercicios.size() : 0;
    }

    public Ejercicio getEjercicioPorPosicion(int posicion) {
        if (listaEjercicios == null || posicion < 0 || posicion >= listaEjercicios.size()) {
            return null;
        }
        return listaEjercicios.get(posicion);
    }

    public int getPosicionActual() {
        if (fragment == null) return 0;
        int position = fragment.getPosicionActualListView();
        if (position == AdapterView.INVALID_POSITION) return 0;
        return position;
    }

    public void setPosicionActual(int posicion) {
        if (getRealSize() > 0 && posicion >= 0 && posicion < getRealSize()) {
            fragment.seleccionarItemListView(posicion);
        }
    }
}