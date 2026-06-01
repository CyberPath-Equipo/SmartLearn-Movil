package com.cyberpath.smartlearn.ui.acceso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.logic.acceso.SignupLogic;

public class SignUpFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SignUpFragment";

    private Button btnRegresar, btnRegistro;
    private EditText etNombre, etNombreCompleto, etContrasena, etCorreo;
    private RadioButton radioVisualActiva, radioVisualInactiva, radioAuditivaActiva, radioAuditivaInactiva, radioAlumno, radioDocente;
    private RadioGroup grupoAccesibilidadVisual, grupoAccesibilidadAuditiva, grupoTipoUsuario;
    private ProgressBar loading;

    private SignupLogic signupLogic;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signupLogic = new SignupLogic(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnRegresar = view.findViewById(R.id.btn_regresar);
        btnRegistro = view.findViewById(R.id.btn_registro);
        etNombre = view.findViewById(R.id.et_nombre);
        etNombreCompleto = view.findViewById(R.id.et_nombre_completo);
        etContrasena = view.findViewById(R.id.et_contrasena);
        etCorreo = view.findViewById(R.id.et_correo);
        radioVisualActiva = view.findViewById(R.id.radio_visual_activa);
        radioVisualInactiva = view.findViewById(R.id.radio_visual_inactiva);
        radioAuditivaActiva = view.findViewById(R.id.radio_auditiva_activa);
        radioAuditivaInactiva = view.findViewById(R.id.radio_auditiva_inactiva);
        radioAlumno = view.findViewById(R.id.radio_alumno);
        radioDocente = view.findViewById(R.id.radio_docente);
        grupoAccesibilidadVisual = view.findViewById(R.id.grupo_accesibilidad_visual);
        grupoAccesibilidadAuditiva = view.findViewById(R.id.grupo_accesibilidad_auditiva);
        grupoTipoUsuario = view.findViewById(R.id.grupo_tipo_usuario);
        loading = view.findViewById(R.id.loading);

        btnRegresar.setOnClickListener(this);
        btnRegistro.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_regresar) {
            Navigation.findNavController(v).navigate(R.id.loginFragment);
        } else if (v.getId() == R.id.btn_registro) {
            signupLogic.registrarUsuario(
                    etNombre.getText().toString().trim(),
                    etNombreCompleto.getText().toString().trim(),
                    etContrasena.getText().toString().trim(),
                    etCorreo.getText().toString().trim(),
                    radioVisualActiva.isChecked(),
                    radioVisualInactiva.isChecked(),
                    radioAuditivaActiva.isChecked(),
                    radioAuditivaInactiva.isChecked(),
                    radioAlumno.isChecked(),
                    radioDocente.isChecked()
            );
        }
    }
}