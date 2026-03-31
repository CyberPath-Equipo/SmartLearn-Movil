package com.cyberpath.smartlearn.ui.main.combo.principal.contenido.teoria;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Teoria;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria.TeoriaLogic;

import java.util.ArrayList;

public class TeoriaFragment extends Fragment {
    TextView tvTituloSubtema, tvContenidoTeoria;
    Subtema subtema;
    ArrayList<String> preguntas;

    private TeoriaLogic teoriaLogic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teoria, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        subtema = TeoriaFragmentArgs.fromBundle(getArguments()).getSubtema();
        preguntas = TeoriaFragmentArgs.fromBundle(getArguments()).getPreguntas();
        tvTituloSubtema = view.findViewById(R.id.tv_titulo_subtema);
        tvContenidoTeoria = view.findViewById(R.id.tv_contenido_teoria);

        tvTituloSubtema.setText(subtema.getNombre());
        teoriaLogic = new TeoriaLogic(this, subtema, preguntas);
    }

    public void mostrarTeoria(String contenido) {
        if (tvContenidoTeoria != null) {
            tvContenidoTeoria.setText(contenido);
        }
    }

    public void mostrarError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    public void resaltarPalabrasClave(String textoTeoria, ArrayList<String> preguntas) {
        if (preguntas == null || preguntas.isEmpty() || textoTeoria == null) {
            tvContenidoTeoria.setText(textoTeoria);
            return;
        }

        SpannableString spannable = new SpannableString(textoTeoria);
        String textoMinus = textoTeoria.toLowerCase();

        for (String pregunta : preguntas) {
            if (pregunta == null || pregunta.trim().isEmpty()) continue;

            String[] palabrasPregunta = pregunta.toLowerCase()
                    .replaceAll("[¿?.,!¡\"()]", " ")
                    .trim()
                    .split("\\s+");

            for (String palabra : palabrasPregunta) {
                if (palabra.length() < 3) continue;

                int index = 0;
                while (index < textoMinus.length()) {
                    int start = textoMinus.indexOf(palabra, index);
                    if (start == -1) break;

                    int end = start + palabra.length();
                    boolean esPalabraCompleta = start <= 0 || !Character.isLetterOrDigit(textoMinus.charAt(start - 1));

                    if (end < textoMinus.length() && Character.isLetterOrDigit(textoMinus.charAt(end))) {
                        esPalabraCompleta = false;
                    }

                    if (esPalabraCompleta) {
                        spannable.setSpan(
                                new ForegroundColorSpan(getResources().getColor(R.color.colorSecondary, null)),
                                start,
                                end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }

                    index = end;
                }
            }
        }

        tvContenidoTeoria.setText(spannable);
        tvContenidoTeoria.setMovementMethod(LinkMovementMethod.getInstance());
    }
}