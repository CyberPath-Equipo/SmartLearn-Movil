package com.cyberpath.smartlearn.ui.main.combo.principal.contenido.practica;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorPractica extends BaseAdapter {

    private final Context contexto;
    private List<Ejercicio> listaEjercicios = new ArrayList<>();
    private final LayoutInflater inflater;

    public AdaptadorPractica(Context contexto, List<Ejercicio> listaEjercicios) {
        this.contexto = contexto;
        this.inflater = LayoutInflater.from(contexto);
        if (listaEjercicios != null) {
            this.listaEjercicios = new ArrayList<>(listaEjercicios);
        }
    }

    public void actualizarLista(List<Ejercicio> nuevosEjercicios) {
        this.listaEjercicios = nuevosEjercicios != null ? new ArrayList<>(nuevosEjercicios) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return listaEjercicios != null ? listaEjercicios.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        if (listaEjercicios == null || position < 0 || position >= listaEjercicios.size()) {
            return null;
        }
        return listaEjercicios.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.element_listview, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textview);
        ImageView imagenEjercicio = convertView.findViewById(R.id.imageicon);

        if (listaEjercicios == null || position < 0 || position >= listaEjercicios.size()) {
            textView.setText("");
            imagenEjercicio.setImageResource(0);
            return convertView;
        }

        Ejercicio ejercicio = listaEjercicios.get(position);

        if (ejercicio != null) {
            textView.setText(ejercicio.getNombre() != null ? ejercicio.getNombre() : "");

            if (ejercicio.isHecho()) {
                imagenEjercicio.setImageResource(R.drawable.img_completado);
            } else {
                imagenEjercicio.setImageResource(0);
            }
        } else {
            textView.setText("");
            imagenEjercicio.setImageResource(0);
        }

        return convertView;
    }
}