package com.cyberpath.smartlearn.util.constants;

import android.content.Context;

import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.util.network.NetworkUtils;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioCst {
    public static Usuario USUARIO_ACTUAL;


    public static void asignarConstantesUsuario(Context context, Integer id, UsuarioLoadCallback callback) {
        if (id == null || id <= 0) {
            if (callback != null) callback.onError("ID de usuario inválido");
            return;
        }

        if (NetworkUtils.shouldUseOfflineMode(context)) {
            Usuario usuarioLocal = reconstructFromPreferences(context);
            if (usuarioLocal != null) {
                USUARIO_ACTUAL = usuarioLocal;
                if (callback != null) callback.onUsuarioLoaded(USUARIO_ACTUAL);
            } else if (callback != null) {
                callback.onError("Sin conexión y sin datos locales de usuario");
            }
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<Usuario> call = apiService.getUsuarioById(id);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    USUARIO_ACTUAL = response.body();
                    PreferencesManager.setIdUsuario(context, USUARIO_ACTUAL.getId());
                    PreferencesManager.setNombreUsuario(context, USUARIO_ACTUAL.getNombreCuenta());
                    PreferencesManager.setCorreoUsuario(context, USUARIO_ACTUAL.getCorreo());
                    if (callback != null) callback.onUsuarioLoaded(USUARIO_ACTUAL);
                } else {
                    if (callback != null)
                        callback.onError("Error al cargar usuario: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Usuario usuarioLocal = reconstructFromPreferences(context);
                if (usuarioLocal != null) {
                    USUARIO_ACTUAL = usuarioLocal;
                    if (callback != null) callback.onUsuarioLoaded(USUARIO_ACTUAL);
                } else if (callback != null) {
                    callback.onError("Error de red: " + t.getMessage());
                }
            }
        });
    }


    public static void asignarConstantesUsuario(Context context, Integer id) {
        asignarConstantesUsuario(context, id, null);
    }


    public static Usuario reconstructFromPreferences(Context context) {
        int idUsuario = PreferencesManager.getIdUsuario(context);
        if (idUsuario <= 0) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        usuario.setNombreCuenta(PreferencesManager.getNombreUsuario(context));
        usuario.setCorreo(PreferencesManager.getCorreoUsuario(context));
        usuario.setVerificado(PreferencesManager.isUsuarioVerificado(context));
        return usuario;
    }


    public static boolean ensureUsuarioLoaded(Context context) {
        if (USUARIO_ACTUAL != null && USUARIO_ACTUAL.getId() != null) {
            return true;
        }


        Usuario usuarioFromPrefs = reconstructFromPreferences(context);
        if (usuarioFromPrefs != null) {
            USUARIO_ACTUAL = usuarioFromPrefs;
            return true;
        }

        return false;
    }


    public interface UsuarioLoadCallback {
        void onUsuarioLoaded(Usuario usuario);

        void onError(String mensaje);
    }
}
