package com.cyberpath.smartlearn.data.remote.api;

import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.model.contenido.Teoria;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.IntentoEjercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
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

    @POST("/smartlearn/api/registro")
    Call<Usuario> save(@Body Usuario usuario);

    @POST("/smartlearn/api/usuario/login")
    Call<Usuario> login(@Body Usuario loginRequest);

    @PUT("/smartlearn/api/usuario/{id}")
    Call<Usuario> update(@Path("id") int id, @Body Usuario usuario);

    @DELETE("/smartlearn/api/usuario/{id}")
    Call<Void> delete(@Path("id") int id);

    // /smartlearn/api/materia
    @GET("/smartlearn/api/materia")
    Call<List<Materia>> getMaterias();

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
    Call<Teoria> getTeoriaById(@Path("id") Integer idSubtema);

    // /smartlearn/api/usuario-materia
    @POST("/smartlearn/api/usuario-materia")
    Call<UsuarioMateria> save(@Body UsuarioMateria usuarioMateria);

    // /smartlearn/api/ultima-conexion
    @POST("/smartlearn/api/ultima-conexion")
    Call<UltimaConexion> save(@Body UltimaConexion ultimaConexion);

    @PUT("/smartlearn/api/ultima-conexion/{id}")
    Call<UltimaConexion> update(@Path("id") int id, @Body UltimaConexion ultimaConexion);

    // /smartlearn/api/ejercicio
    @GET("/smartlearn/api/ejercicio/{id}/preguntas")
    Call<List<Pregunta>> getPreguntasByEjercicio(@Path("id") Integer idEjercicio);

    // /smartlearn/api/pregunta
    @GET("/smartlearn/api/pregunta/{id}/opciones")
    Call<List<Opcion>> getOpcionesByPregunta(@Path("id") Integer idPregunta);

    // /smartlearn/api/intento-ejercicio
    @POST("/smartlearn/api/intento-ejercicio")
    Call<IntentoEjercicio> save(@Body IntentoEjercicio intentoEjercicio);

    // /smartlearn/api/ejercicio
    @PUT("/smartlearn/api/ejercicio/{id}")
    Call<Ejercicio> update(@Path("id") int id, @Body Ejercicio ejercicio);

    // /smartlearn/api/usuario-ejercicio
    @POST("/smartlearn/api/usuario-ejercicio")
    Call<UsuarioEjercicio> save(@Body UsuarioEjercicio usuarioEjercicio);
}