package com.cyberpath.smartlearn.ui.main.combo.principal.materias;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Materia;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorMaterias extends RecyclerView.Adapter<AdaptadorMaterias.MateriaViewHolder> {

    private List<Materia> listaMaterias = new ArrayList<>();
    private final OnMateriaClickListener listener;

    public AdaptadorMaterias(List<Materia> listaMaterias, OnMateriaClickListener listener) {
        this.listener = listener;
        if (listaMaterias != null) this.listaMaterias = new ArrayList<>(listaMaterias);
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
        Materia m = listaMaterias.get(realPos);
        return m != null && m.getId() != null ? m.getId().longValue() : realPos;
    }

    @Override
    public int getItemViewType(int position) {
        return 0; // Mismo viewType para todos
    }

    @NonNull
    @Override
    public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.carousel_element, parent, false);
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

        // Mapear posición "infinita" a posición real
        int realPos = position % realSize;
        if (realPos < 0) realPos += realSize;

        Materia materia = listaMaterias.get(realPos);
        if (materia != null) {
            holder.tvNombre.setText(materia.getNombre() != null ? materia.getNombre() : "");
            holder.tvDescripcion.setText(materia.getDescripcion() != null ? materia.getDescripcion() : "Descripción no disponible");
        } else {
            holder.tvNombre.setText("");
            holder.tvDescripcion.setText("Descripción no disponible");
        }
        holder.imageMateria.setImageResource(R.drawable.ic_launcher_foreground);

        Log.d("AdaptadorMaterias", "Materia: " + materia.getNombre() + ", Progreso: " + materia.getProgreso());

        holder.progressBar.setVisibility(View.VISIBLE);
        holder.tvProgreso.setVisibility(View.VISIBLE);
        holder.progressBar.setProgress(materia.getProgreso());
        holder.tvProgreso.setText("Progreso: " + materia.getProgreso() + "%");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && materia != null) listener.onMateriaClick(materia);
        });
    }

    @Override
    public int getItemCount() {
        int realSize = getRealSize();
        if (realSize == 0) return 0;
        if (realSize == 1) return 1;
        return Integer.MAX_VALUE;
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