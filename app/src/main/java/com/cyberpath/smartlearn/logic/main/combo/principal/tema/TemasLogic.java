package com.cyberpath.smartlearn.logic.main.combo.principal.tema;

import android.content.Context;
import android.util.Log;

import com.cyberpath.smartlearn.data.local.database.repository.TemasRepository;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.principal.tema.TemasFragment;
import com.cyberpath.smartlearn.util.network.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemasLogic {

    private final TemasFragment fragment;
    private final Context context;
    private final Materia materia;

    private final TemasRepository temasRepository;

    @Getter
    private final List<Tema> listaTemas = new ArrayList<>();
    private boolean modoOffline = false;

    public TemasLogic(TemasFragment fragment, Materia materia) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        this.materia = materia;
        this.temasRepository = new TemasRepository(context);

        cargarTemas();
    }

    public void cargarTemas() {
        if (materia == null || materia.getId() == null) {
            fragment.showToast("Materia inválida");
            return;
        }

        if (NetworkUtils.isInternetAvailable(context)) {
            modoOffline = false;
            cargarTemasAPI();
        } else {
            modoOffline = true;
            cargarTemasLocal();
        }
    }

    private void cargarTemasAPI() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Tema>> call = apiService.getTemasByMateria(materia.getId());

        call.enqueue(new Callback<List<Tema>>() {
            @Override
            public void onResponse(Call<List<Tema>> call, Response<List<Tema>> response) {
                if (fragment == null || !fragment.isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    listaTemas.clear();
                    listaTemas.addAll(response.body());


                    fragment.actualizarAdapter(listaTemas);

                    if (listaTemas.isEmpty()) {
                        fragment.showToast("No hay temas para esta materia");
                    } else {
                        fragment.moverViewPagerAPosicion(1, false);
                    }

                    fragment.iniciarNavegacionPorVoz();
                } else {
                    fragment.showToast("Error al cargar temas");
                    cargarTemasLocal();
                }
            }

            @Override
            public void onFailure(Call<List<Tema>> call, Throwable t) {
                if (fragment == null || !fragment.isAdded()) return;
                fragment.showToast("Error de red: " + t.getMessage());
                cargarTemasLocal();
            }
        });
    }

    private void cargarTemasLocal() {
        try {
            List<Tema> temasLocales = temasRepository.obtenerTemasPorMateria(materia.getId());

            listaTemas.clear();
            listaTemas.addAll(temasLocales);

            fragment.actualizarAdapter(listaTemas);

            if (listaTemas.isEmpty()) {
                fragment.showToast("No hay temas disponibles sin conexión");
            } else {
                fragment.showToast("Modo offline - Temas locales");
                fragment.moverViewPagerAPosicion(1, false);
            }

            fragment.iniciarNavegacionPorVoz();

        } catch (Exception e) {
            Log.e("TemasLogic", "Error al cargar temas locales: " + e.getMessage());
            fragment.showToast("Error al cargar temas locales");
        }
    }

    private void guardarEnLocal(List<Tema> temas) {
        try {
            temasRepository.guardarTemas(temas);
            Log.d("TemasLogic", "Temas guardados en BD local");
        } catch (Exception e) {
            Log.e("TemasLogic", "Error al guardar temas localmente: " + e.getMessage());
        }
    }

    public void limpiarDatos() {
        listaTemas.clear();
    }

    public int getRealSize() {
        return listaTemas != null ? listaTemas.size() : 0;
    }
}