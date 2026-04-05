package com.cyberpath.smartlearn.logic.main.combo.agregarmateria;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.cyberpath.smartlearn.data.local.database.dao.ContenidoDAO;
import com.cyberpath.smartlearn.data.local.database.sincronizar.DescargarDatos;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.relaciones.UsuarioMateria;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.agregarmateria.AgregarMateriaFragment;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarMateriaLogic {
    private final AgregarMateriaFragment fragment;
    private final Context context;
    private Usuario usuarioActual;

    private final List<Materia> listaAllMaterias = new ArrayList<>();
    private final List<Materia> listaMateriasUsuario = new ArrayList<>();
    private final List<Materia> listaMateriasDisponibles = new ArrayList<>();
    @Getter
    private final List<Materia> listaMateriasFiltrada = new ArrayList<>();

    private boolean cargandoMateriasUsuario = false;
    private boolean cargandoAllMaterias = false;
    private boolean datosCargados = false;

    public AgregarMateriaLogic(AgregarMateriaFragment fragment) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        // Asegurar que el usuario esté cargado
        UsuarioCst.ensureUsuarioLoaded(context);
        this.usuarioActual = UsuarioCst.USUARIO_ACTUAL;
    }

    public void cargarDatos() {
        if (!datosCargados) {
            cargarMateriasUsuario(usuarioActual != null ? usuarioActual.getId() : null);
            cargarAllMaterias();
        } else {
            actualizarListaDisponibles();
            fragment.iniciarNavegacionPorVoz();
        }
    }

    public void limpiarDatos() {
        datosCargados = false;
        listaAllMaterias.clear();
        listaMateriasUsuario.clear();
        listaMateriasDisponibles.clear();
        listaMateriasFiltrada.clear();
    }

    private void cargarMateriasUsuario(Integer idUsuario) {
        cargandoMateriasUsuario = true;
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Materia>> call = apiService.getMateriasByUsuario(idUsuario);

        if (idUsuario == null) {
            listaMateriasUsuario.clear();
            cargandoMateriasUsuario = false;
            actualizarListaSiCargasListas();
            return;
        }

        call.enqueue(new Callback<List<Materia>>() {
            @Override
            public void onResponse(Call<List<Materia>> call, Response<List<Materia>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaMateriasUsuario.clear();
                    listaMateriasUsuario.addAll(response.body());
                } else {
                    fragment.showToast("Error al cargar tus materias");
                }
                cargandoMateriasUsuario = false;
                actualizarListaSiCargasListas();
            }

            @Override
            public void onFailure(Call<List<Materia>> call, Throwable t) {
                fragment.showToast("Error de conexión (materias usuario)");
                cargandoMateriasUsuario = false;
                actualizarListaSiCargasListas();
            }
        });
    }

    private void cargarAllMaterias() {
        cargandoAllMaterias = true;
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Materia>> call = apiService.getMaterias();

        call.enqueue(new Callback<List<Materia>>() {
            @Override
            public void onResponse(Call<List<Materia>> call, Response<List<Materia>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAllMaterias.clear();
                    listaAllMaterias.addAll(response.body());
                } else {
                    fragment.showToast("No hay más materias disponibles");
                }
                cargandoAllMaterias = false;
                actualizarListaSiCargasListas();
            }

            @Override
            public void onFailure(Call<List<Materia>> call, Throwable t) {
                fragment.showToast("Error de conexión (catálogo)");
                cargandoAllMaterias = false;
                actualizarListaSiCargasListas();
            }
        });
    }

    private void actualizarListaSiCargasListas() {
        if (cargandoMateriasUsuario || cargandoAllMaterias) return;
        actualizarListaDisponibles();
    }

    private void actualizarListaDisponibles() {
        Set<Integer> idsUsuario = new HashSet<>();
        for (Materia m : listaMateriasUsuario) {
            if (m != null && m.getId() != null) idsUsuario.add(m.getId());
        }

        List<Materia> disponibles = listaAllMaterias.stream()
                .filter(m -> m != null && m.getId() != null && !idsUsuario.contains(m.getId()))
                .collect(Collectors.toList());

        Integer idVisible = getCurrentVisibleMateriaId();

        listaMateriasDisponibles.clear();
        listaMateriasDisponibles.addAll(disponibles);

        listaMateriasFiltrada.clear();
        listaMateriasFiltrada.addAll(disponibles);

        fragment.actualizarAdapter(listaMateriasFiltrada);
        datosCargados = true;

        if (idVisible != null && !listaMateriasFiltrada.isEmpty()) {
            int newIndex = findIndexById(listaMateriasFiltrada, idVisible);
            if (newIndex >= 0) {
                int centered = centeredPositionForIndex(newIndex, listaMateriasFiltrada.size());
                fragment.moverViewPager(centered);
                fragment.iniciarNavegacionPorVoz();
                return;
            }
        }

        if (!listaMateriasFiltrada.isEmpty()) {
            fragment.moverViewPager(centeredPositionForIndex(0, listaMateriasFiltrada.size()));
        }

        fragment.iniciarNavegacionPorVoz();
    }

    private Integer getCurrentVisibleMateriaId() {
        int realPos = fragment.getPosicionActualViewPager();
        if (realPos >= 0 && realPos < listaMateriasFiltrada.size()) {
            Materia m = listaMateriasFiltrada.get(realPos);
            return m != null ? m.getId() : null;
        }
        return null;
    }

    public void aplicarFiltro(String query) {
        listaMateriasFiltrada.clear();
        if (query == null || query.trim().isEmpty()) {
            listaMateriasFiltrada.addAll(listaMateriasDisponibles);
        } else {
            String q = normalizarTexto(query);
            for (Materia m : listaMateriasDisponibles) {
                if (m == null) continue;
                String nombre = normalizarTexto(m.getNombre());
                if (nombre.contains(q)) listaMateriasFiltrada.add(m);
            }
        }

        fragment.actualizarAdapter(listaMateriasFiltrada);

        if (!listaMateriasFiltrada.isEmpty()) {
            fragment.moverViewPager(centeredPositionForIndex(0, listaMateriasFiltrada.size()));
        }
    }

    public void inscribirMateria(Materia materia, Consumer<Materia> onSuccess) {
        if (materia == null || materia.getId() == null) {
            fragment.showToast("Materia inválida");
            return;
        }

        // Validar y recargar usuario si es necesario
        if (usuarioActual == null || usuarioActual.getId() == null) {
            UsuarioCst.ensureUsuarioLoaded(context);
            usuarioActual = UsuarioCst.USUARIO_ACTUAL;
        }

        if (usuarioActual == null || usuarioActual.getId() == null) {
            fragment.showToast("Error: usuario no identificado");
            return;
        }

        UsuarioMateria inscripcion = new UsuarioMateria();
        inscripcion.setIdMateria(materia.getId());
        inscripcion.setIdUsuario(usuarioActual.getId());

        fragment.showToast("Inscribiendo...");

        ApiService api = RetrofitClient.getApiService();
        Call<UsuarioMateria> call = api.save(inscripcion);
        call.enqueue(new Callback<UsuarioMateria>() {
            @Override
            public void onResponse(Call<UsuarioMateria> call, Response<UsuarioMateria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fragment.showToastLong("¡Te has inscrito correctamente a " + materia.getNombre() + "!");
                    actualizarListasDespuesInscripcion(materia);
                    onSuccess.accept(materia);
                } else {
                    fragment.showToast("Error al inscribirte. Intenta de nuevo.");
                }
            }

            @Override
            public void onFailure(Call<UsuarioMateria> call, Throwable t) {
                fragment.showToastLong("Error de conexión: " + t.getMessage());
            }
        });
    }

    public void actualizarListasDespuesInscripcion(Materia materia) {
        listaMateriasDisponibles.removeIf(m -> m.getId().equals(materia.getId()));
        listaMateriasFiltrada.removeIf(m -> m.getId().equals(materia.getId()));
        listaMateriasUsuario.add(materia);
        fragment.actualizarAdapter(listaMateriasFiltrada);
        fragment.moverViewPager(centeredPositionForIndex(0, listaMateriasFiltrada.size()));
    }

    public void descargarMateria(Materia materia, ProgressBar progressBar,
                                 TextView tvProgresso, TextView tvMensajeProgreso,
                                 AlertDialog dialogo) {
        DescargarDatos descarga = new DescargarDatos(context);
        descarga.setCallback(new DescargarDatos.DescargaCallback() {
            @Override
            public void onDescargaIniciada() {
                // Opcional
            }

            @Override
            public void onProgreso(int porcentajeActual, String mensaje) {
                progressBar.setProgress(porcentajeActual);
                tvProgresso.setText(porcentajeActual + "%");
                tvMensajeProgreso.setText(mensaje);
            }

            @Override
            public void onDescargaCompletada() {
                ContenidoDAO dao = new ContenidoDAO(context);
                dao.registrarMateriaDescargada(materia, 0.0);
                fragment.showToastLong("¡Descarga completada!");
                dialogo.dismiss();
            }

            @Override
            public void onDescargaFallida(String error) {
                fragment.showToastLong("Error en descarga: " + error);
                dialogo.dismiss();
            }
        });

        descarga.descargarMateria(materia);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private int findIndexById(List<Materia> list, Integer id) {
        if (id == null || list == null) return -1;
        for (int i = 0; i < list.size(); i++) {
            Materia m = list.get(i);
            if (m != null && id.equals(m.getId())) return i;
        }
        return -1;
    }

    public int centeredPositionForIndex(int index, int realSize) {
        if (realSize <= 1) return index;
        int half = Integer.MAX_VALUE / 2;
        int base = half - (half % realSize);
        return base + (index % realSize);
    }
}