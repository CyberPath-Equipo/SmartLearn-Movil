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

    // Interfaz para callbacks de carga de usuario
    public interface UsuarioLoadCallback {
        void onUsuarioLoaded(Usuario usuario);
        void onError(String mensaje);
    }

    /**
     * Carga el usuario con callback para esperar a que se complete
     */
    public static void asignarConstantesUsuario(Context context, Integer id, UsuarioLoadCallback callback) {
        if (id == null || id <= 0) {
            if (callback != null) callback.onError("ID de usuario inválido");
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
                    if (callback != null) callback.onError("Error al cargar usuario: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                if (callback != null) callback.onError("Error de red: " + t.getMessage());
            }
        });
    }

    /**
     * Carga el usuario sin callback (versión asíncrona silenciosa)
     */
    public static void asignarConstantesUsuario(Context context, Integer id) {
        asignarConstantesUsuario(context, id, null);
    }

    /**
     * Reconstruye el usuario desde las preferencias almacenadas
     */
    public static Usuario reconstructFromPreferences(Context context) {
        int idUsuario = PreferencesManager.getIdUsuario(context);
        if (idUsuario <= 0) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        usuario.setNombreCuenta(PreferencesManager.getNombreUsuario(context));
        usuario.setCorreo(PreferencesManager.getCorreoUsuario(context));
        return usuario;
    }

    /**
     * Garantiza que hay un usuario cargado, si no lo hay intenta cargarlo de las preferencias
     */
    public static boolean ensureUsuarioLoaded(Context context) {
        if (USUARIO_ACTUAL != null && USUARIO_ACTUAL.getId() != null) {
            return true;
        }

        // Intenta reconstruir desde preferencias como fallback
        Usuario usuarioFromPrefs = reconstructFromPreferences(context);
        if (usuarioFromPrefs != null) {
            USUARIO_ACTUAL = usuarioFromPrefs;
            return true;
        }

        return false;
    }
}
