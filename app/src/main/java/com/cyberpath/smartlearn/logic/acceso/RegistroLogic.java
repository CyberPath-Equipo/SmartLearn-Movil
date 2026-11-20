package com.cyberpath.smartlearn.logic.acceso;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.model.usuario.acceso.RegistroPendienteResponse;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.acceso.RegistroFragment;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.validation.InputValidator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroLogic {
    private static final String TAG = "SignupLogic";

    private final RegistroFragment registroFragment;

    public RegistroLogic(RegistroFragment fragment) {
        this.registroFragment = fragment;
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

        Integer idRol = 3;

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
                if (!registroFragment.isAdded()) {
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

                    Toast.makeText(registroFragment.requireContext(),
                            registro.getMessage() != null ? registro.getMessage() : "Registro completado",
                            Toast.LENGTH_LONG).show();
                    Navigation.findNavController(registroFragment.requireView()).navigate(R.id.InicioSesionFragment);
                    return;
                }

                if (response.code() == 202) {
                    Toast.makeText(registroFragment.requireContext(), "Registro pendiente de verificación", Toast.LENGTH_SHORT).show();
                } else {
                    String error = "Error " + response.code() + ": " + response.message();
                    Toast.makeText(registroFragment.requireContext(), error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, error);
                }
            }

            @Override
            public void onFailure(Call<RegistroPendienteResponse> call, Throwable t) {
                if (!registroFragment.isAdded()) {
                    return;
                }
                mostrarLoading(false);
                String error = "Error de red: " + t.getMessage();
                Toast.makeText(registroFragment.requireContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error, t);
            }
        });
    }

    private boolean validarCampos(String nombreUsuario, String nombreCompleto, String contrasena, String correo) {
        if (nombreUsuario.isEmpty() || nombreCompleto.isEmpty() || contrasena.isEmpty() || correo.isEmpty()) {
            Toast.makeText(registroFragment.requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!InputValidator.isValidNombreCompleto(nombreCompleto)) {
            Toast.makeText(registroFragment.requireContext(), "El nombre completo no puede contener numeros", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!InputValidator.isValidCorreo(correo)) {
            Toast.makeText(registroFragment.requireContext(), "Ingresa un correo valido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!InputValidator.isValidContrasena(contrasena)) {
            Toast.makeText(registroFragment.requireContext(), "La contrasena debe tener minimo 6 caracteres y al menos un numero", Toast.LENGTH_LONG).show();
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
            Toast.makeText(registroFragment.requireContext(), mensajeError, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void mostrarLoading(boolean mostrar) {
        if (!registroFragment.isAdded() || registroFragment.getView() == null) {
            return;
        }

        ProgressBar loading = registroFragment.requireView().findViewById(R.id.loading);
        View btnRegistro = registroFragment.requireView().findViewById(R.id.btn_registro);

        loading.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnRegistro.setEnabled(!mostrar);
    }

    private void prepararRegistroPendiente(RegistroPendienteResponse registro, String nombreUsuario, String correo) {
        PreferencesManager.setRegistroPendiente(registroFragment.requireContext(), true);
        PreferencesManager.setRegistroTransactionId(registroFragment.requireContext(), registro.getTransactionId());
        PreferencesManager.setRegistroCorreo(registroFragment.requireContext(), registro.getCorreo() != null ? registro.getCorreo() : correo);
        PreferencesManager.setRegistroNombreCuenta(registroFragment.requireContext(), registro.getNombreCuenta() != null ? registro.getNombreCuenta() : nombreUsuario);
        PreferencesManager.setUsuarioRegistrado(registroFragment.requireContext(), false);
        PreferencesManager.setSesionActiva(registroFragment.requireContext(), false);
    }

    private void abrirVerificacionCorreo(RegistroPendienteResponse registro, String nombreUsuario, String correo) {
        if (registro.getTransactionId() == null || registro.getTransactionId().trim().isEmpty()) {
            Toast.makeText(registroFragment.requireContext(), "No se pudo iniciar la verificación por correo", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString("transactionId", registro.getTransactionId());
        args.putString("channel", "EMAIL");
        args.putString("flowMode", "EMAIL_VERIFY");
        args.putString("nombreCuenta", registro.getNombreCuenta() != null ? registro.getNombreCuenta() : nombreUsuario);
        args.putString("correo", registro.getCorreo() != null ? registro.getCorreo() : correo);

        Toast.makeText(registroFragment.requireContext(), "Se envió un código de verificación al correo", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(registroFragment.requireView())
                .navigate(R.id.action_signUpFragment_to_twoFactorFragment, args);
    }
}