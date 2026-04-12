package com.cyberpath.smartlearn.util.validation;

import android.util.Patterns;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d).{6,}$");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");

    private InputValidator() {
    }

    public static boolean isValidEmail(String correo) {
        return correo != null && !correo.trim().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches();
    }

    public static boolean isValidPassword(String contrasena) {
        return contrasena != null && PASSWORD_PATTERN.matcher(contrasena).matches();
    }

    public static boolean isValidNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return false;
        }
        return !DIGIT_PATTERN.matcher(nombreCompleto).matches();
    }
}

