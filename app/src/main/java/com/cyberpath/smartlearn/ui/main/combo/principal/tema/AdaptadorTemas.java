package com.cyberpath.smartlearn.ui.main.combo.principal.tema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Tema;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorTemas extends RecyclerView.Adapter<AdaptadorTemas.TemaViewHolder> {

    private final OnTemaClickListener listener;
    private List<Tema> listaTemasOriginal = new ArrayList<>();
    private List<Tema> listaTemasDuplicada = new ArrayList<>();

    public AdaptadorTemas(List<Tema> listaTemas, OnTemaClickListener listener) {
        this.listener = listener;
        if (listaTemas != null) {
            this.listaTemasOriginal = new ArrayList<>(listaTemas);
        }
        actualizarListaDuplicada();
    }

    public void actualizarListaDuplicada() {
        listaTemasDuplicada = new ArrayList<>();

        if (listaTemasOriginal == null || listaTemasOriginal.isEmpty()) {
            return;
        }

        int size = listaTemasOriginal.size();

        if (size == 1) {
            listaTemasDuplicada.addAll(listaTemasOriginal);
            return;
        }

        listaTemasDuplicada.add(listaTemasOriginal.get(size - 1));
        listaTemasDuplicada.addAll(listaTemasOriginal);
        listaTemasDuplicada.add(listaTemasOriginal.get(0));
    }

    public void actualizarLista(List<Tema> nuevasTemas) {
        this.listaTemasOriginal = nuevasTemas != null ? new ArrayList<>(nuevasTemas) : new ArrayList<>();
        actualizarListaDuplicada();
        notifyDataSetChanged();
    }

    public int getRealSize() {
        return listaTemasOriginal != null ? listaTemasOriginal.size() : 0;
    }

    @NonNull
    @Override
    public TemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_carousel, parent, false);
        return new TemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemaViewHolder holder, int position) {
        if (listaTemasOriginal == null || listaTemasOriginal.isEmpty()) {
            holder.tvNombre.setText("");
            holder.tvDescripcion.setText("");
            holder.imageTema.setImageResource(R.drawable.ic_launcher_foreground);
            holder.itemView.setOnClickListener(null);
            return;
        }

        int realPosition = (position - 1 + listaTemasOriginal.size()) % listaTemasOriginal.size();
        if (realPosition < 0) realPosition += listaTemasOriginal.size();

        Tema tema = listaTemasOriginal.get(realPosition);

        if (tema != null) {
            holder.tvNombre.setText(tema.getNombre() != null ? tema.getNombre() : "");
            holder.tvDescripcion.setText("Descripción");
        } else {
            holder.tvNombre.setText("");
            holder.tvDescripcion.setText("Descripción no disponible");
        }

        holder.imageTema.setImageResource(R.drawable.ic_launcher_foreground);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && tema != null) {
                listener.onTemaClick(tema);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTemasDuplicada != null ? listaTemasDuplicada.size() : 0;
    }

    public interface OnTemaClickListener {
        void onTemaClick(Tema tema);
    }

    public static class TemaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageTema;
        TextView tvNombre, tvDescripcion;

        public TemaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageTema = itemView.findViewById(R.id.imageMateria);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionMateria);
        }
    }
}