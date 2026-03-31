package com.cyberpath.smartlearn.util.constants;

import android.content.Context;

import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioCst {
    public static Usuario USUARIO_ACTUAL;

    public static void asignarConstantesUsuario(Integer id) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<Usuario> call = apiService.getUsuarioById(id);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null)
                    USUARIO_ACTUAL = response.body();
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                System.out.println("Error al cargar usuario");
            }
        });
    }

    public static Integer obtenerIdUsuarioActual(Context context) {
        if (USUARIO_ACTUAL != null && USUARIO_ACTUAL.getId() != null) {
            return USUARIO_ACTUAL.getId();
        }
        int idPref = PreferencesManager.getIdUsuario(context);
        return idPref == -1 ? null : idPref;
    }
}
