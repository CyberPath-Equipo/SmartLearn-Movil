package com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.ejercicio;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica.ejercicio.EjercicioLogic;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica.ejercicio.NavAccesibilidad;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import lombok.Getter;
import lombok.Setter;

public class EjercicioFragment extends Fragment {

    private EjercicioLogic ejercicioLogic;
    private NavAccesibilidad navAccesibilidad;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Ejercicio ejercicio;
    @Getter
    @Setter
    private Subtema subtema;
    private ArrayList<String> listaPreguntas;

    private TextView tvQuestion;
    private RadioGroup rgOptions;
    private Button btnCheck;
    private LinearLayout layoutQuiz, layoutResultado, layoutRetroalimentacion;
    private TextView tvResultadoTitulo, tvPuntaje, tvPorcentaje;
    private Button btnVolver, btnRetroalimentacion;

    private final List<RadioButton> currentOptionButtons = new ArrayList<>();

    public EjercicioFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ejercicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            ejercicio = EjercicioFragmentArgs.fromBundle(getArguments()).getEjercicio();
        }

        tvQuestion = view.findViewById(R.id.tv_question);
        rgOptions = view.findViewById(R.id.rg_options);
        btnCheck = view.findViewById(R.id.btn_check);

        layoutQuiz = view.findViewById(R.id.layout_quiz);
        layoutResultado = view.findViewById(R.id.layout_resultado);
        layoutRetroalimentacion = view.findViewById(R.id.layout_retroalimentacion);
        tvResultadoTitulo = view.findViewById(R.id.tv_resultado_titulo);
        tvPuntaje = view.findViewById(R.id.tv_puntaje);
        tvPorcentaje = view.findViewById(R.id.tv_porcentaje);
        btnVolver = view.findViewById(R.id.btn_volver);
        btnRetroalimentacion = view.findViewById(R.id.btn_retroalimentacion);

        listaPreguntas = new ArrayList<>();

        btnCheck.setOnClickListener(v -> ejercicioLogic.verificarRespuesta());
        btnVolver.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        btnRetroalimentacion.setOnClickListener(v -> {
            if (subtema == null) {
                showToast("Subtema no disponible, intenta de nuevo");
                return;
            }
            NavHostFragment.findNavController(this).navigate(
                    EjercicioFragmentDirections.actionEjercicioFragmentToTeoriaFragment(subtema, listaPreguntas)
            );
        });

        try {
            EntradaAudio.obtenerInstancia().detenerEscucha();
        } catch (Exception ignored) {}
        try {
            SalidaAudio.obtenerInstancia().detener();
        } catch (Exception ignored) {}

        ejercicioLogic = new EjercicioLogic(this, ejercicio);

        navAccesibilidad = new NavAccesibilidad(requireContext(), this, ejercicioLogic);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (navAccesibilidad != null) {
            navAccesibilidad.detenerNavegacion();
        }

        if (ejercicioLogic != null) {
            ejercicioLogic.limpiarDatos();
        }

        navAccesibilidad = null;
        ejercicioLogic = null;
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

    public void setTvQuestion(String texto) {
        if (tvQuestion != null) {
            tvQuestion.setText(texto);
        }
    }

    public void mostrarOpciones(List<Opcion> opciones) {
        if (rgOptions == null) return;

        rgOptions.removeAllViews();
        currentOptionButtons.clear();

        if (opciones == null || opciones.isEmpty()) return;

        for (int i = 0; i < opciones.size(); i++) {
            Opcion opcion = opciones.get(i);
            RadioButton rb = new RadioButton(getContext());
            rb.setId(View.generateViewId());
            rb.setText(opcion.getTexto());
            rb.setTag(opcion);
            rb.setTextSize(16);
            rgOptions.addView(rb);
            currentOptionButtons.add(rb);
        }
    }

    public void setBtnCheckEnabled(boolean enabled) {
        if (btnCheck != null) {
            btnCheck.setEnabled(enabled);
        }
    }

    public void clearRadioGroup() {
        if (rgOptions != null) {
            rgOptions.clearCheck();
        }
    }

    public void mostrarLayoutQuiz(boolean mostrar) {
        if (layoutQuiz != null) {
            layoutQuiz.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }

    public void mostrarLayoutResultado(boolean mostrar) {
        if (layoutResultado != null) {
            layoutResultado.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }

    public void mostrarLayoutRetroalimentacion(boolean mostrar) {
        if (layoutRetroalimentacion != null) {
            layoutRetroalimentacion.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }

    public void setTvPuntaje(String texto) {
        if (tvPuntaje != null) {
            tvPuntaje.setText(texto);
        }
    }

    public void setTvPorcentaje(String texto) {
        if (tvPorcentaje != null) {
            tvPorcentaje.setText(texto);
        }
    }

    public void setTvPuntajeColor(int color) {
        if (tvPuntaje != null) {
            tvPuntaje.setTextColor(color);
        }
    }

    public void setTvPorcentajeColor(int color) {
        if (tvPorcentaje != null) {
            tvPorcentaje.setTextColor(color);
        }
    }

    public void iniciarNavegacionPorVoz() {
        if (navAccesibilidad != null) {
            mainHandler.postDelayed(() -> navAccesibilidad.iniciarNavegacion(), 200);
        }
    }

    public int getTotalPreguntas() {
        return ejercicioLogic != null ? ejercicioLogic.getTotalPreguntas() : 0;
    }

    public int getCurrentQuestionIndex() {
        return ejercicioLogic != null ? ejercicioLogic.getCurrentQuestionIndex() : 0;
    }

    public Pregunta getPreguntaActual() {
        return ejercicioLogic != null ? ejercicioLogic.getPreguntaActual() : null;
    }

    public int getNumeroOpcionesPreguntaActual() {
        return ejercicioLogic != null ? ejercicioLogic.getNumeroOpcionesPreguntaActual() : 0;
    }

    public void seleccionarOpcionPorIndice(int idx) {
        mainHandler.post(() -> {
            if (idx < 0 || idx >= currentOptionButtons.size()) return;
            RadioButton rb = currentOptionButtons.get(idx);
            if (rb != null) {
                rb.setChecked(true);
            }
        });
    }

    public String getTextoOpcionActual(int idx) {
        return ejercicioLogic != null ? ejercicioLogic.getTextoOpcionActual(idx) : "";
    }

    public boolean hayOpcionSeleccionada() {
        return rgOptions != null && rgOptions.getCheckedRadioButtonId() != -1;
    }

    public int getSelectedRadioButtonId() {
        return rgOptions != null ? rgOptions.getCheckedRadioButtonId() : -1;
    }

    public RadioButton findViewById(int id) {
        return requireView().findViewById(id);
    }

    public void comprobarRespuestaDesdeAccesibilidad(BiConsumer<Boolean, Integer> callback) {
        if (ejercicioLogic != null) {
            ejercicioLogic.comprobarRespuestaDesdeAccesibilidad(callback);
        }
    }

    public boolean estaEjercicioFinalizado() {
        return ejercicioLogic != null && ejercicioLogic.estaEjercicioFinalizado();
    }

    public boolean isPreguntaRespondida() {
        return ejercicioLogic != null && ejercicioLogic.isPreguntaRespondida();
    }

    public int getScore() {
        return ejercicioLogic != null ? ejercicioLogic.getScore() : 0;
    }

    public void irATeoriaDesdeAccesibilidad() {
        mainHandler.post(() -> {
            if (subtema == null) {
                showToast("Subtema no disponible, intenta de nuevo");
                return;
            }
            try {
                NavHostFragment.findNavController(this).navigate(
                        EjercicioFragmentDirections.actionEjercicioFragmentToTeoriaFragment(subtema, listaPreguntas)
                );
            } catch (Exception e) {
                showToast("No se pudo navegar a la teoría: " + e.getMessage());
            }
        });
    }

    public void avanzarPregunta() {
        if (ejercicioLogic != null) {
            ejercicioLogic.avanzarPregunta();
        }
    }

    public void mostrarSiguientePregunta() {
        ejercicioLogic.mostrarSiguientePregunta();
    }
}