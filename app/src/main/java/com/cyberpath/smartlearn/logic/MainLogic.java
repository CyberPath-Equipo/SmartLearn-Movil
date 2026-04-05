package com.cyberpath.smartlearn.logic;

import android.util.Log;

import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.MainActivity;
import com.cyberpath.smartlearn.util.network.NetworkUtils;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainLogic {
    private final MainActivity activity;
    private boolean modoOffline = false;

    public MainLogic(MainActivity activity) {
        this.activity = activity;
    }

    public void cargarUltimoSubtema() {
        int idUltimoSubtema = PreferencesManager.getIdSubtemaUltimaConexion(activity);

        if (idUltimoSubtema == -1) {
            activity.actualizarUltimoSubtemaMenu("Es tu primera vez, no tienes un historial", null);
            return;
        }

        modoOffline = !NetworkUtils.isInternetAvailable(activity);
        if (modoOffline) {
            activity.actualizarUltimoSubtemaMenu("Último acceso no disponible (modo offline)", null);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<Subtema> call = apiService.getSubtemaById(idUltimoSubtema);

        call.enqueue(new Callback<Subtema>() {
            @Override
            public void onResponse(Call<Subtema> call, Response<Subtema> response) {
                if (activity == null || activity.isFinishing()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Subtema subtema = response.body();
                    activity.actualizarUltimoSubtemaMenu(subtema.getNombre(), subtema);
                    Log.d("MainLogic", "Último subtema cargado: " + subtema.getNombre());
                } else {
                    activity.actualizarUltimoSubtemaMenu("Error al cargar el subtema", null);
                }
            }

            @Override
            public void onFailure(Call<Subtema> call, Throwable t) {
                if (activity == null || activity.isFinishing()) return;
                Log.e("MainLogic", "Error cargando último subtema: " + t.getMessage());
                activity.actualizarUltimoSubtemaMenu("Error de conexión, intenta más tarde", null);
                activity.showToast("Error de conexión: " + t.getMessage());
            }
        });
    }
}