package com.cyberpath.smartlearn.ui.main.combo.principal.materia;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.util.ui.CarruselUtils;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorMaterias extends RecyclerView.Adapter<AdaptadorMaterias.MateriaViewHolder> {
    private static final int colorRandom = 0;
    private final OnMateriaClickListener listener;
    private final int[][] paresColoresGradiente = {
            {R.color.accentGradientStart, R.color.accentGradientEnd},
            {R.color.accentGradientStart2, R.color.accentGradientEnd2},
            {R.color.accentGradientStart3, R.color.accentGradientEnd3},
            {R.color.accentGradientStart4, R.color.accentGradientEnd4}
    };
    private List<Materia> listaMaterias = new ArrayList<>();

    public AdaptadorMaterias(List<Materia> listaMaterias, OnMateriaClickListener listener) {
        this.listener = listener;
        if (listaMaterias != null) {
            this.listaMaterias = new ArrayList<>(listaMaterias);
        }
        setHasStableIds(true);
    }

    public void actualizarLista(List<Materia> nuevasMaterias) {
        this.listaMaterias = nuevasMaterias != null ? new ArrayList<>(nuevasMaterias) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public int getRealSize() {
        return listaMaterias != null ? listaMaterias.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        int realSize = getRealSize();
        if (realSize == 0) return position;
        Materia m = listaMaterias.get(CarruselUtils.normalizarPosicion(position, realSize));
        return m != null && m.getId() != null ? m.getId().longValue() : 0;
    }

    @NonNull
    @Override
    public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_carousel_materias, parent, false);
        return new MateriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MateriaViewHolder holder, int position) {
        int realSize = getRealSize();

        if (realSize == 0) {
            holder.tvNombre.setText("");
            holder.tvDescripcion.setText("");
            holder.imageMateria.setImageResource(R.drawable.ic_launcher_foreground);
            holder.progressBar.setVisibility(View.GONE);
            holder.tvProgreso.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }

        int posicionNormalizada = CarruselUtils.normalizarPosicion(position, realSize);
        Materia materia = listaMaterias.get(posicionNormalizada);

        int indiceColor = posicionNormalizada % paresColoresGradiente.length;
        aplicarGradienteTarjeta(
                holder.contenedorGradiente,
                paresColoresGradiente[indiceColor][0],
                paresColoresGradiente[indiceColor][1]
        );

        if (materia != null) {
            holder.tvNombre.setText(materia.getNombre() != null ? materia.getNombre() : "");
            holder.tvDescripcion.setText(materia.getDescripcion() != null ?
                    materia.getDescripcion() : "Descripción no disponible");

            holder.progressBar.setVisibility(View.VISIBLE);
            holder.tvProgreso.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(materia.getProgreso());
            holder.tvProgreso.setText("Progreso: " + materia.getProgreso() + "%");

            cargarImagenDesdeSlug(holder.imageMateria, materia.getSlug());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMateriaClick(materia);
                }
            });
        } else {
            holder.tvNombre.setText("");
            holder.tvDescripcion.setText("Descripción no disponible");
            holder.progressBar.setVisibility(View.GONE);
            holder.tvProgreso.setVisibility(View.GONE);
            holder.imageMateria.setImageResource(R.drawable.ic_launcher_foreground);
            holder.itemView.setOnClickListener(null);
        }
    }

    private void aplicarGradienteTarjeta(View contenedor, int idColorInicio, int idColorFin) {
        Drawable fondo = contenedor.getBackground();
        if (fondo instanceof GradientDrawable) {
            Context context = contenedor.getContext();
            GradientDrawable gradiente = (GradientDrawable) fondo.mutate();
            int colorInicio = ContextCompat.getColor(context, idColorInicio);
            int colorFin = ContextCompat.getColor(context, idColorFin);
            gradiente.setColors(new int[]{colorInicio, colorFin});
        }
    }

    @Override
    public int getItemCount() {
        int realSize = getRealSize();
        if (realSize == 0) return 0;
        if (realSize == 1) return 1;
        return Integer.MAX_VALUE;
    }

    private void cargarImagenDesdeSlug(ImageView imageView, String slugUrl) {
        if (slugUrl != null && !slugUrl.isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(slugUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    public interface OnMateriaClickListener {
        void onMateriaClick(Materia materia);
    }

    public static class MateriaViewHolder extends RecyclerView.ViewHolder {
        LinearLayout contenedorGradiente;
        ImageView imageMateria;
        TextView tvNombre, tvDescripcion, tvProgreso;
        ProgressBar progressBar;

        public MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            contenedorGradiente = itemView.findViewById(R.id.contenedorGradienteMateria);
            imageMateria = itemView.findViewById(R.id.imageMateria);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionMateria);
            progressBar = itemView.findViewById(R.id.progressBarMateria);
            tvProgreso = itemView.findViewById(R.id.tvProgresoMateria);
        }
    }
}