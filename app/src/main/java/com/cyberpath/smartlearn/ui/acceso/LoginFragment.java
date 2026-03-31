package com.cyberpath.smartlearn.ui.acceso;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.logic.acceso.LoginLogic;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LoginFragment";
    private static final String KEY_BIOMETRIA_AUTO_SOLICITADA = "biometria_auto_solicitada";

    private Button btnLogin, btnRegistro;
    private EditText etUsuario, etContrasena;

    private LoginLogic loginLogic;
    private boolean biometriaAutoSolicitada = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            biometriaAutoSolicitada = savedInstanceState.getBoolean(KEY_BIOMETRIA_AUTO_SOLICITADA, false);
        }
        loginLogic = new LoginLogic(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogin = view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        btnRegistro = view.findViewById(R.id.btn_registro);
        btnRegistro.setOnClickListener(this);

        etUsuario = view.findViewById(R.id.et_nombre_usuario);
        etContrasena = view.findViewById(R.id.et_contrasena);

        boolean usuarioRegistrado = PreferencesManager.isUsuarioRegistrado(requireContext());
        if (usuarioRegistrado && !biometriaAutoSolicitada) {
            biometriaAutoSolicitada = true;
            Log.d(TAG, "Usuario registrado encontrado. Intentando biometría...");
            view.post(() -> loginLogic.accesoBiometrico());
        } else {
            Log.d(TAG, "Primera vez o sin usuario registrado. Mostrando solo login manual.");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_BIOMETRIA_AUTO_SOLICITADA, biometriaAutoSolicitada);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_registro) {
            Navigation.findNavController(v).navigate(R.id.signUpFragment);
        } else if (v.getId() == R.id.btn_login) {
            String usuario = etUsuario.getText().toString().trim();
            String contrasena = etContrasena.getText().toString().trim();
            loginLogic.validarLoginManual(usuario, contrasena);
        }
    }
}