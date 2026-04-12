package com.cyberpath.smartlearn.util.validation;

import android.util.Patterns;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean isTextoNoVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean isCorreoValido(String correo) {
        return isTextoNoVacio(correo) && Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches();
    }

    public static boolean isContrasenaValida(String contrasena) {
        return isTextoNoVacio(contrasena)
                && contrasena.trim().length() >= 6
                && contrasena.matches(".*\\d.*");
    }

    public static boolean isNombreCompletoValido(String nombreCompleto) {
        if (!isTextoNoVacio(nombreCompleto)) {
            return false;
        }

        String valor = nombreCompleto.trim();
        return !valor.matches(".*\\d.*") && valor.matches(".*\\p{L}.*");
    }
}

