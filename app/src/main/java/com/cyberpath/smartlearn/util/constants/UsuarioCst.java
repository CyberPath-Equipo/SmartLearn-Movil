package com.cyberpath.smartlearn.util.constants;

import android.content.Context;

import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioCst {
    public static Usuario USUARIO_ACTUAL;

    public static void asignarConstantesUsuario(Context context, Integer id) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<Usuario> call = apiService.getUsuarioById(id);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null){
                    USUARIO_ACTUAL = response.body();
                    PreferencesManager.setIdUsuario(context, USUARIO_ACTUAL.getId());
                    PreferencesManager.setNombreUsuario(context, USUARIO_ACTUAL.getNombreCuenta());
                    PreferencesManager.setCorreoUsuario(context, USUARIO_ACTUAL.getCorreo());
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                System.out.println("Error al cargar usuario");
            }
        });
    }
}
