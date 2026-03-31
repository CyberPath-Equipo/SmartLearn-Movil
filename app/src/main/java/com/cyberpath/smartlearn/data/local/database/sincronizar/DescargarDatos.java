package com.cyberpath.smartlearn.data.local.database.sincronizar;

import android.content.Context;

import com.cyberpath.smartlearn.data.local.database.dao.ContenidoDAO;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.model.contenido.Teoria;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DescargarDatos {
    private final Context context;
    private final ContenidoDAO contenidoDAO;
    private final ApiService apiService;
    private DescargaCallback callback;

    public DescargarDatos(Context context) {
        this.context = context;
        this.contenidoDAO = new ContenidoDAO(context);
        this.apiService = RetrofitClient.getApiService();
    }

    public void setCallback(DescargaCallback callback) {
        this.callback = callback;
    }

    public void descargarMateria(Materia materia) {
        if (callback != null) callback.onDescargaIniciada();

        obtenerTemasMateria(materia);
    }

    private void obtenerTemasMateria(Materia materia) {
        notificarProgreso(10, "Obteniendo temas...");

        Call<List<Tema>> call = apiService.getTemasByMateria(materia.getId());
        call.enqueue(new Callback<List<Tema>>() {
            @Override
            public void onResponse(Call<List<Tema>> call, Response<List<Tema>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Tema> temas = response.body();

                    // Insertar materia primero
                    contenidoDAO.insertarMateria(materia);

                    if (temas.isEmpty()) {
                        finalizarDescarga(materia);
                    } else {
                        procesarTemas(materia.getId(), temas, 0);
                    }
                } else {
                    notificarError("Error al obtener temas");
                }
            }

            @Override
            public void onFailure(Call<List<Tema>> call, Throwable t) {
                notificarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void procesarTemas(Integer idMateria, List<Tema> temas, int indiceActual) {
        if (temas == null || indiceActual >= temas.size()) {
            notificarProgreso(80, "Descarga completada");
            finalizarDescarga(null);
            return;
        }

        Tema tema = temas.get(indiceActual);

        // IMPORTANTE: Asegúrate de que id_materia no sea null
        if (tema.getIdMateria() == null) {
            tema.setIdMateria(idMateria);
        }

        // Safeguard: Skip if id_materia is still null to prevent constraint violation
        if (tema.getIdMateria() == null) {
            procesarTemas(idMateria, temas, indiceActual + 1);
            return;
        }

        notificarProgreso(15 + (indiceActual * 50 / temas.size()), "Procesando tema: " + tema.getNombre());

        contenidoDAO.insertarTema(tema);

        // Obtener subtemas
        Call<List<Subtema>> call = apiService.getSubtemasByTema(tema.getId());
        call.enqueue(new Callback<List<Subtema>>() {
            @Override
            public void onResponse(Call<List<Subtema>> call, Response<List<Subtema>> response) {
                if (response.isSuccessful()) {
                    List<Subtema> subtemas = response.body();
                    if (subtemas != null) {
                        procesarSubtemas(subtemas, 0, temas, indiceActual, tema.getId(), idMateria);
                    } else {
                        procesarTemas(idMateria, temas, indiceActual + 1);
                    }
                } else {
                    procesarTemas(idMateria, temas, indiceActual + 1);
                }
            }

            @Override
            public void onFailure(Call<List<Subtema>> call, Throwable t) {
                procesarTemas(idMateria, temas, indiceActual + 1);
            }
        });
    }

    private void procesarSubtemas(List<Subtema> subtemas, int indiceSubtemaActual, List<Tema> temas, int indiceTemaActual, Integer idTemaActual, Integer idMateria) {
        if (subtemas == null || indiceSubtemaActual >= subtemas.size()) {
            procesarTemas(idMateria, temas, indiceTemaActual + 1);
            return;
        }

        Subtema subtema = subtemas.get(indiceSubtemaActual);

        // IMPORTANTE: Asegúrate de que id_tema no sea null
        if (subtema.getIdTema() == null) {
            subtema.setIdTema(idTemaActual);
        }

        contenidoDAO.insertarSubtema(subtema);

        notificarProgreso(20 + (indiceSubtemaActual * 30 / subtemas.size()), "Procesando subtema: " + subtema.getNombre());

        // Obtener teoría
        Call<Teoria> callTeoria = apiService.getTeoriaBySubtema(subtema.getId());
        callTeoria.enqueue(new Callback<Teoria>() {
            @Override
            public void onResponse(Call<Teoria> call, Response<Teoria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Teoria teoria = response.body();
                    // Asegúrate de que id_subtema está establecido
                    if (teoria.getIdSubtema() == null) {
                        teoria.setIdSubtema(subtema.getId());
                    }
                    contenidoDAO.insertarTeoria(teoria);
                }

                // Obtener ejercicios
                obtenerEjercicios(subtema.getId(), subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            }

            @Override
            public void onFailure(Call<Teoria> call, Throwable t) {
                obtenerEjercicios(subtema.getId(), subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            }
        });
    }

    private void obtenerEjercicios(Integer idSubtema, List<Subtema> subtemas, int indiceSubtemaActual, Integer idTemaActual, List<Tema> temas, int indiceTemaActual, Integer idMateria) {
        Call<List<Ejercicio>> call = apiService.getEjerciciosBySubtema(idSubtema);
        call.enqueue(new Callback<List<Ejercicio>>() {
            @Override
            public void onResponse(Call<List<Ejercicio>> call, Response<List<Ejercicio>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Ejercicio> ejercicios = response.body();
                    procesarEjercicios(ejercicios, 0, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
                } else {
                    procesarSubtemas(subtemas, indiceSubtemaActual + 1, temas, indiceTemaActual, idTemaActual, idMateria);
                }
            }

            @Override
            public void onFailure(Call<List<Ejercicio>> call, Throwable t) {
                procesarSubtemas(subtemas, indiceSubtemaActual + 1, temas, indiceTemaActual, idTemaActual, idMateria);
            }
        });
    }

    private void procesarEjercicios(List<Ejercicio> ejercicios, int indiceEjercicio, Integer idSubtema, List<Subtema> subtemas, int indiceSubtemaActual, Integer idTemaActual, List<Tema> temas, int indiceTemaActual, Integer idMateria) {
        if (ejercicios == null || indiceEjercicio >= ejercicios.size()) {
            procesarSubtemas(subtemas, indiceSubtemaActual + 1, temas, indiceTemaActual, idTemaActual, idMateria);
            return;
        }

        Ejercicio ejercicio = ejercicios.get(indiceEjercicio);

        // IMPORTANTE: Asegúrate de que id_subtema no sea null
        if (ejercicio.getIdSubtema() == null) {
            ejercicio.setIdSubtema(idSubtema);
        }

        contenidoDAO.insertarEjercicio(ejercicio);

        // Obtener preguntas del ejercicio
        Call<List<Pregunta>> call = apiService.getPreguntasByEjercicio(ejercicio.getId());
        call.enqueue(new Callback<List<Pregunta>>() {
            @Override
            public void onResponse(Call<List<Pregunta>> call, Response<List<Pregunta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pregunta> preguntas = response.body();
                    procesarPreguntas(preguntas, 0, ejercicio.getId(), ejercicios, indiceEjercicio, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
                } else {
                    procesarEjercicios(ejercicios, indiceEjercicio + 1, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
                }
            }

            @Override
            public void onFailure(Call<List<Pregunta>> call, Throwable t) {
                procesarEjercicios(ejercicios, indiceEjercicio + 1, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            }
        });
    }

    private void procesarPreguntas(List<Pregunta> preguntas, int indicePregunta, Integer idEjercicio, List<Ejercicio> ejercicios, int indiceEjercicio, Integer idSubtema, List<Subtema> subtemas, int indiceSubtemaActual, Integer idTemaActual, List<Tema> temas, int indiceTemaActual, Integer idMateria) {
        if (preguntas == null || indicePregunta >= preguntas.size()) {
            procesarEjercicios(ejercicios, indiceEjercicio + 1, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            return;
        }

        Pregunta pregunta = preguntas.get(indicePregunta);

        // IMPORTANTE: Asegúrate de que id_ejercicio no sea null
        if (pregunta.getIdEjercicio() == null) {
            pregunta.setIdEjercicio(idEjercicio);
        }

        contenidoDAO.insertarPregunta(pregunta);

        // Obtener opciones
        Call<List<Opcion>> call = apiService.getOpcionesByPregunta(pregunta.getId());
        call.enqueue(new Callback<List<Opcion>>() {
            @Override
            public void onResponse(Call<List<Opcion>> call, Response<List<Opcion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Opcion> opciones = response.body();
                    for (Opcion opcion : opciones) {
                        // Asegúrate de que id_pregunta no sea null
                        if (opcion.getIdPregunta() == null) {
                            opcion.setIdPregunta(pregunta.getId());
                        }
                        contenidoDAO.insertarOpcion(opcion);
                    }
                }
                procesarPreguntas(preguntas, indicePregunta + 1, idEjercicio, ejercicios, indiceEjercicio, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            }

            @Override
            public void onFailure(Call<List<Opcion>> call, Throwable t) {
                procesarPreguntas(preguntas, indicePregunta + 1, idEjercicio, ejercicios, indiceEjercicio, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            }
        });
    }

    private void finalizarDescarga(Materia materia) {
        notificarProgreso(100, "¡Descarga completada!");
        if (callback != null) callback.onDescargaCompletada();
    }

    private void notificarProgreso(int porcentaje, String mensaje) {
        if (callback != null) {
            callback.onProgreso(porcentaje, mensaje);
        }
    }

    private void notificarError(String error) {
        if (callback != null) callback.onDescargaFallida(error);
    }

    public interface DescargaCallback {
        void onDescargaIniciada();

        void onProgreso(int porcentajeActual, String mensaje);

        void onDescargaCompletada();

        void onDescargaFallida(String error);
    }


}