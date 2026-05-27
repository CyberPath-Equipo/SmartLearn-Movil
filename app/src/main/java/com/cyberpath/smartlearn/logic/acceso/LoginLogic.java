package com.cyberpath.smartlearn.logic.acceso;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.model.usuario.acceso.LoginResponse;
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
    private static final String TAG = "LoginLogic";

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
                            PreferencesManager.setSesionActiva(loginFragment.requireContext(), true);

                            UsuarioCst.asignarConstantesUsuario(loginFragment.requireContext(), idUsuario,
                                    new UsuarioCst.UsuarioLoadCallback() {
                                        @Override
                                        public void onUsuarioLoaded(Usuario usuario) {
                                            navegacionMainActivity();
                                        }

                                        @Override
                                        public void onError(String mensaje) {
                                            Log.e(TAG, "Error al cargar usuario tras biometría: " + mensaje);
                                        }
                                    });
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
        loginRequest.setTrustedDeviceToken(PreferencesManager.getTrustedDeviceToken(loginFragment.requireContext()));
        loginRequest.setDeviceInfo(buildDeviceInfo());

        api.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse lr = response.body();

                    if (Boolean.TRUE.equals(lr.getRequires2fa())) {
                        if (lr.getIdUsuario() != null) {
                            PreferencesManager.setIdUsuario(loginFragment.requireContext(), lr.getIdUsuario());
                        }
                        navegarTwoFactor(lr);
                        return;
                    }

                    String token = lr.getToken();
                    Integer idUsuarioObj = lr.getIdUsuario();
                    if (token == null || token.isEmpty() || idUsuarioObj == null) {
                        Toast.makeText(loginFragment.getContext(), "Respuesta inválida del servidor", Toast.LENGTH_LONG).show();
                        return;
                    }
                    int idUsuario = idUsuarioObj;

                    PreferencesManager.setToken(loginFragment.requireContext(), token);
                    if (lr.getTrustedDeviceToken() != null) {
                        PreferencesManager.setTrustedDeviceToken(loginFragment.requireContext(), lr.getTrustedDeviceToken());
                    }
                    PreferencesManager.setIdUsuario(loginFragment.requireContext(), idUsuario);
                    PreferencesManager.setUsuarioRegistrado(loginFragment.requireContext(), true);
                    PreferencesManager.setSesionActiva(loginFragment.requireContext(), true);

                    Log.d(TAG, "Login exitoso. ID de usuario guardado: " + idUsuario);
                    Toast.makeText(loginFragment.getContext(), "Login exitoso", Toast.LENGTH_SHORT).show();


                    UsuarioCst.asignarConstantesUsuario(loginFragment.requireContext(), idUsuario,
                            new UsuarioCst.UsuarioLoadCallback() {
                                @Override
                                public void onUsuarioLoaded(Usuario usuario) {
                                    navegacionMainActivity();
                                }

                                @Override
                                public void onError(String mensaje) {
                                    Log.e(TAG, "Error al cargar usuario: " + mensaje);
                                    Toast.makeText(loginFragment.getContext(), "Error al cargar datos de usuario", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else if (response.code() == 401) {
                    Toast.makeText(loginFragment.getContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(loginFragment.getContext(), "Error en el servidor: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(loginFragment.getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void navegacionMainActivity() {

        Intent intent = new Intent(loginFragment.requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginFragment.startActivity(intent);
    }

    private void navegarTwoFactor(LoginResponse loginResponse) {
        String transactionId = loginResponse.getTwoFactorTransactionId();
        if (transactionId == null || transactionId.trim().isEmpty()) {
            Toast.makeText(loginFragment.getContext(), "No se pudo iniciar la verificación 2FA", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString("transactionId", transactionId);
        args.putString("channel", loginResponse.getTwoFactorChannel());
        if (loginResponse.getIdUsuario() != null) {
            args.putInt("idUsuario", loginResponse.getIdUsuario());
        }
        args.putString("nombreCuenta", loginResponse.getNombreCuenta());

        Toast.makeText(loginFragment.getContext(), "Se requiere código de verificación", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(loginFragment.requireView())
                .navigate(R.id.action_loginFragment_to_twoFactorFragment, args);
    }

    private String buildDeviceInfo() {
        return Build.MANUFACTURER + " " + Build.MODEL + ";Android " + Build.VERSION.RELEASE;
    }
}