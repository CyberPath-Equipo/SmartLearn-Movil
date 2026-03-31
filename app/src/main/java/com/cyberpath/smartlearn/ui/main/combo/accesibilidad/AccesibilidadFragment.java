package com.cyberpath.smartlearn.ui.main.combo.accesibilidad;

import android.os.Bundle;
import android.util.Log;
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
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.usuario.Configuracion;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccesibilidadFragment extends Fragment {

    private static final String TAG = "AccesibilidadFragment";
    private RadioGroup radioGroup;
    private RadioButton radioActiva;
    private RadioButton radioInactiva;
    private Configuracion configuracionActual;

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
        aplicarSeleccion(modoAudioActivado);
        cargarConfiguracionRemota();

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean activar = (checkedId == R.id.radio_activa);
            PreferencesManager.setModoAudio(requireContext(), activar);
            sincronizarModoAudio(activar);

            Toast.makeText(requireContext(),
                    activar ? "Modo audio activado" : "Modo audio desactivado", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarConfiguracionRemota() {
        Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
        if (idUsuario == null) return;

        RetrofitClient.getApiService().getConfiguracionById(idUsuario).enqueue(new Callback<Configuracion>() {
            @Override
            public void onResponse(Call<Configuracion> call, Response<Configuracion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    configuracionActual = response.body();
                    boolean activar = configuracionActual.isModoAudio();
                    PreferencesManager.setModoAudio(requireContext(), activar);
                    aplicarSeleccion(activar);
                }
            }

            @Override
            public void onFailure(Call<Configuracion> call, Throwable t) {
                Log.w(TAG, "No se pudo cargar la configuración remota", t);
            }
        });
    }

    private void sincronizarModoAudio(boolean activar) {
        Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
        if (idUsuario == null) return;

        Configuracion configuracion = configuracionActual != null ? configuracionActual : new Configuracion();
        configuracion.setIdUsuario(idUsuario);
        configuracion.setCuentaCreada(true);
        configuracion.setModoAudio(activar);
        configuracion.setModoOffline(false);
        configuracion.setNotificacionesActivadas(true);
        if (configuracion.getTamanoFuente() == null) {
            configuracion.setTamanoFuente(Configuracion.TamanoFuente.MEDIO);
        }

        Callback<Configuracion> callback = new Callback<Configuracion>() {
            @Override
            public void onResponse(Call<Configuracion> call, Response<Configuracion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    configuracionActual = response.body();
                }
            }

            @Override
            public void onFailure(Call<Configuracion> call, Throwable t) {
                Log.w(TAG, "No se pudo sincronizar modo audio", t);
            }
        };

        if (configuracionActual == null) {
            RetrofitClient.getApiService().saveConfiguracion(configuracion).enqueue(callback);
        } else {
            RetrofitClient.getApiService().updateConfiguracion(idUsuario, configuracion).enqueue(callback);
        }
    }

    private void aplicarSeleccion(boolean activar) {
        if (activar) {
            radioActiva.setChecked(true);
        } else {
            radioInactiva.setChecked(true);
        }
    }
}