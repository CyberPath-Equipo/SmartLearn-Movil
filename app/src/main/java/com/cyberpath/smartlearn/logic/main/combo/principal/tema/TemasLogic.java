package com.cyberpath.smartlearn.logic.main.combo.principal.tema;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.cyberpath.smartlearn.data.local.database.dao.ContenidoDAO;
import com.cyberpath.smartlearn.data.local.database.sincronizar.DescargarDatos;
import com.cyberpath.smartlearn.data.local.database.repository.TemasRepository;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.principal.tema.TemasFragment;
import com.cyberpath.smartlearn.util.network.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemasLogic {

    private final TemasFragment fragment;
    private final Context context;
    private final Materia materia;

    private final TemasRepository temasRepository;
    private final ContenidoDAO contenidoDAO;

    @Getter
    private final List<Tema> listaTemas = new ArrayList<>();
    private boolean modoOffline = false;
    private boolean materiaDescargada = false;
    private boolean accionOfflineEnProceso = false;

    public TemasLogic(TemasFragment fragment, Materia materia) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        this.materia = materia;
        this.temasRepository = new TemasRepository(context);
        this.contenidoDAO = new ContenidoDAO(context);

        refrescarEstadoDescarga();
        cargarTemas();
    }

    public void cargarTemas() {
        if (materia == null || materia.getId() == null) {
            fragment.showToast("Materia inválida");
            return;
        }

        if (NetworkUtils.isInternetAvailable(context)) {
            modoOffline = false;
            cargarTemasAPI();
        } else {
            modoOffline = true;
            cargarTemasLocal();
        }
    }

    private void cargarTemasAPI() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Tema>> call = apiService.getTemasByMateria(materia.getId());

        call.enqueue(new Callback<List<Tema>>() {
            @Override
            public void onResponse(Call<List<Tema>> call, Response<List<Tema>> response) {
                if (fragment == null || !fragment.isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    listaTemas.clear();
                    listaTemas.addAll(response.body());


                    fragment.actualizarAdapter(listaTemas);

                    if (listaTemas.isEmpty()) {
                        fragment.showToast("No hay temas para esta materia");
                    } else {
                        fragment.moverViewPagerAPosicion(1, false);
                    }

                    fragment.iniciarNavegacionPorVoz();
                } else {
                    fragment.showToast("Error al cargar temas");
                    cargarTemasLocal();
                }
            }

            @Override
            public void onFailure(Call<List<Tema>> call, Throwable t) {
                if (fragment == null || !fragment.isAdded()) return;
                fragment.showToast("Error de red: " + t.getMessage());
                cargarTemasLocal();
            }
        });
    }

    private void cargarTemasLocal() {
        try {
            List<Tema> temasLocales = temasRepository.obtenerTemasPorMateria(materia.getId());

            listaTemas.clear();
            listaTemas.addAll(temasLocales);

            fragment.actualizarAdapter(listaTemas);

            if (listaTemas.isEmpty()) {
                fragment.showToast("No hay temas disponibles sin conexión");
            } else {
                fragment.showToast("Modo offline - Temas locales");
                fragment.moverViewPagerAPosicion(1, false);
            }

            fragment.iniciarNavegacionPorVoz();

        } catch (Exception e) {
            Log.e("TemasLogic", "Error al cargar temas locales: " + e.getMessage());
            fragment.showToast("Error al cargar temas locales");
        }
    }

    private void refrescarEstadoDescarga() {
        try {
            materiaDescargada = materia != null
                    && materia.getId() != null
                    && contenidoDAO.materiaDescargada(materia.getId());
        } catch (Exception e) {
            materiaDescargada = false;
            Log.e("TemasLogic", "Error al consultar descarga local de materia: " + e.getMessage());
        }

        fragment.actualizarEstadoBotonOffline(materiaDescargada, accionOfflineEnProceso);
    }

    public void descargarMateriaCompleta(ProgressBar progressBar,
                                         TextView tvProgreso, TextView tvMensajeProgreso,
                                         AlertDialog dialogo) {
        if (materia == null || materia.getId() == null) {
            fragment.showToastLong("Materia inválida");
            dialogo.dismiss();
            return;
        }

        if (!NetworkUtils.isInternetAvailable(context)) {
            fragment.showToastLong("Se necesita conexión para descargar contenido");
            dialogo.dismiss();
            return;
        }

        if (accionOfflineEnProceso || materiaDescargada) {
            fragment.showToast("La materia ya está descargada o en proceso");
            dialogo.dismiss();
            refrescarEstadoDescarga();
            return;
        }

        accionOfflineEnProceso = true;
        fragment.actualizarEstadoBotonOffline(materiaDescargada, true);

        DescargarDatos descarga = new DescargarDatos(context);
        descarga.setCallback(new DescargarDatos.DescargaCallback() {
            @Override
            public void onDescargaIniciada() {
                progressBar.setProgress(0);
                tvProgreso.setText("0%");
                tvMensajeProgreso.setText("Preparando descarga...");
            }

            @Override
            public void onProgreso(int porcentajeActual, String mensaje) {
                progressBar.setProgress(porcentajeActual);
                tvProgreso.setText(porcentajeActual + "%");
                tvMensajeProgreso.setText(mensaje);
            }

            @Override
            public void onDescargaCompletada() {
                contenidoDAO.registrarMateriaDescargada(materia, 0.0);
                accionOfflineEnProceso = false;
                materiaDescargada = true;
                fragment.actualizarEstadoBotonOffline(materiaDescargada, false);
                fragment.showToastLong("¡Descarga completada!");
                dialogo.dismiss();
            }

            @Override
            public void onDescargaFallida(String error) {
                accionOfflineEnProceso = false;
                refrescarEstadoDescarga();
                fragment.showToastLong("Error en descarga: " + error);
                dialogo.dismiss();
            }
        });

        descarga.descargarMateria(materia);
    }

    public void desinstalarMateriaCompleta() {
        if (materia == null || materia.getId() == null) {
            fragment.showToastLong("Materia inválida");
            return;
        }

        if (accionOfflineEnProceso) {
            fragment.showToast("Hay una acción en proceso");
            return;
        }

        if (!materiaDescargada) {
            fragment.showToast("La materia no está descargada");
            refrescarEstadoDescarga();
            return;
        }

        accionOfflineEnProceso = true;
        fragment.actualizarEstadoBotonOffline(materiaDescargada, true);

        new Thread(() -> {
            boolean eliminado;
            try {
                eliminado = contenidoDAO.borrarMateriaDescargada(materia.getId());
            } catch (Exception e) {
                Log.e("TemasLogic", "Error al desinstalar materia local: " + e.getMessage());
                eliminado = false;
            }

            boolean finalEliminado = eliminado;
            if (!fragment.isAdded()) {
                return;
            }

            fragment.requireActivity().runOnUiThread(() -> {
                accionOfflineEnProceso = false;
                if (finalEliminado) {
                    materiaDescargada = false;
                    fragment.showToastLong("Materia desinstalada del almacenamiento local");
                } else {
                    fragment.showToastLong("No se pudo desinstalar la materia");
                }
                fragment.actualizarEstadoBotonOffline(materiaDescargada, false);
            });
        }).start();
    }

    public boolean isMateriaDescargada() {
        return materiaDescargada;
    }

    public boolean isAccionOfflineEnProceso() {
        return accionOfflineEnProceso;
    }

    public void limpiarDatos() {
        listaTemas.clear();
    }

    public int getRealSize() {
        return listaTemas != null ? listaTemas.size() : 0;
    }
}