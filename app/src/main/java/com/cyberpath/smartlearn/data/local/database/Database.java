package com.cyberpath.smartlearn.data.local.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smartlearn.db";
    private static final int DATABASE_VERSION = 6;

    private static Database instancia;


    private Database(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }


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
                "descripcion TEXT, " +
                "slug TEXT, " +
                "created_at TEXT, " +
                "updated_at TEXT);");

        db.execSQL("CREATE TABLE tbl_tema (" +
                "id_tema INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_materia INTEGER NOT NULL, " +
                "nombre TEXT, " +
                "orden INTEGER DEFAULT 0, " +
                "created_at TEXT, " +
                "updated_at TEXT, " +
                "FOREIGN KEY (id_materia) REFERENCES tbl_materia(id_materia) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_subtema (" +
                "id_subtema INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_tema INTEGER NOT NULL, " +
                "nombre TEXT, " +
                "orden INTEGER DEFAULT 0, " +
                "created_at TEXT, " +
                "updated_at TEXT, " +
                "FOREIGN KEY (id_tema) REFERENCES tbl_tema(id_tema) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_teoria (" +
                "id_subtema INTEGER PRIMARY KEY, " +
                "id_teoria INTEGER, " +
                "contenido TEXT, " +
                "revisado INTEGER DEFAULT 0, " +
                "fuente TEXT, " +
                "updated_at TEXT, " +
                "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_ejercicio (" +
                "id_ejercicio INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_subtema INTEGER NOT NULL, " +
                "nombre TEXT, " +
                "hecho INTEGER DEFAULT 0, " +
                "activo INTEGER DEFAULT 1, " +
                "tipo TEXT, " +
                "dificultad TEXT, " +
                "orden INTEGER DEFAULT 0, " +
                "created_at TEXT, " +
                "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_pregunta (" +
                "id_pregunta INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_ejercicio INTEGER NOT NULL, " +
                "enunciado TEXT, " +
                "tipo TEXT, " +
                "orden INTEGER DEFAULT 0, " +
                "puntos REAL DEFAULT 1, " +
                "FOREIGN KEY (id_ejercicio) REFERENCES tbl_ejercicio(id_ejercicio) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_opcion (" +
                "id_opcion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_pregunta INTEGER NOT NULL, " +
                "texto TEXT, " +
                "es_correcta INTEGER DEFAULT 0, " +
                "orden INTEGER DEFAULT 0, " +
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
                "mime_type TEXT, " +
                "tamano_bytes INTEGER, " +
                "descripcion TEXT, " +
                "creado_en TEXT, " +
                "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_tipo_recurso (" +
                "id_tipo_recurso INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "descripcion TEXT);");

        db.execSQL("CREATE TABLE tbl_usuario_ejercicio_local (" +
                "id_usuario_ejercicio_local INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_usuario INTEGER NOT NULL, " +
                "id_ejercicio INTEGER NOT NULL, " +
                "hecho INTEGER DEFAULT 0, " +
                "fecha_actualizacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "pendiente_sync INTEGER DEFAULT 1, " +
                "UNIQUE (id_usuario, id_ejercicio), " +
                "FOREIGN KEY (id_ejercicio) REFERENCES tbl_ejercicio(id_ejercicio) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_intento_ejercicio_local (" +
                "id_intento_local INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_usuario INTEGER NOT NULL, " +
                "id_ejercicio INTEGER NOT NULL, " +
                "puntaje REAL DEFAULT 0, " +
                "fecha TEXT, " +
                "pendiente_sync INTEGER DEFAULT 1, " +
                "FOREIGN KEY (id_ejercicio) REFERENCES tbl_ejercicio(id_ejercicio) ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE tbl_sesion_estudio (" +
                "id_sesion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_usuario INTEGER NOT NULL, " +
                "id_subtema INTEGER, " +
                "tipo_contenido TEXT NOT NULL, " +
                "duracion_segundos INTEGER DEFAULT 0, " +
                "fecha_inicio TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE SET NULL ON UPDATE CASCADE);");

        db.execSQL("CREATE INDEX idx_ue_local_usuario ON tbl_usuario_ejercicio_local(id_usuario);");
        db.execSQL("CREATE INDEX idx_ue_local_ejercicio ON tbl_usuario_ejercicio_local(id_ejercicio);");
        db.execSQL("CREATE INDEX idx_ie_local_sync ON tbl_intento_ejercicio_local(pendiente_sync);");
        db.execSQL("CREATE INDEX idx_sesion_usuario_fecha ON tbl_sesion_estudio(id_usuario, fecha_inicio);");
        db.execSQL("CREATE INDEX idx_sesion_subtema ON tbl_sesion_estudio(id_subtema);");
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

        if (oldVersion < 3) {
            addColumnIfNotExists(db, "tbl_materia", "slug", "TEXT");
            addColumnIfNotExists(db, "tbl_materia", "created_at", "TEXT");
            addColumnIfNotExists(db, "tbl_materia", "updated_at", "TEXT");

            addColumnIfNotExists(db, "tbl_tema", "orden", "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, "tbl_tema", "created_at", "TEXT");
            addColumnIfNotExists(db, "tbl_tema", "updated_at", "TEXT");

            addColumnIfNotExists(db, "tbl_subtema", "orden", "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, "tbl_subtema", "created_at", "TEXT");
            addColumnIfNotExists(db, "tbl_subtema", "updated_at", "TEXT");

            addColumnIfNotExists(db, "tbl_teoria", "id_teoria", "INTEGER");
            addColumnIfNotExists(db, "tbl_teoria", "fuente", "TEXT");
            addColumnIfNotExists(db, "tbl_teoria", "updated_at", "TEXT");

            addColumnIfNotExists(db, "tbl_ejercicio", "activo", "INTEGER DEFAULT 1");
            addColumnIfNotExists(db, "tbl_ejercicio", "tipo", "TEXT");
            addColumnIfNotExists(db, "tbl_ejercicio", "dificultad", "TEXT");
            addColumnIfNotExists(db, "tbl_ejercicio", "orden", "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, "tbl_ejercicio", "created_at", "TEXT");
        }

        if (oldVersion < 4) {
            addColumnIfNotExists(db, "tbl_pregunta", "tipo", "TEXT");
            addColumnIfNotExists(db, "tbl_pregunta", "orden", "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, "tbl_pregunta", "puntos", "REAL DEFAULT 1");

            addColumnIfNotExists(db, "tbl_opcion", "orden", "INTEGER DEFAULT 0");

            addColumnIfNotExists(db, "tbl_recurso_adjunto", "mime_type", "TEXT");
            addColumnIfNotExists(db, "tbl_recurso_adjunto", "tamano_bytes", "INTEGER");
            addColumnIfNotExists(db, "tbl_recurso_adjunto", "creado_en", "TEXT");
        }

        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS tbl_usuario_ejercicio_local (" +
                    "id_usuario_ejercicio_local INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_usuario INTEGER NOT NULL, " +
                    "id_ejercicio INTEGER NOT NULL, " +
                    "hecho INTEGER DEFAULT 0, " +
                    "fecha_actualizacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "pendiente_sync INTEGER DEFAULT 1, " +
                    "UNIQUE (id_usuario, id_ejercicio), " +
                    "FOREIGN KEY (id_ejercicio) REFERENCES tbl_ejercicio(id_ejercicio) ON DELETE CASCADE ON UPDATE CASCADE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS tbl_intento_ejercicio_local (" +
                    "id_intento_local INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_usuario INTEGER NOT NULL, " +
                    "id_ejercicio INTEGER NOT NULL, " +
                    "puntaje REAL DEFAULT 0, " +
                    "fecha TEXT, " +
                    "pendiente_sync INTEGER DEFAULT 1, " +
                    "FOREIGN KEY (id_ejercicio) REFERENCES tbl_ejercicio(id_ejercicio) ON DELETE CASCADE ON UPDATE CASCADE);");

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_ue_local_usuario ON tbl_usuario_ejercicio_local(id_usuario);");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_ue_local_ejercicio ON tbl_usuario_ejercicio_local(id_ejercicio);");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_ie_local_sync ON tbl_intento_ejercicio_local(pendiente_sync);");
        }

        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS tbl_sesion_estudio (" +
                    "id_sesion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_usuario INTEGER NOT NULL, " +
                    "id_subtema INTEGER, " +
                    "tipo_contenido TEXT NOT NULL, " +
                    "duracion_segundos INTEGER DEFAULT 0, " +
                    "fecha_inicio TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (id_subtema) REFERENCES tbl_subtema(id_subtema) ON DELETE SET NULL ON UPDATE CASCADE);");

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_sesion_usuario_fecha ON tbl_sesion_estudio(id_usuario, fecha_inicio);");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_sesion_subtema ON tbl_sesion_estudio(id_subtema);");
        }
    }

    private void addColumnIfNotExists(SQLiteDatabase db, String tableName, String columnName, String definition) {
        if (hasColumn(db, tableName, columnName)) {
            return;
        }
        db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
    }

    private boolean hasColumn(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        try {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (columnName.equals(cursor.getString(nameIndex))) {
                    return true;
                }
            }
            return false;
        } finally {
            cursor.close();
        }
    }
}