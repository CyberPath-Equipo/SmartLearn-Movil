package com.cyberpath.smartlearn.util.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.cyberpath.smartlearn.data.model.usuario.Configuracion;

public class PreferencesManager {
    private static final String PREFS_NAME = "smartlearn_prefs";
    private static final String KEY_USUARIO_REGISTRADO = "usuario_registrado";
    private static final String KEY_MODO_AUDIO = "modo_audio";
    private static final String KEY_ID_USUARIO = "id_usuario";
    private static final String KEY_TAMANO_FUENTE = "tamano_letra";
    private static final String KEY_ID_SUBTEMA_ULTIMA_CONEXION = "subtema_ultima_conexion";
    private static final String KEY_TEMA_APP = "tema_app";
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_ACCESSIBLE = 2;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void setUsuarioRegistrado(Context context, boolean registrado) {
        getPrefs(context).edit().putBoolean(KEY_USUARIO_REGISTRADO, registrado).apply();
    }

    public static boolean isUsuarioRegistrado(Context context) {
        return getPrefs(context).getBoolean(KEY_USUARIO_REGISTRADO, false);
    }

    public static void setModoAudio(Context context, boolean activado) {
        getPrefs(context).edit().putBoolean(KEY_MODO_AUDIO, activado).apply();
    }

    public static boolean isModoAudioActivado(Context context) {
        return getPrefs(context).getBoolean(KEY_MODO_AUDIO, false);
    }

    public static void setIdUsuario(Context context, int id) {
        getPrefs(context).edit().putInt(KEY_ID_USUARIO, id).apply();
    }

    public static int getIdUsuario(Context context) {
        return getPrefs(context).getInt(KEY_ID_USUARIO, -1);
    }

    public static void setTamanoTexto(Context context, int tamano) {
        getPrefs(context).edit().putInt(KEY_TAMANO_FUENTE, tamano).apply();
    }

    public static int getTamanoTexto(Context context) {
        return getPrefs(context).getInt(KEY_TAMANO_FUENTE, Configuracion.TamanoFuente.MEDIO.getValor());
    }

    public static void setIdSubtemaUltimaConexion(Context context, int id) {
        getPrefs(context).edit().putInt(KEY_ID_SUBTEMA_ULTIMA_CONEXION, id).apply();
    }

    public static int getIdSubtemaUltimaConexion(Context context) {
        return getPrefs(context).getInt(KEY_ID_SUBTEMA_ULTIMA_CONEXION, -1);
    }

    public static void setTemaApp(Context context, int theme) {
        getPrefs(context).edit().putInt(KEY_TEMA_APP, theme).apply();
    }

    public static int getTemaApp(Context context) {
        return getPrefs(context).getInt(KEY_TEMA_APP, THEME_ACCESSIBLE);
    }

}