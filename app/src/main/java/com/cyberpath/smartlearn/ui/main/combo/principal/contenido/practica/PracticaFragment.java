package com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica.NavAccesibilidad;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica.PracticaLogic;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PracticaFragment extends Fragment implements AdapterView.OnItemClickListener {
    private PracticaLogic practicaLogic;
    private NavAccesibilidad navAccesibilidad;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ListView listViewEjercicios;
    private AdaptadorPractica adaptadorEjercicios;
    private TextView tvSubtema;
    private Subtema subtema;
    private MaterialButton btnVolver;

    public PracticaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_practica, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            subtema = PracticaFragmentArgs.fromBundle(getArguments()).getSubtema();
        }

        tvSubtema = view.findViewById(R.id.tv_subtema);
        if (subtema != null && subtema.getNombre() != null) {
            tvSubtema.setText(subtema.getNombre());
        }

        listViewEjercicios = view.findViewById(R.id.listViewEjercicios);
        adaptadorEjercicios = new AdaptadorPractica(getContext(), new ArrayList<>());
        listViewEjercicios.setAdapter(adaptadorEjercicios);
        listViewEjercicios.setOnItemClickListener(this);

        try {
            EntradaAudio.obtenerInstancia().detenerEscucha();
        } catch (Exception ignored) {
        }
        try {
            SalidaAudio.obtenerInstancia().detener();
        } catch (Exception ignored) {
        }

        practicaLogic = new PracticaLogic(this, subtema);

        navAccesibilidad = new NavAccesibilidad(requireContext(), this, practicaLogic, listViewEjercicios, adaptadorEjercicios);

        btnVolver = view.findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> {
            boolean popped = false;
            try {
                popped = NavHostFragment.findNavController(this).popBackStack();
            } catch (Exception ignored) {}
            if (!popped) requireActivity().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (navAccesibilidad != null) {
            navAccesibilidad.detenerNavegacion();
        }

        if (practicaLogic != null) {
            practicaLogic.limpiarDatos();
        }

        listViewEjercicios = null;
        adaptadorEjercicios = null;
        navAccesibilidad = null;
        practicaLogic = null;
        btnVolver = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Ejercicio ejercicio = practicaLogic.getEjercicioPorPosicion(position);
        if (ejercicio != null) {
            var action = PracticaFragmentDirections
                    .actionPracticaFragmentToEjercicioFragment(ejercicio);
            NavHostFragment.findNavController(this).navigate(action);
        }
    }

    public void simularRegresar() {
        requireActivity().runOnUiThread(() -> {
            try {
                if (!NavHostFragment.findNavController(this).popBackStack()) {
                    requireActivity().onBackPressed();
                }
            } catch (Exception e) {
                requireActivity().onBackPressed();
            }
        });
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

    public void actualizarAdapter(List<Ejercicio> ejercicios) {
        if (adaptadorEjercicios != null) {
            adaptadorEjercicios.actualizarLista(ejercicios);
        }
    }

    public void seleccionarItemListView(int posicion) {
        if (listViewEjercicios != null) {
            listViewEjercicios.setSelection(posicion);
        }
    }

    public void iniciarNavegacionPorVoz() {
        if (navAccesibilidad != null) {
            navAccesibilidad.iniciarNavegacion();
        }
    }

    public int getPosicionActualListView() {
        if (listViewEjercicios == null) return 0;
        return listViewEjercicios.getSelectedItemPosition();
    }

    public void entrarEjercicioDesdeAccesibilidad(Ejercicio ejercicio, BiConsumer<Boolean, Ejercicio> callback) {
        try {
            var action = PracticaFragmentDirections.actionPracticaFragmentToEjercicioFragment(ejercicio);
            NavHostFragment.findNavController(this).navigate(action);
            callback.accept(true, ejercicio);
        } catch (Exception e) {
            callback.accept(false, ejercicio);
        }
    }
}