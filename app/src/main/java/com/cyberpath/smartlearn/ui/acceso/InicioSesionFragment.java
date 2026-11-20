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
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.logic.acceso.InicioSesionLogic;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

public class InicioSesionFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "InicioSesionFragment";

    private Button btnLogin, btnRegistro;
    private EditText etUsuario, etContrasena;

    private InicioSesionLogic inicioSesionLogic;
    private boolean biometriaSolicitada;
    private boolean verificacionRegistroSolicitada;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inicioSesionLogic = new InicioSesionLogic(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio_sesion, container, false);
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
        boolean sesionActiva = PreferencesManager.isSesionActiva(requireContext());

        if (PreferencesManager.isRegistroPendiente(requireContext()) && !verificacionRegistroSolicitada) {
            verificacionRegistroSolicitada = true;
            Bundle args = new Bundle();
            args.putString("transactionId", PreferencesManager.getRegistroTransactionId(requireContext()));
            args.putString("channel", "EMAIL");
            args.putString("flowMode", "EMAIL_VERIFY");
            args.putString("nombreCuenta", PreferencesManager.getRegistroNombreCuenta(requireContext()));
            args.putString("correo", PreferencesManager.getRegistroCorreo(requireContext()));
            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_twoFactorFragment, args);
            return;
        }

        if (sesionActiva) {
            Log.d(TAG, "Sesion activa detectada. Cargando usuario y redirigiendo a MainActivity.");

            int idUsuario = PreferencesManager.getIdUsuario(requireContext());
            if (idUsuario != -1) {
                UsuarioCst.asignarConstantesUsuario(requireContext(), idUsuario,
                        new UsuarioCst.UsuarioLoadCallback() {
                            @Override
                            public void onUsuarioLoaded(Usuario usuario) {
                                inicioSesionLogic.navegacionMainActivity();
                            }

                            @Override
                            public void onError(String mensaje) {

                                UsuarioCst.ensureUsuarioLoaded(requireContext());
                                inicioSesionLogic.navegacionMainActivity();
                            }
                        });
            } else {
                inicioSesionLogic.navegacionMainActivity();
            }
            return;
        }

        if (usuarioRegistrado && !biometriaSolicitada) {
            biometriaSolicitada = true;
            Log.d(TAG, "Usuario registrado encontrado. Intentando biometría...");
            view.post(() -> inicioSesionLogic.accesoBiometrico());
        } else {
            Log.d(TAG, "Primera vez o sin usuario registrado. Mostrando solo login manual.");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_registro) {
            Navigation.findNavController(v).navigate(R.id.RegistroFragment);
        } else if (v.getId() == R.id.btn_login) {
            String usuario = etUsuario.getText().toString().trim();
            String contrasena = etContrasena.getText().toString().trim();
            inicioSesionLogic.validarLoginManual(usuario, contrasena);
        }
    }
}