package com.cyberpath.smartlearn.ui.main.combo.configuracion;

import android.os.Bundle;
import android.util.Log;
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
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.usuario.Configuracion;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfiguracionFragment extends Fragment {

    private static final String TAG = "ConfiguracionFragment";
    private SeekBar seekBarTamanoFuente;
    private Button btnGuardar;
    private int tamanoActual;
    private Configuracion configuracionActual;

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

        btnGuardar.setOnClickListener(v -> guardarConfiguracion());
        cargarConfiguracionRemota();
    }

    private void cargarConfiguracionRemota() {
        Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
        if (idUsuario == null) return;

        RetrofitClient.getApiService().getConfiguracionById(idUsuario).enqueue(new Callback<Configuracion>() {
            @Override
            public void onResponse(Call<Configuracion> call, Response<Configuracion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    configuracionActual = response.body();
                    if (configuracionActual.getTamanoFuente() != null) {
                        int valor = configuracionActual.getTamanoFuente().getValor();
                        tamanoActual = valor;
                        seekBarTamanoFuente.setProgress(valor);
                        PreferencesManager.setTamanoTexto(requireContext(), valor);
                    }
                    PreferencesManager.setModoAudio(requireContext(), configuracionActual.isModoAudio());
                }
            }

            @Override
            public void onFailure(Call<Configuracion> call, Throwable t) {
                Log.w(TAG, "No se pudo cargar configuración remota", t);
            }
        });
    }

    private void guardarConfiguracion() {
        Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
        if (idUsuario == null) {
            Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        int nuevoTamano = seekBarTamanoFuente.getProgress();
        PreferencesManager.setTamanoTexto(requireContext(), nuevoTamano);

        Configuracion configuracion = configuracionActual != null ? configuracionActual : new Configuracion();
        configuracion.setIdUsuario(idUsuario);
        configuracion.setCuentaCreada(true);
        configuracion.setModoAudio(PreferencesManager.isModoAudioActivado(requireContext()));
        configuracion.setModoOffline(false);
        configuracion.setNotificacionesActivadas(true);
        configuracion.setTamanoFuente(mapTamanoFuente(nuevoTamano));

        ApiService api = RetrofitClient.getApiService();
        Callback<Configuracion> callback = new Callback<Configuracion>() {
            @Override
            public void onResponse(Call<Configuracion> call, Response<Configuracion> response) {
                if (response.isSuccessful()) {
                    configuracionActual = response.body() != null ? response.body() : configuracion;
                    tamanoActual = nuevoTamano;
                    Toast.makeText(requireContext(), "Configuración guardada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "No se pudo guardar la configuración", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Configuracion> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        };

        if (configuracionActual == null) {
            api.saveConfiguracion(configuracion).enqueue(callback);
        } else {
            api.updateConfiguracion(idUsuario, configuracion).enqueue(callback);
        }
    }

    private Configuracion.TamanoFuente mapTamanoFuente(int progress) {
        switch (progress) {
            case 0:
                return Configuracion.TamanoFuente.PEQUENO;
            case 2:
                return Configuracion.TamanoFuente.GRANDE;
            case 1:
            default:
                return Configuracion.TamanoFuente.MEDIO;
        }
    }
}