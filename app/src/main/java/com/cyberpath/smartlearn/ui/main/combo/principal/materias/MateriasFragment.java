package com.cyberpath.smartlearn.ui.main.combo.principal.materias;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MateriasFragment extends Fragment {
    private final Usuario usuarioActual = UsuarioCst.USUARIO_ACTUAL;

    private final List<Materia> listaMaterias = new ArrayList<>();
    private final List<Materia> listaMateriasFiltrada = new ArrayList<>();

    private NavAccesibilidadMaterias navAccesibilidadMaterias;

    private SearchView searchViewMaterias;

    private ViewPager2 carruselMaterias;
    private AdaptadorMaterias adapterMaterias;
    private boolean cargandoMateriasUsuario = false;

    private LinearLayout btnUltimoSubtema;
    private TextView tvUltimoSubtema;

    private boolean datosCargados = false;

    private final Handler filtroHandler = new Handler(Looper.getMainLooper());
    private Runnable filtroRunnable;
    private TextView nombreUusario;

    EntradaAudio entradaAudio = EntradaAudio.obtenerInstancia();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_materias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });

        entradaAudio.detenerEscucha();

        nombreUusario = getActivity().findViewById(R.id.tv_nombre_usuario);
        nombreUusario.setText("Hola, " + usuarioActual.getNombreCuenta());

        adapterMaterias = new AdaptadorMaterias(new ArrayList<>(), this::onMateriaClick);

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

        navAccesibilidadMaterias = new NavAccesibilidadMaterias(requireContext(), this, carruselMaterias, adapterMaterias, listaMateriasFiltrada);

        searchViewMaterias.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarMaterias(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (filtroRunnable != null) {
                    filtroHandler.removeCallbacks(filtroRunnable);
                }
                filtroRunnable = () -> filtrarMaterias(newText);
                filtroHandler.postDelayed(filtroRunnable, 300);
                return true;
            }
        });

        if (!datosCargados) {
            cargarMaterias(usuarioActual != null ? usuarioActual.getId() : null);
        } else {
            actualizarListaMaterias();
            navAccesibilidadMaterias.iniciarNavegacion();
        }

        crearUltimoSubtema(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (filtroRunnable != null) {
            filtroHandler.removeCallbacks(filtroRunnable);
            filtroRunnable = null;
        }
        if (navAccesibilidadMaterias != null) {
            navAccesibilidadMaterias.detenerNavegacion();
        }
        searchViewMaterias = null;
        carruselMaterias = null;
        adapterMaterias = null;
        navAccesibilidadMaterias = null;
        datosCargados = false;
        listaMaterias.clear();
        listaMateriasFiltrada.clear();
    }

    public void crearUltimoSubtema(View view) {
        tvUltimoSubtema = view.findViewById(R.id.tv_ultimo_subtema);
        btnUltimoSubtema = view.findViewById(R.id.btn_ultimo_subtema);

        int idUltimoSubtema = PreferencesManager.getIdSubtemaUltimaConexion(requireContext());
        if (idUltimoSubtema == -1) {
            tvUltimoSubtema.setText("Es tu primera vez, no tienes un historial");
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<Subtema> call = apiService.getSubtemaById(idUltimoSubtema);
        call.enqueue(new Callback<Subtema>() {
            @Override
            public void onResponse(Call<Subtema> call, Response<Subtema> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Subtema subtema = response.body();
                    tvUltimoSubtema.setText(subtema.getNombre());
                    btnUltimoSubtema.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            navegarUltimoSubtema(subtema);
                        }
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        tvUltimoSubtema.setText("Error al cargar el subtema");
                    });
                }
            }

            @Override
            public void onFailure(Call<Subtema> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    tvUltimoSubtema.setText("Error de conexión, intenta más tarde");
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void cargarMaterias(Integer idUsuario) {
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
                if (response.isSuccessful() && response.body() != null) {
                    listaMaterias.clear();
                    listaMaterias.addAll(response.body());

                    // PROTECCIÓN: no acceder a listaMaterias.get(0) sin comprobar tamaño y contexto
                    if (getContext() != null) {
                        if (listaMaterias.isEmpty()) {
                            Toast.makeText(getContext(), "No tienes materias inscritas.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    for (Materia m : listaMaterias) {
                        calcularYActualizarProgreso(m, idUsuario);
                    }
                } else {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Error al cargar tus materias", Toast.LENGTH_SHORT).show();
                }
                cargandoMateriasUsuario = false;
                actualizarListaMaterias();
            }

            @Override
            public void onFailure(Call<List<Materia>> call, Throwable t) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Error de conexión (materias usuario)", Toast.LENGTH_SHORT).show();
                cargandoMateriasUsuario = false;
                actualizarListaMaterias();
            }
        });
    }

    private void actualizarListaMaterias() {
        listaMateriasFiltrada.clear();
        listaMateriasFiltrada.addAll(listaMaterias);

        if (adapterMaterias != null) {
            adapterMaterias.actualizarLista(new ArrayList<>(listaMateriasFiltrada));
            datosCargados = true;

            if (!listaMateriasFiltrada.isEmpty() && carruselMaterias != null) {
                int centered = centeredPositionForIndex(0, adapterMaterias.getRealSize());
                carruselMaterias.setCurrentItem(centered, false);
            }
        }

        if (navAccesibilidadMaterias != null) navAccesibilidadMaterias.iniciarNavegacion();
    }

    private void onMateriaClick(Materia materia) {
        var action = MateriasFragmentDirections.actionMateriasFragmentToTemasFragment(materia);
        NavHostFragment.findNavController(this).navigate(action);
    }

    private void navegarUltimoSubtema(Subtema subtema) {
        View vista = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_teoria_practica, null);
        TextView tvMensaje = vista.findViewById(R.id.tv_titulo_subtema);
        tvMensaje.setText(subtema.getNombre());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(vista)
                .setCancelable(true)
                .show();

        vista.findViewById(R.id.btn_teoria).setOnClickListener(v -> {
            var action = MateriasFragmentDirections.actionMateriasFragmentToTeoriaFragment(subtema, null);
            NavHostFragment.findNavController(this).navigate(action);
            dialog.dismiss();
        });

        vista.findViewById(R.id.btn_practica).setOnClickListener(v -> {
            var action = MateriasFragmentDirections.actionMateriasFragmentToPracticaFragment(subtema);
            NavHostFragment.findNavController(this).navigate(action);
            dialog.dismiss();
        });

        vista.findViewById(R.id.btn_cancelar).setOnClickListener(v -> dialog.dismiss());
    }

    private void filtrarMaterias(String query) {
        if (carruselMaterias != null && carruselMaterias.getScrollState() != ViewPager2.SCROLL_STATE_IDLE) {
            filtroHandler.postDelayed(() -> filtrarMaterias(query), 150);
            return;
        }

        listaMateriasFiltrada.clear();
        if (TextUtils.isEmpty(query) || query.trim().isEmpty()) {
            listaMateriasFiltrada.addAll(listaMaterias);
        } else {
            String q = normalizarTexto(query);
            for (Materia m : listaMaterias) {
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
                // Si no se preserva, centra en 0
                int centered = centeredPositionForIndex(0, adapterMaterias.getRealSize());
                if (carruselMaterias != null) carruselMaterias.setCurrentItem(centered, false);
            } else {
                // Lista vacía: no hacer nada
            }
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private void calcularYActualizarProgreso(Materia materia, Integer idUsuario) {
        ApiService apiService = RetrofitClient.getApiService();

        Call<Long> callTotal = apiService.getTotalEjerciciosByMateria(materia.getId());
        callTotal.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long total = response.body();
                    Log.d("MateriasFragment", "Total ejercicios para materia " + materia.getId() + ": " + total);

                    Call<Long> callRealizados = apiService.getEjerciciosRealizadosByUsuarioAndMateria(idUsuario, materia.getId());
                    callRealizados.enqueue(new Callback<Long>() {
                        @Override
                        public void onResponse(Call<Long> call, Response<Long> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Long realizados = response.body();
                                int progreso = total > 0 ? (int) ((realizados * 100) / total) : 0;
                                Log.d("MateriasFragment", "Ejercicios realizados: " + realizados + ", Progreso: " + progreso);

                                materia.setProgreso(progreso);
                                if (adapterMaterias != null) adapterMaterias.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Call<Long> call, Throwable t) {
                            Log.e("MateriasFragment", "Error en callRealizados: " + (t != null ? t.getMessage() : "Error desconocido"));
                            materia.setProgreso(0);
                            if (adapterMaterias != null) adapterMaterias.notifyDataSetChanged();
                            if (getContext() != null)
                                Toast.makeText(getContext(), "Error al calcular progreso: " + (t != null ? t.getMessage() : "Error desconocido"), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("MateriasFragment", "Error en callTotal: " + (t != null ? t.getMessage() : "Error desconocido"));
                materia.setProgreso(0);
                if (adapterMaterias != null) adapterMaterias.notifyDataSetChanged();
                if (getContext() != null)
                    Toast.makeText(getContext(), "Error al obtener total ejercicios: " + (t != null ? t.getMessage() : "Error desconocido"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void entrarMateriaDesdeAccesibilidad(Materia materia, BiConsumer<Boolean, Materia> callback) {
        if (materia == null || materia.getId() == null) {
            callback.accept(false, materia);
            return;
        }
        if (usuarioActual == null || usuarioActual.getId() == null) {
            callback.accept(false, materia);
            return;
        }

        // Navegar a TemasFragment, igual que en onMateriaClick
        var action = MateriasFragmentDirections.actionMateriasFragmentToTemasFragment(materia);
        NavHostFragment.findNavController(this).navigate(action);

        // Indicar éxito (la navegación es síncrona)
        callback.accept(true, materia);
    }

    // Métodos auxiliares para el carrusel (iguales a AgregarMateriasFragment)
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
}