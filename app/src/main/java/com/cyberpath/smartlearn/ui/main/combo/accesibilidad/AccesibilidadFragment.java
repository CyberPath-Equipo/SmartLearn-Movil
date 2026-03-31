package com.cyberpath.smartlearn.ui.main.combo.accesibilidad;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.preferences.ThemeManager;

public class AccesibilidadFragment extends Fragment {

    private RadioGroup radioGroupVisual;
    private RadioGroup radioGroupAuditiva;
    private RadioButton radioVisualActiva;
    private RadioButton radioVisualInactiva;
    private RadioButton radioAuditivaActiva;
    private RadioButton radioAuditivaInactiva;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accesibilidad, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroupVisual = view.findViewById(R.id.grupo_accesibilidad_visual);
        radioGroupAuditiva = view.findViewById(R.id.grupo_accesibilidad_auditiva);
        radioVisualActiva = view.findViewById(R.id.radio_visual_activa);
        radioVisualInactiva = view.findViewById(R.id.radio_visual_inactiva);
        radioAuditivaActiva = view.findViewById(R.id.radio_auditiva_activa);
        radioAuditivaInactiva = view.findViewById(R.id.radio_auditiva_inactiva);

        boolean accesibilidadVisualActiva = PreferencesManager.isAccesibilidadVisualActivada(requireContext());
        boolean accesibilidadAuditivaActiva = PreferencesManager.isAccesibilidadAuditivaActivada(requireContext());

        if (accesibilidadVisualActiva) {
            radioVisualActiva.setChecked(true);
        } else {
            radioVisualInactiva.setChecked(true);
        }

        if (accesibilidadAuditivaActiva) {
            radioAuditivaActiva.setChecked(true);
        } else {
            radioAuditivaInactiva.setChecked(true);
        }

        radioGroupVisual.setOnCheckedChangeListener((group, checkedId) -> {
            boolean activar = (checkedId == R.id.radio_visual_activa);
            PreferencesManager.setAccesibilidadVisual(requireContext(), activar);
            ThemeManager.applyTheme(requireActivity());
            requireActivity().recreate();
            Toast.makeText(requireContext(),
                    activar ? "Accesibilidad visual activada" : "Accesibilidad visual desactivada", Toast.LENGTH_SHORT).show();
        });

        radioGroupAuditiva.setOnCheckedChangeListener((group, checkedId) -> {
            boolean activar = (checkedId == R.id.radio_auditiva_activa);
            PreferencesManager.setAccesibilidadAuditivaActivada(requireContext(), activar);
            Toast.makeText(requireContext(),
                    activar ? "Accesibilidad auditiva activada" : "Accesibilidad auditiva desactivada", Toast.LENGTH_SHORT).show();
        });
    }
}