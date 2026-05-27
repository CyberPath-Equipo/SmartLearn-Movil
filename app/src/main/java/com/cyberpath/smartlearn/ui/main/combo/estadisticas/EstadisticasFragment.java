package com.cyberpath.smartlearn.ui.main.combo.estadisticas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.estadisticas.DatoHistorico;
import com.cyberpath.smartlearn.data.model.estadisticas.InteresItem;
import com.cyberpath.smartlearn.data.model.estadisticas.ResumenEstadisticas;
import com.cyberpath.smartlearn.logic.main.combo.estadisticas.EstadisticasLogic;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EstadisticasFragment extends Fragment {

    private PastelChartView pieEjercicios;
    private PastelChartView piePromedio;
    private TextView tvEjerciciosPieLabel;
    private TextView tvPromedioPieLabel;
    private TextView tvTiempoTotal;
    private TextView tvSesiones;
    private TextView tvAnalisis;
    private LinearLayout chartTiempo;
    private LinearLayout chartRendimiento;
    private LinearLayout listIntereses;
    private View loadingView;
    private View contentView;
    private EstadisticasLogic logic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_estadisticas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        logic = new EstadisticasLogic(requireContext());

        pieEjercicios = view.findViewById(R.id.pie_ejercicios);
        piePromedio = view.findViewById(R.id.pie_promedio);
        tvEjerciciosPieLabel = view.findViewById(R.id.tv_ejercicios_pie_label);
        tvPromedioPieLabel = view.findViewById(R.id.tv_promedio_pie_label);
        tvTiempoTotal = view.findViewById(R.id.tv_tiempo_total);
        tvSesiones = view.findViewById(R.id.tv_sesiones);
        tvAnalisis = view.findViewById(R.id.tv_analisis);
        chartTiempo = view.findViewById(R.id.chart_tiempo);
        chartRendimiento = view.findViewById(R.id.chart_rendimiento);
        listIntereses = view.findViewById(R.id.list_intereses);
        loadingView = view.findViewById(R.id.layout_loading);
        contentView = view.findViewById(R.id.layout_content);
        Button btnActualizar = view.findViewById(R.id.btn_actualizar_estadisticas);

        btnActualizar.setOnClickListener(v -> cargarEstadisticas());
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        int idUsuario = PreferencesManager.getIdUsuario(requireContext());
        if (idUsuario <= 0) {
            showToast("No se pudo identificar al usuario");
            return;
        }

        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);

        new Thread(() -> {
            ResumenEstadisticas resumen = logic.obtenerResumen(idUsuario);
            List<DatoHistorico> tiempo = logic.obtenerTiempoPorDia(idUsuario, 7);
            List<DatoHistorico> rendimiento = logic.obtenerRendimientoHistorico(idUsuario, 8);
            List<InteresItem> intereses = logic.obtenerTopIntereses(idUsuario, 3);

            if (!isAdded()) {
                return;
            }

            requireActivity().runOnUiThread(() -> {
                loadingView.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);
                renderResumen(resumen);
                renderChart(chartTiempo, tiempo, "min", 1f);
                renderChart(chartRendimiento, rendimiento, "%", 100f);
                renderIntereses(intereses);
                renderAnalisis(resumen, intereses);
            });
        }).start();
    }

    private void renderResumen(ResumenEstadisticas resumen) {
        int total = Math.max(0, resumen.getEjerciciosTotales());
        int completados = Math.max(0, resumen.getEjerciciosCompletados());
        float progreso = total > 0 ? (completados * 100f / total) : 0f;
        float pendiente = Math.max(0f, 100f - progreso);

        float promedio = (float) Math.max(0d, Math.min(100d, resumen.getPromedioAcierto()));
        float margenMejora = Math.max(0f, 100f - promedio);
        DecimalFormat decimal = new DecimalFormat("0.0");

        pieEjercicios.setCenterColor(ContextCompat.getColor(requireContext(), R.color.colorSurface));
        piePromedio.setCenterColor(ContextCompat.getColor(requireContext(), R.color.colorSurface));

        pieEjercicios.setSlices(Arrays.asList(
                new PastelChartView.Slice(progreso, 0xFF3A86FF),
                new PastelChartView.Slice(pendiente, 0xFFE6E6E6)
        ));
        pieEjercicios.setCenterText(PastelChartView.formatPercent(progreso) + "\nCompleto");

        piePromedio.setSlices(Arrays.asList(
                new PastelChartView.Slice(promedio, 0xFF2FBF71),
                new PastelChartView.Slice(margenMejora, 0xFFF5D547)
        ));
        piePromedio.setCenterText(PastelChartView.formatPercent(promedio) + "\nPromedio");

        tvEjerciciosPieLabel.setText(String.format(Locale.getDefault(),
                "Ejercicios: %d de %d", completados, total));
        tvPromedioPieLabel.setText(String.format(Locale.getDefault(),
                "Promedio actual: %s%%", decimal.format(promedio)));

        tvTiempoTotal.setText(String.format(Locale.getDefault(), "%d min acumulados", resumen.getMinutosEstudio()));
        tvSesiones.setText(String.format(Locale.getDefault(), "%d sesiones registradas", resumen.getSesionesEstudio()));
    }

    private void renderChart(LinearLayout container, List<DatoHistorico> datos, String sufijo, float escalaMaxima) {
        container.removeAllViews();

        if (datos == null || datos.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Sin datos aún. Estudia un poco y vuelve a consultar.");
            container.addView(empty);
            return;
        }

        float maxDato = 0f;
        for (DatoHistorico dato : datos) {
            maxDato = Math.max(maxDato, dato.getValor());
        }

        if (escalaMaxima > 0f) {
            maxDato = Math.max(maxDato, escalaMaxima);
        }

        for (DatoHistorico dato : datos) {
            View row = LayoutInflater.from(requireContext()).inflate(R.layout.element_stat_bar, container, false);
            TextView tvLabel = row.findViewById(R.id.tv_bar_label);
            TextView tvValue = row.findViewById(R.id.tv_bar_value);
            ProgressBar progressBar = row.findViewById(R.id.progress_bar_value);

            int progress = maxDato <= 0f ? 0 : Math.round((dato.getValor() / maxDato) * 100f);
            progressBar.setProgress(Math.max(0, Math.min(100, progress)));

            tvLabel.setText(dato.getEtiqueta());
            tvValue.setText(String.format(Locale.getDefault(), "%.1f %s", dato.getValor(), sufijo));
            progressBar.setContentDescription(String.format(Locale.getDefault(),
                    "%s con %.1f %s", dato.getEtiqueta(), dato.getValor(), sufijo));

            container.addView(row);
        }
    }

    private void renderIntereses(List<InteresItem> intereses) {
        listIntereses.removeAllViews();

        if (intereses == null || intereses.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Aún no hay suficiente historial para detectar intereses.");
            listIntereses.addView(empty);
            return;
        }

        int posicion = 1;
        for (InteresItem interes : intereses) {
            TextView item = new TextView(requireContext());
            item.setText(String.format(Locale.getDefault(), "%d) %s - %d min", posicion, interes.getTitulo(), interes.getMinutos()));
            item.setPadding(dp(12), dp(10), dp(12), dp(10));
            item.setTextSize(14f);
            item.setTextColor(0xFF1D1D1D);
            item.setBackgroundResource(R.drawable.bg_chip_interes);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = dp(8);
            item.setLayoutParams(params);

            listIntereses.addView(item);
            posicion++;
        }
    }

    private void renderAnalisis(ResumenEstadisticas resumen, List<InteresItem> intereses) {
        StringBuilder analisis = new StringBuilder();

        if (resumen.getMinutosUltimos7Dias() < 60) {
            analisis.append("Tu dedicacion de la ultima semana es baja. Intenta bloques cortos diarios. ");
        } else {
            analisis.append("Buena constancia semanal. Mantener la frecuencia favorece la retencion. ");
        }

        if (resumen.getPromedioAcierto() < 70) {
            analisis.append("El rendimiento en evaluaciones sugiere reforzar teoria antes de continuar. ");
        } else {
            analisis.append("Tu rendimiento en evaluaciones es estable. Puedes avanzar a contenidos mas complejos. ");
        }

        if (intereses != null && !intereses.isEmpty()) {
            analisis.append("Tu interes principal actual es: ")
                    .append(intereses.get(0).getTitulo())
                    .append(".");
        }

        tvAnalisis.setText(analisis.toString().trim());
    }

    private void showToast(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * requireContext().getResources().getDisplayMetrics().density);
    }
}

