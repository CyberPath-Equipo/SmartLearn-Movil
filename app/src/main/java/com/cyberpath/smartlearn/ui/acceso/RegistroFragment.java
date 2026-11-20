package com.cyberpath.smartlearn.ui.acceso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.logic.acceso.RegistroLogic;

public class RegistroFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "RegistroFragment";

    private Button btnRegresar, btnRegistro;
    private EditText etNombre, etNombreCompleto, etContrasena, etCorreo;
    private RadioButton radioVisualActiva, radioVisualInactiva, radioAuditivaActiva, radioAuditivaInactiva, radioAlumno, radioDocente;

    private RegistroLogic registroLogic;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registroLogic = new RegistroLogic(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registro, container, false);
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


        btnRegresar.setOnClickListener(this);
        btnRegistro.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_regresar) {
            Navigation.findNavController(v).navigate(R.id.InicioSesionFragment);
        } else if (v.getId() == R.id.btn_registro) {
            registroLogic.registrarUsuario(
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