package com.cyberpath.smartlearn.ui.main.combo.principal.contenido.teoria;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.ui.PlayerView;
import androidx.navigation.fragment.NavHostFragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.local.database.dao.ContenidoDAO;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria.NavAccesibilidad;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria.TeoriaLogic;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class TeoriaFragment extends Fragment {
    private TextView tvTituloSubtema, tvContenidoTeoria;
    private MaterialButton btnVolver;
    private Subtema subtema;
    private ArrayList<String> preguntas;
    private TeoriaLogic teoriaLogic;
    private View contentContainer;
    private ScrollView scrollTeoria;
    private NavAccesibilidad navAccesibilidad;
    private ImageView imageViewLSM;
    private PlayerView playerViewLSM;
    private FrameLayout containerReproductorLSM;
    private long sessionStartMs = 0L;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teoria, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subtema = TeoriaFragmentArgs.fromBundle(getArguments()).getSubtema();
        preguntas = TeoriaFragmentArgs.fromBundle(getArguments()).getPreguntas();

        tvTituloSubtema = view.findViewById(R.id.tv_titulo_subtema);
        tvContenidoTeoria = view.findViewById(R.id.tv_contenido_teoria);
        btnVolver = view.findViewById(R.id.btn_volver);
        contentContainer = view.findViewById(R.id.content_container);
        scrollTeoria = view.findViewById(R.id.scroll_teoria);
        imageViewLSM = view.findViewById(R.id.image_view_lsm);
        playerViewLSM = view.findViewById(R.id.player_view_lsm);
        containerReproductorLSM = view.findViewById(R.id.container_reproductor_lsm);

        if (subtema != null && subtema.getNombre() != null) {
            tvTituloSubtema.setText(subtema.getNombre());
        } else {
            tvTituloSubtema.setText("Subtema");
        }

        btnVolver.setOnClickListener(v -> {
            if (navAccesibilidad != null) {
                navAccesibilidad.detenerReproduccion();
            }

            boolean popped = false;
            try {
                popped = NavHostFragment.findNavController(this).popBackStack();
            } catch (Exception ignored) {
            }

        });

        scrollTeoria.setClipToPadding(false);

        navAccesibilidad = new NavAccesibilidad(requireContext(), this);

        if (navAccesibilidad.isAccesibilidadAuditivaActivada()) {
            navAccesibilidad.setTargetViews(imageViewLSM, playerViewLSM);
            if (containerReproductorLSM != null) {
                containerReproductorLSM.setVisibility(View.VISIBLE);
            }
        }

        teoriaLogic = new TeoriaLogic(this, subtema, preguntas, navAccesibilidad);

        view.post(this::adjustBottomSpacing);
    }

    public void mostrarTeoria(String contenido) {
        if (tvContenidoTeoria != null) {
            tvContenidoTeoria.setText(contenido);
            tvContenidoTeoria.post(this::adjustBottomSpacing);
        }
    }

    public void mostrarError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    public void resaltarPalabrasClave(String textoTeoria, ArrayList<String> preguntas) {
        if (tvContenidoTeoria == null) return;
        if (preguntas == null || preguntas.isEmpty() || textoTeoria == null) {
            tvContenidoTeoria.setText(textoTeoria);
            tvContenidoTeoria.post(this::adjustBottomSpacing);
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
                                new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorSecondary)
                                ),
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
        tvContenidoTeoria.post(this::adjustBottomSpacing);
    }

    private void adjustBottomSpacing() {
        if (contentContainer == null || tvContenidoTeoria == null || btnVolver == null || scrollTeoria == null)
            return;

        btnVolver.post(() -> {
            int btnHeight = btnVolver.getHeight() > 0 ? btnVolver.getHeight() : dpToPx(56);
            int lineHeight = tvContenidoTeoria.getLineHeight() > 0 ? tvContenidoTeoria.getLineHeight() : dpToPx(20);

            int extraLines = 3;
            int extra = btnHeight + (lineHeight * extraLines) + dpToPx(12);

            int left = contentContainer.getPaddingLeft();
            int top = contentContainer.getPaddingTop();
            int right = contentContainer.getPaddingRight();

            contentContainer.setPadding(left, top, right, extra);

            int scrollLeft = scrollTeoria.getPaddingLeft();
            int scrollTop = scrollTeoria.getPaddingTop();
            int scrollRight = scrollTeoria.getPaddingRight();
            scrollTeoria.setPadding(scrollLeft, scrollTop, scrollRight, extra);
            scrollTeoria.setClipToPadding(false);
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onResume() {
        super.onResume();
        sessionStartMs = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        registrarSesionEstudio();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        registrarSesionEstudio();
        if (navAccesibilidad != null) {
            navAccesibilidad.release();
        }
    }

    private void registrarSesionEstudio() {
        if (!isAdded() || sessionStartMs <= 0L) {
            return;
        }

        long duracionSegundos = Math.max(0L, (System.currentTimeMillis() - sessionStartMs) / 1000L);
        sessionStartMs = 0L;
        if (duracionSegundos < 10L) {
            return;
        }

        int idUsuario = PreferencesManager.getIdUsuario(requireContext());
        if (idUsuario <= 0) {
            return;
        }

        Integer idSubtema = subtema != null ? subtema.getId() : null;
        ContenidoDAO dao = new ContenidoDAO(requireContext());
        dao.registrarSesionEstudio(idUsuario, idSubtema, "TEORIA", duracionSegundos);
    }
}