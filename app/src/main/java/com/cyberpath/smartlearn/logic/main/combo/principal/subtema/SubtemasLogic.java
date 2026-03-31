package com.cyberpath.smartlearn.logic.main.combo.principal.subtema;

import android.content.Context;
import android.util.Log;

import com.cyberpath.smartlearn.data.local.database.repository.SubtemasRepository;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.model.usuario.UltimaConexion;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.principal.subtema.SubtemasFragment;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.network.NetworkUtils;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubtemasLogic {

    private final SubtemasFragment fragment;
    private final Context context;
    private final Tema tema;
    private final SubtemasRepository subtemasRepository;
    private boolean modoOffline = false;

    @Getter
    private final List<Subtema> listaSubtemas = new ArrayList<>();

    public SubtemasLogic(SubtemasFragment fragment, Tema tema) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        this.tema = tema;
        this.subtemasRepository = new SubtemasRepository(context);

        cargarSubtemas();
    }

    public void cargarSubtemas() {
        if (tema == null || tema.getId() == null) {
            fragment.showToast("Tema inválido");
            return;
        }
        if (NetworkUtils.isInternetAvailable(context)) {
            modoOffline = false;
            cargarSubtemasAPI();
        } else {
            modoOffline = true;
            cargarSubtemasLocal();
        }
    }

    private void cargarSubtemasAPI() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Subtema>> call = apiService.getSubtemasByTema(tema.getId());

        call.enqueue(new Callback<List<Subtema>>() {
            @Override
            public void onResponse(Call<List<Subtema>> call, Response<List<Subtema>> response) {
                if (fragment == null || !fragment.isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    listaSubtemas.clear();
                    listaSubtemas.addAll(response.body());
                    //guardarEnLocal(listaSubtemas);

                    fragment.actualizarAdapter(listaSubtemas);

                    if (listaSubtemas.isEmpty()) {
                        fragment.showToast("No hay subtemas disponibles");
                    } else {
                        fragment.moverViewPagerAPosicion(1, false);
                    }

                    fragment.iniciarNavegacionPorVoz();
                } else {
                    fragment.showToast("Error al cargar subtemas");
                    cargarSubtemasLocal();
                }
            }

            @Override
            public void onFailure(Call<List<Subtema>> call, Throwable t) {
                if (fragment == null || !fragment.isAdded()) return;
                fragment.showToast("Error de conexión: " + t.getMessage());
                cargarSubtemasLocal();
            }
        });
    }

    private void cargarSubtemasLocal() {
        try {
            List<Subtema> subtemasLocales = subtemasRepository.obtenerSubtemasPorTema(tema.getId());

            listaSubtemas.clear();
            listaSubtemas.addAll(subtemasLocales);

            fragment.actualizarAdapter(listaSubtemas);

            if (listaSubtemas.isEmpty()) {
                fragment.showToast("No hay subtemas disponibles sin conexión");
            } else {
                fragment.showToast("Modo offline - Subtemas locales");
                fragment.moverViewPagerAPosicion(1, false);
            }

            fragment.iniciarNavegacionPorVoz();

        } catch (Exception e) {
            Log.e("SubtemasLogic", "Error al cargar subtemas locales: " + e.getMessage());
            fragment.showToast("Error al cargar subtemas locales");
        }
    }

    private void guardarEnLocal(List<Subtema> subtemas) {
        try {
            subtemasRepository.guardarSubtemas(subtemas);
            Log.d("SubtemasLogic", "Subtemas guardados en BD local");
        } catch (Exception e) {
            Log.e("SubtemasLogic", "Error al guardar subtemas localmente: " + e.getMessage());
        }
    }

    public void guardarUltimaConexion(Subtema subtema) {
        if (subtema == null || subtema.getId() == null) return;

        if (modoOffline) {
            PreferencesManager.setIdSubtemaUltimaConexion(context, subtema.getId());
            return;
        }

        UltimaConexion ultimaConexion = new UltimaConexion();
        ApiService apiService = RetrofitClient.getApiService();

        Calendar tiempo = Calendar.getInstance();
        SimpleDateFormat formatoTiempo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fecha = formatoTiempo.format(tiempo.getTime());
        Integer idUsuario = UsuarioCst.USUARIO_ACTUAL.getId();

        ultimaConexion.setUltimaConexion(fecha);
        ultimaConexion.setIdSubtema(subtema.getId());

        Call<UltimaConexion> call = apiService.update(idUsuario, ultimaConexion);
        call.enqueue(new Callback<UltimaConexion>() {
            @Override
            public void onResponse(Call<UltimaConexion> call, Response<UltimaConexion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PreferencesManager.setIdSubtemaUltimaConexion(context, subtema.getId());
                }
            }

            @Override
            public void onFailure(Call<UltimaConexion> call, Throwable t) {
                PreferencesManager.setIdSubtemaUltimaConexion(context, subtema.getId());
            }
        });
    }

    public void limpiarDatos() {
        listaSubtemas.clear();
    }

    public int getRealSize() {
        return listaSubtemas != null ? listaSubtemas.size() : 0;
    }
}