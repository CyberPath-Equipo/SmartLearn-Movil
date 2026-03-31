package com.cyberpath.smartlearn.data.local.database.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cyberpath.smartlearn.data.local.database.Database;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;

import java.util.ArrayList;
import java.util.List;

public class SubtemasRepository {

    private final Database database;

    public SubtemasRepository(Context context) {
        this.database = Database.getInstance(context);
    }

    public List<Subtema> obtenerSubtemasPorTema(int idTema) {
        List<Subtema> subtemas = new ArrayList<>();
        SQLiteDatabase db = null;

        try {
            db = database.getReadableDatabase();
            String query = "SELECT id_subtema, id_tema, nombre FROM tbl_subtema WHERE id_tema = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(idTema)});

            if (cursor.moveToFirst()) {
                do {
                    Subtema subtema = new Subtema();
                    subtema.setId(cursor.getInt(0));
                    subtema.setIdTema(cursor.getInt(1));
                    subtema.setNombre(cursor.getString(2));
                    
                    subtemas.add(subtema);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return subtemas;
    }

    public void guardarSubtemas(List<Subtema> subtemas) {
        SQLiteDatabase db = null;

        try {
            db = database.getWritableDatabase();
            db.beginTransaction();

            for (Subtema subtema : subtemas) {
                ContentValues values = new ContentValues();
                values.put("id_subtema", subtema.getId());
                values.put("id_tema", subtema.getIdTema()); // Asegúrate que tu modelo tenga este método
                values.put("nombre", subtema.getNombre());

                db.insertWithOnConflict("tbl_subtema", null, values, SQLiteDatabase.CONFLICT_REPLACE);
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