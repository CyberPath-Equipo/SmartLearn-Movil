package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.practica.ejercicio;

import android.content.Context;
import android.util.Log;
import android.widget.RadioButton;

import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.IntentoEjercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
import com.cyberpath.smartlearn.data.model.relaciones.UsuarioEjercicio;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica.ejercicio.EjercicioFragment;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EjercicioLogic {

    private final EjercicioFragment fragment;
    private final Context context;
    private final Ejercicio ejercicio;
    private final List<Pregunta> allPreguntas = new ArrayList<>();
    private final Set<Integer> preguntasContestadas = new HashSet<>();

    @Getter
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean ejercicioFinalizado = false;
    private Subtema subtema;
    /** Marca el instante en que se empieza el ejercicio para calcular duracionSeg. */
    private long inicioEjercicioMs = 0;

    public EjercicioLogic(EjercicioFragment fragment, Ejercicio ejercicio) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        this.ejercicio = ejercicio;

        cargarSubtema();
        cargarPreguntas();
    }

    private void cargarSubtema() {
        if (ejercicio == null || ejercicio.getIdSubtema() == null) return;

        ApiService apiService = RetrofitClient.getApiService();
        Call<Subtema> call = apiService.getSubtemaById(ejercicio.getIdSubtema());

        call.enqueue(new Callback<Subtema>() {
            @Override
            public void onResponse(Call<Subtema> call, Response<Subtema> response) {
                if (fragment == null || !fragment.isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    subtema = response.body();
                    fragment.setSubtema(subtema);
                }
            }

            @Override
            public void onFailure(Call<Subtema> call, Throwable t) {
            }
        });
    }

    private void cargarPreguntas() {
        if (ejercicio == null || ejercicio.getId() == null) {
            fragment.showToast("Ejercicio no identificado");
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Pregunta>> call = apiService.getPreguntasByEjercicio(ejercicio.getId());

        call.enqueue(new Callback<List<Pregunta>>() {
            @Override
            public void onResponse(Call<List<Pregunta>> call, Response<List<Pregunta>> response) {
                if (fragment == null || !fragment.isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    allPreguntas.addAll(response.body());
                    cargarOpcionesDePreguntas();
                } else {
                    fragment.showToast("Error al cargar preguntas");
                }
            }

            @Override
            public void onFailure(Call<List<Pregunta>> call, Throwable t) {
                if (fragment == null || !fragment.isAdded()) return;
                fragment.showToast("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void cargarOpcionesDePreguntas() {
        if (allPreguntas.isEmpty()) {
            fragment.showToast("No hay preguntas disponibles");
            return;
        }

        inicioEjercicioMs = System.currentTimeMillis();

        final int totalPreguntas = allPreguntas.size();
        final int[] completed = {0};

        for (Pregunta pregunta : allPreguntas) {
            ApiService apiService = RetrofitClient.getApiService();
            Call<List<Opcion>> call = apiService.getOpcionesByPregunta(pregunta.getId());

            call.enqueue(new Callback<List<Opcion>>() {
                @Override
                public void onResponse(Call<List<Opcion>> call, Response<List<Opcion>> response) {
                    if (fragment == null || !fragment.isAdded()) return;

                    if (response.isSuccessful() && response.body() != null) {
                        pregunta.setOpciones(response.body());
                    }

                    completed[0]++;
                    if (completed[0] == totalPreguntas) {
                        fragment.mostrarSiguientePregunta();
                        fragment.iniciarNavegacionPorVoz();
                    }
                }

                @Override
                public void onFailure(Call<List<Opcion>> call, Throwable t) {
                    if (fragment == null || !fragment.isAdded()) return;

                    completed[0]++;
                    if (completed[0] == totalPreguntas) {
                        fragment.mostrarSiguientePregunta();
                        fragment.iniciarNavegacionPorVoz();
                    }
                }
            });
        }
    }

    public void mostrarSiguientePregunta() {
        if (currentQuestionIndex >= allPreguntas.size()) {
            mostrarPuntuacion();
            agregarIntento();
            return;
        }

        Pregunta pregunta = allPreguntas.get(currentQuestionIndex);

        fragment.setTvQuestion(pregunta.getEnunciado());
        fragment.mostrarOpciones(pregunta.getOpciones());
        fragment.setBtnCheckEnabled(true);
    }

    public void verificarRespuesta() {
        if (!fragment.hayOpcionSeleccionada()) {
            fragment.showToast("Selecciona una opción");
            return;
        }

        int selectedId = fragment.getSelectedRadioButtonId();
        android.widget.RadioButton selectedRb = fragment.findViewById(selectedId);
        Opcion selectedOpcion = (Opcion) selectedRb.getTag();

        if (selectedOpcion.isCorrecta()) {
            score++;
            fragment.showToast("¡Correcto!");
        } else {
            fragment.showToast("Incorrecto");
        }

        preguntasContestadas.add(currentQuestionIndex);
        currentQuestionIndex++;
        fragment.clearRadioGroup();
        mostrarSiguientePregunta();
    }

    public void comprobarRespuestaDesdeAccesibilidad(BiConsumer<Boolean, Integer> callback) {
        if (!fragment.hayOpcionSeleccionada()) {
            if (callback != null) callback.accept(false, currentQuestionIndex);
            return;
        }

        int selectedId = fragment.getSelectedRadioButtonId();
        RadioButton selectedRb = fragment.findViewById(selectedId);
        Opcion selectedOpcion = (Opcion) selectedRb.getTag();
        Pregunta pregunta = getPreguntaActual();

        if (pregunta == null) {
            if (callback != null) callback.accept(false, currentQuestionIndex);
            return;
        }

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

        try {
            EntradaAudio.obtenerInstancia().detenerEscucha();
        } catch (Exception ignored) {}

        salida.hablar(feedback, false, () -> {
            preguntasContestadas.add(currentQuestionIndex);
            currentQuestionIndex++;
            fragment.clearRadioGroup();

            if (currentQuestionIndex >= allPreguntas.size()) {
                mostrarPuntuacion();
                agregarIntento();
            } else {
                mostrarSiguientePregunta();
            }

            if (callback != null) callback.accept(true, currentQuestionIndex);
        });
    }

    private void mostrarPuntuacion() {
        int total = allPreguntas.size();
        double porcentaje = total > 0 ? (score * 100.0 / total) : 0;

        fragment.mostrarLayoutQuiz(false);
        fragment.mostrarLayoutResultado(true);

        fragment.setTvPuntaje(score + " / " + total);
        fragment.setTvPorcentaje(String.format("%.1f%% de acierto", porcentaje));

        int color = porcentaje >= 80 ? 0xFF2E7D32 :
                porcentaje >= 60 ? 0xFFED6F00 :
                        0xFFD32F2F;

        fragment.setTvPuntajeColor(color);
        fragment.setTvPorcentajeColor(color);

        if (porcentaje < 100.0) {
            fragment.mostrarLayoutRetroalimentacion(true);
        }

        actualizarEjercicio();
        ejercicioFinalizado = true;
    }

    private void agregarIntento() {
        IntentoEjercicio nuevoIntento = new IntentoEjercicio();

        int idUsuario = PreferencesManager.getIdUsuario(context);
        int total = allPreguntas.size();
        double porcentaje = total > 0 ? (score * 100.0 / total) : 0;

        nuevoIntento.setIdEjercicio(ejercicio.getId());
        nuevoIntento.setIdUsuario(idUsuario);
        nuevoIntento.setPuntaje(porcentaje);
        nuevoIntento.setEstado(porcentaje >= 60 ? "aprobado" : "reprobado");

        if (inicioEjercicioMs > 0) {
            long durMs = System.currentTimeMillis() - inicioEjercicioMs;
            nuevoIntento.setDuracionSeg((int) (durMs / 1000));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String fechaActual = sdf.format(new Date());
        nuevoIntento.setFecha(fechaActual);

        ApiService api = RetrofitClient.getApiService();
        api.saveIntentoEjercicio(nuevoIntento).enqueue(new Callback<IntentoEjercicio>() {
            @Override
            public void onResponse(Call<IntentoEjercicio> call, Response<IntentoEjercicio> response) {
                if (response.isSuccessful()) {
                    Log.d("EjercicioLogic", "Intento guardado exitosamente");
                }
            }

            @Override
            public void onFailure(Call<IntentoEjercicio> call, Throwable t) {
                Log.e("EjercicioLogic", "Error al guardar intento: " + t.getMessage());
            }
        });
    }

    private void actualizarEjercicio() {
        if (ejercicio == null) return;

        UsuarioEjercicio ejercicioHecho = new UsuarioEjercicio();
        ejercicioHecho.setIdEjercicio(ejercicio.getId());

        if (UsuarioCst.USUARIO_ACTUAL != null) {
            ejercicioHecho.setIdUsuario(UsuarioCst.USUARIO_ACTUAL.getId());
        }

        ejercicioHecho.setHecho(true);

        ApiService api = RetrofitClient.getApiService();
        Call<UsuarioEjercicio> call = api.saveUsuarioEjercicio(ejercicioHecho);

        call.enqueue(new Callback<UsuarioEjercicio>() {
            @Override
            public void onResponse(Call<UsuarioEjercicio> call, Response<UsuarioEjercicio> response) {
                Log.d("EjercicioLogic", "Ejercicio actualizado");
            }

            @Override
            public void onFailure(Call<UsuarioEjercicio> call, Throwable t) {
                Log.e("EjercicioLogic", "Error al actualizar ejercicio: " + t.getMessage());
            }
        });
    }

    public void limpiarDatos() {
        allPreguntas.clear();
        preguntasContestadas.clear();
        currentQuestionIndex = 0;
        score = 0;
        ejercicioFinalizado = false;
    }

    public int getTotalPreguntas() {
        return allPreguntas.size();
    }

    public Pregunta getPreguntaActual() {
        if (allPreguntas.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= allPreguntas.size()) {
            return null;
        }
        return allPreguntas.get(currentQuestionIndex);
    }

    public int getNumeroOpcionesPreguntaActual() {
        Pregunta p = getPreguntaActual();
        return (p == null || p.getOpciones() == null) ? 0 : p.getOpciones().size();
    }

    public String getTextoOpcionActual(int idx) {
        Pregunta p = getPreguntaActual();
        if (p == null || p.getOpciones() == null || idx < 0 || idx >= p.getOpciones().size()) {
            return "";
        }
        return p.getOpciones().get(idx).getTexto();
    }

    public boolean estaEjercicioFinalizado() {
        return ejercicioFinalizado;
    }

    public boolean isPreguntaRespondida() {
        return preguntasContestadas.contains(currentQuestionIndex);
    }

    public int getScore() {
        return score;
    }

    public void avanzarPregunta() {
        if (currentQuestionIndex < allPreguntas.size()) {
            preguntasContestadas.add(currentQuestionIndex);
            currentQuestionIndex++;
        }
    }

    private String numeroEnPalabras(int n) {
        switch (n) {
            case 1:
                return "uno";
            case 2:
                return "dos";
            case 3:
                return "tres";
            case 4:
                return "cuatro";
            case 5:
                return "cinco";
            case 6:
                return "seis";
            case 7:
                return "siete";
            case 8:
                return "ocho";
            case 9:
                return "nueve";
            case 10:
                return "diez";
            default:
                return String.valueOf(n);
        }
    }
}
