package com.cyberpath.smartlearn.ui.main.combo.principal.materias.contenido.practica;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.IntentoEjercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * EjercicioFragment con soporte de accesibilidad por voz mediante NavAccesibilidad.
 *
 * Cambios principales:
 * - Control de preguntas contestadas para impedir cambiar de pregunta sin responder.
 * - Nuevo método comprobarRespuestaDesdeAccesibilidad que habla la retroalimentación y avanza la actividad.
 * - Métodos públicos para que NavAccesibilidad pueda orquestar el flujo solicitado.
 */
public class EjercicioFragment extends Fragment {
    private Ejercicio ejercicio;
    private Subtema subtema;
    private final Usuario usuarioActual = UsuarioCst.USUARIO_ACTUAL;

    private final List<Pregunta> allPreguntas = new ArrayList<>();
    private ArrayList<String> listaPreguntas;
    private int currentQuestionIndex = 0;
    private int score = 0;

    private TextView tvQuestion, tvResultadoTitulo, tvPuntaje, tvPorcentaje;
    private RadioGroup rgOptions;
    private Button btnCheck, btnVolver, btnRetroalimentacion;
    private LinearLayout layoutQuiz, layoutResultado, layoutRetroalimentacion;

    private ApiService apiService;

    // Para accesibilidad
    private NavAccesibilidadEjercicio navAccesibilidad;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<RadioButton> currentOptionButtons = new ArrayList<>();
    private boolean ejercicioFinalizado = false;

    // Control de preguntas respondidas (evita navegar sin contestar)
    private final Set<Integer> preguntasContestadas = new HashSet<>();

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
            Log.d("MENSAJE:", "hola1" + ejercicio.getIdSubtema());
            cargarSubtema(ejercicio.getIdSubtema());
        }
        listaPreguntas = new ArrayList<>();
        apiService = RetrofitClient.getApiService();

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

        btnCheck.setOnClickListener(v -> checkAnswer());
        btnVolver.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        btnRetroalimentacion.setOnClickListener(v -> {
            if (subtema == null) {
                Toast.makeText(getContext(), "Subtema no disponible, intenta de nuevo", Toast.LENGTH_SHORT).show();
                return;
            }
            NavHostFragment.findNavController(this).navigate(
                    EjercicioFragmentDirections.actionEjercicioFragmentToTeoriaFragment(subtema, listaPreguntas)
            );
        });

        // crear NavAccesibilidad (se iniciará cuando terminen de cargar preguntas/opciones)
        navAccesibilidad = new NavAccesibilidadEjercicio(requireContext(), this);

        loadPreguntas();
    }

    private void loadPreguntas() {
        Call<List<Pregunta>> call = apiService.getPreguntasByEjercicio(ejercicio.getId());
        call.enqueue(new Callback<List<Pregunta>>() {
            @Override
            public void onResponse(Call<List<Pregunta>> call, Response<List<Pregunta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allPreguntas.addAll(response.body());
                    loadOpciones();
                } else {
                    Toast.makeText(getContext(), "Error al cargar preguntas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Pregunta>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOpciones() {
        if (allPreguntas.isEmpty()) {
            Toast.makeText(getContext(), "No hay preguntas disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        final int totalPreguntas = allPreguntas.size();
        final int[] completed = {0};

        for (Pregunta pregunta : allPreguntas) {
            Call<List<Opcion>> call = apiService.getOpcionesByPregunta(pregunta.getId());
            call.enqueue(new Callback<List<Opcion>>() {
                @Override
                public void onResponse(Call<List<Opcion>> call, Response<List<Opcion>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        pregunta.setOpciones(response.body());
                    }
                    completed[0]++;
                    if (completed[0] == totalPreguntas) {
                        // Mostrar la primera pregunta y luego iniciar navegación por voz
                        showQuestion(currentQuestionIndex);
                        mainHandler.postDelayed(() -> navAccesibilidad.iniciarNavegacion(), 200);
                    }
                }

                @Override
                public void onFailure(Call<List<Opcion>> call, Throwable t) {
                    completed[0]++;
                    if (completed[0] == totalPreguntas) {
                        showQuestion(currentQuestionIndex);
                        mainHandler.postDelayed(() -> navAccesibilidad.iniciarNavegacion(), 200);
                    }
                }
            });
        }
    }

    private void showQuestion(int index) {
        if (index >= allPreguntas.size()) {
            showScore();
            agregarIntento();
            return;
        }

        Pregunta pregunta = allPreguntas.get(index);
        listaPreguntas.add(pregunta.getEnunciado());

        tvQuestion.setText(pregunta.getEnunciado());
        rgOptions.removeAllViews();
        currentOptionButtons.clear();

        for (int i = 0; i < pregunta.getOpciones().size(); i++) {
            Opcion opcion = pregunta.getOpciones().get(i);
            RadioButton rb = new RadioButton(getContext());
            rb.setId(View.generateViewId());
            rb.setText(opcion.getTexto());
            rb.setTag(opcion);
            rb.setTextSize(16);
            rgOptions.addView(rb);
            currentOptionButtons.add(rb);
        }

        btnCheck.setEnabled(true);
    }

    private void checkAnswer() {
        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(getContext(), "Selecciona una opción", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRb = requireView().findViewById(selectedId);
        Opcion selectedOpcion = (Opcion) selectedRb.getTag();

        if (selectedOpcion.isCorrecta()) {
            score++;
            Toast.makeText(getContext(), "¡Correcto!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Incorrecto", Toast.LENGTH_SHORT).show();
        }

        // marcar pregunta como contestada y avanzar
        preguntasContestadas.add(currentQuestionIndex);
        currentQuestionIndex++;
        rgOptions.clearCheck();
        showQuestion(currentQuestionIndex);
    }

    /**
     * Comprobar respuesta desde accesibilidad. Callback: (exito, nuevaPosicionIndex)
     *
     * Este método realiza la comprobación y además habla la retroalimentación:
     * - Si correcta: dice "¡Correcto!"
     * - Si incorrecta: dice "Incorrecto. La respuesta correcta es la opción X: <texto>"
     *
     * Luego avanza la pregunta (o muestra resultados si era la última).
     */
    public void comprobarRespuestaDesdeAccesibilidad(BiConsumer<Boolean, Integer> callback) {
        mainHandler.post(() -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                if (callback != null) callback.accept(false, currentQuestionIndex);
                return;
            }
            RadioButton selectedRb = requireView().findViewById(selectedId);
            Opcion selectedOpcion = (Opcion) selectedRb.getTag();
            Pregunta pregunta = getPreguntaActual();
            if (pregunta == null) {
                if (callback != null) callback.accept(false, currentQuestionIndex);
                return;
            }

            // encontrar opción correcta
            int correctIdx = -1;
            String correctText = "";
            List<Opcion> opciones = pregunta.getOpciones();
            if (opciones != null) {
                for (int i = 0; i < opciones.size(); i++) {
                    if (opciones.get(i).isCorrecta()) {
                        correctIdx = i;
                        correctText = opciones.get(i).getTexto();
                        break;
                    }
                }
            }

            boolean correcta = selectedOpcion.isCorrecta();
            SalidaAudio salida = SalidaAudio.obtenerInstancia();

            String feedback;
            if (correcta) {
                score++;
                feedback = "¡Correcto!";
            } else {
                String num = numeroEnPalabras(correctIdx + 1);
                feedback = "Incorrecto. La respuesta correcta es la opción " + num + ": " + correctText + ".";
            }

            // detener escucha antes de hablar
            try { EntradaAudio.obtenerInstancia().detenerEscucha(); } catch (Exception ignored) {}

            salida.hablar(feedback, false, () -> {
                // después de hablar, marcar contestada y avanzar
                preguntasContestadas.add(currentQuestionIndex);
                currentQuestionIndex++;
                rgOptions.clearCheck();

                if (currentQuestionIndex >= allPreguntas.size()) {
                    showScore();
                    agregarIntento();
                } else {
                    showQuestion(currentQuestionIndex);
                }

                if (callback != null) callback.accept(true, currentQuestionIndex);
            });
        });
    }

    private void showScore() {
        int total = allPreguntas.size();
        double porcentaje = total > 0 ? (score * 100.0 / total) : 0;

        layoutQuiz.setVisibility(View.GONE);
        layoutResultado.setVisibility(View.VISIBLE);

        tvPuntaje.setText(score + " / " + total);
        tvPorcentaje.setText(String.format("%.1f%% de acierto", porcentaje));

        int color = porcentaje >= 80 ? 0xFF2E7D32 :
                porcentaje >= 60 ? 0xFFED6F00 :
                        0xFFD32F2F;
        tvPuntaje.setTextColor(color);
        tvPorcentaje.setTextColor(color);

        if (porcentaje < 100.0) {
            layoutRetroalimentacion.setVisibility(View.VISIBLE);
        }
        actualizarEjercicio();
        ejercicioFinalizado = true;
    }

    private void agregarIntento() {
        IntentoEjercicio nuevoIntento = new IntentoEjercicio();

        int idUsuario = PreferencesManager.getIdUsuario(requireContext());

        int total = allPreguntas.size();
        double porcentaje = total > 0 ? (score * 100.0 / total) : 0;

        nuevoIntento.setIdEjercicio(ejercicio.getId());
        nuevoIntento.setIdUsuario(idUsuario);
        nuevoIntento.setPuntaje(porcentaje);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String fechaActual = sdf.format(new Date());
        nuevoIntento.setFecha(fechaActual);
        nuevoIntento.setEstado("completado");

        ApiService api = RetrofitClient.getApiService();
        api.saveIntentoEjercicio(nuevoIntento).enqueue(new Callback<IntentoEjercicio>() {
            @Override
            public void onResponse(Call<IntentoEjercicio> call, Response<IntentoEjercicio> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Intento guardado exitosamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al guardar intento", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<IntentoEjercicio> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de conexión al guardar intento: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarSubtema(Integer idEjercicio) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<Subtema> call = apiService.getSubtemaById(idEjercicio);
        call.enqueue(new Callback<Subtema>() {
            @Override
            public void onResponse(Call<Subtema> call, Response<Subtema> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subtema = response.body();
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error al cargar subtema", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(Call<Subtema> call, Throwable t) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error de conexión al cargar subtema: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void actualizarEjercicio() {
        // Con el nuevo esquema, la finalización se registra en tbl_intento_ejercicio.
        // Dejamos el estado local para la UI actual sin usar una tabla intermedia eliminada.
        ejercicio.setHecho(true);
    }

    // ----------------------------
    // Métodos públicos para NavAccesibilidad
    // ----------------------------

    public int getTotalPreguntas() {
        return allPreguntas.size();
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public Pregunta getPreguntaActual() {
        if (allPreguntas.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= allPreguntas.size())
            return null;
        return allPreguntas.get(currentQuestionIndex);
    }

    public int getNumeroOpcionesPreguntaActual() {
        Pregunta p = getPreguntaActual();
        return (p == null || p.getOpciones() == null) ? 0 : p.getOpciones().size();
    }

    /**
     * Selecciona la opción UI por índice 0-based (marca el RadioButton correspondiente).
     */
    public void seleccionarOpcionPorIndice(int idx) {
        mainHandler.post(() -> {
            if (idx < 0 || idx >= currentOptionButtons.size()) return;
            RadioButton rb = currentOptionButtons.get(idx);
            if (rb != null) {
                rb.setChecked(true);
            }
        });
    }

    /**
     * Devuelve el texto de la opción actual (si existe).
     */
    public String getTextoOpcionActual(int idx) {
        Pregunta p = getPreguntaActual();
        if (p == null || p.getOpciones() == null || idx < 0 || idx >= p.getOpciones().size()) return "";
        return p.getOpciones().get(idx).getTexto();
    }

    /**
     * Comprueba si hay una opción seleccionada en la UI.
     */
    public boolean hayOpcionSeleccionada() {
        return rgOptions.getCheckedRadioButtonId() != -1;
    }

    /**
     * Comprobar respuesta desde accesibilidad. Callback: (exito, nuevaPosicionIndex)
     *
     * Nota: este método habla la retroalimentación y luego avanza la pregunta.
     */
    public void comprobarRespuestaDesdeAccesibilidad(BiConsumer<Boolean, Integer> callback, Runnable onFinishedSpeaking) {
        // Para compatibilidad si alguien lo llama con dos params, pero la implementación principal está arriba.
        comprobarRespuestaDesdeAccesibilidad(callback);
    }

    /**
     * Comprobar respuesta desde accesibilidad. Versión original (usada por NavAccesibilidad).
     */

    /**
     * Indica si el ejercicio ya finalizó (se mostró el score).
     */
    public boolean estaEjercicioFinalizado() {
        return ejercicioFinalizado;
    }

    /**
     * Indica si la pregunta actual ya fue respondida.
     */
    public boolean isPreguntaRespondida() {
        return preguntasContestadas.contains(currentQuestionIndex);
    }

    /**
     * Obtiene la puntuación actual (cantidad correctas).
     */
    public int getScore() {
        return score;
    }

    /**
     * Navegar a la teoría desde accesibilidad.
     */
    public void irATeoriaDesdeAccesibilidad() {
        mainHandler.post(() -> {
            if (subtema == null) {
                Toast.makeText(getContext(), "Subtema no disponible, intenta de nuevo", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                NavHostFragment.findNavController(this).navigate(
                        EjercicioFragmentDirections.actionEjercicioFragmentToTeoriaFragment(subtema, listaPreguntas)
                );
            } catch (Exception e) {
                Toast.makeText(getContext(), "No se pudo navegar a la teoría: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Simula presionar volver (popBackStack / onBackPressed).
     */
    public void simularRegresar() {
        requireActivity().runOnUiThread(() -> {
            try {
                if (!NavHostFragment.findNavController(EjercicioFragment.this).popBackStack()) {
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

    // ----------------------------
    // Utilidades
    // ----------------------------
    private String numeroEnPalabras(int n) {
        switch (n) {
            case 1: return "uno";
            case 2: return "dos";
            case 3: return "tres";
            case 4: return "cuatro";
            case 5: return "cinco";
            case 6: return "seis";
            case 7: return "siete";
            case 8: return "ocho";
            case 9: return "nueve";
            case 10: return "diez";
            default: return String.valueOf(n);
        }
    }
}