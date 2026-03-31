package com.cyberpath.smartlearn.data.local.database.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cyberpath.smartlearn.data.local.database.Database;
import com.cyberpath.smartlearn.data.model.contenido.Tema;

import java.util.ArrayList;
import java.util.List;

public class TemasRepository {

    private final Database database;

    public TemasRepository(Context context) {
        this.database = Database.getInstance(context);
    }

    public List<Tema> obtenerTemasPorMateria(int idMateria) {
        List<Tema> temas = new ArrayList<>();
        SQLiteDatabase db = null;

        try {
            db = database.getReadableDatabase();
            String query = "SELECT id_tema, id_materia, nombre, orden, created_at, updated_at FROM tbl_tema WHERE id_materia = ? ORDER BY COALESCE(orden, 0), id_tema";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(idMateria)});

            if (cursor.moveToFirst()) {
                do {
                    Tema tema = new Tema();
                    tema.setId(cursor.getInt(0));
                    tema.setIdMateria(cursor.getInt(1));
                    tema.setNombre(cursor.getString(2));
                    tema.setOrden(cursor.getInt(3));
                    tema.setCreatedAt(cursor.getString(4));
                    tema.setUpdatedAt(cursor.getString(5));

                    temas.add(tema);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return temas;
    }

    public void guardarTemas(List<Tema> temas) {
        SQLiteDatabase db = null;

        try {
            db = database.getWritableDatabase();
            db.beginTransaction();

            for (Tema tema : temas) {
                ContentValues values = new ContentValues();
                values.put("id_tema", tema.getId());
                values.put("id_materia", tema.getIdMateria());
                values.put("nombre", tema.getNombre());
                values.put("orden", tema.getOrden());
                values.put("created_at", tema.getCreatedAt());
                values.put("updated_at", tema.getUpdatedAt());

                db.insertWithOnConflict("tbl_tema", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cerrar() {
        if (database != null) {
            database.close();
        }
    }
}