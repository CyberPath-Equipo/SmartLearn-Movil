package com.cyberpath.smartlearn.logic.acceso;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import com.cyberpath.smartlearn.ui.acceso.InicioSesionFragment;
import com.cyberpath.smartlearn.ui.main.MainActivity;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioSesionLogic {
    private static final String TAG = "LoginLogic";

    private final InicioSesionFragment inicioSesionFragment;
    private final ActivityResultLauncher<String> biometricPermissionLauncher;

    public InicioSesionLogic(InicioSesionFragment fragment) {
        this.inicioSesionFragment = fragment;
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
        if (!PreferencesManager.isUsuarioRegistrado(inicioSesionFragment.requireContext())) {
            Toast.makeText(inicioSesionFragment.getContext(), "No hay usuario registrado para biometría.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(inicioSesionFragment.requireContext(),
                    Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED) {
                iniciarBiometria();
            } else {
                biometricPermissionLauncher.launch(Manifest.permission.USE_BIOMETRIC);
            }
        } else {
            Toast.makeText(inicioSesionFragment.getContext(), "Biometría no soportada en este dispositivo. Usa login manual.", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarBiometria() {
        mostrarAccesoBiometrico();
    }

    private void mostrarAccesoBiometrico() {
        Executor executor = ContextCompat.getMainExecutor(inicioSesionFragment.requireContext());

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                inicioSesionFragment.requireActivity(),
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
                        Toast.makeText(inicioSesionFragment.getContext(), "Autenticación biométrica exitosa", Toast.LENGTH_SHORT).show();

                        int idUsuario = PreferencesManager.getIdUsuario(inicioSesionFragment.requireContext());
                        if (idUsuario != -1) {
                            PreferencesManager.setSesionActiva(inicioSesionFragment.requireContext(), true);

                            UsuarioCst.asignarConstantesUsuario(inicioSesionFragment.requireContext(), idUsuario,
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
                            Toast.makeText(inicioSesionFragment.getContext(), "Error: No hay usuario guardado para biometría. Usa login manual.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(inicioSesionFragment.getContext(),
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
            Toast.makeText(inicioSesionFragment.getContext(), "Por favor, ingresa usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        mostrarLoading(true);
        realizarLoginManual(nombreCuenta, contrasena);
    }

    private void realizarLoginManual(String nombreCuenta, String contrasena) {
        ApiService api = RetrofitClient.getApiService();
        Usuario loginRequest = new Usuario();
        loginRequest.setNombreCuenta(nombreCuenta);
        loginRequest.setContrasena(contrasena);
        loginRequest.setTrustedDeviceToken(PreferencesManager.getTrustedDeviceToken(inicioSesionFragment.requireContext()));
        loginRequest.setDeviceInfo(buildDeviceInfo());

        api.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse lr = response.body();

                    if (Boolean.TRUE.equals(lr.getRequires2fa())) {
                        if (lr.getIdUsuario() != null) {
                            PreferencesManager.setIdUsuario(inicioSesionFragment.requireContext(), lr.getIdUsuario());
                        }
                        navegarTwoFactor(lr);
                        return;
                    }

                    String token = lr.getToken();
                    Integer idUsuarioObj = lr.getIdUsuario();
                    if (token == null || token.isEmpty() || idUsuarioObj == null) {
                        Toast.makeText(inicioSesionFragment.getContext(), "Respuesta inválida del servidor", Toast.LENGTH_LONG).show();
                        return;
                    }
                    int idUsuario = idUsuarioObj;

                    PreferencesManager.setToken(inicioSesionFragment.requireContext(), token);
                    if (lr.getTrustedDeviceToken() != null) {
                        PreferencesManager.setTrustedDeviceToken(inicioSesionFragment.requireContext(), lr.getTrustedDeviceToken());
                    }
                    PreferencesManager.setIdUsuario(inicioSesionFragment.requireContext(), idUsuario);
                    PreferencesManager.setUsuarioRegistrado(inicioSesionFragment.requireContext(), true);
                    PreferencesManager.setSesionActiva(inicioSesionFragment.requireContext(), true);

                    Log.d(TAG, "Login exitoso. ID de usuario guardado: " + idUsuario);
                    Toast.makeText(inicioSesionFragment.getContext(), "Login exitoso", Toast.LENGTH_SHORT).show();


                    UsuarioCst.asignarConstantesUsuario(inicioSesionFragment.requireContext(), idUsuario,
                            new UsuarioCst.UsuarioLoadCallback() {
                                @Override
                                public void onUsuarioLoaded(Usuario usuario) {
                                    navegacionMainActivity();
                                }

                                @Override
                                public void onError(String mensaje) {
                                    Log.e(TAG, "Error al cargar usuario: " + mensaje);
                                    Toast.makeText(inicioSesionFragment.getContext(), "Error al cargar datos de usuario", Toast.LENGTH_SHORT).show();
                                    mostrarLoading(false);
                                }
                            });
                } else if (response.code() == 401) {
                    Toast.makeText(inicioSesionFragment.getContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    mostrarLoading(false);
                } else {
                    Toast.makeText(inicioSesionFragment.getContext(), "Error en el servidor: " + response.message(), Toast.LENGTH_SHORT).show();
                    mostrarLoading(false);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(inicioSesionFragment.getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void navegacionMainActivity() {
        Intent intent = new Intent(inicioSesionFragment.requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        inicioSesionFragment.startActivity(intent);
        mostrarLoading(false);
    }

    private void navegarTwoFactor(LoginResponse loginResponse) {
        String transactionId = loginResponse.getTwoFactorTransactionId();
        if (transactionId == null || transactionId.trim().isEmpty()) {
            Toast.makeText(inicioSesionFragment.getContext(), "No se pudo iniciar la verificación 2FA", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString("transactionId", transactionId);
        args.putString("channel", loginResponse.getTwoFactorChannel());
        if (loginResponse.getIdUsuario() != null) {
            args.putInt("idUsuario", loginResponse.getIdUsuario());
        }
        args.putString("nombreCuenta", loginResponse.getNombreCuenta());

        Toast.makeText(inicioSesionFragment.getContext(), "Se requiere código de verificación", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(inicioSesionFragment.requireView())
                .navigate(R.id.action_loginFragment_to_twoFactorFragment, args);
    }

    private boolean validarCampos(String nombreUsuario, String contrasena) {
        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(inicioSesionFragment.requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void mostrarLoading(boolean mostrar) {
        if (!inicioSesionFragment.isAdded() || inicioSesionFragment.getView() == null) {
            return;
        }

        ProgressBar loading = inicioSesionFragment.requireView().findViewById(R.id.loading);
        View btnRegistro = inicioSesionFragment.requireView().findViewById(R.id.btn_registro);

        loading.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnRegistro.setEnabled(!mostrar);
    }

    private String buildDeviceInfo() {
        return Build.MANUFACTURER + " " + Build.MODEL + ";Android " + Build.VERSION.RELEASE;
    }
}