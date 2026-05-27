package com.cyberpath.smartlearn.data.local.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cyberpath.smartlearn.data.local.database.Database;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.data.model.contenido.Teoria;
import com.cyberpath.smartlearn.data.model.estadisticas.DatoHistorico;
import com.cyberpath.smartlearn.data.model.estadisticas.InteresItem;
import com.cyberpath.smartlearn.data.model.estadisticas.ResumenEstadisticas;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContenidoDAO {
    private final Database dbHelper;
    private SQLiteDatabase db;

    public ContenidoDAO(Context context) {
        dbHelper = Database.getInstance(context);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
        }
    }

    public void close() {
        // No cerramos dbHelper aqui para evitar invalidar el singleton en operaciones encadenadas.
    }

    public void beginTransaction() {
        open();
        db.beginTransaction();
    }

    public void setTransactionSuccessful() {
        if (db != null && db.inTransaction()) {
            db.setTransactionSuccessful();
        }
    }

    public void endTransaction() {
        if (db != null && db.inTransaction()) {
            db.endTransaction();
        }
    }

    // -----------------------
    // MATERIAS
    // -----------------------
    public long insertarMateria(Materia materia) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_materia", materia.getId());
        values.put("nombre", materia.getNombre());
        values.put("descripcion", materia.getDescripcion());
        values.put("slug", materia.getSlug());
        values.put("created_at", materia.getCreatedAt());
        values.put("updated_at", materia.getUpdatedAt());
        long resultado = db.insertWithOnConflict("tbl_materia", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
        return resultado;
    }

    public boolean materiaDescargada(Integer idMateria) {
        open();
        Cursor cursor = db.query("tbl_materia_descargada",
                null,
                "id_materia = ?",
                new String[]{String.valueOf(idMateria)},
                null, null, null);
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        close();
        return existe;
    }

    public long registrarMateriaDescargada(Materia materia, double tamaño) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_materia", materia.getId());
        values.put("nombre", materia.getNombre());
        values.put("descripcion", materia.getDescripcion());
        values.put("tamaño_mb", tamaño);
        long resultado = db.insertWithOnConflict("tbl_materia_descargada", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
        return resultado;
    }

    public Materia obtenerMateria(Integer idMateria) {
        open();
        Cursor cursor = db.query("tbl_materia",
                null,
                "id_materia = ?",
                new String[]{String.valueOf(idMateria)},
                null, null, null);

        Materia materia = null;
        if (cursor.moveToFirst()) {
            materia = new Materia();
            materia.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_materia")));
            materia.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            materia.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow("descripcion")));
            materia.setSlug(cursor.getString(cursor.getColumnIndexOrThrow("slug")));
            materia.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            materia.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        }
        cursor.close();
        close();
        return materia;
    }

    public List<Materia> obtenerTodasLasMaterias() {
        open();
        List<Materia> materias = new ArrayList<>();
        Cursor cursor = db.query("tbl_materia", null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            Materia materia = new Materia();
            materia.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_materia")));
            materia.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            materia.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow("descripcion")));
            materia.setSlug(cursor.getString(cursor.getColumnIndexOrThrow("slug")));
            materia.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            materia.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
            materias.add(materia);
        }
        cursor.close();
        close();
        return materias;
    }

    public List<Materia> obtenerMateriasDescargadas() {
        open();
        List<Materia> materias = new ArrayList<>();
        Cursor cursor = db.query("tbl_materia_descargada", null, null, null, null, null, "fecha_descarga DESC");

        while (cursor.moveToNext()) {
            Materia materia = new Materia();
            materia.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_materia")));
            materia.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            materia.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow("descripcion")));
            materias.add(materia);
        }
        cursor.close();
        close();
        return materias;
    }

    public List<Integer> obtenerIdsMateriasDescargadas() {
        open();
        List<Integer> idsMaterias = new ArrayList<>();
        Cursor cursor = db.query(
                "tbl_materia_descargada",
                new String[]{"id_materia"},
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            idsMaterias.add(cursor.getInt(cursor.getColumnIndexOrThrow("id_materia")));
        }

        cursor.close();
        close();
        return idsMaterias;
    }

    // -----------------------
    // temas
    // -----------------------
    public long insertarTema(Tema tema) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_tema", tema.getId());
        values.put("id_materia", tema.getIdMateria());
        values.put("nombre", tema.getNombre());
        values.put("orden", tema.getOrden());
        values.put("created_at", tema.getCreatedAt());
        values.put("updated_at", tema.getUpdatedAt());
        long resultado = db.insertWithOnConflict("tbl_tema", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
        return resultado;
    }

    public List<Tema> obtenerTemasPorMateria(Integer idMateria) {
        open();
        List<Tema> temas = new ArrayList<>();
        Cursor cursor = db.query("tbl_tema",
                null,
                "id_materia = ?",
                new String[]{String.valueOf(idMateria)},
                null, null, "COALESCE(orden, 0) ASC, id_tema ASC");

        while (cursor.moveToNext()) {
            Tema tema = new Tema();
            tema.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_tema")));
            tema.setIdMateria(cursor.getInt(cursor.getColumnIndexOrThrow("id_materia")));
            tema.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            tema.setOrden(cursor.getInt(cursor.getColumnIndexOrThrow("orden")));
            tema.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            tema.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
            temas.add(tema);
        }
        cursor.close();
        close();
        return temas;
    }

    // -----------------------
    // SUBTEMAS
    // -----------------------
    public long insertarSubtema(Subtema subtema) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_subtema", subtema.getId());
        values.put("id_tema", subtema.getIdTema());
        values.put("nombre", subtema.getNombre());
        values.put("orden", subtema.getOrden());
        values.put("created_at", subtema.getCreatedAt());
        values.put("updated_at", subtema.getUpdatedAt());
        long resultado = db.insertWithOnConflict("tbl_subtema", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
        return resultado;
    }

    public List<Subtema> obtenerSubtemasPorTema(Integer idTema) {
        open();
        List<Subtema> subtemas = new ArrayList<>();
        Cursor cursor = db.query("tbl_subtema",
                null,
                "id_tema = ?",
                new String[]{String.valueOf(idTema)},
                null, null, "COALESCE(orden, 0) ASC, id_subtema ASC");

        while (cursor.moveToNext()) {
            Subtema subtema = new Subtema();
            subtema.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_subtema")));
            subtema.setIdTema(cursor.getInt(cursor.getColumnIndexOrThrow("id_tema")));
            subtema.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            subtema.setOrden(cursor.getInt(cursor.getColumnIndexOrThrow("orden")));
            subtema.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            subtema.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
            subtemas.add(subtema);
        }
        cursor.close();
        close();
        return subtemas;
    }

    public Subtema obtenerSubtemaPorId(Integer idSubtema) {
        open();
        Cursor cursor = db.query("tbl_subtema",
                null,
                "id_subtema = ?",
                new String[]{String.valueOf(idSubtema)},
                null, null, null);

        Subtema subtema = null;
        if (cursor.moveToFirst()) {
            subtema = new Subtema();
            subtema.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_subtema")));
            subtema.setIdTema(cursor.getInt(cursor.getColumnIndexOrThrow("id_tema")));
            subtema.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            subtema.setOrden(cursor.getInt(cursor.getColumnIndexOrThrow("orden")));
            subtema.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            subtema.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        }
        cursor.close();
        close();
        return subtema;
    }

    // -----------------------
    // TEORÍA
    // -----------------------
    public long insertarTeoria(Teoria teoria) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_teoria", teoria.getId());
        values.put("id_subtema", teoria.getIdSubtema());
        values.put("contenido", teoria.getContenido());
        values.put("revisado", teoria.isRevisado() ? 1 : 0);
        values.put("fuente", teoria.getFuente());
        values.put("updated_at", teoria.getUpdatedAt());
        long resultado = db.insertWithOnConflict("tbl_teoria", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
        return resultado;
    }

    public Teoria obtenerTeoriaPorSubtema(Integer idSubtema) {
        open();
        Cursor cursor = db.query("tbl_teoria",
                null,
                "id_subtema = ?",
                new String[]{String.valueOf(idSubtema)},
                null, null, null);

        Teoria teoria = null;
        if (cursor.moveToFirst()) {
            teoria = new Teoria();
            teoria.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_teoria")));
            teoria.setIdSubtema(cursor.getInt(cursor.getColumnIndexOrThrow("id_subtema")));
            teoria.setContenido(cursor.getString(cursor.getColumnIndexOrThrow("contenido")));
            teoria.setRevisado(cursor.getInt(cursor.getColumnIndexOrThrow("revisado")) == 1);
            teoria.setFuente(cursor.getString(cursor.getColumnIndexOrThrow("fuente")));
            teoria.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        }
        cursor.close();
        close();
        return teoria;
    }

    // -----------------------
    // EJERCICIOS
    // -----------------------
    public long insertarEjercicio(Ejercicio ejercicio) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_ejercicio", ejercicio.getId());
        values.put("id_subtema", ejercicio.getIdSubtema());
        values.put("nombre", ejercicio.getNombre());
        values.put("hecho", ejercicio.isHecho() ? 1 : 0);
        values.put("activo", Boolean.TRUE.equals(ejercicio.getActivo()) ? 1 : 0);
        values.put("tipo", ejercicio.getTipo());
        values.put("dificultad", ejercicio.getDificultad());
        values.put("orden", ejercicio.getOrden());
        values.put("created_at", ejercicio.getCreatedAt());
        long resultado = db.insertWithOnConflict("tbl_ejercicio", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
        return resultado;
    }

    public List<Ejercicio> obtenerEjerciciosPorSubtema(Integer idSubtema) {
        open();
        List<Ejercicio> ejercicios = new ArrayList<>();
        Cursor cursor = db.query("tbl_ejercicio",
                null,
                "id_subtema = ?",
                new String[]{String.valueOf(idSubtema)},
                null, null, "COALESCE(orden, 0) ASC, id_ejercicio ASC");

        while (cursor.moveToNext()) {
            Ejercicio ejercicio = new Ejercicio();
            ejercicio.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_ejercicio")));
            ejercicio.setIdSubtema(cursor.getInt(cursor.getColumnIndexOrThrow("id_subtema")));
            ejercicio.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            ejercicio.setHecho(cursor.getInt(cursor.getColumnIndexOrThrow("hecho")) == 1);
            ejercicio.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1);
            ejercicio.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
            ejercicio.setDificultad(cursor.getString(cursor.getColumnIndexOrThrow("dificultad")));
            ejercicio.setOrden(cursor.getInt(cursor.getColumnIndexOrThrow("orden")));
            ejercicio.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            ejercicios.add(ejercicio);
        }
        cursor.close();
        close();
        return ejercicios;
    }


    // -----------------------
    // PREGUNTAS
    // -----------------------
    public long insertarPregunta(Pregunta pregunta) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_pregunta", pregunta.getId());
        values.put("id_ejercicio", pregunta.getIdEjercicio());
        values.put("enunciado", pregunta.getEnunciado());
        values.put("tipo", pregunta.getTipo());
        values.put("orden", pregunta.getOrden());
        values.put("puntos", pregunta.getPuntos());
        long resultado = db.insertWithOnConflict("tbl_pregunta", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
        return resultado;
    }

    public List<Pregunta> obtenerPreguntasPorEjercicio(Integer idEjercicio) {
        open();
        List<Pregunta> preguntas = new ArrayList<>();
        Cursor cursor = db.query("tbl_pregunta",
                null,
                "id_ejercicio = ?",
                new String[]{String.valueOf(idEjercicio)},
                null, null, "COALESCE(orden, 0) ASC, id_pregunta ASC");

        while (cursor.moveToNext()) {
            Pregunta pregunta = new Pregunta();
            pregunta.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_pregunta")));
            pregunta.setIdEjercicio(cursor.getInt(cursor.getColumnIndexOrThrow("id_ejercicio")));
            pregunta.setEnunciado(cursor.getString(cursor.getColumnIndexOrThrow("enunciado")));
            pregunta.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
            pregunta.setOrden(cursor.getInt(cursor.getColumnIndexOrThrow("orden")));
            int puntosIdx = cursor.getColumnIndex("puntos");
            if (puntosIdx != -1 && !cursor.isNull(puntosIdx)) {
                pregunta.setPuntos(cursor.getDouble(puntosIdx));
            }
            preguntas.add(pregunta);
        }
        cursor.close();
        close();
        return preguntas;
    }

    // -----------------------
    // OPCIONES
    // -----------------------
    public long insertarOpcion(Opcion opcion) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_opcion", opcion.getId());
        values.put("id_pregunta", opcion.getIdPregunta());
        values.put("texto", opcion.getTexto());
        values.put("es_correcta", opcion.isCorrecta() ? 1 : 0);
        values.put("orden", opcion.getOrden());
        long resultado = db.insertWithOnConflict("tbl_opcion", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
        return resultado;
    }

    public List<Opcion> obtenerOpcionesPorPregunta(Integer idPregunta) {
        open();
        List<Opcion> opciones = new ArrayList<>();
        Cursor cursor = db.query("tbl_opcion",
                null,
                "id_pregunta = ?",
                new String[]{String.valueOf(idPregunta)},
                null, null, "COALESCE(orden, 0) ASC, id_opcion ASC");

        while (cursor.moveToNext()) {
            Opcion opcion = new Opcion();
            opcion.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id_opcion")));
            opcion.setIdPregunta(cursor.getInt(cursor.getColumnIndexOrThrow("id_pregunta")));
            opcion.setTexto(cursor.getString(cursor.getColumnIndexOrThrow("texto")));
            opcion.setCorrecta(cursor.getInt(cursor.getColumnIndexOrThrow("es_correcta")) == 1);
            int ordenIdx = cursor.getColumnIndex("orden");
            if (ordenIdx != -1) opcion.setOrden(cursor.getInt(ordenIdx));
            opciones.add(opcion);
        }
        cursor.close();
        close();
        return opciones;
    }

    public long marcarEjercicioUsuarioLocal(int idUsuario, int idEjercicio, boolean hecho, boolean pendienteSync) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_usuario", idUsuario);
        values.put("id_ejercicio", idEjercicio);
        values.put("hecho", hecho ? 1 : 0);
        values.put("pendiente_sync", pendienteSync ? 1 : 0);
        values.put("fecha_actualizacion", String.valueOf(System.currentTimeMillis()));
        long resultado = db.insertWithOnConflict("tbl_usuario_ejercicio_local", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
        return resultado;
    }

    public long guardarIntentoEjercicioLocal(int idUsuario, int idEjercicio, double puntaje, String fecha, boolean pendienteSync) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_usuario", idUsuario);
        values.put("id_ejercicio", idEjercicio);
        values.put("puntaje", puntaje);
        values.put("fecha", fecha);
        values.put("pendiente_sync", pendienteSync ? 1 : 0);
        long resultado = db.insert("tbl_intento_ejercicio_local", null, values);
        close();
        return resultado;
    }

    public void marcarIntentoLocalSincronizado(long idIntentoLocal) {
        open();
        ContentValues values = new ContentValues();
        values.put("pendiente_sync", 0);
        db.update("tbl_intento_ejercicio_local", values,
                "id_intento_local = ?", new String[]{String.valueOf(idIntentoLocal)});
        close();
    }

    public long registrarSesionEstudio(int idUsuario, Integer idSubtema, String tipoContenido, long duracionSegundos) {
        open();
        ContentValues values = new ContentValues();
        values.put("id_usuario", idUsuario);
        if (idSubtema != null) {
            values.put("id_subtema", idSubtema);
        } else {
            values.putNull("id_subtema");
        }
        values.put("tipo_contenido", tipoContenido != null ? tipoContenido : "GENERAL");
        values.put("duracion_segundos", Math.max(0, duracionSegundos));
        long resultado = db.insert("tbl_sesion_estudio", null, values);
        close();
        return resultado;
    }

    public ResumenEstadisticas obtenerResumenEstadisticas(int idUsuario) {
        open();
        ResumenEstadisticas resumen = new ResumenEstadisticas();

        Cursor cursorTotalEjercicios = db.rawQuery("SELECT COUNT(*) FROM tbl_ejercicio", null);
        if (cursorTotalEjercicios.moveToFirst()) {
            resumen.setEjerciciosTotales(cursorTotalEjercicios.getInt(0));
        }
        cursorTotalEjercicios.close();

        Cursor cursorCompletados = db.rawQuery(
                "SELECT COUNT(*) FROM tbl_usuario_ejercicio_local WHERE id_usuario = ? AND hecho = 1",
                new String[]{String.valueOf(idUsuario)}
        );
        if (cursorCompletados.moveToFirst()) {
            resumen.setEjerciciosCompletados(cursorCompletados.getInt(0));
        }
        cursorCompletados.close();

        Cursor cursorPromedio = db.rawQuery(
                "SELECT AVG(puntaje) FROM tbl_intento_ejercicio_local WHERE id_usuario = ?",
                new String[]{String.valueOf(idUsuario)}
        );
        if (cursorPromedio.moveToFirst() && !cursorPromedio.isNull(0)) {
            resumen.setPromedioAcierto(cursorPromedio.getDouble(0));
        }
        cursorPromedio.close();

        Cursor cursorTiempo = db.rawQuery(
                "SELECT IFNULL(SUM(duracion_segundos), 0), COUNT(*) FROM tbl_sesion_estudio WHERE id_usuario = ?",
                new String[]{String.valueOf(idUsuario)}
        );
        if (cursorTiempo.moveToFirst()) {
            long segundos = cursorTiempo.getLong(0);
            resumen.setMinutosEstudio(Math.round(segundos / 60f));
            resumen.setSesionesEstudio(cursorTiempo.getInt(1));
        }
        cursorTiempo.close();

        Cursor cursorTiempo7Dias = db.rawQuery(
                "SELECT IFNULL(SUM(duracion_segundos), 0) " +
                        "FROM tbl_sesion_estudio " +
                        "WHERE id_usuario = ? AND datetime(fecha_inicio) >= datetime('now', '-7 day')",
                new String[]{String.valueOf(idUsuario)}
        );
        if (cursorTiempo7Dias.moveToFirst()) {
            long segundos7Dias = cursorTiempo7Dias.getLong(0);
            resumen.setMinutosUltimos7Dias(Math.round(segundos7Dias / 60f));
        }
        cursorTiempo7Dias.close();

        close();
        return resumen;
    }

    public List<DatoHistorico> obtenerTiempoEstudioPorDia(int idUsuario, int dias) {
        open();
        List<DatoHistorico> datos = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT substr(fecha_inicio, 1, 10) AS fecha, IFNULL(SUM(duracion_segundos), 0) / 60.0 AS minutos " +
                        "FROM tbl_sesion_estudio " +
                        "WHERE id_usuario = ? AND datetime(fecha_inicio) >= datetime('now', ?) " +
                        "GROUP BY substr(fecha_inicio, 1, 10) " +
                        "ORDER BY fecha ASC",
                new String[]{String.valueOf(idUsuario), String.format(Locale.US, "-%d day", Math.max(1, dias))}
        );

        while (cursor.moveToNext()) {
            String fecha = cursor.getString(0);
            float minutos = cursor.getFloat(1);
            datos.add(new DatoHistorico(fecha, minutos));
        }
        cursor.close();
        close();
        return datos;
    }

    public List<DatoHistorico> obtenerRendimientoHistorico(int idUsuario, int limite) {
        open();
        List<DatoHistorico> datos = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT substr(fecha, 1, 10) AS fecha, puntaje " +
                        "FROM tbl_intento_ejercicio_local " +
                        "WHERE id_usuario = ? " +
                        "ORDER BY datetime(fecha) DESC " +
                        "LIMIT ?",
                new String[]{String.valueOf(idUsuario), String.valueOf(Math.max(1, limite))}
        );

        while (cursor.moveToNext()) {
            String fecha = cursor.getString(0);
            float puntaje = cursor.getFloat(1);
            datos.add(new DatoHistorico(fecha, puntaje));
        }
        cursor.close();
        close();
        return datos;
    }

    public List<InteresItem> obtenerTopIntereses(int idUsuario, int limite) {
        open();
        List<InteresItem> intereses = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT " +
                        "COALESCE(m.nombre, 'Materia') || ' / ' || COALESCE(t.nombre, 'Tema') || ' / ' || COALESCE(s.nombre, 'Subtema') AS ruta, " +
                        "IFNULL(SUM(se.duracion_segundos), 0) / 60.0 AS minutos " +
                        "FROM tbl_sesion_estudio se " +
                        "LEFT JOIN tbl_subtema s ON s.id_subtema = se.id_subtema " +
                        "LEFT JOIN tbl_tema t ON t.id_tema = s.id_tema " +
                        "LEFT JOIN tbl_materia m ON m.id_materia = t.id_materia " +
                        "WHERE se.id_usuario = ? AND se.id_subtema IS NOT NULL " +
                        "GROUP BY se.id_subtema " +
                        "ORDER BY minutos DESC " +
                        "LIMIT ?",
                new String[]{String.valueOf(idUsuario), String.valueOf(Math.max(1, limite))}
        );

        while (cursor.moveToNext()) {
            String ruta = cursor.getString(0);
            long minutos = Math.round(cursor.getFloat(1));
            intereses.add(new InteresItem(ruta, minutos));
        }
        cursor.close();
        close();
        return intereses;
    }

    // -----------------------
    // BORRAR MATERIA DESCARGADA (BORRA EL CONTENIDO RELACIONADO)
    // -----------------------
    public boolean borrarMateriaDescargada(Integer idMateria) {
        open();

        db.delete("tbl_intento_ejercicio_local", "id_ejercicio IN (SELECT id_ejercicio FROM tbl_ejercicio WHERE id_subtema IN (SELECT id_subtema FROM tbl_subtema WHERE id_tema IN (SELECT id_tema FROM tbl_tema WHERE id_materia = ?)))", new String[]{String.valueOf(idMateria)});
        db.delete("tbl_usuario_ejercicio_local", "id_ejercicio IN (SELECT id_ejercicio FROM tbl_ejercicio WHERE id_subtema IN (SELECT id_subtema FROM tbl_subtema WHERE id_tema IN (SELECT id_tema FROM tbl_tema WHERE id_materia = ?)))", new String[]{String.valueOf(idMateria)});

        db.delete("tbl_opcion", "id_pregunta IN (SELECT id_pregunta FROM tbl_pregunta WHERE id_ejercicio IN (SELECT id_ejercicio FROM tbl_ejercicio WHERE id_subtema IN (SELECT id_subtema FROM tbl_subtema WHERE id_tema IN (SELECT id_tema FROM tbl_tema WHERE id_materia = ?))))", new String[]{String.valueOf(idMateria)});
        db.delete("tbl_pregunta", "id_ejercicio IN (SELECT id_ejercicio FROM tbl_ejercicio WHERE id_subtema IN (SELECT id_subtema FROM tbl_subtema WHERE id_tema IN (SELECT id_tema FROM tbl_tema WHERE id_materia = ?)))", new String[]{String.valueOf(idMateria)});
        db.delete("tbl_ejercicio", "id_subtema IN (SELECT id_subtema FROM tbl_subtema WHERE id_tema IN (SELECT id_tema FROM tbl_tema WHERE id_materia = ?))", new String[]{String.valueOf(idMateria)});
        db.delete("tbl_teoria", "id_subtema IN (SELECT id_subtema FROM tbl_subtema WHERE id_tema IN (SELECT id_tema FROM tbl_tema WHERE id_materia = ?))", new String[]{String.valueOf(idMateria)});
        db.delete("tbl_subtema", "id_tema IN (SELECT id_tema FROM tbl_tema WHERE id_materia = ?)", new String[]{String.valueOf(idMateria)});
        db.delete("tbl_tema", "id_materia = ?", new String[]{String.valueOf(idMateria)});
        db.delete("tbl_materia", "id_materia = ?", new String[]{String.valueOf(idMateria)});


        int resultado = db.delete("tbl_materia_descargada", "id_materia = ?", new String[]{String.valueOf(idMateria)});
        close();
        return resultado > 0;
    }
}
