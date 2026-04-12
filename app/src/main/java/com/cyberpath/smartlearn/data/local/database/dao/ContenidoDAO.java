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
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;
import com.cyberpath.smartlearn.data.model.ejercicio.Opcion;
import com.cyberpath.smartlearn.data.model.ejercicio.Pregunta;

import java.util.ArrayList;
import java.util.List;

public class ContenidoDAO {
    private final Database dbHelper;
    private SQLiteDatabase db;

    public ContenidoDAO(Context context) {
        dbHelper = Database.getInstance(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
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

    // -----------------------
    // BORRAR MATERIA DESCARGADA (BORRA EL CONTENIDO RELACIONADO)
    // -----------------------
    public boolean borrarMateriaDescargada(Integer idMateria) {
        open();

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
