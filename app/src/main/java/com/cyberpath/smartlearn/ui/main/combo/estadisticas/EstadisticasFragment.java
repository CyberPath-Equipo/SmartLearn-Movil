package com.cyberpath.smartlearn.ui.main.combo.estadisticas;

import android.os.Bundle;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import org.eazegraph.lib.models.PieModel;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class EstadisticasFragment extends Fragment {

    private org.eazegraph.lib.charts.PieChart pieEjercicios;
    private org.eazegraph.lib.charts.PieChart piePromedio;
    private TextView tvEjerciciosPieLabel;
    private TextView tvPromedioPieLabel;
    private TextView tvTiempoTotal;
    private TextView tvSesiones;
    private TextView tvAnalisis;
    private BarChart chartTiempo;
    private ScatterChart chartRendimiento;
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

        configurarPieChart(pieEjercicios);
        configurarPieChart(piePromedio);
        configurarBarChart(chartTiempo);
        configurarScatterChart(chartRendimiento);

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
                renderTiempoChart(tiempo);
                renderRendimientoScatter(rendimiento);
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

        renderPieData(
                pieEjercicios,
                progreso,
                pendiente,
                "Completado",
                "Pendiente",
                new int[]{0xFF3A86FF, 0xFFE7ECF4}
        );

        renderPieData(
                piePromedio,
                promedio,
                margenMejora,
                "Acierto",
                "Margen",
                new int[]{0xFF2FBF71, 0xFFF5D547}
        );

        tvEjerciciosPieLabel.setText(String.format(Locale.getDefault(),
                "Ejercicios: %d de %d", completados, total));
        tvPromedioPieLabel.setText(String.format(Locale.getDefault(),
                "Promedio actual: %s%%", decimal.format(promedio)));

        tvTiempoTotal.setText(String.format(Locale.getDefault(), "%d min acumulados", resumen.getMinutosEstudio()));
        tvSesiones.setText(String.format(Locale.getDefault(), "%d sesiones registradas", resumen.getSesionesEstudio()));
    }

    private void renderTiempoChart(List<DatoHistorico> datos) {
        if (datos == null || datos.isEmpty()) {
            chartTiempo.clear();
            chartTiempo.setNoDataText("Sin datos aún. Estudia un poco y vuelve a consultar.");
            chartTiempo.invalidate();
            return;
        }

        List<BarEntry> entries = new java.util.ArrayList<>();
        List<String> labels = new java.util.ArrayList<>();
        float maxDato = 1f;

        for (int i = 0; i < datos.size(); i++) {
            DatoHistorico dato = datos.get(i);
            entries.add(new BarEntry(i, dato.getValor()));
            labels.add(dato.getEtiqueta());
            maxDato = Math.max(maxDato, dato.getValor());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Minutos por dia");
        dataSet.setColor(0xFF3A86FF);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.58f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return String.format(Locale.getDefault(), "%.0f", barEntry.getY());
            }
        });

        chartTiempo.setData(data);
        chartTiempo.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartTiempo.getAxisLeft().setAxisMaximum(maxDato * 1.2f);
        chartTiempo.animateY(450);
        chartTiempo.invalidate();
    }

    private void renderRendimientoScatter(List<DatoHistorico> datos) {
        if (datos == null || datos.isEmpty()) {
            chartRendimiento.clear();
            chartRendimiento.setNoDataText("Sin evaluaciones suficientes para mostrar tendencia.");
            chartRendimiento.invalidate();
            return;
        }

        List<com.github.mikephil.charting.data.Entry> entries = new java.util.ArrayList<>();
        List<String> labels = new java.util.ArrayList<>();

        for (int i = 0; i < datos.size(); i++) {
            DatoHistorico dato = datos.get(i);
            entries.add(new com.github.mikephil.charting.data.Entry(i, dato.getValor()));
            labels.add(dato.getEtiqueta());
        }

        ScatterDataSet dataSet = new ScatterDataSet(entries, "Acierto en evaluaciones");
        dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        dataSet.setScatterShapeSize(11f);
        dataSet.setColor(0xFF2FBF71);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(com.github.mikephil.charting.data.Entry entry) {
                return String.format(Locale.getDefault(), "%.0f%%", entry.getY());
            }
        });

        ScatterData data = new ScatterData(dataSet);
        chartRendimiento.setData(data);
        chartRendimiento.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartRendimiento.animateY(450);
        chartRendimiento.invalidate();
    }

    private void configurarPieChart(org.eazegraph.lib.charts.PieChart chart) {
        chart.setUsePieRotation(false);
        chart.setInnerPadding(24f);
        chart.setInnerPaddingColor(ContextCompat.getColor(requireContext(), R.color.cardColor));
    }

    private void renderPieData(org.eazegraph.lib.charts.PieChart chart,
                               float valueA,
                               float valueB,
                               String labelA,
                               String labelB,
                               int[] colors) {
        chart.clearChart();

        if ((valueA + valueB) <= 0f) {
            chart.addPieSlice(new PieModel("Sin datos", 100f, 0xFFDDDDDD));
        } else {
            if (valueA > 0f) {
                chart.addPieSlice(new PieModel(labelA, valueA, colors[0]));
            }
            if (valueB > 0f) {
                chart.addPieSlice(new PieModel(labelB, valueB, colors[1]));
            }
        }

        chart.startAnimation();
    }

    private void configurarBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setFitBars(true);
        chart.setNoDataText("Sin datos disponibles");
        chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.colorNeutral600));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-20f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNeutral600));

        YAxis left = chart.getAxisLeft();
        left.setAxisMinimum(0f);
        left.setDrawGridLines(true);
        left.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNeutral600));

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
    }

    private void configurarScatterChart(ScatterChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setNoDataText("Sin datos disponibles");
        chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.colorNeutral600));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-20f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNeutral600));

        YAxis left = chart.getAxisLeft();
        left.setAxisMinimum(0f);
        left.setAxisMaximum(100f);
        left.setDrawGridLines(true);
        left.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNeutral600));

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNeutral600));
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

