package com.cyberpath.smartlearn.ui.main.combo.principal.materias;

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

    private List<Tema> listaTemasOriginal;
    private List<Tema> listaTemasDuplicada;
    private final OnTemaClickListener listener;

    public AdaptadorTemas(List<Tema> listaTemas, OnTemaClickListener listener) {
        this.listaTemasOriginal = listaTemas;
        this.listener = listener;
        actualizarListaDuplicada();
    }

    public void actualizarListaDuplicada() {
        listaTemasDuplicada = new ArrayList<>();
        if (listaTemasOriginal == null || listaTemasOriginal.isEmpty()) return;

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
        this.listaTemasOriginal = nuevasTemas;
        actualizarListaDuplicada();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.carousel_element, parent, false);
        return new TemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemaViewHolder holder, int position) {
        int realPosition = (position - 1 + listaTemasOriginal.size()) % listaTemasOriginal.size();
        Tema tema = listaTemasOriginal.get(realPosition);

        holder.tvNombre.setText(tema.getNombre());
        //holder.tvDescripcion.setText(tema.getDescripcion() != null ? tema.getDescripcion() : "Descripción no disponible");
        holder.tvDescripcion.setText("Descripción");
        holder.imageTema.setImageResource(R.drawable.ic_launcher_foreground);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
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
            imageTema = itemView.findViewById(R.id.imageMateria); // Reutilizando ID, ajusta si es diferente
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionMateria);
        }
    }
}