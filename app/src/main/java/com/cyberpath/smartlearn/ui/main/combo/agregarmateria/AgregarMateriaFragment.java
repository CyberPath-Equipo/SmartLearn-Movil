package com.cyberpath.smartlearn.ui.main.combo.agregarmateria;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.logic.main.combo.agregarmateria.AgregarMateriaLogic;
import com.cyberpath.smartlearn.logic.main.combo.agregarmateria.NavAccesibilidad;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AgregarMateriaFragment extends Fragment {
    private final Handler filtroHandler = new Handler(Looper.getMainLooper());
    private SearchView searchViewMaterias;
    private ViewPager2 carruselMaterias;
    private AdaptadorAgregarMaterias adapterMaterias;
    private Runnable filtroRunnable;
    private NavAccesibilidad navAccesibilidad;

    private AgregarMateriaLogic agregarMateriaLogic;
    private EntradaAudio entradaAudio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        agregarMateriaLogic = new AgregarMateriaLogic(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agregar_materia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        entradaAudio = EntradaAudio.obtenerInstancia();
        if (entradaAudio != null) {
            entradaAudio.detenerEscucha();
        }

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

        navAccesibilidad = new NavAccesibilidad(requireContext(), this, agregarMateriaLogic, carruselMaterias, adapterMaterias);

        searchViewMaterias.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                agregarMateriaLogic.aplicarFiltro(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (filtroRunnable != null) {
                    filtroHandler.removeCallbacks(filtroRunnable);
                }
                filtroRunnable = () -> agregarMateriaLogic.aplicarFiltro(newText);
                filtroHandler.postDelayed(filtroRunnable, 300);
                return true;
            }
        });

        agregarMateriaLogic.cargarDatos();
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
        agregarMateriaLogic.limpiarDatos();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (navAccesibilidad != null) {
            navAccesibilidad.detenerNavegacion();
        }
        try {
            EntradaAudio.obtenerInstancia().detenerEscucha();
        } catch (Exception ignored) {
        }
        try {
            SalidaAudio.obtenerInstancia().detener();
        } catch (Exception ignored) {
        }
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
            if (materia != null) {
                agregarMateriaLogic.inscribirMateria(materia, this::mostrarDialogoDescarga);
            }
        });

        vista.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());
    }

    private void mostrarDialogoDescarga(Materia materia) {
        View vistaDescarga = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_descargar_materia, null);

        TextView tvTituloDescarga = vistaDescarga.findViewById(R.id.tvTituloDescarga);
        TextView tvMensajeDescarga = vistaDescarga.findViewById(R.id.tvMensajeDescarga);
        ProgressBar progressBar = vistaDescarga.findViewById(R.id.progressBarDescarga);
        TextView tvProgressoDescarga = vistaDescarga.findViewById(R.id.tvProgressoDescarga);
        TextView tvMensajeProgreso = vistaDescarga.findViewById(R.id.tvMensajeProgreso);
        Button btnNoDescargar = vistaDescarga.findViewById(R.id.btnNoDescargar);
        Button btnDescargar = vistaDescarga.findViewById(R.id.btnDescargar);
        LinearLayout llBotones = vistaDescarga.findViewById(R.id.llBotones);

        tvTituloDescarga.setText("Descargar: " + materia.getNombre());

        final androidx.appcompat.app.AlertDialog dialogoDescarga = new MaterialAlertDialogBuilder(requireContext())
                .setView(vistaDescarga)
                .setCancelable(false)
                .show();

        btnNoDescargar.setOnClickListener(v -> dialogoDescarga.dismiss());

        btnDescargar.setOnClickListener(v -> {
            llBotones.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            tvProgressoDescarga.setVisibility(View.VISIBLE);
            tvMensajeProgreso.setVisibility(View.VISIBLE);
            tvMensajeDescarga.setText("Descargando contenido...");

            agregarMateriaLogic.descargarMateria(materia, progressBar, tvProgressoDescarga, tvMensajeProgreso, dialogoDescarga);
        });
    }

    public void actualizarAdapter(List<Materia> materias) {
        if (adapterMaterias != null) {
            adapterMaterias.actualizarLista(new ArrayList<>(materias));
        }
    }

    public void moverViewPager(int posicion) {
        if (carruselMaterias != null && adapterMaterias != null) {
            int centered = agregarMateriaLogic.centeredPositionForIndex(posicion, adapterMaterias.getRealSize());
            carruselMaterias.setCurrentItem(centered, false);
        }
    }

    public void iniciarNavegacionPorVoz() {
        if (!PreferencesManager.isAsistenciaVozActivada(requireContext())) {
            return;
        }
        if (navAccesibilidad != null) {
            navAccesibilidad.iniciarNavegacion();
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

    public void showToast(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    public void actualizarListasDespuesInscripcion(Materia materia) {
        agregarMateriaLogic.actualizarListasDespuesInscripcion(materia);
    }
}