package com.cyberpath.smartlearn.ui.main.combo.cuenta;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.logic.main.combo.cuenta.CuentaLogic;
import com.cyberpath.smartlearn.ui.acceso.AccesoActivity;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.preferences.ThemeManager;

public class CuentaFragment extends Fragment {

    private CuentaLogic cuentaLogic;
    private NavController navController;
    private TextView textoNombre, textoNombreCompleto, textoCorreo;
    private RadioGroup grupoAccesibilidadVisual;
    private RadioGroup grupoAccesibilidadAuditiva;

    public CuentaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuenta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        cuentaLogic = new CuentaLogic(this);

        textoNombre = view.findViewById(R.id.tv_nombre_actual);
        textoNombreCompleto = view.findViewById(R.id.tv_nombre_completo_actual);
        textoCorreo = view.findViewById(R.id.tv_correo_actual);
        grupoAccesibilidadVisual = view.findViewById(R.id.grupo_accesibilidad_visual_cuenta);
        grupoAccesibilidadAuditiva = view.findViewById(R.id.grupo_accesibilidad_auditiva_cuenta);

        Usuario usuarioActual = UsuarioCst.USUARIO_ACTUAL;
        if (usuarioActual != null) {
            textoNombre.setText(usuarioActual.getNombreCuenta());
            textoNombreCompleto.setText(usuarioActual.getNombreCompleto());
            textoCorreo.setText(usuarioActual.getCorreo());
        }

        cargarPreferenciasAccesibilidad();

        LinearLayout btnCambiarNombre = view.findViewById(R.id.btn_cambiar_nombre);
        btnCambiarNombre.setOnClickListener(v -> navegarAEdicion("nombre"));

        LinearLayout btnCambiarNombreCompleto = view.findViewById(R.id.btn_cambiar_nombre_completo);
        btnCambiarNombreCompleto.setOnClickListener(v -> navegarAEdicion("nombreCompleto"));

        LinearLayout btnCambiarCorreo = view.findViewById(R.id.btn_cambiar_correo);
        btnCambiarCorreo.setOnClickListener(v -> navegarAEdicion("correo"));

        LinearLayout btnCambiarContrasena = view.findViewById(R.id.btn_cambiar_contrasena);
        btnCambiarContrasena.setOnClickListener(v -> navegarAEdicion("contrasena"));

        Button btnGuardarAccesibilidad = view.findViewById(R.id.btn_guardar_accesibilidad);
        if (btnGuardarAccesibilidad != null) {
            btnGuardarAccesibilidad.setOnClickListener(v -> guardarAccesibilidad());
        }

        Button btnEliminarCuenta = view.findViewById(R.id.btn_eliminar_cuenta);
        if (btnEliminarCuenta != null) {
            btnEliminarCuenta.setOnClickListener(v -> mostrarDialogoEliminarCuenta());
        }
    }

    private void cargarPreferenciasAccesibilidad() {
        if (grupoAccesibilidadVisual != null) {
            boolean visualActiva = PreferencesManager.isAccesibilidadVisualActivada(requireContext());
            grupoAccesibilidadVisual.check(visualActiva
                    ? R.id.radio_visual_activa_cuenta
                    : R.id.radio_visual_inactiva_cuenta);
        }

        if (grupoAccesibilidadAuditiva != null) {
            boolean auditivaActiva = PreferencesManager.isAccesibilidadAuditivaActivada(requireContext());
            grupoAccesibilidadAuditiva.check(auditivaActiva
                    ? R.id.radio_auditiva_activa_cuenta
                    : R.id.radio_auditiva_inactiva_cuenta);
        }
    }

    private void guardarAccesibilidad() {
        Boolean visualActiva = obtenerSeleccion(grupoAccesibilidadVisual,
                R.id.radio_visual_activa_cuenta,
                R.id.radio_visual_inactiva_cuenta,
                "visual");
        if (visualActiva == null) {
            return;
        }

        Boolean auditivaActiva = obtenerSeleccion(grupoAccesibilidadAuditiva,
                R.id.radio_auditiva_activa_cuenta,
                R.id.radio_auditiva_inactiva_cuenta,
                "auditiva");
        if (auditivaActiva == null) {
            return;
        }

        cuentaLogic.guardarPreferenciasAccesibilidad(visualActiva, auditivaActiva);
    }

    private Boolean obtenerSeleccion(RadioGroup grupo, int idActiva, int idInactiva, String tipo) {
        if (grupo == null || grupo.getCheckedRadioButtonId() == -1) {
            showToast("Selecciona la accesibilidad " + tipo);
            return null;
        }

        int checkedId = grupo.getCheckedRadioButtonId();
        if (checkedId == idActiva) {
            return true;
        }
        if (checkedId == idInactiva) {
            return false;
        }
        showToast("Selecciona la accesibilidad " + tipo);
        return null;
    }

    private void navegarAEdicion(String tipoEdicion) {
        Bundle args = new Bundle();
        args.putString("tipoEdicion", tipoEdicion);
        navController.navigate(R.id.action_cuentaFragment_to_editarCuentaFragment, args);
    }

    private void mostrarDialogoEliminarCuenta() {
        View vista = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialogo_eliminar_cuenta, null);

        com.google.android.material.textfield.TextInputEditText etContrasena =
                vista.findViewById(R.id.etContrasena);
        Button btnCancelar = vista.findViewById(R.id.btnCancelar);
        Button btnAceptar = vista.findViewById(R.id.btnAceptar);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(vista)
                .setCancelable(true)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            String contrasena = etContrasena.getText() != null ?
                    etContrasena.getText().toString().trim() : "";

            if (contrasena.isEmpty()) {
                showToast("Ingresa tu contraseña");
                return;
            }

            dialog.dismiss();
            cuentaLogic.eliminarCuenta(contrasena);
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cuentaLogic = null;
    }

    public void showToast(String mensaje) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), mensaje,
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    public void actualizarDatosUsuario() {
        Usuario usuarioActual = UsuarioCst.USUARIO_ACTUAL;
        if (usuarioActual != null && textoNombre != null && textoNombreCompleto != null && textoCorreo != null) {
            textoNombre.setText(usuarioActual.getNombreCuenta());
            textoNombreCompleto.setText(usuarioActual.getNombreCompleto());
            textoCorreo.setText(usuarioActual.getCorreo());
        }
    }

    public void onPreferenciasAccesibilidadGuardadas(boolean requiereRecrear) {
        showToast("Preferencias de accesibilidad actualizadas");

        if (requiereRecrear && getActivity() != null) {
            ThemeManager.applyTheme(requireActivity());
            requireActivity().recreate();
        }
    }

    public void onCuentaEliminada() {
        PreferencesManager.setUsuarioRegistrado(requireContext(), false);
        PreferencesManager.setIdUsuario(requireContext(), -1);
        PreferencesManager.setNombreUsuario(requireContext(), "");
        PreferencesManager.setNombreCompletoUsuario(requireContext(), "");
        PreferencesManager.setCorreoUsuario(requireContext(), "");
        PreferencesManager.setAccesibilidadVisual(requireContext(), false);
        PreferencesManager.setAccesibilidadAuditivaActivada(requireContext(), false);

        UsuarioCst.USUARIO_ACTUAL = null;

        Intent intent = new Intent(requireContext(), AccesoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}