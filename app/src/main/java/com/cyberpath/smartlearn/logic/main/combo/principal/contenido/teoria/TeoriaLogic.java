package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria;

import android.content.Context;
import android.util.Log;

import com.cyberpath.smartlearn.data.local.database.repository.TeoriaRepository;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Teoria;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.teoria.TeoriaFragment;
import com.cyberpath.smartlearn.util.network.NetworkUtils;

import java.util.ArrayList;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeoriaLogic {

    private final TeoriaFragment fragment;
    private final Context context;
    private final Subtema subtema;
    private final TeoriaRepository teoriaRepository;
    private final NavAccesibilidad navAccesibilidad;
    private boolean modoOffline = false;
    private final ArrayList<String> preguntas;
    @Getter
    private Teoria teoriaActual;

    public TeoriaLogic(TeoriaFragment fragment, Subtema subtema, ArrayList<String> preguntas) {
        this(fragment, subtema, preguntas, null);
    }

    public TeoriaLogic(TeoriaFragment fragment, Subtema subtema, ArrayList<String> preguntas, NavAccesibilidad navAccesibilidad) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        this.subtema = subtema;
        this.preguntas = preguntas;
        this.navAccesibilidad = navAccesibilidad;
        this.teoriaRepository = new TeoriaRepository(context);

        cargarTeoria();
    }

    public void cargarTeoria() {
        if (subtema == null || subtema.getId() == null) {
            fragment.mostrarError("Subtema inválido");
            return;
        }
        if (NetworkUtils.isInternetAvailable(context)) {
            modoOffline = false;
            cargarTeoriaAPI();
        } else {
            modoOffline = true;
            cargarTeoriaLocal();
        }
    }

    private void cargarTeoriaAPI() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<Teoria> call = apiService.getTeoriaBySubtema(subtema.getId());

        call.enqueue(new Callback<Teoria>() {
            @Override
            public void onResponse(Call<Teoria> call, Response<Teoria> response) {
                if (fragment == null || !fragment.isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    teoriaActual = response.body();
                    if (teoriaActual.getIdSubtema() == null) {
                        teoriaActual.setIdSubtema(subtema.getId());
                    }
                    guardarEnLocal(teoriaActual);

                    mostrarContenido(teoriaActual);
                } else {
                    cargarTeoriaLocal();
                }
            }

            @Override
            public void onFailure(Call<Teoria> call, Throwable t) {
                if (fragment == null || !fragment.isAdded()) return;
                cargarTeoriaLocal();
            }
        });
    }

    private void cargarTeoriaLocal() {
        try {
            teoriaActual = teoriaRepository.obtenerTeoriaPorSubtema(subtema.getId());

            if (teoriaActual != null) {
                mostrarContenido(teoriaActual);
            } else {
                fragment.mostrarError("No hay teoría disponible sin conexión");
            }

        } catch (Exception e) {
            Log.e("TeoriaLogic", "Error al cargar teoría local: " + e.getMessage());
            fragment.mostrarError("Error al cargar teoría local");
        }
    }

    private void mostrarContenido(Teoria teoria) {
        String contenido = teoria.getContenido();

        if (preguntas != null && !preguntas.isEmpty()) {
            fragment.resaltarPalabrasClave(contenido, preguntas);
        } else {
            fragment.mostrarTeoria(contenido);
        }

        if (navAccesibilidad != null && navAccesibilidad.isAccesibilidadAuditivaActivada()) {
            String lessonId = subtema != null ? String.valueOf(subtema.getId()) : "lesson_1";
            navAccesibilidad.reproducirContenido(contenido, "lesson_1");
        }
    }

    private void guardarEnLocal(Teoria teoria) {
        try {
            teoriaRepository.guardarTeoria(teoria);
            Log.d("TeoriaLogic", "Teoría guardada en BD local");
        } catch (Exception e) {
            Log.e("TeoriaLogic", "Error al guardar teoría localmente: " + e.getMessage());
        }
    }

}