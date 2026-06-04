package com.cyberpath.smartlearn.ui.main.combo.principal.materia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Materia;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorMaterias extends RecyclerView.Adapter<AdaptadorMaterias.MateriaViewHolder> {
    private final OnMateriaClickListener listener;
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

        int realPos = position % realSize;
        if (realPos < 0) realPos += realSize;

        Materia m = listaMaterias.get(realPos);
        return m != null && m.getId() != null ? m.getId().longValue() : realPos;
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

        int realPos = position % realSize;
        if (realPos < 0) realPos += realSize;

        Materia materia = listaMaterias.get(realPos);

        if (materia != null) {
            holder.tvNombre.setText(materia.getNombre() != null ? materia.getNombre() : "");
            holder.tvDescripcion.setText(materia.getDescripcion() != null ?
                    materia.getDescripcion() : "Descripción no disponible");

            holder.progressBar.setVisibility(View.VISIBLE);
            holder.tvProgreso.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(materia.getProgreso());
            holder.tvProgreso.setText("Progreso: " + materia.getProgreso() + "%");

            // Cargar imagen desde slug usando Glide
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

    @Override
    public int getItemCount() {
        int realSize = getRealSize();
        if (realSize == 0) return 0;
        if (realSize == 1) return 1;
        return Integer.MAX_VALUE;
    }

    /**
     * Carga una imagen desde una URL usando Glide.
     * Si la URL no existe o está vacía, carga la imagen por defecto.
     */
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
        ImageView imageMateria;
        TextView tvNombre, tvDescripcion, tvProgreso;
        ProgressBar progressBar;

        public MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMateria = itemView.findViewById(R.id.imageMateria);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionMateria);
            progressBar = itemView.findViewById(R.id.progressBarMateria);
            tvProgreso = itemView.findViewById(R.id.tvProgresoMateria);
        }
    }
}