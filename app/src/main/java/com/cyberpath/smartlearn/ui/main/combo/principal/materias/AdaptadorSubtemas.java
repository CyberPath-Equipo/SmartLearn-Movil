package com.cyberpath.smartlearn.ui.main.combo.principal.materias;

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

    private List<Subtema> listaSubtemasOriginal;
    private List<Subtema> listaSubtemasDuplicada;
    private final OnSubtemaClickListener listener;

    public AdaptadorSubtemas(List<Subtema> listaSubtemas, OnSubtemaClickListener listener) {
        this.listaSubtemasOriginal = listaSubtemas;
        this.listener = listener;
        actualizarListaDuplicada();
    }

    public void actualizarListaDuplicada() {
        listaSubtemasDuplicada = new ArrayList<>();
        if (listaSubtemasOriginal == null || listaSubtemasOriginal.isEmpty()) return;

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
        this.listaSubtemasOriginal = nuevasSubtemas;
        actualizarListaDuplicada();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubtemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.carousel_element, parent, false);
        return new SubtemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtemaViewHolder holder, int position) {
        int realPosition = (position - 1 + listaSubtemasOriginal.size()) % listaSubtemasOriginal.size();
        Subtema subtema = listaSubtemasOriginal.get(realPosition);

        holder.tvNombre.setText(subtema.getNombre());
        holder.tvDescripcion.setText("Descripcion");
        holder.imageSubtema.setImageResource(R.drawable.ic_launcher_foreground);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSubtemaClick(subtema);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaSubtemasDuplicada != null ? listaSubtemasDuplicada.size() : 0;
    }

    public interface OnSubtemaClickListener {
        void onSubtemaClick(Subtema subtema);
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