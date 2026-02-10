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
import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.usuario.Configuracion;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

public class ConfiguracionFragment extends Fragment {

    private static final String TAG = "ConfiguracionFragment";
    private SeekBar seekBarTamanoFuente;
    private Button btnGuardar;
    private int tamanoActual;

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

        //btnGuardar.setOnClickListener(v -> guardarConfiguracion());
    }

    /*private void guardarConfiguracion() {
        int nuevoTamano = seekBarTamanoFuente.getProgress();

        if (nuevoTamano == tamanoActual) {
            Toast.makeText(requireContext(), "No hay cambios", Toast.LENGTH_SHORT).show();
            return;
        }

        PreferencesManager.setTamanoTexto(requireContext(), nuevoTamano);

        if (nuevoTamano == Configuracion.TamanoFuente.PEQUENO.getValor()) {
            requireActivity().setTheme(R.style.Base_Theme_SmartLearn_TextoPequeno);
        } else if (nuevoTamano == Configuracion.TamanoFuente.GRANDE.getValor()) {
            requireActivity().setTheme(R.style.Base_Theme_SmartLearn_TextoGrande);
        } else {
            requireActivity().setTheme(R.style.Base_Theme_SmartLearn);
        }

        Toast.makeText(requireContext(), "Tamaño cambiado. Reiniciando...", Toast.LENGTH_LONG).show();
        requireActivity().recreate(); // ¡Cambia todo!
    }*/
}