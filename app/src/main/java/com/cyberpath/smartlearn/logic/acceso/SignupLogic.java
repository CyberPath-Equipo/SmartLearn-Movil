package com.cyberpath.smartlearn.logic.acceso;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.acceso.RegistroPendienteResponse;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.acceso.SignUpFragment;
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
        api.saveUsuarioEjercicio(nuevoUsuario).enqueue(new Callback<RegistroPendienteResponse>() {
            @Override
            public void onResponse(Call<RegistroPendienteResponse> call, Response<RegistroPendienteResponse> response) {
                if (!signUpFragment.isAdded()) {
                    return;
                }
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    RegistroPendienteResponse registro = response.body();
                    if (Boolean.TRUE.equals(registro.getRequiresVerification()) || registro.getTransactionId() != null) {
                        prepararRegistroPendiente(registro, nombreUsuario, correo);
                        abrirVerificacionCorreo(registro, nombreUsuario, correo);
                        return;
                    }

                    Toast.makeText(signUpFragment.requireContext(),
                            registro.getMessage() != null ? registro.getMessage() : "Registro completado",
                            Toast.LENGTH_LONG).show();
                    Navigation.findNavController(signUpFragment.requireView()).navigate(R.id.loginFragment);
                    return;
                }

                if (response.code() == 202) {
                    Toast.makeText(signUpFragment.requireContext(), "Registro pendiente de verificación", Toast.LENGTH_SHORT).show();
                } else {
                    String error = "Error " + response.code() + ": " + response.message();
                    Toast.makeText(signUpFragment.requireContext(), error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, error);
                }
            }

            @Override
            public void onFailure(Call<RegistroPendienteResponse> call, Throwable t) {
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

    private void prepararRegistroPendiente(RegistroPendienteResponse registro, String nombreUsuario, String correo) {
        PreferencesManager.setRegistroPendiente(signUpFragment.requireContext(), true);
        PreferencesManager.setRegistroTransactionId(signUpFragment.requireContext(), registro.getTransactionId());
        PreferencesManager.setRegistroCorreo(signUpFragment.requireContext(), registro.getCorreo() != null ? registro.getCorreo() : correo);
        PreferencesManager.setRegistroNombreCuenta(signUpFragment.requireContext(), registro.getNombreCuenta() != null ? registro.getNombreCuenta() : nombreUsuario);
        PreferencesManager.setUsuarioRegistrado(signUpFragment.requireContext(), false);
        PreferencesManager.setSesionActiva(signUpFragment.requireContext(), false);
    }

    private void abrirVerificacionCorreo(RegistroPendienteResponse registro, String nombreUsuario, String correo) {
        if (registro.getTransactionId() == null || registro.getTransactionId().trim().isEmpty()) {
            Toast.makeText(signUpFragment.requireContext(), "No se pudo iniciar la verificación por correo", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString("transactionId", registro.getTransactionId());
        args.putString("channel", "EMAIL");
        args.putString("flowMode", "EMAIL_VERIFY");
        args.putString("nombreCuenta", registro.getNombreCuenta() != null ? registro.getNombreCuenta() : nombreUsuario);
        args.putString("correo", registro.getCorreo() != null ? registro.getCorreo() : correo);

        Toast.makeText(signUpFragment.requireContext(), "Se envió un código de verificación al correo", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(signUpFragment.requireView())
                .navigate(R.id.action_signUpFragment_to_twoFactorFragment, args);
    }
}