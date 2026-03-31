package com.cyberpath.smartlearn.data.api;

import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.model.contenido.Teoria;
import com.cyberpath.smartlearn.data.model.contenido.ProgresoSubtema;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.IntentoEjercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;
import com.cyberpath.smartlearn.data.model.relaciones.UsuarioMateria;
import com.cyberpath.smartlearn.data.model.usuario.Configuracion;
import com.cyberpath.smartlearn.data.model.usuario.Rol;
import com.cyberpath.smartlearn.data.model.usuario.UltimaConexion;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.model.recurso.RecursoAdjunto;
import com.cyberpath.smartlearn.data.model.recurso.TipoRecurso;
import com.cyberpath.smartlearn.web.login.CambioPasswordDto;
import com.cyberpath.smartlearn.web.login.LoginRequest;
import com.cyberpath.smartlearn.web.login.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // =========== /smartlearn/api/usuario ======================
    @GET("/smartlearn/api/usuario")
    Call<List<Usuario>> getUsuarios();

    @GET("/smartlearn/api/usuario/{id}")
    Call<Usuario> getUsuarioById(@Path("id") Integer idUsuario);

    @GET("/smartlearn/api/usuario/{id}/materias")
    Call<List<Materia>> getMateriasByUsuario(@Path("id") Integer idUsuario);

    @GET("/smartlearn/api/usuario/{idUsuario}/materia/{idMateria}/ejercicios-realizados")
    Call<Long> getEjerciciosRealizadosByUsuarioAndMateria(@Path("idUsuario") int idUsuario, @Path("idMateria") int idMateria);

    @POST("/smartlearn/api/usuario")
    Call<Usuario> saveUsuario(@Body Usuario usuario);

    @POST("/smartlearn/api/usuario/registro")
    Call<Usuario> registerUsuario(@Body Usuario usuario);

    @POST("/smartlearn/api/usuario/login")
    Call<Usuario> login(@Body Usuario loginRequest);

    @POST("/smartlearn/api/usuario/login/docente")
    Call<LoginResponse> loginDocente(@Body LoginRequest loginRequest);

    @PUT("/smartlearn/api/usuario/{id}")
    Call<Usuario> updateUsuario(@Path("id") int id, @Body Usuario usuario);

    @PUT("/smartlearn/api/usuario/{id}/password")
    Call<Void> updateUsuarioPassword(@Path("id") int id, @Body CambioPasswordDto cambioPasswordDto);

    // =========== /smartlearn/api/materia ======================
    @GET("/smartlearn/api/materia")
    Call<List<Materia>> getMaterias();

    @GET("/smartlearn/api/materia/{id}")
    Call<Materia> getMateriaById(@Path("id") Integer idMateria);

    @GET("/smartlearn/api/materia/{id}/temas")
    Call<List<Tema>> getTemasByMateria(@Path("id") Integer idMateria);

    @GET("/smartlearn/api/materia/{id}/total-ejercicios")
    Call<Long> getTotalEjerciciosByMateria(@Path("id") int idMateria);

    @POST("/smartlearn/api/materia")
    Call<Materia> saveMateria(@Body Materia materia);

    @PUT("/smartlearn/api/materia/{id}")
    Call<Materia> updateMateria(@Path("id") int idMateria, @Body Materia materia);

    @DELETE("/smartlearn/api/materia/{id}")
    Call<Void> deleteMateria(@Path("id") int idMateria);

    // =========== /smartlearn/api/tema ======================
    @GET("/smartlearn/api/tema")
    Call<List<Tema>> getTemas();

    @GET("/smartlearn/api/tema/{id}")
    Call<Tema> getTemaById(@Path("id") Integer idTema);

    @GET("/smartlearn/api/tema/{id}/subtemas")
    Call<List<Subtema>> getSubtemasByTema(@Path("id") Integer idTema);

    @GET("/smartlearn/api/tema/{id}/materia")
    Call<Materia> getMateriaByTema(@Path("id") Integer idTema);

    @POST("/smartlearn/api/tema")
    Call<Tema> saveTema(@Body Tema tema);

    @PUT("/smartlearn/api/tema/{id}")
    Call<Tema> updateTema(@Path("id") int idTema, @Body Tema tema);

    @DELETE("/smartlearn/api/tema/{id}")
    Call<Void> deleteTema(@Path("id") int idTema);

    // =========== /smartlearn/api/subtema ======================
    @GET("/smartlearn/api/subtema")
    Call<List<Subtema>> getSubtemas();

    @GET("/smartlearn/api/subtema/{id}")
    Call<Subtema> getSubtemaById(@Path("id") Integer idSubtema);

    @GET("/smartlearn/api/subtema/{id}/tema")
    Call<Tema> getTemaBySubtema(@Path("id") Integer idSubtema);

    @GET("/smartlearn/api/subtema/{id}/teoria")
    Call<Teoria> getTeoriaBySubtema(@Path("id") Integer idSubtema);

    @GET("/smartlearn/api/subtema/{id}/ejercicios")
    Call<List<Ejercicio>> getEjerciciosBySubtema(@Path("id") Integer idSubtema);

    @POST("/smartlearn/api/subtema")
    Call<Subtema> saveSubtema(@Body Subtema subtema);

    @PUT("/smartlearn/api/subtema/{id}")
    Call<Subtema> updateSubtema(@Path("id") int idSubtema, @Body Subtema subtema);

    @DELETE("/smartlearn/api/subtema/{id}")
    Call<Void> deleteSubtema(@Path("id") int idSubtema);

    // =========== /smartlearn/api/teoria ======================
    @GET("/smartlearn/api/teoria")
    Call<List<Teoria>> getTeorias();

    @GET("/smartlearn/api/teoria/{id}")
    Call<Teoria> getTeoriaById(@Path("id") Integer idTeoria);

    @POST("/smartlearn/api/teoria")
    Call<Teoria> saveTeoria(@Body Teoria teoria);

    @PUT("/smartlearn/api/teoria/{id}")
    Call<Teoria> updateTeoria(@Path("id") int idTeoria, @Body Teoria teoria);

    @DELETE("/smartlearn/api/teoria/{id}")
    Call<Void> deleteTeoria(@Path("id") int idTeoria);

    // =========== /smartlearn/api/progreso-subtema ======================
    @GET("/smartlearn/api/progreso-subtema")
    Call<List<ProgresoSubtema>> getProgresosSubtema();

    @GET("/smartlearn/api/progreso-subtema/{id}")
    Call<ProgresoSubtema> getProgresoSubtemaById(@Path("id") Integer idProgreso);

    @POST("/smartlearn/api/progreso-subtema")
    Call<ProgresoSubtema> saveProgresoSubtema(@Body ProgresoSubtema progresoSubtema);

    @PUT("/smartlearn/api/progreso-subtema/{id}")
    Call<ProgresoSubtema> updateProgresoSubtema(@Path("id") int idProgreso, @Body ProgresoSubtema progresoSubtema);

    @DELETE("/smartlearn/api/progreso-subtema/{id}")
    Call<Void> deleteProgresoSubtema(@Path("id") int idProgreso);

    // =========== /smartlearn/api/usuario-materia ======================
    @GET("/smartlearn/api/usuario-materia")
    Call<List<UsuarioMateria>> getUsuariosMateria();

    @GET("/smartlearn/api/usuario-materia/materia/{idMateria}/usuarios")
    Call<List<UsuarioMateria>> getUsuariosByMateria(@Path("idMateria") Integer idMateria);

    @GET("/smartlearn/api/usuario-materia/usuario/{idUsuario}/materias")
    Call<List<Materia>> getMateriasByUsuarioRelacion(@Path("idUsuario") Integer idUsuario);

    @POST("/smartlearn/api/usuario-materia")
    Call<UsuarioMateria> saveUsuarioMateria(@Body UsuarioMateria usuarioMateria);

    @PUT("/smartlearn/api/usuario-materia/{idUsuario}/{idMateria}")
    Call<UsuarioMateria> updateUsuarioMateria(@Path("idUsuario") int idUsuario, @Path("idMateria") int idMateria, @Body UsuarioMateria usuarioMateria);

    @DELETE("/smartlearn/api/usuario-materia/{idUsuario}/{idMateria}")
    Call<Void> deleteUsuarioMateria(@Path("idUsuario") int idUsuario, @Path("idMateria") int idMateria);

    // =========== /smartlearn/api/configuracion ======================
    @GET("/smartlearn/api/configuracion")
    Call<List<Configuracion>> getConfiguraciones();

    @GET("/smartlearn/api/configuracion/{id}")
    Call<Configuracion> getConfiguracionByUsuarioId(@Path("id") Integer idUsuario);

    @POST("/smartlearn/api/configuracion")
    Call<Configuracion> saveConfiguracion(@Body Configuracion configuracion);

    @PUT("/smartlearn/api/configuracion/{id}")
    Call<Configuracion> updateConfiguracionByUsuarioId(@Path("id") int idUsuario, @Body Configuracion configuracion);

    @DELETE("/smartlearn/api/configuracion/{id}")
    Call<Void> deleteConfiguracion(@Path("id") int idConfiguracion);

    // =========== /smartlearn/api/rol ======================
    @GET("/smartlearn/api/rol")
    Call<List<Rol>> getRoles();

    @GET("/smartlearn/api/rol/{id}")
    Call<Rol> getRolById(@Path("id") Integer idRol);

    @POST("/smartlearn/api/rol")
    Call<Rol> saveRol(@Body Rol rol);

    @PUT("/smartlearn/api/rol/{id}")
    Call<Rol> updateRol(@Path("id") int idRol, @Body Rol rol);

    @DELETE("/smartlearn/api/rol/{id}")
    Call<Void> deleteRol(@Path("id") int idRol);

    // =========== /smartlearn/api/ultima-conexion ======================
    @GET("/smartlearn/api/ultima-conexion")
    Call<List<UltimaConexion>> getUltimasConexiones();

    @GET("/smartlearn/api/ultima-conexion/{id}")
    Call<UltimaConexion> getUltimaConexionById(@Path("id") Integer idConexion);

    @POST("/smartlearn/api/ultima-conexion")
    Call<UltimaConexion> saveUltimaConexion(@Body UltimaConexion ultimaConexion);

    @PUT("/smartlearn/api/ultima-conexion/{id}")
    Call<UltimaConexion> updateUltimaConexion(@Path("id") int id, @Body UltimaConexion ultimaConexion);

    @DELETE("/smartlearn/api/ultima-conexion/{id}")
    Call<Void> deleteUltimaConexion(@Path("id") int idConexion);

    // =========== /smartlearn/api/ejercicio ======================
    @GET("/smartlearn/api/ejercicio")
    Call<List<Ejercicio>> getEjercicios();

    @GET("/smartlearn/api/ejercicio/{id}")
    Call<Ejercicio> getEjercicioById(@Path("id") Integer idEjercicio);

    @POST("/smartlearn/api/ejercicio")
    Call<Ejercicio> saveEjercicio(@Body Ejercicio ejercicio);

    @PUT("/smartlearn/api/ejercicio/{id}")
    Call<Ejercicio> updateEjercicio(@Path("id") int idEjercicio, @Body Ejercicio ejercicio);

    @DELETE("/smartlearn/api/ejercicio/{id}")
    Call<Void> deleteEjercicio(@Path("id") int idEjercicio);

    @GET("/smartlearn/api/ejercicio/{id}/preguntas")
    Call<List<Pregunta>> getPreguntasByEjercicio(@Path("id") Integer idEjercicio);

    @POST("/smartlearn/api/ejercicio/{id}/pregunta")
    Call<Pregunta> crearPreguntaEnEjercicio(@Path("id") Integer idEjercicio, @Body Pregunta pregunta);

    // =========== /smartlearn/api/pregunta ======================
    @GET("/smartlearn/api/pregunta")
    Call<List<Pregunta>> getPreguntas();

    @GET("/smartlearn/api/pregunta/{id}")
    Call<Pregunta> getPreguntaById(@Path("id") Integer idPregunta);

    @POST("/smartlearn/api/pregunta")
    Call<Pregunta> savePregunta(@Body Pregunta pregunta);

    @PUT("/smartlearn/api/pregunta/{id}")
    Call<Pregunta> updatePregunta(@Path("id") int idPregunta, @Body Pregunta pregunta);

    @DELETE("/smartlearn/api/pregunta/{id}")
    Call<Void> deletePregunta(@Path("id") int idPregunta);

    @GET("/smartlearn/api/pregunta/{id}/opciones")
    Call<List<Opcion>> getOpcionesByPregunta(@Path("id") Integer idPregunta);

    // =========== /smartlearn/api/opcion ======================
    @GET("/smartlearn/api/opcion")
    Call<List<Opcion>> getOpciones();

    @GET("/smartlearn/api/opcion/{id}")
    Call<Opcion> getOpcionById(@Path("id") Integer idOpcion);

    @POST("/smartlearn/api/opcion")
    Call<Opcion> saveOpcion(@Body Opcion opcion);

    @PUT("/smartlearn/api/opcion/{id}")
    Call<Opcion> updateOpcion(@Path("id") int idOpcion, @Body Opcion opcion);

    @DELETE("/smartlearn/api/opcion/{id}")
    Call<Void> deleteOpcion(@Path("id") int idOpcion);

    // =========== /smartlearn/api/intento-ejercicio ======================
    @GET("/smartlearn/api/intento-ejercicio")
    Call<List<IntentoEjercicio>> getIntentosEjercicio();

    @GET("/smartlearn/api/intento-ejercicio/{id}")
    Call<IntentoEjercicio> getIntentoEjercicioById(@Path("id") Integer idIntento);

    @POST("/smartlearn/api/intento-ejercicio")
    Call<IntentoEjercicio> saveIntentoEjercicio(@Body IntentoEjercicio intentoEjercicio);

    @PUT("/smartlearn/api/intento-ejercicio/{id}")
    Call<IntentoEjercicio> updateIntentoEjercicio(@Path("id") int idIntento, @Body IntentoEjercicio intentoEjercicio);

    @DELETE("/smartlearn/api/intento-ejercicio/{id}")
    Call<Void> deleteIntentoEjercicio(@Path("id") int idIntento);

    // =========== /smartlearn/api/recurso-adjunto ======================
    @GET("/smartlearn/api/recurso-adjunto")
    Call<List<RecursoAdjunto>> getRecursosAdjuntos();

    @GET("/smartlearn/api/recurso-adjunto/{id}")
    Call<RecursoAdjunto> getRecursoAdjuntoById(@Path("id") Integer idRecurso);

    @POST("/smartlearn/api/recurso-adjunto")
    Call<RecursoAdjunto> saveRecursoAdjunto(@Body RecursoAdjunto recursoAdjunto);

    @PUT("/smartlearn/api/recurso-adjunto/{id}")
    Call<RecursoAdjunto> updateRecursoAdjunto(@Path("id") int idRecurso, @Body RecursoAdjunto recursoAdjunto);

    @DELETE("/smartlearn/api/recurso-adjunto/{id}")
    Call<Void> deleteRecursoAdjunto(@Path("id") int idRecurso);

    // =========== /smartlearn/api/tipo-recurso ======================
    @GET("/smartlearn/api/tipo-recurso")
    Call<List<TipoRecurso>> getTiposRecurso();

    @GET("/smartlearn/api/tipo-recurso/{id}")
    Call<TipoRecurso> getTipoRecursoById(@Path("id") Integer idTipoRecurso);

    @POST("/smartlearn/api/tipo-recurso")
    Call<TipoRecurso> saveTipoRecurso(@Body TipoRecurso tipoRecurso);

    @PUT("/smartlearn/api/tipo-recurso/{id}")
    Call<TipoRecurso> updateTipoRecurso(@Path("id") int idTipoRecurso, @Body TipoRecurso tipoRecurso);

    @DELETE("/smartlearn/api/tipo-recurso/{id}")
    Call<Void> deleteTipoRecurso(@Path("id") int idTipoRecurso);
}