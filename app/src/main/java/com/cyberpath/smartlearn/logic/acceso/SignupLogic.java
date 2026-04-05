package com.cyberpath.smartlearn.logic.acceso;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View;

import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.acceso.SignUpFragment;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.validation.InputValidator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupLogic {
    private static final String TAG = "SignupLogic";

    private final SignUpFragment signUpFragment;

    public SignupLogic(SignUpFragment fragment) {
        this.signUpFragment = fragment;
    }

    public void registrarUsuario(String nombreUsuario, String nombreCompleto, String contrasena, String correo,
                                 boolean radioVisualActivaChecked, boolean radioVisualInactivaChecked,
                                 boolean radioAuditivaActivaChecked, boolean radioAuditivaInactivaChecked,
                                 boolean radioAlumnoChecked, boolean radioDocenteChecked) {
        if (!validarCampos(nombreUsuario, nombreCompleto, contrasena, correo)) {
            return;
        }

        Boolean accesibilidadVisual = obtenerEstadoAccesibilidad(
                radioVisualActivaChecked,
                radioVisualInactivaChecked,
                "Selecciona la accesibilidad visual"
        );
        if (accesibilidadVisual == null) return;

        Boolean accesibilidadAuditiva = obtenerEstadoAccesibilidad(
                radioAuditivaActivaChecked,
                radioAuditivaInactivaChecked,
                "Selecciona la accesibilidad auditiva"
        );
        if (accesibilidadAuditiva == null) return;

        Integer idRol = obtenerIdRol(radioAlumnoChecked, radioDocenteChecked);
        if (idRol == null) return;

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCuenta(nombreUsuario);
        nuevoUsuario.setNombreCompleto(nombreCompleto);
        nuevoUsuario.setContrasena(contrasena);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setIdRol(idRol);

        Log.d(TAG, "Registrando usuario: " + nombreUsuario + ", correo: " + correo + ", rol: " + idRol);

        mostrarLoading(true);

        ApiService api = RetrofitClient.getApiService();
        api.save(nuevoUsuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (!signUpFragment.isAdded()) {
                    return;
                }
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuarioRegistrado = response.body();
                    int idUsuario = usuarioRegistrado.getId();

                    PreferencesManager.setUsuarioRegistrado(signUpFragment.requireContext(), true);
                    PreferencesManager.setSesionActiva(signUpFragment.requireContext(), true);
                    PreferencesManager.setIdUsuario(signUpFragment.requireContext(), idUsuario);
                    PreferencesManager.setAccesibilidadVisual(signUpFragment.requireContext(), accesibilidadVisual);
                    PreferencesManager.setAccesibilidadAuditiva(signUpFragment.requireContext(), accesibilidadAuditiva);
                    PreferencesManager.setIdSubtemaUltimaConexion(signUpFragment.requireContext(), -1);
                    PreferencesManager.setTemaApp(
                            signUpFragment.requireContext(),
                            accesibilidadVisual ? PreferencesManager.THEME_ACCESSIBLE : PreferencesManager.THEME_LIGHT
                    );

                    Log.d(TAG, "Registro exitoso. ID de usuario guardado: " + idUsuario);
                    Toast.makeText(signUpFragment.requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();

                    // Usar callback para cargar usuario antes de navegar
                    UsuarioCst.asignarConstantesUsuario(signUpFragment.requireContext(), idUsuario,
                            new UsuarioCst.UsuarioLoadCallback() {
                                @Override
                                public void onUsuarioLoaded(Usuario usuario) {
                                    Navigation.findNavController(signUpFragment.requireView()).navigate(R.id.loginFragment);
                                }

                                @Override
                                public void onError(String mensaje) {
                                    Log.e(TAG, "Error al cargar usuario tras registro: " + mensaje);
                                    Navigation.findNavController(signUpFragment.requireView()).navigate(R.id.loginFragment);
                                }
                            });
                } else {
                    String error = "Error " + response.code() + ": " + response.message();
                    Toast.makeText(signUpFragment.requireContext(), error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, error);
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                if (!signUpFragment.isAdded()) {
                    return;
                }
                mostrarLoading(false);
                String error = "Error de red: " + t.getMessage();
                Toast.makeText(signUpFragment.requireContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error, t);
            }
        });
    }

    private boolean validarCampos(String nombreUsuario, String nombreCompleto, String contrasena, String correo) {
        if (nombreUsuario.isEmpty() || nombreCompleto.isEmpty() || contrasena.isEmpty() || correo.isEmpty()) {
            Toast.makeText(signUpFragment.requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!InputValidator.isValidNombreCompleto(nombreCompleto)) {
            Toast.makeText(signUpFragment.requireContext(), "El nombre completo no puede contener numeros", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!InputValidator.isValidEmail(correo)) {
            Toast.makeText(signUpFragment.requireContext(), "Ingresa un correo valido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!InputValidator.isValidPassword(contrasena)) {
            Toast.makeText(signUpFragment.requireContext(), "La contrasena debe tener minimo 6 caracteres y al menos un numero", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private Boolean obtenerEstadoAccesibilidad(boolean radioActivaChecked, boolean radioInactivaChecked, String mensajeError) {
        if (radioActivaChecked) {
            return true;
        } else if (radioInactivaChecked) {
            return false;
        } else {
            Toast.makeText(signUpFragment.requireContext(), mensajeError, Toast.LENGTH_SHORT).show();
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
        if (!signUpFragment.isAdded() || signUpFragment.getView() == null) {
            return;
        }

        ProgressBar loading = signUpFragment.requireView().findViewById(R.id.loading);
        View btnRegistro = signUpFragment.requireView().findViewById(R.id.btn_registro);

        loading.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnRegistro.setEnabled(!mostrar);
    }
}