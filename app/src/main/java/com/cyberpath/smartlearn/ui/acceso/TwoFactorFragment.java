package com.cyberpath.smartlearn.ui.acceso;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.acceso.LoginResponse;
import com.cyberpath.smartlearn.data.model.usuario.autenticacion.TwoFactorConfirmSetupRequest;
import com.cyberpath.smartlearn.data.model.usuario.autenticacion.TwoFactorResendRequest;
import com.cyberpath.smartlearn.data.model.usuario.autenticacion.TwoFactorVerifyRequest;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.MainActivity;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TwoFactorFragment extends Fragment implements View.OnClickListener {
    private static final String MODE_LOGIN = "LOGIN";
    private static final String MODE_SETUP = "SETUP";
    private static final String MODE_EMAIL_VERIFY = "EMAIL_VERIFY";

    private EditText etCode;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvSetupHelp;
    private TextView tvProvisioningUri;
    private CheckBox cbRememberDevice;
    private Button btnVerify;
    private Button btnResend;

    private String transactionId;
    private String channel;
    private String flowMode = MODE_LOGIN;
    private String provisioningUri = "";
    private String tempSecret = "";
    private String correo = "";
    private String nombreCuenta = "";
    private int idUsuario = -1;

    private ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RetrofitClient.getApiService();

        Bundle args = getArguments();
        if (args != null) {
            transactionId = args.getString("transactionId", "");
            channel = args.getString("channel", "");
            idUsuario = args.getInt("idUsuario", -1);
            flowMode = args.getString("flowMode", MODE_LOGIN);
            provisioningUri = args.getString("provisioningUri", "");
            tempSecret = args.getString("tempSecret", "");
            correo = args.getString("correo", "");
            nombreCuenta = args.getString("nombreCuenta", "");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_two_factor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (transactionId == null || transactionId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Transacción 2FA no válida. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show();
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
            return;
        }

        tvTitle = view.findViewById(R.id.tv_two_factor_title);
        etCode = view.findViewById(R.id.et_two_factor_code);
        tvSubtitle = view.findViewById(R.id.tv_two_factor_subtitle);
        tvSetupHelp = view.findViewById(R.id.tv_two_factor_setup_help);
        tvProvisioningUri = view.findViewById(R.id.tv_two_factor_provisioning_uri);
        cbRememberDevice = view.findViewById(R.id.cb_remember_device);
        btnVerify = view.findViewById(R.id.btn_verify_code);
        btnResend = view.findViewById(R.id.btn_resend_code);

        btnVerify.setOnClickListener(this);
        btnResend.setOnClickListener(this);

        if (isSetupMode()) {
            tvTitle.setText(R.string.two_factor_setup_title);
            tvSubtitle.setText(R.string.two_factor_setup_subtitle);
            tvSetupHelp.setVisibility(View.VISIBLE);
            tvProvisioningUri.setVisibility(View.VISIBLE);
            tvProvisioningUri.setText(getProvisioningUriText());
            cbRememberDevice.setVisibility(View.GONE);
            btnResend.setVisibility(View.GONE);
            btnVerify.setText(R.string.two_factor_confirm_setup_button);
        } else if (isEmailVerifyMode()) {
            tvTitle.setText(R.string.two_factor_email_title);
            tvSubtitle.setText(R.string.two_factor_email_subtitle);
            tvSetupHelp.setVisibility(View.VISIBLE);
            tvSetupHelp.setText(getString(R.string.two_factor_email_help, obtenerCorreoMostrable()));
            tvProvisioningUri.setVisibility(View.GONE);
            cbRememberDevice.setVisibility(View.GONE);
            btnResend.setVisibility(View.VISIBLE);
            btnVerify.setText(R.string.two_factor_email_verify_button);
        } else if ("SMS".equalsIgnoreCase(channel)) {
            tvTitle.setText(R.string.two_factor_title);
            tvSubtitle.setText(R.string.two_factor_sms_subtitle);
            tvSetupHelp.setVisibility(View.GONE);
            tvProvisioningUri.setVisibility(View.GONE);
            cbRememberDevice.setVisibility(View.VISIBLE);
            btnResend.setVisibility(View.VISIBLE);
            btnVerify.setText(R.string.two_factor_verify_button);
        } else {
            tvTitle.setText(R.string.two_factor_title);
            tvSubtitle.setText(R.string.two_factor_totp_subtitle);
            tvSetupHelp.setVisibility(View.GONE);
            tvProvisioningUri.setVisibility(View.GONE);
            cbRememberDevice.setVisibility(View.VISIBLE);
            btnResend.setVisibility(View.GONE);
            btnVerify.setText(R.string.two_factor_verify_button);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_verify_code) {
            verifyCode();
        } else if (v.getId() == R.id.btn_resend_code) {
            resendCode();
        }
    }

    private void verifyCode() {
        String code = etCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa el código de verificación", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerify.setEnabled(false);

        if (isSetupMode()) {
            TwoFactorConfirmSetupRequest request = new TwoFactorConfirmSetupRequest();
            request.setTransactionId(transactionId);
            request.setCode(code);
            request.setTempSecret(tempSecret);

            apiService.confirmTwoFactorSetup(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    btnVerify.setEnabled(true);
                    if (response.isSuccessful()) {
                        completeSetup();
                    } else {
                        Toast.makeText(requireContext(), "No se pudo confirmar la configuración 2FA", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnVerify.setEnabled(true);
                    Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        TwoFactorVerifyRequest request = new TwoFactorVerifyRequest();
        request.setTransactionId(transactionId);
        request.setCode(code);
        request.setRememberDevice(cbRememberDevice.isChecked());
        request.setDeviceInfo(android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + ";Android " + android.os.Build.VERSION.RELEASE);

        apiService.verifyTwoFactor(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnVerify.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.getToken() == null || loginResponse.getToken().isEmpty()) {
                        Toast.makeText(requireContext(), "Respuesta inválida al verificar 2FA", Toast.LENGTH_LONG).show();
                        return;
                    }

                    completeLogin(loginResponse);
                    return;
                }

                Toast.makeText(requireContext(), "Código inválido o expirado", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnVerify.setEnabled(true);
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendCode() {
        if (!"SMS".equalsIgnoreCase(channel) || isSetupMode()) {
            return;
        }

        TwoFactorResendRequest request = new TwoFactorResendRequest();
        request.setTransactionId(transactionId);

        btnResend.setEnabled(false);
        apiService.resendTwoFactorCode(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnResend.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Código reenviado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "No se pudo reenviar el código", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnResend.setEnabled(true);
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void completeLogin(LoginResponse loginResponse) {
        int userId = loginResponse.getIdUsuario() != null ? loginResponse.getIdUsuario() : idUsuario;
        if (userId <= 0) {
            Toast.makeText(requireContext(), "No se recibió ID de usuario", Toast.LENGTH_LONG).show();
            return;
        }

        PreferencesManager.setToken(requireContext(), loginResponse.getToken());
        if (loginResponse.getTrustedDeviceToken() != null) {
            PreferencesManager.setTrustedDeviceToken(requireContext(), loginResponse.getTrustedDeviceToken());
        }
        PreferencesManager.setIdUsuario(requireContext(), userId);
        PreferencesManager.setUsuarioRegistrado(requireContext(), true);
        PreferencesManager.setSesionActiva(requireContext(), true);

        UsuarioCst.asignarConstantesUsuario(requireContext(), userId, new UsuarioCst.UsuarioLoadCallback() {
            @Override
            public void onUsuarioLoaded(Usuario usuario) {
                navigateToMain();
            }

            @Override
            public void onError(String mensaje) {
                navigateToMain();
            }
        });
    }

    private void completeSetup() {
        PreferencesManager.setSesionActiva(requireContext(), true);

        int userId = idUsuario > 0 ? idUsuario : PreferencesManager.getIdUsuario(requireContext());
        if (userId > 0) {
            UsuarioCst.asignarConstantesUsuario(requireContext(), userId, new UsuarioCst.UsuarioLoadCallback() {
                @Override
                public void onUsuarioLoaded(Usuario usuario) {
                    navigateToMain();
                }

                @Override
                public void onError(String mensaje) {
                    navigateToMain();
                }
            });
        } else {
            navigateToMain();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean isSetupMode() {
        return MODE_SETUP.equalsIgnoreCase(flowMode);
    }

    private boolean isEmailVerifyMode() {
        return MODE_EMAIL_VERIFY.equalsIgnoreCase(flowMode);
    }

    private String getProvisioningUriText() {
        if (provisioningUri == null || provisioningUri.trim().isEmpty()) {
            return getString(R.string.two_factor_setup_uri_label) + "\n" + getString(R.string.two_factor_setup_uri_not_available);
        }

        return getString(R.string.two_factor_setup_uri_label) + "\n" + provisioningUri;
    }

    private String obtenerCorreoMostrable() {
        if (correo != null && !correo.trim().isEmpty()) {
            return correo;
        }
        return PreferencesManager.getRegistroCorreo(requireContext());
    }
}

