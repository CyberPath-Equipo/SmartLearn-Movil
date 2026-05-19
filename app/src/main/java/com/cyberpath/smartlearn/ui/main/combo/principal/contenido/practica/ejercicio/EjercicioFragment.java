package com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.ejercicio;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import lombok.Getter;
import lombok.Setter;

public class EjercicioFragment extends Fragment {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Button> currentOptionButtons = new ArrayList<>();
    private EjercicioLogic ejercicioLogic;
    private NavAccesibilidad navAccesibilidad;
    private Ejercicio ejercicio;
    @Getter
    @Setter
    private Subtema subtema;
    private ArrayList<String> listaPreguntas;
    private TextView tvQuestion;
    private LinearLayout llOptions;
    private Button btnCheck;
    private LinearLayout layoutQuiz, layoutResultado, layoutRetroalimentacion;
    private TextView tvResultadoTitulo, tvPuntaje, tvPorcentaje;
    private Button btnVolver, btnRetroalimentacion;

    // estado de selección local (un solo índice seleccionado, -1 = none)
    private int selectedOptionIndex = -1;

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
        llOptions = view.findViewById(R.id.ll_options);
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

        // Al presionar "Verificar" delegamos al logic (que ahora debe consultar la opción seleccionada a través del fragment)
        btnCheck.setOnClickListener(v -> {
            if (ejercicioLogic != null) {
                ejercicioLogic.verificarRespuesta();
            }
        });
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
        } catch (Exception ignored) {
        }
        try {
            SalidaAudio.obtenerInstancia().detener();
        } catch (Exception ignored) {
        }

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

    /**
     * Crea botones para cada opción. La selección queda local (selectedOptionIndex) hasta que el usuario
     * presione "Verificar".
     */
    public void mostrarOpciones(List<Opcion> opciones) {
        if (llOptions == null) return;

        llOptions.removeAllViews();
        currentOptionButtons.clear();
        selectedOptionIndex = -1;

        if (opciones == null || opciones.isEmpty()) return;

        for (int i = 0; i < opciones.size(); i++) {
            Opcion opcion = opciones.get(i);
            Button btn = new Button(getContext());
            btn.setId(View.generateViewId());
            btn.setText(opcion.getTexto());
            btn.setTag(opcion);
            btn.setAllCaps(false);
            btn.setTextSize(16);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            // margen entre botones
            final int marginPx = (int) (8 * getResources().getDisplayMetrics().density);
            lp.setMargins(0, 0, 0, marginPx);
            btn.setLayoutParams(lp);

            final int index = i;
            btn.setOnClickListener(v -> {
                // selección (solo un botón seleccionado a la vez)
                if (selectedOptionIndex == index) {
                    selectedOptionIndex = -1; // deseleccionar si toca el mismo nuevamente
                } else {
                    selectedOptionIndex = index;
                }
                updateOptionSelectionUI();
            });

            // estilos iniciales (no seleccionado)
            btn.setBackgroundTintList(
                    ColorStateList.valueOf(requireContext().getColor(R.color.colorAccent))
            );

            btn.setTextColor(
                    requireContext().getColor(R.color.colorSurface)
            );

            llOptions.addView(btn);
            currentOptionButtons.add(btn);
        }
    }

    private void updateOptionSelectionUI() {
        for (int i = 0; i < currentOptionButtons.size(); i++) {
            Button b = currentOptionButtons.get(i);
            if (i == selectedOptionIndex) {
                b.setBackgroundTintList(
                        ColorStateList.valueOf(requireContext().getColor(R.color.colorSecondary))
                );

                b.setTextColor(
                        requireContext().getColor(R.color.colorSurface)
                );
            } else {
                b.setBackgroundTintList(
                        ColorStateList.valueOf(requireContext().getColor(R.color.colorAccent))
                );
                b.setTextColor(
                        requireContext().getColor(R.color.colorSurface)
                );
            }
        }
    }

    private int getColorAttr(int attrRes) {
        TypedValue tv = new TypedValue();
        if (requireContext().getTheme().resolveAttribute(attrRes, tv, true)) {
            // si es color directo en theme
            if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                return tv.data;
            } else {
                // si es referencia a recurso
                return requireContext().getResources().getColor(tv.resourceId, requireContext().getTheme());
            }
        }
        // fallback
        return requireContext().getResources().getColor(android.R.color.black);
    }

    public void setBtnCheckEnabled(boolean enabled) {
        if (btnCheck != null) {
            btnCheck.setEnabled(enabled);
        }
    }

    public void clearOptionsSelection() {
        selectedOptionIndex = -1;
        updateOptionSelectionUI();
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

    /**
     * Selecciona la opción con índice idx (se usa para accesibilidad/automatización).
     */
    public void seleccionarOpcionPorIndice(int idx) {
        mainHandler.post(() -> {
            if (idx < 0 || idx >= currentOptionButtons.size()) return;
            selectedOptionIndex = idx;
            updateOptionSelectionUI();
        });
    }

    public String getTextoOpcionActual(int idx) {
        if (idx < 0 || idx >= currentOptionButtons.size()) return "";
        return currentOptionButtons.get(idx).getText().toString();
    }

    public boolean hayOpcionSeleccionada() {
        return selectedOptionIndex != -1;
    }

    /**
     * Devuelve el índice de la opción seleccionada, o -1 si ninguna.
     */
    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    /**
     * Devuelve la Opcion (modelo) seleccionada, o null si ninguna.
     */
    public Opcion getSelectedOpcion() {
        if (selectedOptionIndex < 0 || selectedOptionIndex >= currentOptionButtons.size()) return null;
        Object tag = currentOptionButtons.get(selectedOptionIndex).getTag();
        if (tag instanceof Opcion) return (Opcion) tag;
        return null;
    }

    public Button findViewById(int id) {
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
        if (ejercicioLogic != null) {
            ejercicioLogic.mostrarSiguientePregunta();
        }
    }
}