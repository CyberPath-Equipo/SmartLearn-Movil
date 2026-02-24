package com.cyberpath.smartlearn.logic.acceso;

import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.acceso.SignUpFragment;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupLogic {
    private static final String TAG = "SignupLogic";

    private final SignUpFragment signUpFragment;
    private ProgressBar loading;
    private Button btnRegistro;

    public SignupLogic(SignUpFragment fragment) {
        this.signUpFragment = fragment;
    }

    public void registrarUsuario(String nombre, String contrasena, String correo,
                                 boolean radioActivaChecked, boolean radioInactivaChecked,
                                 boolean radioAlumnoChecked, boolean radioDocenteChecked) {
        if (!validarCampos(nombre, contrasena, correo)) {
            return;
        }
        Boolean modoAudio = obtenerModoAccesibilidad(radioActivaChecked, radioInactivaChecked);
        if (modoAudio == null) return;
        Integer idRol = obtenerIdRol(radioAlumnoChecked, radioDocenteChecked);
        if (idRol == null) return;

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCuenta(nombre);
        nuevoUsuario.setContrasena(contrasena);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setIdRol(idRol);

        Log.d(TAG, "Registrando usuario: " + nombre + ", correo: " + correo + ", rol: " + idRol);

        mostrarLoading(true);

        ApiService api = RetrofitClient.getApiService();
        api.save(nuevoUsuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuarioRegistrado = response.body();
                    int idUsuario = usuarioRegistrado.getId();

                    PreferencesManager.setUsuarioRegistrado(signUpFragment.requireContext(), true);
                    PreferencesManager.setIdUsuario(signUpFragment.requireContext(), idUsuario);
                    PreferencesManager.setModoAudio(signUpFragment.requireContext(), modoAudio);
                    PreferencesManager.setIdSubtemaUltimaConexion(signUpFragment.requireContext(), -1);

                    UsuarioCst.asignarConstantesUsuario(signUpFragment.requireContext(), idUsuario);

                    Log.d(TAG, "Registro exitoso. ID de usuario guardado: " + idUsuario);
                    Toast.makeText(signUpFragment.requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();

                    Navigation.findNavController(signUpFragment.requireView()).navigate(R.id.loginFragment);
                } else {
                    String error = "Error " + response.code() + ": " + response.message();
                    Toast.makeText(signUpFragment.requireContext(), error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, error);
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                mostrarLoading(false);
                String error = "Error de red: " + t.getMessage();
                Toast.makeText(signUpFragment.requireContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error, t);
            }
        });
    }

    private boolean validarCampos(String nombre, String contrasena, String correo) {
        if (nombre.isEmpty() || contrasena.isEmpty() || correo.isEmpty()) {
            Toast.makeText(signUpFragment.requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Boolean obtenerModoAccesibilidad(boolean radioActivaChecked, boolean radioInactivaChecked) {
        if (radioActivaChecked) {
            return true;
        } else if (radioInactivaChecked) {
            return false;
        } else {
            Toast.makeText(signUpFragment.requireContext(), "Selecciona el modo de accesibilidad", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Integer obtenerIdRol(boolean radioAlumnoChecked, boolean radioDocenteChecked) {
        if (radioAlumnoChecked) {
            return 1;
        } else if (radioDocenteChecked) {
            return 2;
        } else {
            Toast.makeText(signUpFragment.requireContext(), "Selecciona tipo de usuario", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void mostrarLoading(boolean mostrar) {
        if (mostrar) {
        } else {
        }
    }
}