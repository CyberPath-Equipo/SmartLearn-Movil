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
import com.cyberpath.smartlearn.logic.main.combo.cuenta.CuentaLogic;

public class EditarCuentaFragment extends Fragment {

    private CuentaLogic cuentaLogic;
    private NavController navController;
    private String tipoEdicion;
    private EditText etNuevoValor, etNuevoValorConfirmacion, etContrasenaActual;
    private TextView tvTitulo;
    private Button btnGuardar, btnCancelar;

    public EditarCuentaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_cuenta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        cuentaLogic = new CuentaLogic(this);

        tvTitulo = view.findViewById(R.id.tv_titulo_editar);
        etNuevoValor = view.findViewById(R.id.et_nuevo_valor);
        etNuevoValorConfirmacion = view.findViewById(R.id.et_nuevo_valor_confirmacion);
        etContrasenaActual = view.findViewById(R.id.et_contrasena_actual);
        btnGuardar = view.findViewById(R.id.btn_guardar_cambio);
        btnCancelar = view.findViewById(R.id.btn_cancelar);

        if (getArguments() != null) {
            tipoEdicion = getArguments().getString("tipoEdicion", "nombre");
        }

        configurarUI();

        btnCancelar.setOnClickListener(v -> navegarAtras());

        btnGuardar.setOnClickListener(v -> {
            String nuevoValor = etNuevoValor.getText().toString().trim();
            String confirmacion = etNuevoValorConfirmacion.getText().toString().trim();
            String contrasenaActual = etContrasenaActual.getText().toString().trim();

            cuentaLogic.validarYGuardarCambios(tipoEdicion, nuevoValor, confirmacion, contrasenaActual);
        });
    }

    private void configurarUI() {
        switch (tipoEdicion) {
            case "nombre":
                tvTitulo.setText("Cambiar Nombre de Usuario");
                etNuevoValor.setHint("Nuevo nombre");
                etNuevoValorConfirmacion.setVisibility(View.GONE);
                break;
            case "correo":
                tvTitulo.setText("Cambiar Correo Electrónico");
                etNuevoValor.setHint("Nuevo correo");
                etNuevoValorConfirmacion.setVisibility(View.GONE);
                break;
            case "contrasena":
                tvTitulo.setText("Cambiar Contraseña");
                etNuevoValor.setHint("Nueva contraseña");
                etNuevoValorConfirmacion.setHint("Confirmar nueva contraseña");
                etNuevoValorConfirmacion.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void navegarAtras() {
        navController.navigate(R.id.action_editarCuentaFragment_to_cuentaFragment);
    }

    public void showToast(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    public void showToastLong(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        }
    }

    public void onCambiosGuardados() {
        Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
        navegarAtras();
    }

    public void onErrorGuardar() {
        Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
    }

    public void onErrorConexion() {
        Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
    }

    public void navigateToCuentaFragment() {
        if (navController != null) {
            navController.navigate(R.id.action_editarCuentaFragment_to_cuentaFragment);
        }
    }
}