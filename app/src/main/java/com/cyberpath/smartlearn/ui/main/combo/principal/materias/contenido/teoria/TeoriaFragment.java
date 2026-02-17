package com.cyberpath.smartlearn.ui.main.combo.principal.materias.contenido.teoria;

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
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.contenido.Teoria;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeoriaFragment extends Fragment {
    TextView tvTituloSubtema, tvContenidoTeoria;
    Subtema subtema;
    ArrayList<String> preguntas;

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
        asignarTeoria();
    }

    private void asignarTeoria() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<Teoria> call = apiService.getTeoriaById(subtema.getId());
        call.enqueue(new Callback<Teoria>() {
            @Override
            public void onResponse(Call<Teoria> call, Response<Teoria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Teoria teoria = response.body();
                    String contenido = teoria.getContenido();

                    if (preguntas != null && !preguntas.isEmpty()) {
                        resaltarPalabrasClave(contenido, preguntas);
                    } else {
                        tvContenidoTeoria.setText(contenido);
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar la teoría", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Teoria> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resaltarPalabrasClave(String textoTeoria, ArrayList<String> preguntas) {
        if (preguntas == null || preguntas.isEmpty() || textoTeoria == null) {
            tvContenidoTeoria.setText(textoTeoria);
            return;
        }

        SpannableString spannable = new SpannableString(textoTeoria);

        // Convertimos las preguntas a minúsculas para comparación insensible a mayúsculas
        String textoMinus = textoTeoria.toLowerCase();

        for (String pregunta : preguntas) {
            if (pregunta == null || pregunta.trim().isEmpty()) continue;

            // Extraemos palabras clave de la pregunta (opcional: puedes filtrar mejor)
            String[] palabrasPregunta = pregunta.toLowerCase()
                    .replaceAll("[¿?.,!¡\"()]", " ")  // quitamos signos
                    .trim()
                    .split("\\s+"); // separamos por espacios

            for (String palabra : palabrasPregunta) {
                if (palabra.length() < 3)
                    continue; // ignoramos palabras muy cortas como "el", "la", "de"

                int index = 0;
                while (index < textoMinus.length()) {
                    int start = textoMinus.indexOf(palabra, index);
                    if (start == -1) break;

                    // Verificamos que sea una palabra completa (no parte de otra)
                    int end = start + palabra.length();
                    boolean esPalabraCompleta = start <= 0 || !Character.isLetterOrDigit(textoMinus.charAt(start - 1));

                    // Verificar límites de palabra (opcional, para más precisión)
                    if (end < textoMinus.length() && Character.isLetterOrDigit(textoMinus.charAt(end))) {
                        esPalabraCompleta = false;
                    }

                    if (esPalabraCompleta) {
                        // Aplicamos color al texto original (manteniendo mayúsculas/minúsculas)
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
        tvContenidoTeoria.setMovementMethod(LinkMovementMethod.getInstance()); // opcional: para que no sea seleccionable raro
    }
}