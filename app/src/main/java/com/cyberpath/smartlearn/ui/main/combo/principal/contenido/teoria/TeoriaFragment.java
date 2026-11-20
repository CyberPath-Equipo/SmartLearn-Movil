package com.cyberpath.smartlearn.ui.main.combo.principal.contenido.teoria;

import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.ui.PlayerView;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.local.database.dao.ContenidoDAO;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria.TeoriaBloqueContenido;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria.TeoriaContenidoFormatter;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria.TeoriaLogic;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria.TeoriaNavAccesibilidad;
import com.cyberpath.smartlearn.logic.main.combo.principal.contenido.teoria.TeoriaNavAccesibilidadVisual;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class TeoriaFragment extends Fragment {


    private static final int ESPACIO_ENTRE_PARRAFOS_DP = 16;


    private static final int ESPACIO_ENTRE_BLOQUES_DP = 12;


    private static final int ALTO_MAXIMO_IMAGEN_DP = 260;

    private TextView tvTituloSubtema;
    private MaterialButton btnVolver;
    private Subtema subtema;
    private ArrayList<String> preguntas;
    private TeoriaLogic teoriaLogic;
    private View contentContainer;
    private ScrollView scrollTeoria;
    private LinearLayout contenedorTeoria;
    private TeoriaNavAccesibilidad teoriaNavAccesibilidad;
    private TeoriaNavAccesibilidadVisual teoriaNavAccesibilidadVisual;
    private ImageView imageViewLSM;
    private PlayerView playerViewLSM;
    private FrameLayout containerReproductorLSM;
    private long sessionStartMs = 0L;


    private String textoPlanoParaVoz = "";

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
        contenedorTeoria = view.findViewById(R.id.contenedor_teoria);
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
            if (teoriaNavAccesibilidad != null) {
                teoriaNavAccesibilidad.detenerReproduccion();
            }

            boolean popped = false;
            try {
                popped = NavHostFragment.findNavController(this).popBackStack();
            } catch (Exception ignored) {
            }

        });

        scrollTeoria.setClipToPadding(false);

        teoriaNavAccesibilidad = new TeoriaNavAccesibilidad(requireContext(), this);
        teoriaNavAccesibilidadVisual = new TeoriaNavAccesibilidadVisual(requireContext(), this);

        if (teoriaNavAccesibilidad.isAccesibilidadAuditivaActivada()) {
            teoriaNavAccesibilidad.setTargetViews(imageViewLSM, playerViewLSM);
            if (containerReproductorLSM != null) {
                containerReproductorLSM.setVisibility(View.VISIBLE);
            }
        }

        teoriaLogic = new TeoriaLogic(this, subtema, preguntas, teoriaNavAccesibilidad);

        view.post(this::adjustBottomSpacing);
    }


    public void mostrarContenido(String contenido, @Nullable ArrayList<String> preguntas) {
        if (contenedorTeoria == null) return;

        contenedorTeoria.removeAllViews();

        String normalizado = TeoriaContenidoFormatter.normalizarParrafos(contenido);
        List<TeoriaBloqueContenido> bloques = TeoriaContenidoFormatter.partirEnBloques(normalizado);

        StringBuilder textoParaVoz = new StringBuilder();
        boolean esPrimerBloque = true;

        for (TeoriaBloqueContenido bloque : bloques) {
            int margenSuperior = esPrimerBloque ? 0 : dpToPx(ESPACIO_ENTRE_BLOQUES_DP);

            if (bloque.tipo == TeoriaBloqueContenido.Tipo.TEXTO) {
                contenedorTeoria.addView(crearVistaDeTexto(bloque.valor, preguntas, margenSuperior));
                if (textoParaVoz.length() > 0) textoParaVoz.append(" ");
                textoParaVoz.append(bloque.valor);
            } else {
                contenedorTeoria.addView(crearVistaDeImagen(bloque.valor, margenSuperior));
            }

            esPrimerBloque = false;
        }

        textoPlanoParaVoz = textoParaVoz.toString();

        contenedorTeoria.post(this::adjustBottomSpacing);
    }

    private TextView crearVistaDeTexto(String textoDelBloque, @Nullable ArrayList<String> preguntas, int margenSuperior) {
        TextView textView = new TextView(requireContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = margenSuperior;
        textView.setLayoutParams(params);

        textView.setTextSize(17f);
        textView.setLineSpacing(0f, 1.3f);
        textView.setLetterSpacing(0.01f);
        textView.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            textView.setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY);
            textView.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL);
        }

        SpannableStringBuilder contenidoFormateado = TeoriaContenidoFormatter.construirSpannableDeBloque(
                requireContext(), textoDelBloque, preguntas, ESPACIO_ENTRE_PARRAFOS_DP);
        textView.setText(contenidoFormateado);

        return textView;
    }

    private ImageView crearVistaDeImagen(String url, int margenSuperior) {
        ImageView imageView = new ImageView(requireContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = margenSuperior;


        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        imageView.setLayoutParams(params);

        imageView.setAdjustViewBounds(true);
        imageView.setMaxHeight(dpToPx(ALTO_MAXIMO_IMAGEN_DP));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);


        Glide.with(this)
                .load(url)
                .error(android.R.drawable.stat_notify_error)
                .into(imageView);

        return imageView;
    }

    public void iniciarNavegacionPorVoz() {
        if (!PreferencesManager.isAsistenciaVozActivada(requireContext())) {
            return;
        }
        if (teoriaNavAccesibilidadVisual != null) {
            teoriaNavAccesibilidadVisual.iniciarNavegacion(textoPlanoParaVoz);
        }
    }

    public void mostrarError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    private void adjustBottomSpacing() {
        if (contentContainer == null || contenedorTeoria == null || btnVolver == null || scrollTeoria == null)
            return;

        btnVolver.post(() -> {
            int btnHeight = btnVolver.getHeight() > 0 ? btnVolver.getHeight() : dpToPx(56);


            int lineHeight = dpToPx(20);
            if (contenedorTeoria.getChildCount() > 0) {
                View ultimo = contenedorTeoria.getChildAt(contenedorTeoria.getChildCount() - 1);
                if (ultimo instanceof TextView && ((TextView) ultimo).getLineHeight() > 0) {
                    lineHeight = ((TextView) ultimo).getLineHeight();
                }
            }

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
        if (teoriaNavAccesibilidad != null) {
            teoriaNavAccesibilidad.release();
        }
        if (teoriaNavAccesibilidadVisual != null) {
            teoriaNavAccesibilidadVisual.release();
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

    public void regresarPantallaAnterior() {
        requireActivity().runOnUiThread(() -> {
            try {
                if (!NavHostFragment.findNavController(this).popBackStack()) {
                    requireActivity().onBackPressed();
                }
            } catch (Exception e) {
                requireActivity().onBackPressed();
            }
        });
    }
}