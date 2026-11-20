package com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.ejercicio;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.local.database.dao.ContenidoDAO;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica.ejercicio.EjercicioLogic;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica.ejercicio.EjercicioNavAccesibilidad;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import lombok.Getter;
import lombok.Setter;

public class EjercicioFragment extends Fragment {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Button> currentOptionButtons = new ArrayList<>();
    private EjercicioLogic ejercicioLogic;
    private EjercicioNavAccesibilidad ejercicioNavAccesibilidad;
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
    private TextView tvFeedbackFx;
    private Button btnVolver, btnRetroalimentacion;
    private long sessionStartMs = 0L;

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
        tvFeedbackFx = view.findViewById(R.id.tv_feedback_fx);
        btnVolver = view.findViewById(R.id.btn_volver);
        btnRetroalimentacion = view.findViewById(R.id.btn_retroalimentacion);

        listaPreguntas = new ArrayList<>();

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

        ejercicioNavAccesibilidad = new EjercicioNavAccesibilidad(requireContext(), this, ejercicioLogic);
    }

    @Override
    public void onResume() {
        super.onResume();
        sessionStartMs = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        registrarSesionPractica();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        registrarSesionPractica();

        if (ejercicioNavAccesibilidad != null) {
            ejercicioNavAccesibilidad.detenerNavegacion();
        }

        if (ejercicioLogic != null) {
            ejercicioLogic.limpiarDatos();
        }

        ejercicioNavAccesibilidad = null;
        ejercicioLogic = null;
    }

    private void registrarSesionPractica() {
        if (!isAdded() || sessionStartMs <= 0L) {
            return;
        }

        long duracionSegundos = Math.max(0L, (System.currentTimeMillis() - sessionStartMs) / 1000L);
        sessionStartMs = 0L;
        if (duracionSegundos < 10L) {
            return;
        }

        int idUsuario = PreferencesManager.getIdUsuario(requireContext());
        if (idUsuario <= 0) {
            return;
        }

        Integer idSubtema = null;
        if (ejercicio != null) {
            idSubtema = ejercicio.getIdSubtema();
        }

        ContenidoDAO dao = new ContenidoDAO(requireContext());
        dao.registrarSesionEstudio(idUsuario, idSubtema, "PRACTICA", duracionSegundos);
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
        if (llOptions == null) return;

        llOptions.removeAllViews();
        currentOptionButtons.clear();
        selectedOptionIndex = -1;

        if (opciones == null || opciones.isEmpty()) return;

        int[] coloresGamificados = {
                R.color.blue_option,
                R.color.orange_option,
                R.color.purple_option
        };

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < opciones.size(); i++) {
            Opcion opcion = opciones.get(i);

            MaterialButton btn = (MaterialButton) inflater.inflate(R.layout.element_opcion_pregunta, llOptions, false);
            btn.setId(View.generateViewId());
            btn.setTag(opcion);

            int numeroIndicador = i + 1;
            String textoFormateado = String.format(Locale.getDefault(), "%d      %s", numeroIndicador, opcion.getTexto());
            btn.setText(textoFormateado);

            int colorAsignado = coloresGamificados[i % coloresGamificados.length];
            btn.setBackgroundTintList(
                    ColorStateList.valueOf(requireContext().getColor(colorAsignado))
            );

            final int index = i;
            btn.setOnClickListener(v -> {
                selectedOptionIndex = (selectedOptionIndex == index) ? -1 : index;
                updateOptionSelectionUI();
            });

            llOptions.addView(btn);
            currentOptionButtons.add(btn);
        }
    }

    private void updateOptionSelectionUI() {
        int strokeWidthPx = (int) (3 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < currentOptionButtons.size(); i++) {
            MaterialButton btn = (MaterialButton) currentOptionButtons.get(i);

            if (i == selectedOptionIndex) {
                btn.setAlpha(1.0f);
                btn.setStrokeWidth(strokeWidthPx);
                btn.setStrokeColor(ColorStateList.valueOf(requireContext().getColor(android.R.color.white)));
            } else {
                btn.setAlpha(0.6f);
                btn.setStrokeWidth(0);
            }
        }
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
        if (!PreferencesManager.isAsistenciaVozActivada(requireContext())) {
            return;
        }
        if (ejercicioNavAccesibilidad != null) {
            mainHandler.postDelayed(() -> ejercicioNavAccesibilidad.iniciarNavegacion(), 200);
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


    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }


    public Opcion getSelectedOpcion() {
        if (selectedOptionIndex < 0 || selectedOptionIndex >= currentOptionButtons.size())
            return null;
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

    public void animarResultadoVerificacion(boolean correcta, Runnable onEnd) {
        if (!isAdded() || tvFeedbackFx == null) {
            if (onEnd != null) onEnd.run();
            return;
        }

        tvFeedbackFx.setVisibility(View.VISIBLE);
        tvFeedbackFx.setText(correcta ? "✨" : "✖");
        tvFeedbackFx.setTextColor(requireContext().getColor(correcta ? R.color.colorSecondary : R.color.colorError));
        tvFeedbackFx.setScaleX(0.3f);
        tvFeedbackFx.setScaleY(0.3f);
        tvFeedbackFx.setAlpha(0f);

        tvFeedbackFx.animate()
                .alpha(0.4f)
                .scaleX(2f)
                .scaleY(2f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.6f))
                .withEndAction(() -> tvFeedbackFx.animate()
                        .alpha(0f)
                        .scaleX(0.75f)
                        .scaleY(0.75f)
                        .setDuration(250)
                        .withEndAction(() -> {
                            tvFeedbackFx.setVisibility(View.GONE);
                            if (onEnd != null) onEnd.run();
                        })
                        .start())
                .start();
    }

    public void animarResumenResultado() {
        if (!isAdded() || layoutResultado == null) {
            return;
        }

        layoutResultado.setAlpha(0f);
        layoutResultado.setTranslationY(40f);
        layoutResultado.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(320)
                .start();

        if (tvResultadoTitulo != null) {
            tvResultadoTitulo.setScaleX(0.85f);
            tvResultadoTitulo.setScaleY(0.85f);
            tvResultadoTitulo.animate().scaleX(1f).scaleY(1f).setDuration(260).setStartDelay(80).start();
        }
        if (tvPuntaje != null) {
            tvPuntaje.setAlpha(0f);
            tvPuntaje.animate().alpha(1f).setDuration(220).setStartDelay(140).start();
        }
        if (tvPorcentaje != null) {
            tvPorcentaje.setAlpha(0f);
            tvPorcentaje.animate().alpha(1f).setDuration(220).setStartDelay(220).start();
        }
    }
}