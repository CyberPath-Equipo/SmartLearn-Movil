package com.cyberpath.smartlearn.ui.main.combo.principal.tema;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Materia;
import com.cyberpath.smartlearn.data.model.contenido.Tema;
import com.cyberpath.smartlearn.util.ui.CarruselUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class AdaptadorTemas extends RecyclerView.Adapter<AdaptadorTemas.TemaViewHolder> {
    private static final int colorRandom = 0;
    private final OnTemaClickListener listener;
    private final int[][] paresColoresGradiente = {
            {R.color.accentGradientStart, R.color.accentGradientEnd},
            {R.color.accentGradientStart2, R.color.accentGradientEnd2},
            {R.color.accentGradientStart3, R.color.accentGradientEnd3},
            {R.color.accentGradientStart4, R.color.accentGradientEnd4}
    };
    private List<Tema> listaTemas = new ArrayList<>();
    @Setter
    private Materia materia;

    public AdaptadorTemas(List<Tema> listaTemas, OnTemaClickListener listener, Materia materia) {
        this.listener = listener;
        this.materia = materia;
        if (listaTemas != null) {
            this.listaTemas = new ArrayList<>(listaTemas);
        }
    }

    public void actualizarLista(List<Tema> nuevosTemas) {
        this.listaTemas = nuevosTemas != null ? new ArrayList<>(nuevosTemas) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public int getRealSize() {
        return listaTemas != null ? listaTemas.size() : 0;
    }

    private void cargarImagenDesdeMateria(ImageView imageView) {
        if (materia != null && materia.getSlug() != null && !materia.getSlug().isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(materia.getSlug())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @NonNull
    @Override
    public TemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_carousel_temas, parent, false);
        return new TemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemaViewHolder holder, int position) {
        int realSize = getRealSize();

        if (realSize == 0) {
            holder.tvNombre.setText("");
            holder.imageTema.setImageResource(R.drawable.ic_launcher_foreground);
            holder.itemView.setOnClickListener(null);
            return;
        }

        int posicionNormalizada = CarruselUtils.normalizarPosicion(position, realSize);
        Tema tema = listaTemas.get(posicionNormalizada);

        int indiceColor = posicionNormalizada % paresColoresGradiente.length;
        aplicarGradienteTarjeta(
                holder.contenedorGradiente,
                paresColoresGradiente[indiceColor][0],
                paresColoresGradiente[indiceColor][1]
        );

        if (tema != null) {
            holder.tvNombre.setText(tema.getNombre() != null ? tema.getNombre() : "");

            cargarImagenDesdeMateria(holder.imageTema);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null && tema != null) {
                    listener.onTemaClick(tema);
                }
            });
        } else {
            holder.tvNombre.setText("");
            holder.imageTema.setImageResource(R.drawable.ic_launcher_foreground);
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

    public interface OnTemaClickListener {
        void onTemaClick(Tema tema);
    }

    public static class TemaViewHolder extends RecyclerView.ViewHolder {
        LinearLayout contenedorGradiente;
        ImageView imageTema;
        TextView tvNombre;

        public TemaViewHolder(@NonNull View itemView) {
            super(itemView);
            contenedorGradiente = itemView.findViewById(R.id.contenedorGradienteTema);
            imageTema = itemView.findViewById(R.id.imageMateria);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
        }
    }
}