package com.cyberpath.smartlearn.util.logic.acceso;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.cyberpath.smartlearn.ui.acceso.LoginFragment;
import com.cyberpath.smartlearn.ui.main.MainActivity;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.util.concurrent.Executor;

public class validarAcceso {

    private final LoginFragment loginFragment;
    private final ActivityResultLauncher<String> biometricPermissionLauncher;

    public validarAcceso(LoginFragment fragment) {
        loginFragment = fragment;
        biometricPermissionLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        iniciarBiometria();
                    } else {
                        Toast.makeText(fragment.getContext(), "Permiso de biometría denegado. Usa login manual.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void verificarPermisoBiometria() {
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
                            Toast.makeText(loginFragment.getContext(),
                                    "Error al autenticar: " + errString,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(loginFragment.getContext(), "Autenticación biométrica exitosa", Toast.LENGTH_SHORT).show();

                        // Verificar y asignar usuario si exist e
                        int idUsuario = PreferencesManager.getIdUsuario(loginFragment.requireContext());
                        if (idUsuario != -1) {
                            UsuarioCst.asignarConstantesUsuario(idUsuario);
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
        loginFragment.realizarLoginManual(nombreCuenta, contrasena);
    }

    public void navegacionMainActivity() {
        Intent intent = new Intent(loginFragment.requireContext(), MainActivity.class);
        loginFragment.startActivity(intent);
    }
}