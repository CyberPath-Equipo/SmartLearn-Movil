package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica;

import android.content.Context;
import android.util.Log;
import android.widget.AdapterView;

import com.cyberpath.smartlearn.data.local.database.dao.ContenidoDAO;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.PracticaFragment;
import com.cyberpath.smartlearn.util.network.NetworkUtils;

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
    private final ContenidoDAO contenidoDAO;

    @Getter
    private final List<Ejercicio> listaEjercicios = new ArrayList<>();

    public PracticaLogic(PracticaFragment fragment, Subtema subtema) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        this.subtema = subtema;
        this.contenidoDAO = new ContenidoDAO(context);

        cargarEjercicios();
    }

    public void cargarEjercicios() {
        if (subtema == null || subtema.getId() == null) {
            fragment.showToast("Subtema no identificado");
            return;
        }

        if (!NetworkUtils.isInternetAvailable(context)) {
            cargarEjerciciosLocal();
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
                    guardarEnLocal(listaEjercicios);

                    fragment.actualizarAdapter(listaEjercicios);

                    if (listaEjercicios.isEmpty()) {
                        fragment.showToast("No hay ejercicios para este subtema");
                    } else {

                        fragment.seleccionarItemListView(0);
                    }

                    fragment.iniciarNavegacionPorVoz();
                } else {
                    cargarEjerciciosLocal();
                }
            }

            @Override
            public void onFailure(Call<List<Ejercicio>> call, Throwable t) {
                if (fragment == null || !fragment.isAdded()) return;
                Log.d("PRACTICA ERROR: ", "Error de conexión: " + t.getMessage());
                cargarEjerciciosLocal();
            }
        });
    }

    private void cargarEjerciciosLocal() {
        try {
            List<Ejercicio> ejerciciosLocales = contenidoDAO.obtenerEjerciciosPorSubtema(subtema.getId());
            listaEjercicios.clear();
            listaEjercicios.addAll(ejerciciosLocales);
            fragment.actualizarAdapter(listaEjercicios);

            if (listaEjercicios.isEmpty()) {
                fragment.showToast("No hay ejercicios disponibles sin conexión");
            } else {
                fragment.showToast("Modo offline - Ejercicios locales");
                fragment.seleccionarItemListView(0);
                fragment.iniciarNavegacionPorVoz();
            }
        } catch (Exception e) {
            Log.e("PracticaLogic", "Error al cargar ejercicios locales", e);
            fragment.showToast("Error al cargar ejercicios locales");
        }
    }

    private void guardarEnLocal(List<Ejercicio> ejercicios) {
        try {
            for (Ejercicio ejercicio : ejercicios) {
                if (ejercicio.getIdSubtema() == null) {
                    ejercicio.setIdSubtema(subtema.getId());
                }
                contenidoDAO.insertarEjercicio(ejercicio);
            }
        } catch (Exception e) {
            Log.e("PracticaLogic", "Error al guardar ejercicios en local", e);
        }
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