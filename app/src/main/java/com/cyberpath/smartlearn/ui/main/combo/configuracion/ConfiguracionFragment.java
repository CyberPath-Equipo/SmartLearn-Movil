package com.cyberpath.smartlearn.ui.main.combo.configuracion;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.propiedades.Configuracion;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfiguracionFragment extends Fragment {

    private static final String TAG = "ConfiguracionFragment";
    private SeekBar seekBarTamanoFuente;
    private Button btnGuardar;
    private int tamanoActual;
    private SwitchMaterial switchMaterialTema;
    private boolean temaDark;
    private int temaApp;

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
        this.temaApp = 0;

        tamanoActual = PreferencesManager.getTamanoTexto(requireContext());
        if (tamanoActual == -1) {
            tamanoActual = Configuracion.TamanoFuente.MEDIO.getValor() - 1;
        }
        seekBarTamanoFuente.setProgress(tamanoActual);

        seekBarTamanoFuente.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "Tamaño seleccionado: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        temaApp = PreferencesManager.getTemaApp(requireContext());
        if (temaApp == PreferencesManager.THEME_ACCESSIBLE) {
            switchMaterialTema.setEnabled(false);
        }
        temaDark = PreferencesManager.getTemaApp(requireContext()) == PreferencesManager.THEME_DARK;
        switchMaterialTema.setChecked(temaDark);
        switchMaterialTema.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                temaApp = PreferencesManager.THEME_DARK;
                PreferencesManager.setTemaApp(requireContext(), temaApp);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                temaApp = PreferencesManager.THEME_LIGHT;
                PreferencesManager.setTemaApp(requireContext(), temaApp);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }));


    }
}