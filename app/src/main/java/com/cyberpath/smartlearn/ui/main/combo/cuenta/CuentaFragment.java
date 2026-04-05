package com.cyberpath.smartlearn.ui.main.combo.cuenta;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class CuentaFragment extends Fragment {

    private CuentaLogic cuentaLogic;
    private NavController navController;
    private TextView textoNombre, textoNombreCompleto, textoCorreo, textoAccesibilidadVisual, textoAccesibilidadAuditiva;

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
        textoAccesibilidadVisual = view.findViewById(R.id.tv_accesibilidad_visual_actual);
        textoAccesibilidadAuditiva = view.findViewById(R.id.tv_accesibilidad_auditiva_actual);

        // Asegurar que el usuario está cargado
        if (!UsuarioCst.ensureUsuarioLoaded(requireContext())) {
            // Si aún así no hay usuario, mostrar error y cargar desde preferencias como fallback
            Usuario usuarioFallback = UsuarioCst.reconstructFromPreferences(requireContext());
            if (usuarioFallback != null) {
                UsuarioCst.USUARIO_ACTUAL = usuarioFallback;
            }
        }

        Usuario usuarioActual = UsuarioCst.USUARIO_ACTUAL;
        if (usuarioActual != null) {
            textoNombre.setText(usuarioActual.getNombreCuenta());
            textoNombreCompleto.setText(usuarioActual.getNombreCompleto() != null ? usuarioActual.getNombreCompleto() : usuarioActual.getNombreCuenta());
            textoCorreo.setText(usuarioActual.getCorreo());
        } else {
            // Último recurso: mostrar datos desde preferencias directamente
            String nombreCuenta = PreferencesManager.getNombreUsuario(requireContext());
            String correo = PreferencesManager.getCorreoUsuario(requireContext());
            textoNombre.setText(nombreCuenta.isEmpty() ? "No disponible" : nombreCuenta);
            textoCorreo.setText(correo.isEmpty() ? "No disponible" : correo);
            textoNombreCompleto.setText("No disponible");
        }
        actualizarEstadosAccesibilidad();

        LinearLayout btnCambiarNombre = view.findViewById(R.id.btn_cambiar_nombre);
        btnCambiarNombre.setOnClickListener(v -> navegarAEdicion("nombre"));

        LinearLayout btnCambiarNombreCompleto = view.findViewById(R.id.btn_cambiar_nombre_completo);
        btnCambiarNombreCompleto.setOnClickListener(v -> navegarAEdicion("nombreCompleto"));

        LinearLayout btnCambiarCorreo = view.findViewById(R.id.btn_cambiar_correo);
        btnCambiarCorreo.setOnClickListener(v -> navegarAEdicion("correo"));

        LinearLayout btnCambiarContrasena = view.findViewById(R.id.btn_cambiar_contrasena);
        btnCambiarContrasena.setOnClickListener(v -> navegarAEdicion("contrasena"));

        LinearLayout btnAccesibilidadVisual = view.findViewById(R.id.btn_accesibilidad_visual);
        btnAccesibilidadVisual.setOnClickListener(v -> navegarAEdicion("accesibilidadVisual"));

        LinearLayout btnAccesibilidadAuditiva = view.findViewById(R.id.btn_accesibilidad_auditiva);
        btnAccesibilidadAuditiva.setOnClickListener(v -> navegarAEdicion("accesibilidadAuditiva"));

        Button btnEliminarCuenta = view.findViewById(R.id.btn_eliminar_cuenta);
        if (btnEliminarCuenta != null) {
            btnEliminarCuenta.setOnClickListener(v -> mostrarDialogoEliminarCuenta());
        }
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
            textoNombreCompleto.setText(usuarioActual.getNombreCompleto() != null ? usuarioActual.getNombreCompleto() : usuarioActual.getNombreCuenta());
            textoCorreo.setText(usuarioActual.getCorreo());
        }
        actualizarEstadosAccesibilidad();
    }

    private void actualizarEstadosAccesibilidad() {
        if (textoAccesibilidadVisual != null) {
            textoAccesibilidadVisual.setText(
                    PreferencesManager.isAccesibilidadVisualActivada(requireContext()) ? "Activa" : "Inactiva"
            );
        }
        if (textoAccesibilidadAuditiva != null) {
            textoAccesibilidadAuditiva.setText(
                    PreferencesManager.isAccesibilidadAuditivaActivada(requireContext()) ? "Activa" : "Inactiva"
            );
        }
    }

    public void onCuentaEliminada() {
        PreferencesManager.setUsuarioRegistrado(requireContext(), false);
        PreferencesManager.setSesionActiva(requireContext(), false);
        PreferencesManager.setIdUsuario(requireContext(), -1);

        UsuarioCst.USUARIO_ACTUAL = null;

        Intent intent = new Intent(requireContext(), AccesoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}