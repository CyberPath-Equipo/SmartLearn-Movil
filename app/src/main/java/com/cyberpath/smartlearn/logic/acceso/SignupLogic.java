package com.cyberpath.smartlearn.logic.acceso;

import android.util.Log;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.acceso.SignUpFragment;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.validation.ValidationUtils;

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
            Log.e(TAG, "Validación de campos fallida");
            return;
        }

        Boolean accesibilidadVisual = obtenerSeleccionAccesibilidad(
                radioVisualActivaChecked,
                radioVisualInactivaChecked,
                "visual"
        );
        if (accesibilidadVisual == null) return;

        Boolean accesibilidadAuditiva = obtenerSeleccionAccesibilidad(
                radioAuditivaActivaChecked,
                radioAuditivaInactivaChecked,
                "auditiva"
        );
        if (accesibilidadAuditiva == null) return;

        Integer idRol = obtenerIdRol(radioAlumnoChecked, radioDocenteChecked);
        if (idRol == null) return;

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCuenta(nombreUsuario);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setContrasena(contrasena);
        nuevoUsuario.setNombreCompleto(nombreCompleto);
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setVerificado(false);
        nuevoUsuario.setCreadoEn("2024-01-01T00:00:00Z");
        nuevoUsuario.setActualizadoEn("2024-01-01T00:00:00Z");
        nuevoUsuario.setIdRol(idRol);

        Log.d(TAG, "Registrando usuario: " + nombreUsuario + ", correo: " + correo + ", rol: " + idRol);

        mostrarLoading(true);

        ApiService api = RetrofitClient.getApiService();
        api.registrarUsuario(nuevoUsuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuarioRegistrado = response.body();
                    int idUsuario = usuarioRegistrado.getId();
                    String nombreUsuarioGuardado = ValidationUtils.isTextoNoVacio(usuarioRegistrado.getNombreCuenta())
                            ? usuarioRegistrado.getNombreCuenta()
                            : nombreUsuario;
                    String nombreCompletoGuardado = ValidationUtils.isTextoNoVacio(usuarioRegistrado.getNombreCompleto())
                            ? usuarioRegistrado.getNombreCompleto()
                            : nombreCompleto;
                    String correoGuardado = ValidationUtils.isTextoNoVacio(usuarioRegistrado.getCorreo())
                            ? usuarioRegistrado.getCorreo()
                            : correo;

                    PreferencesManager.setUsuarioRegistrado(signUpFragment.requireContext(), true);
                    PreferencesManager.setIdUsuario(signUpFragment.requireContext(), idUsuario);
                    PreferencesManager.setNombreUsuario(signUpFragment.requireContext(), nombreUsuarioGuardado);
                    PreferencesManager.setNombreCompletoUsuario(signUpFragment.requireContext(), nombreCompletoGuardado);
                    PreferencesManager.setCorreoUsuario(signUpFragment.requireContext(), correoGuardado);
                    PreferencesManager.setAccesibilidadVisual(signUpFragment.requireContext(), accesibilidadVisual);
                    PreferencesManager.setAccesibilidadAuditivaActivada(signUpFragment.requireContext(), accesibilidadAuditiva);
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

    private boolean validarCampos(String nombreUsuario, String nombreCompleto, String contrasena, String correo) {
        if (!ValidationUtils.isTextoNoVacio(nombreUsuario)
                || !ValidationUtils.isTextoNoVacio(nombreCompleto)
                || !ValidationUtils.isTextoNoVacio(contrasena)
                || !ValidationUtils.isTextoNoVacio(correo)) {
            Toast.makeText(signUpFragment.requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!ValidationUtils.isNombreCompletoValido(nombreCompleto)) {
            Toast.makeText(signUpFragment.requireContext(), "El nombre completo no puede contener números", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!ValidationUtils.isCorreoValido(correo)) {
            Toast.makeText(signUpFragment.requireContext(), "Ingresa un correo con formato válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!ValidationUtils.isContrasenaValida(contrasena)) {
            Toast.makeText(signUpFragment.requireContext(), "La contraseña debe tener mínimo 6 caracteres y al menos un número", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Boolean obtenerSeleccionAccesibilidad(boolean radioActivaChecked, boolean radioInactivaChecked, String tipoAccesibilidad) {
        if (radioActivaChecked) {
            return true;
        } else if (radioInactivaChecked) {
            return false;
        } else {
            Toast.makeText(signUpFragment.requireContext(), "Selecciona la accesibilidad " + tipoAccesibilidad, Toast.LENGTH_SHORT).show();
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
        signUpFragment.actualizarEstadoCarga(mostrar);
    }
}