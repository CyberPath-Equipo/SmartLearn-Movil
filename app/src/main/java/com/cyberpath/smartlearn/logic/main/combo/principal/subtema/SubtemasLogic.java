package com.cyberpath.smartlearn.logic.main.combo.principal.subtema;

import android.content.Context;
import android.util.Log;

import com.cyberpath.smartlearn.data.local.database.repository.SubtemasRepository;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.model.usuario.propiedades.UltimaConexion;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.MainActivity;
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
    @Getter
    private final List<Subtema> listaSubtemas = new ArrayList<>();
    private boolean modoOffline = false;

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


    public void guardarUltimaConexion(Subtema subtema) {
        if (subtema == null || subtema.getId() == null) return;
        PreferencesManager.setIdSubtemaUltimaConexion(context, subtema.getId());

        if (!modoOffline && !NetworkUtils.shouldUseOfflineMode(context)) {
            UltimaConexion ultimaConexion = new UltimaConexion();
            ApiService apiService = RetrofitClient.getApiService();

            Calendar tiempo = Calendar.getInstance();
            SimpleDateFormat formatoTiempo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fecha = formatoTiempo.format(tiempo.getTime());
            ultimaConexion.setUltimaConexion(fecha);
            Integer idUsuario = UsuarioCst.USUARIO_ACTUAL != null ? UsuarioCst.USUARIO_ACTUAL.getId() : null;

            if (idUsuario == null) {
                notifyMainActivity(subtema);
                return;
            }

            ultimaConexion.setUltimaConexion(fecha);
            ultimaConexion.setIdSubtema(subtema.getId());

            Call<UltimaConexion> call = apiService.updateUsuario(UsuarioCst.USUARIO_ACTUAL.getId(), ultimaConexion);
            call.enqueue(new Callback<UltimaConexion>() {
                @Override
                public void onResponse(Call<UltimaConexion> call, Response<UltimaConexion> response) {
                    if (response.isSuccessful()) {
                        notifyMainActivity(subtema);
                    }
                }

                @Override
                public void onFailure(Call<UltimaConexion> call, Throwable t) {
                    notifyMainActivity(subtema);
                }
            });
        } else {
            notifyMainActivity(subtema);
        }
    }

    private void notifyMainActivity(Subtema subtema) {
        if (context instanceof MainActivity mainActivity) {
            mainActivity.actualizarUltimoSubtemaMenu(subtema.getNombre(), subtema);
        }
    }

    public void limpiarDatos() {
        listaSubtemas.clear();
    }

    public int getRealSize() {
        return listaSubtemas != null ? listaSubtemas.size() : 0;
    }
}