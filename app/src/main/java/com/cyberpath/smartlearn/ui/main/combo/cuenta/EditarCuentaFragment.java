package com.cyberpath.smartlearn.ui.main.combo.cuenta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.web.login.CambioPasswordDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarCuentaFragment extends Fragment {

    private Usuario usuarioActual;
    private NavController navController;
    private String tipoEdicion;
    private EditText etNuevoValor, etNuevoValorConfirmacion, etContrasenaActual;
    private TextView tvTitulo;
    private Button btnGuardar, btnCancelar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_cuenta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        tvTitulo = view.findViewById(R.id.tv_titulo_editar);
        etNuevoValor = view.findViewById(R.id.et_nuevo_valor);
        etNuevoValorConfirmacion = view.findViewById(R.id.et_nuevo_valor_confirmacion);
        etContrasenaActual = view.findViewById(R.id.et_contrasena_actual);
        btnGuardar = view.findViewById(R.id.btn_guardar_cambio);
        btnCancelar = view.findViewById(R.id.btn_cancelar);

        usuarioActual = UsuarioCst.USUARIO_ACTUAL;
        if (usuarioActual == null) {
            Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
            if (idUsuario == null) {
                Toast.makeText(getContext(), "No se pudo identificar al usuario", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_editarCuentaFragment_to_cuentaFragment);
                return;
            }
            cargarUsuarioActual(idUsuario);
        }

        tipoEdicion = getArguments() != null ? getArguments().getString("tipoEdicion", "nombre") : "nombre";

        configurarUI();

        btnCancelar.setOnClickListener(v -> navController.navigate(R.id.action_editarCuentaFragment_to_cuentaFragment));
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void cargarUsuarioActual(int idUsuario) {
        ApiService api = RetrofitClient.getApiService();
        api.getUsuarioById(idUsuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    usuarioActual = response.body();
                    UsuarioCst.USUARIO_ACTUAL = usuarioActual;
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(getContext(), "No se pudo cargar el usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarUI() {
        switch (tipoEdicion) {
            case "nombre":
                tvTitulo.setText("Cambiar Nombre de Usuario");
                etNuevoValor.setHint("Nuevo nombre");
                etNuevoValorConfirmacion.setVisibility(View.GONE);
                etContrasenaActual.setVisibility(View.GONE);
                break;
            case "correo":
                tvTitulo.setText("Cambiar Correo Electrónico");
                etNuevoValor.setHint("Nuevo correo");
                etNuevoValorConfirmacion.setVisibility(View.GONE);
                etContrasenaActual.setVisibility(View.GONE);
                break;
            case "contrasena":
                tvTitulo.setText("Cambiar Contraseña");
                etNuevoValor.setHint("Nueva contraseña");
                etNuevoValorConfirmacion.setHint("Confirmar nueva contraseña");
                etNuevoValorConfirmacion.setVisibility(View.VISIBLE);
                etContrasenaActual.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void guardarCambios() {
        if (usuarioActual == null || usuarioActual.getId() == null) {
            Toast.makeText(getContext(), "Usuario no disponible. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevoValor = etNuevoValor.getText().toString().trim();
        String confirmacion = etNuevoValorConfirmacion.getText().toString().trim();
        String contrasenaActual = etContrasenaActual.getText().toString().trim();

        if (nuevoValor.isEmpty()) {
            Toast.makeText(getContext(), "Completa el nuevo valor", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("contrasena".equals(tipoEdicion)) {
            if (contrasenaActual.isEmpty()) {
                Toast.makeText(getContext(), "Ingresa tu contraseña actual", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!nuevoValor.equals(confirmacion)) {
                Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            actualizarPassword(contrasenaActual, nuevoValor);
            return;
        }

        switch (tipoEdicion) {
            case "nombre":
                usuarioActual.setNombreCuenta(nuevoValor);
                break;
            case "correo":
                usuarioActual.setCorreo(nuevoValor);
                break;
        }

        actualizarUsuarioAPI(usuarioActual);
    }

    private void actualizarPassword(String passwordActual, String passwordNueva) {
        ApiService api = RetrofitClient.getApiService();
        CambioPasswordDto dto = new CambioPasswordDto(passwordActual, passwordNueva);
        api.updateUsuarioPassword(usuarioActual.getId(), dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                    navController.navigate(R.id.action_editarCuentaFragment_to_cuentaFragment);
                } else if (response.code() == 401) {
                    Toast.makeText(getContext(), "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar contraseña", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUsuarioAPI(Usuario usuario) {
        ApiService api = RetrofitClient.getApiService();
        Call<Usuario> call = api.updateUsuario(usuario.getId(), usuario);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        UsuarioCst.USUARIO_ACTUAL = response.body();
                    }
                    Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
                    navController.navigate(R.id.action_editarCuentaFragment_to_cuentaFragment);
                } else {
                    Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

}