package com.cyberpath.smartlearn.data.local.database.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cyberpath.smartlearn.data.local.database.Database;
import com.cyberpath.smartlearn.data.model.contenido.Teoria;

public class TeoriaRepository {

    private final Database database;

    public TeoriaRepository(Context context) {
        this.database = Database.getInstance(context);
    }

    /**
     * Obtiene la teoría de un subtema específico desde la BD local.
     */
    public Teoria obtenerTeoriaPorSubtema(int idSubtema) {
        Teoria teoria = null;
        SQLiteDatabase db = null;

        try {
            db = database.getReadableDatabase();
            String query = "SELECT id_subtema, contenido, revisado FROM tbl_teoria WHERE id_subtema = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(idSubtema)});

            if (cursor.moveToFirst()) {
                teoria = new Teoria();
                teoria.setId(cursor.getInt(0));
                teoria.setContenido(cursor.getString(1));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return teoria;
    }

    public void guardarTeoria(Teoria teoria) {
        SQLiteDatabase db = null;

        try {
            db = database.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id_subtema", teoria.getId());
            values.put("contenido", teoria.getContenido());

            db.insertWithOnConflict("tbl_teoria", null, values, SQLiteDatabase.CONFLICT_REPLACE);
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