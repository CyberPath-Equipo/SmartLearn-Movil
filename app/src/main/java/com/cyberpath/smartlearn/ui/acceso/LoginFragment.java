package com.cyberpath.smartlearn.ui.acceso;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.logic.acceso.validarAcceso;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private Button btnLogin, btnRegistro;
    private EditText etUsuario, etContrasena;
    private validarAcceso validarAcceso;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        validarAcceso = new validarAcceso(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Verificar si ya hay usuario logueado y asignar constantes
        int idUsuarioActual = PreferencesManager.getIdUsuario(requireContext());
        if (idUsuarioActual != -1) {
            UsuarioCst.asignarConstantesUsuario(idUsuarioActual);
            // Si ya hay usuario, podrías navegar directamente a MainActivity, pero por ahora mostramos login
        }

        btnLogin = view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        btnRegistro = view.findViewById(R.id.btn_registro);
        btnRegistro.setOnClickListener(this);

        etUsuario = view.findViewById(R.id.et_nombre_usuario);
        etContrasena = view.findViewById(R.id.et_contrasena);

        // Solo intentar biometría si el usuario está registrado
        boolean usuarioRegistrado = PreferencesManager.isUsuarioRegistrado(requireContext());
        if (usuarioRegistrado) {
            Log.d("LoginFragment", "Usuario registrado encontrado. Intentando biometría...");
            view.post(() -> validarAcceso.verificarPermisoBiometria());
        } else {
            Log.d("LoginFragment", "Primera vez o sin usuario registrado. Mostrando solo login manual.");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_registro) {
            Navigation.findNavController(v).navigate(R.id.signUpFragment);
        } else if (v.getId() == R.id.btn_login) {
            String usuario = etUsuario.getText().toString().trim();
            String contrasena = etContrasena.getText().toString().trim();
            validarAcceso.validarLoginManual(usuario, contrasena);
        }
    }

    public void realizarLoginManual(String nombreCuenta, String contrasena) {
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

                    // Guardar en preferencias: marcar como registrado y guardar ID
                    PreferencesManager.setUsuarioRegistrado(requireContext(), true);
                    PreferencesManager.setIdUsuario(requireContext(), idUsuario);

                    // Asignar constantes
                    UsuarioCst.asignarConstantesUsuario(idUsuario);

                    Log.d("LoginFragment", "Login exitoso. ID de usuario guardado: " + idUsuario);
                    Toast.makeText(getContext(), "Login exitoso", Toast.LENGTH_SHORT).show();
                    validarAcceso.navegacionMainActivity();
                } else if (response.code() == 401) {
                    Toast.makeText(getContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error en el servidor: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}