package com.cyberpath.smartlearn.util.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.cyberpath.smartlearn.data.model.usuario.propiedades.Configuracion;

public class PreferencesManager {

    // -----------------------
    // Temas disponibles
    // -----------------------
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_ACCESSIBLE = 2;
    public static final int TEXT_SIZE_SMALL = 0;
    public static final int TEXT_SIZE_MEDIUM = 1;
    public static final int TEXT_SIZE_LARGE = 2;
    // -----------------------
    // Constantes de keys
    // -----------------------
    private static final String PREFS_NAME = "smartlearn_prefs";
    private static final String KEY_USUARIO_REGISTRADO = "usuario_registrado";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TRUSTED_DEVICE_TOKEN = "trusted_device_token";
    private static final String KEY_SESION_ACTIVA = "sesion_activa";
    private static final String KEY_ID_USUARIO = "id_usuario";
    private static final String KEY_REGISTRO_PENDIENTE = "registro_pendiente";
    private static final String KEY_REGISTRO_TRANSACTION_ID = "registro_transaction_id";
    private static final String KEY_REGISTRO_CORREO = "registro_correo";
    private static final String KEY_REGISTRO_NOMBRE_CUENTA = "registro_nombre_cuenta";
    private static final String KEY_NOMBRE_USUARIO = "nombre_usuario";
    private static final String KEY_CORREO_USUARIO = "correo_usuario";
    private static final String KEY_ACCESIBILIDAD_VISUAL = "accesibilidad_visual";
    private static final String KEY_ACCESIBILIDAD_AUDITIVA = "accesibilidad_auditiva";
    private static final String KEY_TAMANO_FUENTE = "tamano_letra";
    private static final String KEY_ID_SUBTEMA_ULTIMA_CONEXION = "subtema_ultima_conexion";
    private static final String KEY_TEMA_APP = "tema_app";

    // -----------------------
    // Helper
    // -----------------------
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // -----------------------
    // Usuario / Sesión
    // -----------------------
    public static void setUsuarioRegistrado(Context context, boolean registrado) {
        getPrefs(context).edit().putBoolean(KEY_USUARIO_REGISTRADO, registrado).apply();
    }

    public static boolean isUsuarioRegistrado(Context context) {
        return getPrefs(context).getBoolean(KEY_USUARIO_REGISTRADO, false);
    }

    public static void setSesionActiva(Context context, boolean sesionActiva) {
        getPrefs(context).edit().putBoolean(KEY_SESION_ACTIVA, sesionActiva).apply();
    }

    public static boolean isSesionActiva(Context context) {
        return getPrefs(context).getBoolean(KEY_SESION_ACTIVA, false);
    }

    public static void setIdUsuario(Context context, int id) {
        getPrefs(context).edit().putInt(KEY_ID_USUARIO, id).apply();
    }

    public static int getIdUsuario(Context context) {
        return getPrefs(context).getInt(KEY_ID_USUARIO, -1);
    }

    public static void setRegistroPendiente(Context context, boolean pendiente) {
        getPrefs(context).edit().putBoolean(KEY_REGISTRO_PENDIENTE, pendiente).apply();
    }

    public static boolean isRegistroPendiente(Context context) {
        return getPrefs(context).getBoolean(KEY_REGISTRO_PENDIENTE, false);
    }

    public static void setRegistroTransactionId(Context context, String transactionId) {
        getPrefs(context).edit().putString(KEY_REGISTRO_TRANSACTION_ID, transactionId == null ? "" : transactionId).apply();
    }

    public static String getRegistroTransactionId(Context context) {
        return getPrefs(context).getString(KEY_REGISTRO_TRANSACTION_ID, "");
    }

    public static void setRegistroCorreo(Context context, String correo) {
        getPrefs(context).edit().putString(KEY_REGISTRO_CORREO, correo == null ? "" : correo).apply();
    }

    public static String getRegistroCorreo(Context context) {
        return getPrefs(context).getString(KEY_REGISTRO_CORREO, "");
    }

    public static void setRegistroNombreCuenta(Context context, String nombreCuenta) {
        getPrefs(context).edit().putString(KEY_REGISTRO_NOMBRE_CUENTA, nombreCuenta == null ? "" : nombreCuenta).apply();
    }

    public static String getRegistroNombreCuenta(Context context) {
        return getPrefs(context).getString(KEY_REGISTRO_NOMBRE_CUENTA, "");
    }

    public static void clearRegistroPendiente(Context context) {
        getPrefs(context).edit()
                .putBoolean(KEY_REGISTRO_PENDIENTE, false)
                .putString(KEY_REGISTRO_TRANSACTION_ID, "")
                .putString(KEY_REGISTRO_CORREO, "")
                .putString(KEY_REGISTRO_NOMBRE_CUENTA, "")
                .apply();
    }

    // -----------------------
    // Datos de usuario (persistencia local)
    // -----------------------
    public static void setNombreUsuario(Context context, String nombre) {
        getPrefs(context).edit().putString(KEY_NOMBRE_USUARIO, nombre).apply();
    }

    public static String getNombreUsuario(Context context) {
        return getPrefs(context).getString(KEY_NOMBRE_USUARIO, "");
    }

    public static void setCorreoUsuario(Context context, String correo) {
        getPrefs(context).edit().putString(KEY_CORREO_USUARIO, correo).apply();
    }

    public static String getCorreoUsuario(Context context) {
        return getPrefs(context).getString(KEY_CORREO_USUARIO, "");
    }

    // -----------------------
    // Accesibilidad / Audio
    // -----------------------
    public static boolean isModoAudioActivado(Context context) {
        return isAccesibilidadVisualActivada(context);
    }

    public static boolean isAsistenciaVozActivada(Context context) {
        return isAccesibilidadVisualActivada(context);
    }

    public static void setAccesibilidadVisualActivada(Context context, boolean activado) {
        getPrefs(context).edit()
                .putBoolean(KEY_ACCESIBILIDAD_VISUAL, activado)
                .apply();
    }

    public static boolean isAccesibilidadVisualActivada(Context context) {
        return getPrefs(context).getBoolean(KEY_ACCESIBILIDAD_VISUAL, false);
    }

    public static void setAccesibilidadAuditivaActivada(Context context, boolean activado) {
        getPrefs(context).edit()
                .putBoolean(KEY_ACCESIBILIDAD_AUDITIVA, activado)
                .apply();
    }

    public static boolean isAccesibilidadAuditivaActivada(Context context) {
        return getPrefs(context).getBoolean(KEY_ACCESIBILIDAD_AUDITIVA, false);
    }

    // -----------------------
    // Preferencias de interfaz
    // -----------------------
    public static void setTamanoTexto(Context context, int tamano) {
        getPrefs(context).edit().putInt(KEY_TAMANO_FUENTE, normalizeTamanoTexto(tamano)).apply();
    }

    public static int getTamanoTexto(Context context) {
        int tamano = getPrefs(context).getInt(KEY_TAMANO_FUENTE, TEXT_SIZE_MEDIUM);
        return normalizeTamanoTexto(tamano);
    }

    public static void setTemaApp(Context context, int theme) {
        getPrefs(context).edit().putInt(KEY_TEMA_APP, normalizeTema(theme)).apply();
    }

    public static int getTemaApp(Context context) {
        int tema = getPrefs(context).getInt(KEY_TEMA_APP, THEME_LIGHT);
        return normalizeTema(tema);
    }

    public static int getTemaResuelto(Context context) {
        if (isAccesibilidadVisualActivada(context)) {
            return THEME_ACCESSIBLE;
        }
        int temaGuardado = getTemaApp(context);
        if (temaGuardado == THEME_ACCESSIBLE) {
            return THEME_LIGHT;
        }
        return temaGuardado;
    }

    public static void setIdSubtemaUltimaConexion(Context context, int id) {
        getPrefs(context).edit().putInt(KEY_ID_SUBTEMA_ULTIMA_CONEXION, id).apply();
    }

    public static int getIdSubtemaUltimaConexion(Context context) {
        return getPrefs(context).getInt(KEY_ID_SUBTEMA_ULTIMA_CONEXION, -1);
    }

    public static void setToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, "");
    }

    public static void setTrustedDeviceToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_TRUSTED_DEVICE_TOKEN, token).apply();
    }

    public static String getTrustedDeviceToken(Context context) {
        return getPrefs(context).getString(KEY_TRUSTED_DEVICE_TOKEN, "");
    }

    public static void clearSesion(Context context) {
        getPrefs(context).edit()
                .putBoolean(KEY_SESION_ACTIVA, false)
                .putString(KEY_TOKEN, "")
                .putInt(KEY_ID_USUARIO, -1)
                .putString(KEY_TRUSTED_DEVICE_TOKEN, "")
                .putBoolean(KEY_REGISTRO_PENDIENTE, false)
                .putString(KEY_REGISTRO_TRANSACTION_ID, "")
                .putString(KEY_REGISTRO_CORREO, "")
                .putString(KEY_REGISTRO_NOMBRE_CUENTA, "")
                .apply();
    }

    private static int normalizeTema(int tema) {
        if (tema == THEME_DARK || tema == THEME_ACCESSIBLE) {
            return tema;
        }
        return THEME_LIGHT;
    }

    private static int normalizeTamanoTexto(int tamano) {
        if (tamano == TEXT_SIZE_SMALL || tamano == TEXT_SIZE_MEDIUM || tamano == TEXT_SIZE_LARGE) {
            return tamano;
        }
        return TEXT_SIZE_MEDIUM;
    }
}