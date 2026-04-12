package com.cyberpath.smartlearn.ui.main.combo.principal.materia;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.logic.main.combo.principal.materia.MateriasLogic;
import com.cyberpath.smartlearn.logic.main.combo.principal.materia.NavAccesibilidad;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class MateriasFragment extends Fragment {

    private final Handler filtroHandler = new Handler(Looper.getMainLooper());
    private MateriasLogic materiasLogic;
    private NavAccesibilidad navAccesibilidadMaterias;
    private EntradaAudio entradaAudio;
    private SearchView searchViewMaterias;
    private ViewPager2 carruselMaterias;
    private ImageView[] indicadores;
    private LinearLayout indicadoresContainer;
    private FloatingActionButton btnPrev;
    private FloatingActionButton btnNext;
    private AdaptadorMaterias adapterMaterias;
    private Runnable filtroRunnable;
    private TextView nombreUsuario;

    public MateriasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        entradaAudio = EntradaAudio.obtenerInstancia();
        if (entradaAudio != null) {
            entradaAudio.detenerEscucha();
        }

        nombreUsuario = view.findViewById(R.id.tv_nombre_usuario);
        searchViewMaterias = view.findViewById(R.id.searchViewMaterias);
        carruselMaterias = view.findViewById(R.id.viewPagerMaterias);
        indicadoresContainer = view.findViewById(R.id.indicadores_container);
        btnPrev = view.findViewById(R.id.btn_prev_materia);
        btnNext = view.findViewById(R.id.btn_next_materia);

        btnPrev.setScaleX(0.86f);
        btnPrev.setScaleY(0.86f);
        btnNext.setScaleX(0.86f);
        btnNext.setScaleY(0.86f);

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
        navAccesibilidadMaterias = new NavAccesibilidad(requireContext(), this, materiasLogic, carruselMaterias, adapterMaterias);

        btnPrev.setOnClickListener(v -> {
            if (adapterMaterias == null || adapterMaterias.getRealSize() == 0) return;
            int currentPage = carruselMaterias.getCurrentItem();
            carruselMaterias.setCurrentItem(currentPage - 1, true);
        });

        btnNext.setOnClickListener(v -> {
            if (adapterMaterias == null || adapterMaterias.getRealSize() == 0) return;
            int currentPage = carruselMaterias.getCurrentItem();
            carruselMaterias.setCurrentItem(currentPage + 1, true);
        });

        carruselMaterias.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                actualizarIndicadores(position);
                actualizarBotones(position);
            }
        });

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

    private void actualizarIndicadores(int posicionActualPagina) {
        if (adapterMaterias == null) return;
        int total = adapterMaterias.getRealSize();
        indicadoresContainer.removeAllViews();
        indicadores = new ImageView[total];
        int sizePx = dpToPx(10);
        int marginPx = dpToPx(4);
        int posicionReal = getRealPositionFromPageIndex(posicionActualPagina);
        for (int i = 0; i < total; i++) {
            indicadores[i] = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMargins(marginPx, 0, marginPx, 0);
            indicadores[i].setLayoutParams(params);
            indicadores[i].setImageResource(i == posicionReal ? R.drawable.ic_dot_active : R.drawable.ic_dot_inactive);
            indicadoresContainer.addView(indicadores[i]);
        }
    }

    private void actualizarBotones(int posicionPagina) {
        int total = adapterMaterias != null ? adapterMaterias.getRealSize() : 0;
        if (total == 0) {
            btnPrev.setAlpha(0.4f);
            btnNext.setAlpha(0.4f);
            return;
        }
        int realPos = getRealPositionFromPageIndex(posicionPagina);
        btnPrev.setAlpha(realPos > 0 ? 1.0f : 0.4f);
        btnNext.setAlpha(realPos < total - 1 ? 1.0f : 0.4f);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int getRealPositionFromPageIndex(int pageIndex) {
        if (adapterMaterias == null) return 0;
        int realSize = adapterMaterias.getRealSize();
        if (realSize == 0) return 0;
        int realPos = pageIndex % realSize;
        if (realPos < 0) realPos += realSize;
        return realPos;
    }

    private void onMateriaClick(Materia materia) {
        var action = MateriasFragmentDirections.actionMateriasFragmentToTemasFragment(materia);
        NavHostFragment.findNavController(this).navigate(action);
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