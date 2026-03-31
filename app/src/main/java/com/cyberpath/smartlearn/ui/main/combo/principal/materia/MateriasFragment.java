package com.cyberpath.smartlearn.ui.main.combo.principal.materia;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.logic.main.combo.principal.materia.MateriasLogic;
import com.cyberpath.smartlearn.logic.main.combo.principal.materia.NavAccesibilidad;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class MateriasFragment extends Fragment {

    private MateriasLogic materiasLogic;
    private NavAccesibilidad navAccesibilidadMaterias;
    private final EntradaAudio entradaAudio = EntradaAudio.obtenerInstancia();
    private final Handler filtroHandler = new Handler(Looper.getMainLooper());

    private SearchView searchViewMaterias;
    private ViewPager2 carruselMaterias;
    private AdaptadorMaterias adapterMaterias;
    private Runnable filtroRunnable;
    private TextView nombreUsuario;
    private TextView tvUltimoSubtema;
    private LinearLayout btnUltimoSubtema;

    public MateriasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_materias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                    }
                });

        entradaAudio.detenerEscucha();

        nombreUsuario = view.findViewById(R.id.tv_nombre_usuario);
        searchViewMaterias = view.findViewById(R.id.searchViewMaterias);
        carruselMaterias = view.findViewById(R.id.viewPagerMaterias);
        tvUltimoSubtema = view.findViewById(R.id.tv_ultimo_subtema);
        btnUltimoSubtema = view.findViewById(R.id.btn_ultimo_subtema);

        String nombreUsuarioActual = PreferencesManager.getNombreUsuario(requireContext());
        nombreUsuario.setText("Hola, " + nombreUsuarioActual);

        adapterMaterias = new AdaptadorMaterias(new ArrayList<>(), this::onMateriaClick);
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

        materiasLogic = new MateriasLogic(this);

        navAccesibilidadMaterias = new NavAccesibilidad(requireContext(), this,
                materiasLogic, carruselMaterias, adapterMaterias);

        searchViewMaterias.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                materiasLogic.filtrarMaterias(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (filtroRunnable != null) {
                    filtroHandler.removeCallbacks(filtroRunnable);
                }
                filtroRunnable = () -> materiasLogic.filtrarMaterias(newText);
                filtroHandler.postDelayed(filtroRunnable, 300);
                return true;
            }
        });

        materiasLogic.cargarUltimoSubtema();
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("MateriasFragment", "onResume llamado");

        if (materiasLogic != null) {
            materiasLogic.cargarMaterias();
        }
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

        if (materiasLogic != null) {
            materiasLogic.limpiarDatos();
        }

        searchViewMaterias = null;
        carruselMaterias = null;
        adapterMaterias = null;
        navAccesibilidadMaterias = null;
        materiasLogic = null;
    }

    private void onMateriaClick(Materia materia) {
        var action = MateriasFragmentDirections.actionMateriasFragmentToTemasFragment(materia);
        NavHostFragment.findNavController(this).navigate(action);
    }

    public void navegarUltimoSubtema(Subtema subtema) {
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

    public void showToast(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    public void showToastLong(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        }
    }

    public void actualizarAdapter(List<Materia> materias) {
        if (adapterMaterias != null) {
            adapterMaterias.actualizarLista(new ArrayList<>(materias));
        }
    }

    public void moverViewPager(int posicion) {
        if (carruselMaterias != null && adapterMaterias != null) {
            int centered = materiasLogic.centeredPositionForIndex(posicion, adapterMaterias.getRealSize());
            carruselMaterias.setCurrentItem(centered, false);
        }
    }

    public void iniciarNavegacionPorVoz() {
        if (navAccesibilidadMaterias != null) {
            navAccesibilidadMaterias.iniciarNavegacion();
        }
    }

    public int getPosicionActualViewPager() {
        if (carruselMaterias == null || adapterMaterias == null) return 0;
        int current = carruselMaterias.getCurrentItem();
        int realSize = adapterMaterias.getRealSize();
        if (realSize == 0) return 0;
        int realPos = current % realSize;
        if (realPos < 0) realPos += realSize;
        return realPos;
    }

    public void setTvUltimoSubtema(String texto) {
        if (tvUltimoSubtema != null) {
            tvUltimoSubtema.setText(texto);
        }
    }

    public void setOnClickBtnUltimoSubtema(View.OnClickListener listener) {
        if (btnUltimoSubtema != null) {
            btnUltimoSubtema.setOnClickListener(listener);
        }
    }

    public void entrarMateriaDesdeAccesibilidad(Materia materia, BiConsumer<Boolean, Materia> callback) {
        var action = MateriasFragmentDirections.actionMateriasFragmentToTemasFragment(materia);
        NavHostFragment.findNavController(this).navigate(action);
        callback.accept(true, materia);
    }

    public void notificarCambiosAdapter() {
        if (adapterMaterias != null) {
            adapterMaterias.notifyDataSetChanged();
        }
    }
}