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
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;

public class CuentaFragment extends Fragment {

    private final Usuario usuarioActual = UsuarioCst.USUARIO_ACTUAL;
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
        textoNombre.setText(usuarioActual.getNombreCuenta());
        textoCorreo = view.findViewById(R.id.tv_correo_actual);
        textoCorreo.setText(usuarioActual.getCorreo());

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

        // view.findViewById(R.id.btn_eliminar_cuenta).setOnClickListener(v -> { /* Lógica para eliminar */ });
    }
}