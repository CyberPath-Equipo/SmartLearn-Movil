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
    private final ContenidoDAO contenidoDAO;
    private final ApiService apiService;
    private DescargaCallback callback;
    private boolean descargaActiva = false;

    public DescargarDatos(Context context) {
        this.contenidoDAO = new ContenidoDAO(context);
        this.apiService = RetrofitClient.getApiService();
    }

    public void setCallback(DescargaCallback callback) {
        this.callback = callback;
    }

    public void descargarMateria(Materia materia) {
        if (materia == null || materia.getId() == null) {
            notificarError("Materia inválida");
            return;
        }

        if (callback != null) callback.onDescargaIniciada();

        descargaActiva = true;
        contenidoDAO.beginTransaction();
        contenidoDAO.insertarMateria(materia);

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

                    if (temas.isEmpty()) {
                        finalizarDescarga();
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
            finalizarDescarga();
            return;
        }

        Tema tema = temas.get(indiceActual);


        if (tema.getIdMateria() == null) {
            tema.setIdMateria(idMateria);
        }


        if (tema.getIdMateria() == null) {
            notificarError("No se pudo resolver la relación de tema con materia");
            return;
        }

        notificarProgreso(15 + (indiceActual * 50 / temas.size()), "Procesando tema: " + tema.getNombre());

        contenidoDAO.insertarTema(tema);


        Call<List<Subtema>> call = apiService.getSubtemasByTema(tema.getId());
        call.enqueue(new Callback<List<Subtema>>() {
            @Override
            public void onResponse(Call<List<Subtema>> call, Response<List<Subtema>> response) {
                if (response.isSuccessful()) {
                    List<Subtema> subtemas = response.body();
                    if (subtemas != null) {
                        procesarSubtemas(subtemas, 0, temas, indiceActual, tema.getId(), idMateria);
                    } else {
                        notificarError("Respuesta inválida al obtener subtemas");
                    }
                } else {
                    notificarError("Error al obtener subtemas del tema " + tema.getNombre());
                }
            }

            @Override
            public void onFailure(Call<List<Subtema>> call, Throwable t) {
                notificarError("Error de conexión al obtener subtemas: " + t.getMessage());
            }
        });
    }

    private void procesarSubtemas(List<Subtema> subtemas, int indiceSubtemaActual, List<Tema> temas, int indiceTemaActual, Integer idTemaActual, Integer idMateria) {
        if (subtemas == null || indiceSubtemaActual >= subtemas.size()) {
            procesarTemas(idMateria, temas, indiceTemaActual + 1);
            return;
        }

        Subtema subtema = subtemas.get(indiceSubtemaActual);


        if (subtema.getIdTema() == null) {
            subtema.setIdTema(idTemaActual);
        }

        if (subtema.getIdTema() == null || subtema.getId() == null) {
            notificarError("Subtema inválido durante descarga");
            return;
        }

        contenidoDAO.insertarSubtema(subtema);

        notificarProgreso(20 + (indiceSubtemaActual * 30 / subtemas.size()), "Procesando subtema: " + subtema.getNombre());


        Call<Teoria> callTeoria = apiService.getTeoriaBySubtema(subtema.getId());
        callTeoria.enqueue(new Callback<Teoria>() {
            @Override
            public void onResponse(Call<Teoria> call, Response<Teoria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Teoria teoria = response.body();

                    if (teoria.getIdSubtema() == null) {
                        teoria.setIdSubtema(subtema.getId());
                    }
                    contenidoDAO.insertarTeoria(teoria);
                }


                obtenerEjercicios(subtema.getId(), subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            }

            @Override
            public void onFailure(Call<Teoria> call, Throwable t) {
                notificarError("Error de conexión al obtener teoría: " + t.getMessage());
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
                    notificarError("Error al obtener ejercicios del subtema");
                }
            }

            @Override
            public void onFailure(Call<List<Ejercicio>> call, Throwable t) {
                notificarError("Error de conexión al obtener ejercicios: " + t.getMessage());
            }
        });
    }

    private void procesarEjercicios(List<Ejercicio> ejercicios, int indiceEjercicio, Integer idSubtema, List<Subtema> subtemas, int indiceSubtemaActual, Integer idTemaActual, List<Tema> temas, int indiceTemaActual, Integer idMateria) {
        if (ejercicios == null || indiceEjercicio >= ejercicios.size()) {
            procesarSubtemas(subtemas, indiceSubtemaActual + 1, temas, indiceTemaActual, idTemaActual, idMateria);
            return;
        }

        Ejercicio ejercicio = ejercicios.get(indiceEjercicio);


        if (ejercicio.getIdSubtema() == null) {
            ejercicio.setIdSubtema(idSubtema);
        }

        if (ejercicio.getIdSubtema() == null || ejercicio.getId() == null) {
            notificarError("Ejercicio inválido durante descarga");
            return;
        }

        contenidoDAO.insertarEjercicio(ejercicio);


        Call<List<Pregunta>> call = apiService.getPreguntasByEjercicio(ejercicio.getId());
        call.enqueue(new Callback<List<Pregunta>>() {
            @Override
            public void onResponse(Call<List<Pregunta>> call, Response<List<Pregunta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pregunta> preguntas = response.body();
                    procesarPreguntas(preguntas, 0, ejercicio.getId(), ejercicios, indiceEjercicio, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
                } else {
                    notificarError("Error al obtener preguntas del ejercicio");
                }
            }

            @Override
            public void onFailure(Call<List<Pregunta>> call, Throwable t) {
                notificarError("Error de conexión al obtener preguntas: " + t.getMessage());
            }
        });
    }

    private void procesarPreguntas(List<Pregunta> preguntas, int indicePregunta, Integer idEjercicio, List<Ejercicio> ejercicios, int indiceEjercicio, Integer idSubtema, List<Subtema> subtemas, int indiceSubtemaActual, Integer idTemaActual, List<Tema> temas, int indiceTemaActual, Integer idMateria) {
        if (preguntas == null || indicePregunta >= preguntas.size()) {
            procesarEjercicios(ejercicios, indiceEjercicio + 1, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            return;
        }

        Pregunta pregunta = preguntas.get(indicePregunta);


        if (pregunta.getIdEjercicio() == null) {
            pregunta.setIdEjercicio(idEjercicio);
        }

        if (pregunta.getIdEjercicio() == null || pregunta.getId() == null) {
            notificarError("Pregunta inválida durante descarga");
            return;
        }

        contenidoDAO.insertarPregunta(pregunta);

        // Si la API ya envió las opciones dentro de la pregunta, persistirlas sin depender de otra llamada.
        if (pregunta.getOpciones() != null && !pregunta.getOpciones().isEmpty()) {
            for (Opcion opcion : pregunta.getOpciones()) {
                if (opcion.getIdPregunta() == null) {
                    opcion.setIdPregunta(pregunta.getId());
                }
                if (opcion.getIdPregunta() == null) {
                    notificarError("Opción inválida durante descarga");
                    return;
                }
                contenidoDAO.insertarOpcion(opcion);
            }
            procesarPreguntas(preguntas, indicePregunta + 1, idEjercicio, ejercicios, indiceEjercicio, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            return;
        }


        Call<List<Opcion>> call = apiService.getOpcionesByPregunta(pregunta.getId());
        call.enqueue(new Callback<List<Opcion>>() {
            @Override
            public void onResponse(Call<List<Opcion>> call, Response<List<Opcion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Opcion> opciones = response.body();
                    for (Opcion opcion : opciones) {

                        if (opcion.getIdPregunta() == null) {
                            opcion.setIdPregunta(pregunta.getId());
                        }
                        if (opcion.getIdPregunta() == null) {
                            notificarError("Opción inválida durante descarga");
                            return;
                        }
                        contenidoDAO.insertarOpcion(opcion);
                    }
                } else {
                    notificarError("Error al obtener opciones de la pregunta");
                    return;
                }
                procesarPreguntas(preguntas, indicePregunta + 1, idEjercicio, ejercicios, indiceEjercicio, idSubtema, subtemas, indiceSubtemaActual, idTemaActual, temas, indiceTemaActual, idMateria);
            }

            @Override
            public void onFailure(Call<List<Opcion>> call, Throwable t) {
                notificarError("Error de conexión al obtener opciones: " + t.getMessage());
            }
        });
    }

    private void finalizarDescarga() {
        if (!descargaActiva) return;
        descargaActiva = false;
        contenidoDAO.setTransactionSuccessful();
        contenidoDAO.endTransaction();
        notificarProgreso(100, "¡Descarga completada!");
        if (callback != null) callback.onDescargaCompletada();
    }

    private void notificarProgreso(int porcentaje, String mensaje) {
        if (callback != null) {
            callback.onProgreso(porcentaje, mensaje);
        }
    }

    private void notificarError(String error) {
        if (!descargaActiva) return;
        descargaActiva = false;
        contenidoDAO.endTransaction();
        if (callback != null) callback.onDescargaFallida(error);
    }

    public interface DescargaCallback {
        void onDescargaIniciada();

        void onProgreso(int porcentajeActual, String mensaje);

        void onDescargaCompletada();

        void onDescargaFallida(String error);
    }


}