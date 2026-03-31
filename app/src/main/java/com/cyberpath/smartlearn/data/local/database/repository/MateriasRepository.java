package com.cyberpath.smartlearn.data.local.database.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cyberpath.smartlearn.data.local.database.Database;
import com.cyberpath.smartlearn.data.model.contenido.Materia;

import java.util.ArrayList;
import java.util.List;

public class MateriasRepository {

    private final Database database;

    public MateriasRepository(Context context) {
        this.database = Database.getInstance(context);
    }

    public List<Materia> obtenerTodasLasMaterias() {
        List<Materia> materias = new ArrayList<>();
        SQLiteDatabase db = null;

        try {
            db = database.getReadableDatabase();
            String query = "SELECT id_materia, nombre, descripcion FROM tbl_materia";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Materia materia = new Materia();
                    materia.setId(cursor.getInt(0)); // id_materia
                    materia.setNombre(cursor.getString(1)); // nombre
                    materia.setDescripcion(cursor.getString(2)); // descripcion
                    materia.setProgreso(0); // Por defecto en offline

                    materias.add(materia);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return materias;
    }

    public List<Materia> obtenerMateriasPorNombre(String nombre) {
        List<Materia> materias = new ArrayList<>();
        SQLiteDatabase db = null;

        try {
            db = database.getReadableDatabase();
            String query = "SELECT id_materia, nombre, descripcion FROM tbl_materia WHERE nombre LIKE ?";
            String[] selectionArgs = new String[]{"%" + nombre + "%"};
            Cursor cursor = db.rawQuery(query, selectionArgs);

            if (cursor.moveToFirst()) {
                do {
                    Materia materia = new Materia();
                    materia.setId(cursor.getInt(0));
                    materia.setNombre(cursor.getString(1));
                    materia.setDescripcion(cursor.getString(2));
                    materia.setProgreso(0);

                    materias.add(materia);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return materias;
    }

    public Materia obtenerMateriaPorId(Integer id) {
        SQLiteDatabase db = null;

        try {
            db = database.getReadableDatabase();
            String query = "SELECT id_materia, nombre, descripcion FROM tbl_materia WHERE id_materia = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

            if (cursor.moveToFirst()) {
                Materia materia = new Materia();
                materia.setId(cursor.getInt(0));
                materia.setNombre(cursor.getString(1));
                materia.setDescripcion(cursor.getString(2));
                materia.setProgreso(0);

                cursor.close();
                return materia;
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void guardarMateria(Materia materia) {
        SQLiteDatabase db = null;

        try {
            db = database.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id_materia", materia.getId());
            values.put("nombre", materia.getNombre());
            values.put("descripcion", materia.getDescripcion());

            // INSERT OR REPLACE para actualizar si existe
            db.insertWithOnConflict("tbl_materia", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void guardarMaterias(List<Materia> materias) {
        SQLiteDatabase db = null;

        try {
            db = database.getWritableDatabase();
            db.beginTransaction();

            for (Materia materia : materias) {
                ContentValues values = new ContentValues();
                values.put("id_materia", materia.getId());
                values.put("nombre", materia.getNombre());
                values.put("descripcion", materia.getDescripcion());

                db.insertWithOnConflict("tbl_materia", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int calcularProgresoLocal(Integer idMateria, Integer idUsuario) {
        SQLiteDatabase db = null;

        try {
            db = database.getReadableDatabase();

            // Contar ejercicios totales de la materia
            String queryTotal = "SELECT COUNT(*) FROM tbl_ejercicio e " +
                    "INNER JOIN tbl_subtema s ON e.id_subtema = s.id_subtema " +
                    "INNER JOIN tbl_tema t ON s.id_tema = t.id_tema " +
                    "WHERE t.id_materia = ?";
            Cursor cursorTotal = db.rawQuery(queryTotal, new String[]{String.valueOf(idMateria)});
            int totalEjercicios = 0;

            if (cursorTotal.moveToFirst()) {
                totalEjercicios = cursorTotal.getInt(0);
            }
            cursorTotal.close();

            if (totalEjercicios == 0) return 0;

            // Contar ejercicios completados
            String queryCompletados = "SELECT COUNT(*) FROM tbl_ejercicio e " +
                    "INNER JOIN tbl_subtema s ON e.id_subtema = s.id_subtema " +
                    "INNER JOIN tbl_tema t ON s.id_tema = t.id_tema " +
                    "WHERE t.id_materia = ? AND e.hecho = 1";
            Cursor cursorCompletados = db.rawQuery(queryCompletados, new String[]{String.valueOf(idMateria)});
            int ejerciciosCompletados = 0;

            if (cursorCompletados.moveToFirst()) {
                ejerciciosCompletados = cursorCompletados.getInt(0);
            }
            cursorCompletados.close();

            return (ejerciciosCompletados * 100) / totalEjercicios;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void cerrar() {
        if (database != null) {
            database.close();
        }
    }
}