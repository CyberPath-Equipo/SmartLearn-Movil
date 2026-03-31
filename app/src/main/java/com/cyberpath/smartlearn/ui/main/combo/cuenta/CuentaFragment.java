package com.cyberpath.smartlearn.ui.main.combo.cuenta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CuentaFragment extends Fragment {

    private Usuario usuarioActual;
    private NavController navController;
    private TextView textoNombre, textoCorreo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuenta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        textoNombre = view.findViewById(R.id.tv_nombre_actual);
        textoCorreo = view.findViewById(R.id.tv_correo_actual);

        usuarioActual = UsuarioCst.USUARIO_ACTUAL;
        if (usuarioActual != null) {
            bindUsuario(usuarioActual);
        } else {
            textoNombre.setText("Cargando...");
            textoCorreo.setText("Cargando...");
            Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
            if (idUsuario != null) {
                cargarUsuario(idUsuario);
            } else {
                textoNombre.setText("No disponible");
                textoCorreo.setText("No disponible");
            }
        }

        LinearLayout btnCambiarNombre = view.findViewById(R.id.btn_cambiar_nombre);
        btnCambiarNombre.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("tipoEdicion", "nombre");
            navController.navigate(R.id.action_cuentaFragment_to_editarCuentaFragment, args);
        });

        LinearLayout btnCambiarCorreo = view.findViewById(R.id.btn_cambiar_correo);
        btnCambiarCorreo.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("tipoEdicion", "correo");
            navController.navigate(R.id.action_cuentaFragment_to_editarCuentaFragment, args);
        });

        LinearLayout btnCambiarContrasena = view.findViewById(R.id.btn_cambiar_contrasena);
        btnCambiarContrasena.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("tipoEdicion", "contrasena");
            navController.navigate(R.id.action_cuentaFragment_to_editarCuentaFragment, args);
        });
    }

    private void cargarUsuario(int idUsuario) {
        ApiService api = RetrofitClient.getApiService();
        api.getUsuarioById(idUsuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    usuarioActual = response.body();
                    UsuarioCst.USUARIO_ACTUAL = usuarioActual;
                    bindUsuario(usuarioActual);
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                textoNombre.setText("No disponible");
                textoCorreo.setText("No disponible");
            }
        });
    }

    private void bindUsuario(Usuario usuario) {
        textoNombre.setText(usuario.getNombreCuenta());
        textoCorreo.setText(usuario.getCorreo());
    }
}