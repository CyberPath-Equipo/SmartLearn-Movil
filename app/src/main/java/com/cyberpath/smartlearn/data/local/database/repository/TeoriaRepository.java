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


    public Teoria obtenerTeoriaPorSubtema(int idSubtema) {
        Teoria teoria = null;
        SQLiteDatabase db = null;

        try {
            db = database.getReadableDatabase();
            String query = "SELECT id_subtema, id_teoria, contenido, revisado, fuente, updated_at FROM tbl_teoria WHERE id_subtema = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(idSubtema)});

            if (cursor.moveToFirst()) {
                teoria = new Teoria();
                teoria.setIdSubtema(cursor.getInt(0));
                teoria.setId(cursor.getInt(1));
                teoria.setContenido(cursor.getString(2));
                teoria.setRevisado(cursor.getInt(3) == 1);
                teoria.setFuente(cursor.getString(4));
                teoria.setUpdatedAt(cursor.getString(5));
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
            values.put("id_subtema", teoria.getIdSubtema());
            values.put("id_teoria", teoria.getId());
            values.put("contenido", teoria.getContenido());
            values.put("revisado", teoria.isRevisado() ? 1 : 0);
            values.put("fuente", teoria.getFuente());
            values.put("updated_at", teoria.getUpdatedAt());

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