package com.cyberpath.smartlearn.logic.main.combo.principal.materia;

import android.content.Context;
import android.util.Log;

import com.cyberpath.smartlearn.data.local.database.repository.MateriasRepository;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.principal.materia.MateriasFragment;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.network.NetworkUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MateriasLogic {

    private final MateriasFragment fragment;
    private final Context context;
    private final Usuario usuarioActual;
    private final MateriasRepository materiasRepository;

    private final List<Materia> listaMaterias = new ArrayList<>();
    @Getter
    private final List<Materia> listaMateriasFiltrada = new ArrayList<>();

    private boolean cargandoMateriasUsuario = false;
    private boolean datosCargados = false;
    private boolean modoOffline = false;

    private int progresosPendientes = 0;
    private final Map<Integer, Boolean> progresoEnProceso = new HashMap<>();


    public MateriasLogic(MateriasFragment fragment) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        UsuarioCst.ensureUsuarioLoaded(context);
        this.usuarioActual = UsuarioCst.USUARIO_ACTUAL;
        this.materiasRepository = new MateriasRepository(context);
    }

    public void cargarMaterias() {
        if (!datosCargados) {
            if (NetworkUtils.isInternetAvailable(context)) {
                modoOffline = false;
                cargarMateriasAPI(usuarioActual != null ? usuarioActual.getId() : null);
            } else {
                modoOffline = true;
                cargarMateriasLocal();
            }
        } else {
            actualizarListaMaterias();
            fragment.iniciarNavegacionPorVoz();
        }
    }

    public void limpiarDatos() {
        datosCargados = false;
        listaMaterias.clear();
        listaMateriasFiltrada.clear();
        progresoEnProceso.clear();
    }

    private void cargarMateriasAPI(Integer idUsuario) {
        cargandoMateriasUsuario = true;
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Materia>> call = apiService.getMateriasByUsuario(idUsuario);

        if (idUsuario == null) {
            listaMaterias.clear();
            cargandoMateriasUsuario = false;
            actualizarListaMaterias();
            return;
        }

        call.enqueue(new Callback<List<Materia>>() {
            @Override
            public void onResponse(Call<List<Materia>> call, Response<List<Materia>> response) {
                if (fragment == null || !fragment.isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    listaMaterias.clear();
                    listaMaterias.addAll(response.body());
                    listaMateriasFiltrada.clear();
                    listaMateriasFiltrada.addAll(listaMaterias);

                    if (listaMaterias.isEmpty()) {
                        fragment.showToast("No tienes materias inscritas.");
                    }
                    actualizarListaMaterias();
                    progresosPendientes = listaMaterias.size();
                    for (Materia m : listaMaterias) {
                        if (m != null && m.getId() != null) {
                            calcularYActualizarProgreso(m, idUsuario);
                        }
                    }
                } else {
                    fragment.showToast("Error al cargar tus materias");
                }

                cargandoMateriasUsuario = false;
            }

            @Override
            public void onFailure(Call<List<Materia>> call, Throwable t) {
                if (fragment == null || !fragment.isAdded()) return;
                fragment.showToast("Error de conexión (materias usuario)");
                cargandoMateriasUsuario = false;
                actualizarListaMaterias();
            }
        });
    }

    private void cargarMateriasLocal() {
        cargandoMateriasUsuario = true;

        try {
            List<Materia> materiasLocales = materiasRepository.obtenerTodasLasMaterias();

            listaMaterias.clear();
            listaMaterias.addAll(materiasLocales);
            listaMateriasFiltrada.clear();
            listaMateriasFiltrada.addAll(listaMaterias);

            if (listaMaterias.isEmpty()) {
                fragment.showToast("No hay materias disponibles sin conexión a internet");
            } else {
                fragment.showToast("Modo offline - Mostrando materias locales");
            }
            actualizarListaMaterias();
            progresosPendientes = listaMaterias.size();
            Integer idUsuario = usuarioActual != null ? usuarioActual.getId() : null;

            for (Materia m : listaMaterias) {
                if (m != null && m.getId() != null && idUsuario != null) {
                    calcularProgresoLocal(m, idUsuario);
                } else {
                    decrementarProgresosPendientes();
                }
            }
        } catch (Exception e) {
            Log.e("MateriasLogic", "Error al cargar materias locales: " + e.getMessage());
            fragment.showToast("Error al cargar materias locales");
            actualizarListaMaterias();
        }

        cargandoMateriasUsuario = false;
    }

    private void actualizarListaMaterias() {
        listaMateriasFiltrada.clear();
        listaMateriasFiltrada.addAll(listaMaterias);

        fragment.actualizarAdapter(listaMateriasFiltrada);
        datosCargados = true;

        if (!listaMateriasFiltrada.isEmpty()) {
            fragment.moverViewPager(1);
        }

        fragment.iniciarNavegacionPorVoz();
    }

    public void filtrarMaterias(String query) {
        listaMateriasFiltrada.clear();

        if (query == null || query.trim().isEmpty()) {
            listaMateriasFiltrada.addAll(listaMaterias);
        } else {
            String q = normalizarTexto(query);
            for (Materia m : listaMaterias) {
                if (m == null) continue;
                String nombre = normalizarTexto(m.getNombre());
                if (nombre.contains(q)) listaMateriasFiltrada.add(m);
            }
        }

        fragment.actualizarAdapter(listaMateriasFiltrada);

        if (!listaMateriasFiltrada.isEmpty()) {
            fragment.moverViewPager(1);
        }
    }

    private void calcularYActualizarProgreso(Materia materia, Integer idUsuario) {
        if (materia == null || materia.getId() == null) {
            decrementarProgresosPendientes();
            return;
        }
        if (progresoEnProceso.getOrDefault(materia.getId(), false)) {
            decrementarProgresosPendientes();
            return;
        }

        progresoEnProceso.put(materia.getId(), true);

        ApiService apiService = RetrofitClient.getApiService();
        Call<Long> callTotal = apiService.getTotalEjerciciosByMateria(materia.getId());

        callTotal.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (fragment == null || !fragment.isAdded()) {
                    decrementarProgresosPendientes();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    Long total = response.body();
                    Log.d("MateriasLogic", "Total ejercicios para materia " + materia.getId() + ": " + total);

                    Call<Long> callRealizados = apiService.getEjerciciosRealizadosByUsuarioAndMateria(idUsuario, materia.getId());
                    callRealizados.enqueue(new Callback<Long>() {
                        @Override
                        public void onResponse(Call<Long> call, Response<Long> response) {
                            if (fragment == null || !fragment.isAdded()) {
                                decrementarProgresosPendientes();
                                return;
                            }

                            if (response.isSuccessful() && response.body() != null) {
                                Long realizados = response.body();
                                int progreso = total > 0 ? (int) ((realizados * 100) / total) : 0;
                                materia.setProgreso(progreso);
                                Log.d("MateriasLogic", "Ejercicios realizados: " + realizados + ", Progreso: " + progreso);
                                fragment.notificarCambiosAdapter();
                            } else {
                                materia.setProgreso(0);
                            }

                            decrementarProgresosPendientes();
                        }

                        @Override
                        public void onFailure(Call<Long> call, Throwable t) {
                            Log.e("MateriasLogic", "Error en callRealizados: " + t.getMessage());
                            materia.setProgreso(0);
                            decrementarProgresosPendientes();
                        }
                    });
                } else {
                    materia.setProgreso(0);
                    decrementarProgresosPendientes();
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("MateriasLogic", "Error en callTotal: " + t.getMessage());
                materia.setProgreso(0);
                decrementarProgresosPendientes();
            }
        });
    }

    private void calcularProgresoLocal(Materia materia, Integer idUsuario) {
        if (materia == null || materia.getId() == null) {
            decrementarProgresosPendientes();
            return;
        }

        try {
            int progreso = materiasRepository.calcularProgresoLocal(materia.getId(), idUsuario);
            materia.setProgreso(progreso);
            Log.d("MateriasLogic", "Progreso local para materia " + materia.getId() + ": " + progreso + "%");
            fragment.notificarCambiosAdapter();
        } catch (Exception e) {
            Log.e("MateriasLogic", "Error al calcular progreso local: " + e.getMessage());
            materia.setProgreso(0);
        }

        decrementarProgresosPendientes();
    }

    private void decrementarProgresosPendientes() {
        progresosPendientes--;
        if (progresosPendientes <= 0) {
            progresosPendientes = 0;
            fragment.notificarCambiosAdapter();
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    public int getPosicionActual() {
        return fragment.getPosicionActualViewPager();
    }

    public void setPosicionActual(int posicion) {
        fragment.moverViewPager(centeredPositionForIndex(posicion, listaMateriasFiltrada.size()));
    }

    public int centeredPositionForIndex(int index, int realSize) {
        if (realSize <= 0) return 0;
        if (realSize == 1) return index;
        int half = Integer.MAX_VALUE / 2;
        int base = half - (half % realSize);
        return base + (index % realSize);
    }
}