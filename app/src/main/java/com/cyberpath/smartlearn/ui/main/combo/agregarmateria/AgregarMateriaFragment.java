package com.cyberpath.smartlearn.ui.main.combo.agregarmateria;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.relaciones.UsuarioMateria;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarMateriaFragment extends Fragment {
    private final Usuario usuarioActual = UsuarioCst.USUARIO_ACTUAL;

    private final List<Materia> listaAllMaterias = new ArrayList<>();
    private final List<Materia> listaMateriasUsuario = new ArrayList<>();
    private final List<Materia> listaMateriasDisponibles = new ArrayList<>();
    private final List<Materia> listaMateriasFiltrada = new ArrayList<>();;

    private SearchView searchViewMaterias;

    private ViewPager2 carruselMaterias;
    private AdaptadorAgregarMaterias adapterMaterias;
    private boolean cargandoMateriasUsuario = false;
    private boolean cargandoAllMaterias = false;

    private boolean datosCargados = false;

    private final Handler filtroHandler = new Handler(Looper.getMainLooper());
    private Runnable filtroRunnable;

    private NavAccesibilidad navAccesibilidad;

    EntradaAudio entradaAudio = EntradaAudio.obtenerInstancia();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agregar_materia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        entradaAudio.detenerEscucha();


        adapterMaterias = new AdaptadorAgregarMaterias(new ArrayList<>(), this::mostrarDialogoInscribir);

        searchViewMaterias = view.findViewById(R.id.searchViewMaterias);

        carruselMaterias = view.findViewById(R.id.viewPagerMaterias);
        carruselMaterias.setAdapter(adapterMaterias);
        carruselMaterias.setOffscreenPageLimit(3);
        carruselMaterias.setClipToPadding(false);
        carruselMaterias.setClipChildren(false);
        carruselMaterias.setPageTransformer(new ViewPager2.PageTransformer() {
            private static final float MIN_SCALE = 0.85f;
            private static final float MIN_ALPHA = 0.5f;

            @Override
            public void transformPage(@NonNull View page, float position) {
                if (position < -1 || position > 1) {
                    page.setAlpha(0f);
                } else {
                    float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position) * 0.15f);
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
                }
            }
        });

        navAccesibilidad = new NavAccesibilidad(requireContext(), this, carruselMaterias, adapterMaterias, listaMateriasFiltrada);

        searchViewMaterias.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                aplicarFiltro(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (filtroRunnable != null) {
                    filtroHandler.removeCallbacks(filtroRunnable);
                }
                filtroRunnable = () -> aplicarFiltro(newText);
                filtroHandler.postDelayed(filtroRunnable, 300);
                return true;
            }
        });

        if (!datosCargados) {
            Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
            cargarMateriasUsuario(idUsuario);
            cargarAllMaterias();
        } else {
            actualizarListaDisponibles();
            navAccesibilidad.iniciarNavegacion();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (filtroRunnable != null) {
            filtroHandler.removeCallbacks(filtroRunnable);
            filtroRunnable = null;
        }
        if (navAccesibilidad != null) {
            navAccesibilidad.detenerNavegacion();
        }
        searchViewMaterias = null;
        carruselMaterias = null;
        adapterMaterias = null;
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
                    Toast.makeText(getContext(), "Error al cargar tus materias", Toast.LENGTH_SHORT).show();
                }
                cargandoMateriasUsuario = false;
                actualizarListaSiCargasListas();
            }

            @Override
            public void onFailure(Call<List<Materia>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión (materias usuario)", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Error al cargar catálogo de materias", Toast.LENGTH_SHORT).show();
                }
                cargandoAllMaterias = false;
                actualizarListaSiCargasListas();
            }

            @Override
            public void onFailure(Call<List<Materia>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión (catálogo)", Toast.LENGTH_SHORT).show();
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
        for (Materia m : listaMateriasUsuario) if (m != null && m.getId() != null) idsUsuario.add(m.getId());

        List<Materia> disponibles = listaAllMaterias.stream()
                .filter(m -> m != null && m.getId() != null && !idsUsuario.contains(m.getId()))
                .collect(Collectors.toList());

        Integer idVisible = getCurrentVisibleMateriaId();

        listaMateriasDisponibles.clear();
        listaMateriasDisponibles.addAll(disponibles);

        listaMateriasFiltrada.clear();
        listaMateriasFiltrada.addAll(disponibles);

        if (adapterMaterias != null) {
            adapterMaterias.actualizarLista(new ArrayList<>(listaMateriasFiltrada));
            datosCargados = true;

            if (idVisible != null && !listaMateriasFiltrada.isEmpty()) {
                int newIndex = findIndexById(listaMateriasFiltrada, idVisible);
                if (newIndex >= 0) {
                    int centered = centeredPositionForIndex(newIndex, adapterMaterias.getRealSize());
                    if (carruselMaterias != null) carruselMaterias.setCurrentItem(centered, false);
                    // iniciar navegación por voz sólo si lo deseamos aquí
                    if (navAccesibilidad != null) navAccesibilidad.iniciarNavegacion();
                    return;
                }
            }

            if (!listaMateriasFiltrada.isEmpty() && carruselMaterias != null) {
                int centered = centeredPositionForIndex(0, adapterMaterias.getRealSize());
                carruselMaterias.setCurrentItem(centered, false);
            }

            // Iniciar navegación por voz automáticamente cuando los datos estén listos
            if (navAccesibilidad != null) navAccesibilidad.iniciarNavegacion();
        }
    }

    private Integer getCurrentVisibleMateriaId() {
        if (carruselMaterias == null || adapterMaterias == null) return null;
        int current = carruselMaterias.getCurrentItem();
        int realSize = adapterMaterias.getRealSize();
        if (realSize == 0) return null;
        int realPos = current % realSize;
        if (realPos < 0) realPos += realSize;
        if (realPos >= 0 && realPos < listaMateriasFiltrada.size()) {
            Materia m = listaMateriasFiltrada.get(realPos);
            return m != null ? m.getId() : null;
        }
        return null;
    }

    private int findIndexById(List<Materia> list, Integer id) {
        if (id == null || list == null) return -1;
        for (int i = 0; i < list.size(); i++) {
            Materia m = list.get(i);
            if (m != null && id.equals(m.getId())) return i;
        }
        return -1;
    }

    private int centeredPositionForIndex(int index, int realSize) {
        if (realSize <= 1) return index;
        int half = Integer.MAX_VALUE / 2;
        int base = half - (half % realSize);
        return base + (index % realSize);
    }

    private void mostrarDialogoInscribir(Materia materia) {
        View vista = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_aceptar_cancelar, null);
        TextView tvMensaje = vista.findViewById(R.id.tvMensaje);
        tvMensaje.setText("¿Deseas inscribirte en: " + (materia != null ? materia.getNombre() : "") + "?");

        final androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(vista)
                .setCancelable(true)
                .show();

        vista.findViewById(R.id.btnAceptar).setOnClickListener(v -> {
            dialog.dismiss();
            if (materia != null) inscribirMateria(materia);
        });

        vista.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());
    }

    private void inscribirMateria(Materia materia) {
        if (materia == null || materia.getId() == null) {
            Toast.makeText(getContext(), "Materia inválida", Toast.LENGTH_SHORT).show();
            return;
        }
        Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
        if (idUsuario == null) {
            Toast.makeText(getContext(), "Error: usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        UsuarioMateria inscripcion = new UsuarioMateria();
        inscripcion.setIdMateria(materia.getId());
        inscripcion.setIdUsuario(idUsuario);

        Toast.makeText(getContext(), "Inscribiendo...", Toast.LENGTH_SHORT).show();

        ApiService api = RetrofitClient.getApiService();
        Call<UsuarioMateria> call = api.saveUsuarioMateria(inscripcion);
        call.enqueue(new Callback<UsuarioMateria>() {
            @Override
            public void onResponse(Call<UsuarioMateria> call, Response<UsuarioMateria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(),
                            "¡Te has inscrito correctamente a " + materia.getNombre() + "!",
                            Toast.LENGTH_LONG).show();

                    // Actualizar listas locales y adapter
                    listaMateriasDisponibles.removeIf(m -> m.getId().equals(materia.getId()));
                    listaMateriasFiltrada.removeIf(m -> m.getId().equals(materia.getId()));
                    listaMateriasUsuario.add(materia);

                    if (adapterMaterias != null) adapterMaterias.actualizarLista(new ArrayList<>(listaMateriasFiltrada));

                    if (carruselMaterias != null && adapterMaterias.getItemCount() > 0) {
                        int centered = centeredPositionForIndex(0, adapterMaterias.getRealSize());
                        carruselMaterias.setCurrentItem(centered, false);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al inscribirte. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UsuarioMateria> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Método usado por NavAccesibilidad para inscribir por voz (ya lo tenías)
    public void inscribirMateriaDesdeAccesibilidad(Materia materia, BiConsumer<Boolean, Materia> callback) {
        if (materia == null || materia.getId() == null) {
            callback.accept(false, materia);
            return;
        }
        Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
        if (idUsuario == null) {
            callback.accept(false, materia);
            return;
        }

        UsuarioMateria inscripcion = new UsuarioMateria();
        inscripcion.setIdMateria(materia.getId());
        inscripcion.setIdUsuario(idUsuario);

        ApiService api = RetrofitClient.getApiService();
        Call<UsuarioMateria> call = api.saveUsuarioMateria(inscripcion);
        call.enqueue(new Callback<UsuarioMateria>() {
            @Override
            public void onResponse(Call<UsuarioMateria> call, Response<UsuarioMateria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.accept(true, materia);
                } else {
                    callback.accept(false, materia);
                }
            }

            @Override
            public void onFailure(Call<UsuarioMateria> call, Throwable t) {
                callback.accept(false, materia);
            }
        });
    }

    // Actualiza UI y listas después de una inscripción por voz (NavAccesibilidad la llamará)
    public void actualizarListasDespuesInscripcion(Materia materia) {
        // Actualizar listas locales
        listaMateriasDisponibles.removeIf(m -> m.getId().equals(materia.getId()));
        listaMateriasFiltrada.removeIf(m -> m.getId().equals(materia.getId()));
        listaMateriasUsuario.add(materia);

        // Actualizar adapter
        if (adapterMaterias != null) {
            adapterMaterias.actualizarLista(new ArrayList<>(listaMateriasFiltrada));
        }

        // Ajustar ViewPager si es necesario
        if (carruselMaterias != null && adapterMaterias != null && adapterMaterias.getItemCount() > 0) {
            int centered = centeredPositionForIndex(0, adapterMaterias.getRealSize());
            carruselMaterias.setCurrentItem(centered, false);
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private void aplicarFiltro(String query) {
        if (carruselMaterias != null && carruselMaterias.getScrollState() != ViewPager2.SCROLL_STATE_IDLE) {
            filtroHandler.postDelayed(() -> aplicarFiltro(query), 150);
            return;
        }

        listaMateriasFiltrada.clear();
        if (TextUtils.isEmpty(query) || query.trim().isEmpty()) {
            listaMateriasFiltrada.addAll(listaMateriasDisponibles);
        } else {
            String q = normalizarTexto(query);
            for (Materia m : listaMateriasDisponibles) {
                if (m == null) continue;
                String nombre = normalizarTexto(m.getNombre());
                if (nombre.contains(q)) listaMateriasFiltrada.add(m);
            }
        }

        Integer visibleId = getCurrentVisibleMateriaId();

        if (adapterMaterias != null) {
            adapterMaterias.actualizarLista(new ArrayList<>(listaMateriasFiltrada));
            if (!listaMateriasFiltrada.isEmpty()) {
                if (visibleId != null) {
                    int idx = findIndexById(listaMateriasFiltrada, visibleId);
                    if (idx >= 0) {
                        int centered = centeredPositionForIndex(idx, adapterMaterias.getRealSize());
                        if (carruselMaterias != null) carruselMaterias.setCurrentItem(centered, false);
                        return;
                    }
                }
                // si no se preserva, centra en 0
                int centered = centeredPositionForIndex(0, adapterMaterias.getRealSize());
                if (carruselMaterias != null) carruselMaterias.setCurrentItem(centered, false);
            } else {
            }
        }
    }
}