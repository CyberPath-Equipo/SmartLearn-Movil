package com.cyberpath.smartlearn.ui.main.combo.principal.subtema;

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
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.util.ui.CarruselUtils;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorSubtemas extends RecyclerView.Adapter<AdaptadorSubtemas.SubtemaViewHolder> {
    private static final int colorRandom = 0;
    private final OnSubtemaClickListener listener;
    private final int[][] paresColoresGradiente = {
            {R.color.accentGradientStart, R.color.accentGradientEnd},
            {R.color.accentGradientStart2, R.color.accentGradientEnd2},
            {R.color.accentGradientStart3, R.color.accentGradientEnd3},
            {R.color.accentGradientStart4, R.color.accentGradientEnd4}
    };
    private List<Subtema> listaSubtemasOriginal = new ArrayList<>();
    private List<Subtema> listaSubtemasDuplicada = new ArrayList<>();
    private Materia materiaPadre;

    public AdaptadorSubtemas(List<Subtema> listaSubtemas, OnSubtemaClickListener listener) {
        this(listaSubtemas, listener, null);
    }

    public AdaptadorSubtemas(List<Subtema> listaSubtemas, OnSubtemaClickListener listener, Materia materiaPadre) {
        this.listener = listener;
        this.materiaPadre = materiaPadre;
        if (listaSubtemas != null) {
            this.listaSubtemasOriginal = new ArrayList<>(listaSubtemas);
        }
        actualizarListaDuplicada();
    }

    public void actualizarListaDuplicada() {
        listaSubtemasDuplicada = new ArrayList<>();

        if (listaSubtemasOriginal == null || listaSubtemasOriginal.isEmpty()) {
            return;
        }

        int size = listaSubtemasOriginal.size();

        if (size == 1) {
            listaSubtemasDuplicada.addAll(listaSubtemasOriginal);
            return;
        }

        listaSubtemasDuplicada.add(listaSubtemasOriginal.get(size - 1));
        listaSubtemasDuplicada.addAll(listaSubtemasOriginal);
        listaSubtemasDuplicada.add(listaSubtemasOriginal.get(0));
    }

    public void actualizarLista(List<Subtema> nuevasSubtemas) {
        this.listaSubtemasOriginal = nuevasSubtemas != null ? new ArrayList<>(nuevasSubtemas) : new ArrayList<>();
        actualizarListaDuplicada();
        notifyDataSetChanged();
    }

    public void setMateriaPadre(Materia materiaPadre) {
        this.materiaPadre = materiaPadre;
        notifyDataSetChanged();
    }

    public int getRealSize() {
        return listaSubtemasOriginal != null ? listaSubtemasOriginal.size() : 0;
    }

    @NonNull
    @Override
    public SubtemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_carousel_temas, parent, false);
        return new SubtemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtemaViewHolder holder, int position) {
        if (listaSubtemasOriginal == null || listaSubtemasOriginal.isEmpty()) {
            holder.tvNombre.setText("");
            holder.imageSubtema.setImageResource(R.drawable.ic_launcher_foreground);
            holder.itemView.setOnClickListener(null);
            return;
        }

        int realSize = getRealSize();

        if (realSize == 0) {
            holder.tvNombre.setText("");
            holder.imageSubtema.setImageResource(R.drawable.ic_launcher_foreground);
            holder.itemView.setOnClickListener(null);
            return;
        }

        int posicionNormalizada = CarruselUtils.normalizarPosicion(position, realSize);
        Subtema subtema = listaSubtemasOriginal.get(posicionNormalizada);

        int indiceColor = posicionNormalizada % paresColoresGradiente.length;
        aplicarGradienteTarjeta(
                holder.contenedorGradiente,
                paresColoresGradiente[indiceColor][0],
                paresColoresGradiente[indiceColor][1]
        );

        if (subtema != null) {
            holder.tvNombre.setText(subtema.getNombre() != null ? subtema.getNombre() : "");
        } else {
            holder.tvNombre.setText("");
            holder.imageSubtema.setImageResource(R.drawable.ic_launcher_foreground);
            holder.itemView.setOnClickListener(null);
            return;
        }

        cargarImagenDesdeMateria(holder.imageSubtema);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && subtema != null) {
                listener.onSubtemaClick(subtema);
            }
        });
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
        return listaSubtemasDuplicada != null ? listaSubtemasDuplicada.size() : 0;
    }

    private void cargarImagenDesdeMateria(ImageView imageView) {
        if (materiaPadre != null && materiaPadre.getSlug() != null && !materiaPadre.getSlug().isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(materiaPadre.getSlug())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    public interface OnSubtemaClickListener {
        void onSubtemaClick(Subtema subtema);
    }

    public static class SubtemaViewHolder extends RecyclerView.ViewHolder {
        LinearLayout contenedorGradiente;
        ImageView imageSubtema;
        TextView tvNombre;

        public SubtemaViewHolder(@NonNull View itemView) {
            super(itemView);
            contenedorGradiente = itemView.findViewById(R.id.contenedorGradienteTema);
            imageSubtema = itemView.findViewById(R.id.imageMateria);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
        }
    }
}