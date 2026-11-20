package com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.cyberpath.smartlearn.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class TeoriaContenidoFormatter {


    private static final Pattern PATRON_URL_IMAGEN = Pattern.compile(
            "https?://\\S+?\\.(?:jpe?g|png|gif|webp|bmp)(?:\\?\\S*)?",
            Pattern.CASE_INSENSITIVE
    );

    private TeoriaContenidoFormatter() {
    }


    public static String normalizarParrafos(@Nullable String contenidoCrudo) {
        if (contenidoCrudo == null) return "";

        String normalizado = contenidoCrudo.replace("\r\n", "\n").trim();
        normalizado = normalizado.replaceAll("[ \\t]+\\n", "\n");
        normalizado = normalizado.replaceAll("\\n{3,}", "\n\n");
        return normalizado;
    }


    public static List<TeoriaBloqueContenido> partirEnBloques(String contenidoNormalizado) {
        List<TeoriaBloqueContenido> bloques = new ArrayList<>();
        if (contenidoNormalizado == null || contenidoNormalizado.isEmpty()) {
            return bloques;
        }

        Matcher matcher = PATRON_URL_IMAGEN.matcher(contenidoNormalizado);
        int cursor = 0;

        while (matcher.find()) {
            int inicio = matcher.start();
            int fin = matcher.end();

            boolean limiteInicioValido = inicio == 0
                    || Character.isWhitespace(contenidoNormalizado.charAt(inicio - 1));
            boolean limiteFinValido = fin == contenidoNormalizado.length()
                    || Character.isWhitespace(contenidoNormalizado.charAt(fin));

            if (!limiteInicioValido || !limiteFinValido) {


                continue;
            }

            String textoPrevio = contenidoNormalizado.substring(cursor, inicio);
            agregarBloqueDeTextoSiNoEstaVacio(bloques, textoPrevio);

            bloques.add(new TeoriaBloqueContenido(TeoriaBloqueContenido.Tipo.IMAGEN, matcher.group()));
            cursor = fin;
        }

        String resto = contenidoNormalizado.substring(cursor);
        agregarBloqueDeTextoSiNoEstaVacio(bloques, resto);

        return bloques;
    }

    private static void agregarBloqueDeTextoSiNoEstaVacio(List<TeoriaBloqueContenido> bloques, String texto) {
        if (texto != null && !texto.trim().isEmpty()) {
            bloques.add(new TeoriaBloqueContenido(TeoriaBloqueContenido.Tipo.TEXTO, texto.trim()));
        }
    }


    public static SpannableStringBuilder construirSpannableDeBloque(
            Context context,
            String textoDelBloque,
            @Nullable ArrayList<String> preguntas,
            int espacioEntreParrafosDp) {

        SpannableStringBuilder builder = new SpannableStringBuilder(textoDelBloque);

        aplicarEspaciadoDeParrafos(builder, espacioEntreParrafosDp);

        if (preguntas != null && !preguntas.isEmpty()) {
            aplicarResaltadoPalabrasClave(context, builder, preguntas);
        }

        return builder;
    }


    private static void aplicarEspaciadoDeParrafos(SpannableStringBuilder builder, int espacioDp) {
        String texto = builder.toString();
        int index = texto.indexOf("\n\n");

        while (index != -1) {
            int inicioSegundoSalto = index + 1;
            int finSegundoSalto = inicioSegundoSalto + 1;

            builder.setSpan(
                    new AbsoluteSizeSpan(espacioDp, true),
                    inicioSegundoSalto,
                    finSegundoSalto,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            index = texto.indexOf("\n\n", finSegundoSalto);
        }
    }

    private static void aplicarResaltadoPalabrasClave(Context context, SpannableStringBuilder builder, ArrayList<String> preguntas) {
        String textoMinus = builder.toString().toLowerCase();
        int colorResaltado = ContextCompat.getColor(context, R.color.colorSecondary);

        for (String pregunta : preguntas) {
            if (pregunta == null || pregunta.trim().isEmpty()) continue;

            String[] palabrasPregunta = pregunta.toLowerCase()
                    .replaceAll("[¿?.,!¡\"()]", " ")
                    .trim()
                    .split("\\s+");

            for (String palabra : palabrasPregunta) {
                if (palabra.length() < 3) continue;
                resaltarOcurrencias(builder, textoMinus, palabra, colorResaltado);
            }
        }
    }

    private static void resaltarOcurrencias(SpannableStringBuilder builder, String textoMinus, String palabra, int color) {
        int index = 0;
        while (index < textoMinus.length()) {
            int start = textoMinus.indexOf(palabra, index);
            if (start == -1) break;

            int end = start + palabra.length();
            boolean esPalabraCompleta =
                    (start == 0 || !Character.isLetterOrDigit(textoMinus.charAt(start - 1)))
                            && (end >= textoMinus.length() || !Character.isLetterOrDigit(textoMinus.charAt(end)));

            if (esPalabraCompleta) {
                builder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            index = end;
        }
    }
}