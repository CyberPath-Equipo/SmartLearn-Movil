package com.cyberpath.smartlearn.ui.main.combo.estadisticas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PastelChartView extends View {

    public static class Slice {
        public final float value;
        public final int color;

        public Slice(float value, int color) {
            this.value = Math.max(0f, value);
            this.color = color;
        }
    }

    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private final List<Slice> slices = new ArrayList<>();

    private int centerColor = 0xFFFFFFFF;
    private String centerText = "";

    public PastelChartView(Context context) {
        super(context);
        init();
    }

    public PastelChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PastelChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        centerPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(0xFF1D1D1D);
        textPaint.setTextSize(dp(14));
        textPaint.setFakeBoldText(true);
    }

    public void setCenterColor(int color) {
        this.centerColor = color;
        invalidate();
    }

    public void setCenterText(String centerText) {
        this.centerText = centerText == null ? "" : centerText;
        invalidate();
    }

    public void setSlices(List<Slice> data) {
        slices.clear();
        if (data != null) {
            slices.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float size = Math.min(w, h);
        float chartRadius = size * 0.46f;
        float cx = w / 2f;
        float cy = h / 2f;

        arcBounds.set(cx - chartRadius, cy - chartRadius, cx + chartRadius, cy + chartRadius);

        float total = 0f;
        for (Slice slice : slices) {
            total += Math.max(0f, slice.value);
        }

        float startAngle = -90f;
        if (total <= 0f) {
            slicePaint.setColor(0xFFDDDDDD);
            canvas.drawArc(arcBounds, 0, 360, true, slicePaint);
        } else {
            for (Slice slice : slices) {
                if (slice.value <= 0f) {
                    continue;
                }
                float sweep = (slice.value / total) * 360f;
                slicePaint.setColor(slice.color);
                canvas.drawArc(arcBounds, startAngle, sweep, true, slicePaint);
                startAngle += sweep;
            }
        }

        centerPaint.setColor(centerColor);
        canvas.drawCircle(cx, cy, chartRadius * 0.55f, centerPaint);

        drawCenterText(canvas, cx, cy);
    }

    private void drawCenterText(Canvas canvas, float cx, float cy) {
        if (centerText == null || centerText.trim().isEmpty()) {
            return;
        }

        String[] lines = centerText.split("\\n");
        float lineHeight = textPaint.getTextSize() * 1.25f;
        float startY = cy - ((lines.length - 1) * lineHeight / 2f);

        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], cx, startY + (i * lineHeight), textPaint);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    public static String formatPercent(float value) {
        return String.format(Locale.getDefault(), "%.1f%%", value);
    }
}

