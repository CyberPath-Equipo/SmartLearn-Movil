package com.cyberpath.smartlearn.logic.acceso;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.acceso.LoginFragment;
import com.cyberpath.smartlearn.ui.main.MainActivity;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginLogic {
    private static final String TAG = "LogicLogic";

    private final LoginFragment loginFragment;
    private final ActivityResultLauncher<String> biometricPermissionLauncher;

    public LoginLogic(LoginFragment fragment) {
        this.loginFragment = fragment;
        this.biometricPermissionLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        iniciarBiometria();
                    } else {
                        Toast.makeText(fragment.getContext(), "Permiso de biometría denegado. Usa login manual.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void accesoBiometrico() {
        if (!PreferencesManager.isUsuarioRegistrado(loginFragment.requireContext())) {
            Toast.makeText(loginFragment.getContext(), "No hay usuario registrado para biometría.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(loginFragment.requireContext(),
                    Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED) {
                iniciarBiometria();
            } else {
                biometricPermissionLauncher.launch(Manifest.permission.USE_BIOMETRIC);
            }
        } else {
            Toast.makeText(loginFragment.getContext(), "Biometría no soportada en este dispositivo. Usa login manual.", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarBiometria() {
        mostrarAccesoBiometrico();
    }

    private void mostrarAccesoBiometrico() {
        Executor executor = ContextCompat.getMainExecutor(loginFragment.requireContext());

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                loginFragment.requireActivity(),
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                            Log.d(TAG, "Error al autenticar");
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(loginFragment.getContext(), "Autenticación biométrica exitosa", Toast.LENGTH_SHORT).show();

                        int idUsuario = PreferencesManager.getIdUsuario(loginFragment.requireContext());
                        if (idUsuario != -1) {
                            UsuarioCst.asignarConstantesUsuario(loginFragment.requireContext(), idUsuario);
                            navegacionMainActivity();
                        } else {
                            Toast.makeText(loginFragment.getContext(), "Error: No hay usuario guardado para biometría. Usa login manual.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(loginFragment.getContext(),
                                "Autenticación biométrica fallida. Intenta de nuevo o usa login manual.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acceso Biométrico")
                .setSubtitle("Usa tu huella digital para acceder")
                .setNegativeButtonText("Cancelar")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    public void validarLoginManual(String nombreCuenta, String contrasena) {
        if (nombreCuenta.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(loginFragment.getContext(), "Por favor, ingresa usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        realizarLoginManual(nombreCuenta, contrasena);
    }

    private void realizarLoginManual(String nombreCuenta, String contrasena) {
        ApiService api = RetrofitClient.getApiService();
        Usuario loginRequest = new Usuario();
        loginRequest.setNombreCuenta(nombreCuenta);
        loginRequest.setContrasena(contrasena);

        api.login(loginRequest).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuarioLogueado = response.body();
                    int idUsuario = usuarioLogueado.getId();

                    PreferencesManager.setUsuarioRegistrado(loginFragment.requireContext(), true);

                    UsuarioCst.asignarConstantesUsuario(loginFragment.requireContext(), idUsuario);

                    Log.d(TAG, "Login exitoso. ID de usuario guardado: " + idUsuario);
                    Toast.makeText(loginFragment.getContext(), "Login exitoso", Toast.LENGTH_SHORT).show();
                    navegacionMainActivity();
                } else if (response.code() == 401) {
                    Toast.makeText(loginFragment.getContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(loginFragment.getContext(), "Error en el servidor: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(loginFragment.getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void navegacionMainActivity() {
        //Log.d(TAG, "Usuario " + UsuarioCst.USUARIO_ACTUAL.getId());
        Intent intent = new Intent(loginFragment.requireContext(), MainActivity.class);
        loginFragment.startActivity(intent);
    }
}