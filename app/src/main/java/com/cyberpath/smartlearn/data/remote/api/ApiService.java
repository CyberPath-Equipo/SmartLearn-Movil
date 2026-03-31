package com.cyberpath.smartlearn.data.remote.api;

import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.ProgresoSubtema;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.model.contenido.Teoria;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.IntentoEjercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
import com.cyberpath.smartlearn.data.model.recurso.RecursoAdjunto;
import com.cyberpath.smartlearn.data.model.recurso.TipoRecurso;
import com.cyberpath.smartlearn.data.model.relaciones.UsuarioEjercicio;
import com.cyberpath.smartlearn.data.model.relaciones.UsuarioMateria;
import com.cyberpath.smartlearn.data.model.usuario.UltimaConexion;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // /smartlearn/api/usuario
    @GET("/smartlearn/api/usuario")
    Call<List<Usuario>> getUsuarios();

    @GET("/smartlearn/api/usuario/{id}")
    Call<Usuario> getUsuarioById(@Path("id") Integer idUsuario);

    @GET("/smartlearn/api/usuario/{id}/materias")
    Call<List<Materia>> getMateriasByUsuario(@Path("id") Integer idUsuario);

    @GET("/smartlearn/api/usuario/{idUsuario}/materia/{idMateria}/ejercicios-realizados")
    Call<Long> getEjerciciosRealizadosByUsuarioAndMateria(@Path("idUsuario") int idUsuario, @Path("idMateria") int idMateria);

    @POST("/smartlearn/api/usuario/registro")
    Call<Usuario> registrarUsuario(@Body Usuario usuario);

    @POST("/smartlearn/api/usuario/login")
    Call<Usuario> login(@Body Usuario loginRequest);

    @PUT("/smartlearn/api/usuario/{id}")
    Call<Usuario> updateUsuario(@Path("id") int id, @Body Usuario usuario);

    @DELETE("/smartlearn/api/usuario/{id}")
    Call<Void> deleteUsuario(@Path("id") int id);

    // /smartlearn/api/materia
    @GET("/smartlearn/api/materia")
    Call<List<Materia>> getMateriasCatalogo();

    @GET("/smartlearn/api/materia/{id}/temas")
    Call<List<Tema>> getTemasByMateria(@Path("id") Integer idMateria);

    @GET("/smartlearn/api/materia/{id}/total-ejercicios")
    Call<Long> getTotalEjerciciosByMateria(@Path("id") int idMateria);

    // /smartlearn/api/tema
    @GET("/smartlearn/api/tema/{id}/subtemas")
    Call<List<Subtema>> getSubtemasByTema(@Path("id") Integer idTema);

    // /smartlearn/api/subtema
    @GET("/smartlearn/api/subtema/{id}")
    Call<Subtema> getSubtemaById(@Path("id") Integer idSubtema);

    @GET("/smartlearn/api/subtema/{id}/tema")
    Call<Tema> getTemaBySubtema(@Path("id") Integer idSubtema);

    @GET("/smartlearn/api/subtema/{id}/teoria")
    Call<Teoria> getTeoriaBySubtema(@Path("id") Integer idSubtema);

    @GET("/smartlearn/api/subtema/{id}/ejercicios")
    Call<List<Ejercicio>> getEjerciciosBySubtema(@Path("id") Integer idSubtema);

    // /smartlearn/api/teoria
    @GET("/smartlearn/api/teoria/{id}")
    Call<Teoria> getTeoriaById(@Path("id") Integer idTeoria);

    // /smartlearn/api/progreso-subtema
    @GET("/smartlearn/api/progreso-subtema")
    Call<List<ProgresoSubtema>> getProgresosSubtema();

    @GET("/smartlearn/api/progreso-subtema/{id}")
    Call<ProgresoSubtema> getProgresoSubtemaById(@Path("id") Integer idProgreso);

    @POST("/smartlearn/api/progreso-subtema")
    Call<ProgresoSubtema> saveProgresoSubtema(@Body ProgresoSubtema progresoSubtema);

    @PUT("/smartlearn/api/progreso-subtema/{id}")
    Call<ProgresoSubtema> updateProgresoSubtema(@Path("id") int id, @Body ProgresoSubtema progresoSubtema);

    @DELETE("/smartlearn/api/progreso-subtema/{id}")
    Call<Void> deleteProgresoSubtema(@Path("id") int id);

    // /smartlearn/api/usuario-materia
    @POST("/smartlearn/api/usuario-materia")
    Call<UsuarioMateria> saveUsuarioMateria(@Body UsuarioMateria usuarioMateria);

    // /smartlearn/api/ultima-conexion
    @POST("/smartlearn/api/ultima-conexion")
    Call<UltimaConexion> saveUltimaConexion(@Body UltimaConexion ultimaConexion);

    @PUT("/smartlearn/api/ultima-conexion/{id}")
    Call<UltimaConexion> updateUltimaConexion(@Path("id") int id, @Body UltimaConexion ultimaConexion);

    // /smartlearn/api/ejercicio
    @GET("/smartlearn/api/ejercicio/{id}/preguntas")
    Call<List<Pregunta>> getPreguntasByEjercicio(@Path("id") Integer idEjercicio);

    // /smartlearn/api/pregunta
    @GET("/smartlearn/api/pregunta/{id}/opciones")
    Call<List<Opcion>> getOpcionesByPregunta(@Path("id") Integer idPregunta);

    // /smartlearn/api/intento-ejercicio
    @POST("/smartlearn/api/intento-ejercicio")
    Call<IntentoEjercicio> saveIntentoEjercicio(@Body IntentoEjercicio intentoEjercicio);

    // /smartlearn/api/ejercicio
    @PUT("/smartlearn/api/ejercicio/{id}")
    Call<Ejercicio> updateEjercicio(@Path("id") int id, @Body Ejercicio ejercicio);

    // /smartlearn/api/usuario-ejercicio
    @POST("/smartlearn/api/usuario-ejercicio")
    Call<UsuarioEjercicio> saveUsuarioEjercicio(@Body UsuarioEjercicio usuarioEjercicio);

    // /smartlearn/api/recurso-adjunto
    @GET("/smartlearn/api/recurso-adjunto")
    Call<List<RecursoAdjunto>> getRecursosAdjuntos();

    @GET("/smartlearn/api/recurso-adjunto/{id}")
    Call<RecursoAdjunto> getRecursoAdjuntoById(@Path("id") Integer idRecurso);

    @POST("/smartlearn/api/recurso-adjunto")
    Call<RecursoAdjunto> saveRecursoAdjunto(@Body RecursoAdjunto recursoAdjunto);

    @PUT("/smartlearn/api/recurso-adjunto/{id}")
    Call<RecursoAdjunto> updateRecursoAdjunto(@Path("id") int id, @Body RecursoAdjunto recursoAdjunto);

    @DELETE("/smartlearn/api/recurso-adjunto/{id}")
    Call<Void> deleteRecursoAdjunto(@Path("id") int id);

    // /smartlearn/api/tipo-recurso
    @GET("/smartlearn/api/tipo-recurso")
    Call<List<TipoRecurso>> getTiposRecurso();

    @GET("/smartlearn/api/tipo-recurso/{id}")
    Call<TipoRecurso> getTipoRecursoById(@Path("id") Integer idTipo);
}

