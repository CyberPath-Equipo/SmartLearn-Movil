package com.cyberpath.smartlearn.ui.main.combo.cuenta;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.logic.main.combo.cuenta.CuentaLogic;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class EditarCuentaFragment extends Fragment {

    private CuentaLogic cuentaLogic;
    private NavController navController;
    private String tipoEdicion;
    private TextInputLayout tilNuevoValor, tilConfirmacion, tilContrasena;
    private TextInputEditText etNuevoValor, etNuevoValorConfirmacion, etContrasenaActual;
    private RadioGroup rgEstadoAccesibilidad;
    private RadioButton radioEstadoActiva, radioEstadoInactiva;
    private TextView tvTitulo;
    private MaterialButton btnGuardar, btnCancelar;

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

        tilNuevoValor = view.findViewById(R.id.til_nuevo_valor);
        tilConfirmacion = view.findViewById(R.id.til_confirmacion);
        tilContrasena = view.findViewById(R.id.til_contrasena);

        etNuevoValor = view.findViewById(R.id.et_nuevo_valor);
        etNuevoValorConfirmacion = view.findViewById(R.id.et_nuevo_valor_confirmacion);
        etContrasenaActual = view.findViewById(R.id.et_contrasena_actual);

        rgEstadoAccesibilidad = view.findViewById(R.id.rg_estado_accesibilidad);
        radioEstadoActiva = view.findViewById(R.id.radio_estado_activa);
        radioEstadoInactiva = view.findViewById(R.id.radio_estado_inactiva);
        btnGuardar = view.findViewById(R.id.btn_guardar_cambio);
        btnCancelar = view.findViewById(R.id.btn_cancelar);

        if (getArguments() != null) {
            tipoEdicion = getArguments().getString("tipoEdicion", "nombre");
        }

        configurarUI();

        btnCancelar.setOnClickListener(v -> navegarAtras());

        btnGuardar.setOnClickListener(v -> {
            String nuevoValor;
            if (esEdicionAccesibilidad()) {
                nuevoValor = radioEstadoActiva.isChecked() ? "activa" : radioEstadoInactiva.isChecked() ? "inactiva" : "";
            } else {
                nuevoValor = Objects.requireNonNull(etNuevoValor.getText()).toString().trim();
            }
            String confirmacion = tilConfirmacion.getVisibility() == View.VISIBLE
                    ? Objects.requireNonNull(etNuevoValorConfirmacion.getText()).toString().trim() : "";
            String contrasenaActual = Objects.requireNonNull(etContrasenaActual.getText()).toString().trim();

            cuentaLogic.validarYGuardarCambios(tipoEdicion, nuevoValor, confirmacion, contrasenaActual);
        });
    }

    private void configurarUI() {
        rgEstadoAccesibilidad.setVisibility(View.GONE);
        tilNuevoValor.setVisibility(View.VISIBLE);
        tilConfirmacion.setVisibility(View.GONE);
        tilContrasena.setVisibility(View.VISIBLE);

        etNuevoValor.setInputType(InputType.TYPE_CLASS_TEXT);
        etNuevoValor.setText("");
        etNuevoValorConfirmacion.setText("");
        etContrasenaActual.setText("");

        switch (tipoEdicion) {
            case "nombre":
                tvTitulo.setText("Cambiar Nombre de Usuario");
                tilNuevoValor.setHint("Nuevo nombre");
                tilConfirmacion.setVisibility(View.GONE);
                etNuevoValor.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "nombreCompleto":
                tvTitulo.setText("Cambiar Nombre Completo");
                tilNuevoValor.setHint("Nuevo nombre completo");
                tilConfirmacion.setVisibility(View.GONE);
                etNuevoValor.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "correo":
                tvTitulo.setText("Cambiar Correo Electrónico");
                tilNuevoValor.setHint("Nuevo correo");
                etNuevoValor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                tilConfirmacion.setVisibility(View.GONE);
                break;
            case "contrasena":
                tvTitulo.setText("Cambiar Contraseña");
                tilNuevoValor.setHint("Nueva contraseña");
                tilConfirmacion.setHint("Confirmar nueva contraseña");
                etNuevoValor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                etNuevoValorConfirmacion.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                tilConfirmacion.setVisibility(View.VISIBLE);
                break;
            case "accesibilidadVisual":
                tvTitulo.setText("Accesibilidad Visual");
                tilNuevoValor.setVisibility(View.GONE);
                tilConfirmacion.setVisibility(View.GONE);
                tilContrasena.setVisibility(View.GONE);
                rgEstadoAccesibilidad.setVisibility(View.VISIBLE);
                radioEstadoActiva.setChecked(PreferencesManager.isAccesibilidadVisualActivada(requireContext()));
                radioEstadoInactiva.setChecked(!PreferencesManager.isAccesibilidadVisualActivada(requireContext()));
                break;
            case "accesibilidadAuditiva":
                tvTitulo.setText("Accesibilidad Auditiva");
                tilNuevoValor.setVisibility(View.GONE);
                tilConfirmacion.setVisibility(View.GONE);
                tilContrasena.setVisibility(View.GONE);
                rgEstadoAccesibilidad.setVisibility(View.VISIBLE);
                radioEstadoActiva.setChecked(PreferencesManager.isAccesibilidadAuditivaActivada(requireContext()));
                radioEstadoInactiva.setChecked(!PreferencesManager.isAccesibilidadAuditivaActivada(requireContext()));
                break;
            default:

                tilNuevoValor.setVisibility(View.VISIBLE);
                tilConfirmacion.setVisibility(View.GONE);
                tilContrasena.setVisibility(View.VISIBLE);
                break;
        }
    }

    private boolean esEdicionAccesibilidad() {
        return "accesibilidadVisual".equals(tipoEdicion) || "accesibilidadAuditiva".equals(tipoEdicion);
    }

    private void navegarAtras() {
        if (navController != null) {
            navController.navigate(R.id.action_editarCuentaFragment_to_cuentaFragment);
        }
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