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

    private RadioGroup radioGroup;
    private RadioButton radioActiva;
    private RadioButton radioInactiva;
    private boolean isTemaAccesible;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accesibilidad, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroup = view.findViewById(R.id.grupo_accesibilidad);
        radioActiva = view.findViewById(R.id.radio_activa);
        radioInactiva = view.findViewById(R.id.radio_inactiva);

        boolean modoAudioActivado = PreferencesManager.isModoAudioActivado(requireContext());
        isTemaAccesible = PreferencesManager.getTemaApp(requireContext()) == PreferencesManager.THEME_ACCESSIBLE;

        if (modoAudioActivado) {
            radioActiva.setChecked(true);
        } else {
            radioInactiva.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean activar = (checkedId == R.id.radio_activa);
            PreferencesManager.setModoAudio(requireContext(), activar);
            if (activar){
                PreferencesManager.setTemaApp(requireContext(), PreferencesManager.THEME_ACCESSIBLE);
                ThemeManager.applyTheme(requireActivity());
            } else{
                PreferencesManager.setTemaApp(requireContext(), PreferencesManager.THEME_LIGHT);
                ThemeManager.applyTheme(requireActivity());
            }

            requireActivity().recreate(); 

            Toast.makeText(requireContext(),
                    activar ? "Modo audio activado" : "Modo audio desactivado", Toast.LENGTH_SHORT).show();
        });
    }
}