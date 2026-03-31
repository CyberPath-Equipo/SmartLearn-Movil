package com.cyberpath.smartlearn.data.local.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

    // Singleton - única instancia para toda la app
    private static Database instancia;
    private static final String DATABASE_NAME = "smartlearn.db";
    private static final int DATABASE_VERSION = 2;

    // Constructor privado para evitar nuevas instancias
    private Database(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Método estático para obtener la única instancia
    public static synchronized Database getInstance(Context context) {
        if (instancia == null) {
            instancia = new Database(context.getApplicationContext());
        }
        return instancia;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");

        db.execSQL("CREATE TABLE tbl_materia (" +
                "id_materia INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "descripcion TEXT);");

        db.execSQL("CREATE TABLE tbl_tema (" +
                "id_tema INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_materia INTEGER NOT NULL, " +
                "nombre TEXT, " +
                "FOREIGN KEY (id_materia) REFERENCES tbl_materia(id_materia) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_subtema (" +
                "id_subtema INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_tema INTEGER NOT NULL, " +
                "nombre TEXT, " +
                "FOREIGN KEY (id_tema) REFERENCES tbl_tema(id_tema) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_teoria (" +
                "id_subtema INTEGER PRIMARY KEY, " +
                "contenido TEXT, " +
                "revisado INTEGER DEFAULT 0, " +
                "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_ejercicio (" +
                "id_ejercicio INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_subtema INTEGER NOT NULL, " +
                "nombre TEXT, " +
                "hecho INTEGER DEFAULT 0, " +
                "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_pregunta (" +
                "id_pregunta INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_ejercicio INTEGER NOT NULL, " +
                "enunciado TEXT, " +
                "FOREIGN KEY (id_ejercicio) REFERENCES tbl_ejercicio(id_ejercicio) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_opcion (" +
                "id_opcion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_pregunta INTEGER NOT NULL, " +
                "texto TEXT, " +
                "es_correcta INTEGER DEFAULT 0, " +
                "FOREIGN KEY (id_pregunta) REFERENCES tbl_pregunta(id_pregunta) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_progreso_subtema (" +
                "id_progreso INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_usuario INTEGER NOT NULL, " +
                "id_subtema INTEGER NOT NULL, " +
                "teoria_leida INTEGER DEFAULT 0, " +
                "ejercicios_completados INTEGER DEFAULT 0, " +
                "ejercicios_totales INTEGER DEFAULT 0, " +
                "porcentaje REAL, " +
                "ultimo_acceso TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (id_usuario, id_subtema), " +
                "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_materia_descargada (" +
                "id_materia_descargada INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_materia INTEGER NOT NULL UNIQUE, " +
                "nombre TEXT, " +
                "descripcion TEXT, " +
                "fecha_descarga TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "tamaño_mb REAL DEFAULT 0, " +
                "FOREIGN KEY (id_materia) REFERENCES tbl_materia(id_materia) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_recurso_adjunto (" +
                "id_recurso INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_subtema INTEGER NOT NULL, " +
                "id_tipo_recurso INTEGER, " +
                "orden INTEGER DEFAULT 0, " +
                "titulo TEXT, " +
                "url TEXT, " +
                "descripcion TEXT, " +
                "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_tipo_recurso (" +
                "id_tipo_recurso INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "descripcion TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS tbl_materia_descargada (" +
                    "id_materia_descargada INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_materia INTEGER NOT NULL UNIQUE, " +
                    "nombre TEXT, " +
                    "descripcion TEXT, " +
                    "fecha_descarga TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "tamaño_mb REAL DEFAULT 0, " +
                    "FOREIGN KEY (id_materia) REFERENCES tbl_materia(id_materia) ON DELETE CASCADE ON UPDATE CASCADE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS tbl_recurso_adjunto (" +
                    "id_recurso INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_subtema INTEGER NOT NULL, " +
                    "id_tipo_recurso INTEGER, " +
                    "orden INTEGER DEFAULT 0, " +
                    "titulo TEXT, " +
                    "url TEXT, " +
                    "descripcion TEXT, " +
                    "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE CASCADE ON UPDATE CASCADE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS tbl_tipo_recurso (" +
                    "id_tipo_recurso INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT, " +
                    "descripcion TEXT);");
        }
    }
}