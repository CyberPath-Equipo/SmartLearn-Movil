package com.cyberpath.smartlearn.ui.main.combo.configuracion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.preferences.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfiguracionFragment extends Fragment {

    private SeekBar seekBarTamanoFuente;
    private Button btnGuardar;
    private SwitchMaterial switchMaterialTema;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuracion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        seekBarTamanoFuente = view.findViewById(R.id.seekbar_tamano_fuente);
        btnGuardar = view.findViewById(R.id.btn_guardar_preferencias);
        switchMaterialTema = view.findViewById(R.id.switch_modo_oscuro);
        seekBarTamanoFuente.setProgress(PreferencesManager.getTamanoTexto(requireContext()));

        seekBarTamanoFuente.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        boolean accesibilidadVisualActiva = PreferencesManager.isAccesibilidadVisualActivada(requireContext());
        if (accesibilidadVisualActiva) {
            switchMaterialTema.setEnabled(false);
        }
        boolean temaDark = PreferencesManager.getTemaApp(requireContext()) == PreferencesManager.THEME_DARK;
        switchMaterialTema.setChecked(temaDark);

        btnGuardar.setOnClickListener(v -> {
            PreferencesManager.setTamanoTexto(requireContext(), seekBarTamanoFuente.getProgress());
            if (switchMaterialTema.isEnabled()) {
                int temaSeleccionado = switchMaterialTema.isChecked()
                        ? PreferencesManager.THEME_DARK
                        : PreferencesManager.THEME_LIGHT;
                PreferencesManager.setTemaApp(requireContext(), temaSeleccionado);
            }

            ThemeManager.applyTheme(requireActivity());
            requireActivity().recreate();
            Toast.makeText(requireContext(), "Preferencias aplicadas", Toast.LENGTH_SHORT).show();
        });

    }
}