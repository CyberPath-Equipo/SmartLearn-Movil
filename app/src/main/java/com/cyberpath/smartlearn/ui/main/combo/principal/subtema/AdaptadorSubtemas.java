package com.cyberpath.smartlearn.ui.main.combo.principal.subtema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorSubtemas extends RecyclerView.Adapter<AdaptadorSubtemas.SubtemaViewHolder> {

    private final OnSubtemaClickListener listener;
    private List<Subtema> listaSubtemasOriginal = new ArrayList<>();
    private List<Subtema> listaSubtemasDuplicada = new ArrayList<>();

    public interface OnSubtemaClickListener {
        void onSubtemaClick(Subtema subtema);
    }

    public AdaptadorSubtemas(List<Subtema> listaSubtemas, OnSubtemaClickListener listener) {
        this.listener = listener;
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

    public int getRealSize() {
        return listaSubtemasOriginal != null ? listaSubtemasOriginal.size() : 0;
    }

    @NonNull
    @Override
    public SubtemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_carousel, parent, false);
        return new SubtemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtemaViewHolder holder, int position) {
        if (listaSubtemasOriginal == null || listaSubtemasOriginal.isEmpty()) {
            holder.tvNombre.setText("");
            holder.tvDescripcion.setText("");
            holder.imageSubtema.setImageResource(R.drawable.ic_launcher_foreground);
            holder.itemView.setOnClickListener(null);
            return;
        }

        int realPosition = (position - 1 + listaSubtemasOriginal.size()) % listaSubtemasOriginal.size();
        if (realPosition < 0) realPosition += listaSubtemasOriginal.size();

        Subtema subtema = listaSubtemasOriginal.get(realPosition);

        if (subtema != null) {
            holder.tvNombre.setText(subtema.getNombre() != null ? subtema.getNombre() : "");
            holder.tvDescripcion.setText("Descripción");
        } else {
            holder.tvNombre.setText("");
            holder.tvDescripcion.setText("Descripción no disponible");
        }

        holder.imageSubtema.setImageResource(R.drawable.ic_launcher_foreground);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && subtema != null) {
                listener.onSubtemaClick(subtema);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaSubtemasDuplicada != null ? listaSubtemasDuplicada.size() : 0;
    }

    public static class SubtemaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageSubtema;
        TextView tvNombre, tvDescripcion;

        public SubtemaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageSubtema = itemView.findViewById(R.id.imageMateria);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionMateria);
        }
    }
}