package com.cyberpath.smartlearn.ui.main.combo.principal.materias;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.model.usuario.UltimaConexion;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java.util.function.BiConsumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * SubtemasFragment extendido con navegación por voz (NavAccesibilidad).
 * - Lee subtemas, permite navegar por voz y entrar al subtema (teoría/práctica).
 * - Permite simular volver a la sección de temas mediante comando "regresar".
 */
public class SubtemasFragment extends Fragment {
    private final List<Subtema> listaSubtemas = new ArrayList<>();
    private ViewPager2 viewPagerSubtemas;
    private AdaptadorSubtemas adapterSubtemas;
    private TextView textoTema;
    private Tema tema;

    private int ultimaPosicionReal = 1;

    private NavAccesibilidadSubtemas navAccesibilidad;

    EntradaAudio entradaAudio = EntradaAudio.obtenerInstancia();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subtemas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        entradaAudio.detenerEscucha();

        textoTema = view.findViewById(R.id.tvNombreTema);
        crearCarrusel(view);

        tema = SubtemasFragmentArgs.fromBundle(getArguments()).getTema();
        int temaId = tema.getId();
        textoTema.setText(tema.getNombre());

        // inicializar NavAccesibilidad con la lista (vacía por ahora)
        navAccesibilidad = new NavAccesibilidadSubtemas(requireContext(), this, viewPagerSubtemas, adapterSubtemas, listaSubtemas);

        cargarSubtemas(temaId);
    }

    private void cargarSubtemas(int temaId) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Subtema>> call = apiService.getSubtemasByTema(temaId);

        call.enqueue(new Callback<List<Subtema>>() {
            @Override
            public void onResponse(Call<List<Subtema>> call, Response<List<Subtema>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subtema> subtemas = response.body();
                    requireActivity().runOnUiThread(() -> {
                        listaSubtemas.clear();
                        listaSubtemas.addAll(subtemas);
                        adapterSubtemas.actualizarLista(new ArrayList<>(listaSubtemas));
                        adapterSubtemas.notifyDataSetChanged();

                        if (subtemas.size() > 0) {
                            viewPagerSubtemas.setCurrentItem(1, false);
                        }

                        if (subtemas.isEmpty()) {
                            Toast.makeText(requireContext(), "No hay subtemas disponibles", Toast.LENGTH_SHORT).show();
                        }

                        // Iniciar navegación por voz una vez que el adapter y ViewPager están listos
                        if (navAccesibilidad != null) {
                            // Pequeña protección: iniciar sólo si no está ya activa
                            mainHandler.postDelayed(() -> navAccesibilidad.iniciarNavegacion(), 200);
                        }
                    });
                } else {
                    mostrarError("Error al cargar subtemas");
                }
            }

            @Override
            public void onFailure(Call<List<Subtema>> call, Throwable t) {
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void mostrarError(String mensaje) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
    }

    private void onSubtemaClick(Subtema subtema) {
        guardarUltimaConexion(subtema);
        navegarContenido(subtema);
    }

    public void crearCarrusel(View view) {
        viewPagerSubtemas = view.findViewById(R.id.viewPagerSubtemas);
        adapterSubtemas = new AdaptadorSubtemas(listaSubtemas, this::onSubtemaClick);
        viewPagerSubtemas.setAdapter(adapterSubtemas);

        viewPagerSubtemas.setCurrentItem(1, false);
        viewPagerSubtemas.setOffscreenPageLimit(3);
        viewPagerSubtemas.setClipToPadding(false);
        viewPagerSubtemas.setClipChildren(false);
        viewPagerSubtemas.setPadding(80, 0, 80, 0);

        viewPagerSubtemas.setPageTransformer((page, position) -> {
            float scaleFactor = Math.max(0.85f, 1 - Math.abs(position) * 0.15f);
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setAlpha(0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * 0.5f);
        });

        viewPagerSubtemas.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private boolean isResetting = false;

            @Override
            public void onPageSelected(int position) {
                int realSize = listaSubtemas.size();
                if (realSize > 0) {
                    ultimaPosicionReal = (position - 1 + realSize) % realSize;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE && !isResetting) {
                    int position = viewPagerSubtemas.getCurrentItem();
                    int realSize = listaSubtemas.size();

                    if (realSize <= 1) return;

                    if (position == 0) {
                        isResetting = true;
                        viewPagerSubtemas.setCurrentItem(realSize, false);
                        viewPagerSubtemas.post(() -> {
                            viewPagerSubtemas.requestTransform();
                            isResetting = false;
                        });
                    } else if (position == realSize + 1) {
                        isResetting = true;
                        viewPagerSubtemas.setCurrentItem(1, false);
                        viewPagerSubtemas.post(() -> {
                            viewPagerSubtemas.requestTransform();
                            isResetting = false;
                        });
                    }
                }
            }
        });
    }

    public void guardarUltimaConexion(Subtema subtema) {
        UltimaConexion ultimaConexion = new UltimaConexion();
        ApiService apiService = RetrofitClient.getApiService();

        Calendar tiempo = Calendar.getInstance();
        SimpleDateFormat formatoTiempo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fecha = formatoTiempo.format(tiempo.getTime());
        Integer idUsuario = UsuarioCst.obtenerIdUsuarioActual(requireContext());
        if (idUsuario == null) return;

        ultimaConexion.setUltimaConexion(fecha);
        ultimaConexion.setIdUsuario(idUsuario);
        ultimaConexion.setIdSubtema(subtema.getId());

        Call<UltimaConexion> createCall = apiService.saveUltimaConexion(ultimaConexion);
        createCall.enqueue(new Callback<UltimaConexion>() {
            @Override
            public void onResponse(Call<UltimaConexion> call, Response<UltimaConexion> response) {
                if (!isAdded()) return;
                android.content.Context ctx = getContext();
                if (ctx == null) return;
                if (response.isSuccessful()) {
                    PreferencesManager.setIdSubtemaUltimaConexion(ctx, subtema.getId());
                    return;
                }
                // Si ya existe (409/conflicto), lo tratamos como éxito: el backend ya tiene un registro.
                if (response.code() == 409) {
                    PreferencesManager.setIdSubtemaUltimaConexion(ctx, subtema.getId());
                } else {
                    Toast.makeText(
                            ctx,
                            "No se pudo guardar la última conexión. Código: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<UltimaConexion> call, Throwable t) {
                if (!isAdded()) return;
                android.content.Context ctx = getContext();
                if (ctx == null) return;
                Toast.makeText(ctx, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navegarContenido(Subtema subtema) {
        View vista = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_teoria_practica, null);
        TextView tvMensaje = vista.findViewById(R.id.tv_titulo_subtema);
        tvMensaje.setText(subtema.getNombre());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(vista)
                .setCancelable(true)
                .show();

        vista.findViewById(R.id.btn_teoria).setOnClickListener(v -> {
            var action = SubtemasFragmentDirections.actionSubtemasFragmentToTeoriaFragment(subtema, null);
            NavHostFragment.findNavController(this).navigate(action);
            dialog.dismiss();
        });

        vista.findViewById(R.id.btn_practica).setOnClickListener(v -> {
            var action = SubtemasFragmentDirections.actionSubtemasFragmentToPracticaFragment(subtema);
            NavHostFragment.findNavController(this).navigate(action);
            dialog.dismiss();
        });

        vista.findViewById(R.id.btn_cancelar).setOnClickListener(v -> dialog.dismiss());
    }

    /**
     * Entrada por accesibilidad: guarda la última conexión y pide via voz si abrir teoría o práctica.
     * Devuelve por callback si la navegación se realizó correctamente.
     */
    public void entrarSubtemaDesdeAccesibilidad(Subtema subtema, BiConsumer<Boolean, Subtema> callback) {
        if (subtema == null) {
            if (callback != null) callback.accept(false, null);
            return;
        }

        // Guardar última conexión (no es estrictamente necesario esperar la respuesta)
        guardarUltimaConexion(subtema);

        // Usar TTS/ASR para preguntar teoría/práctica
        SalidaAudio salida = SalidaAudio.obtenerInstancia();
        EntradaAudio entrada = EntradaAudio.obtenerInstancia();

        // Opciones de elección
        List<String> opciones = List.of("teoría", "práctica", "cancelar");

        // Primero hablar la pregunta
        try { entrada.detenerEscucha(); } catch (Exception ignored) {}
        salida.hablar("¿Deseas ver teoría o práctica? Di teoría o práctica.", true, () -> {
            // pequeña espera para asegurar liberación de audio
            mainHandler.postDelayed(() -> entrada.seleccionarOpcion(opciones, indice -> {
                if (indice < 0 || indice >= opciones.size()) {
                    salida.hablar("No te entendí. Cancelando.", true, () -> {
                        if (callback != null) callback.accept(false, subtema);
                    });
                    return;
                }

                String opcion = opciones.get(indice);
                if ("teoría".equals(opcion)) {
                    mainHandler.post(() -> {
                        try {
                            var action = SubtemasFragmentDirections.actionSubtemasFragmentToTeoriaFragment(subtema, null);
                            NavHostFragment.findNavController(SubtemasFragment.this).navigate(action);
                            if (callback != null) callback.accept(true, subtema);
                        } catch (Exception e) {
                            if (callback != null) callback.accept(false, subtema);
                        }
                    });
                    return;
                }

                if ("práctica".equals(opcion)) {
                    mainHandler.post(() -> {
                        try {
                            var action = SubtemasFragmentDirections.actionSubtemasFragmentToPracticaFragment(subtema);
                            NavHostFragment.findNavController(SubtemasFragment.this).navigate(action);
                            if (callback != null) callback.accept(true, subtema);
                        } catch (Exception e) {
                            if (callback != null) callback.accept(false, subtema);
                        }
                    });
                    return;
                }

                // cancelar
                salida.hablar("Operación cancelada.", true, () -> {
                    if (callback != null) callback.accept(false, subtema);
                });

            }), 350);
        });
    }

    /**
     * Simula que se presionó el botón de regresar (popBackStack / onBackPressed).
     */
    public void simularRegresar() {
        requireActivity().runOnUiThread(() -> {
            try {
                if (!NavHostFragment.findNavController(SubtemasFragment.this).popBackStack()) {
                    requireActivity().onBackPressed();
                }
            } catch (Exception e) {
                requireActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (navAccesibilidad != null) navAccesibilidad.detenerNavegacion();
    }
}