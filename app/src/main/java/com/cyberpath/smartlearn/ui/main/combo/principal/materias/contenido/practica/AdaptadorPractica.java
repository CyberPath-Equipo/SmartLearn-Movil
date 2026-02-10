package com.cyberpath.smartlearn.ui.main.combo.principal.materias.contenido.practica;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.ejercicio.Ejercicio;

import java.util.List;

public class AdaptadorPractica extends BaseAdapter {
    Context contexto;
    List<Ejercicio> listaEjercicios;
    LayoutInflater inflater;

    public AdaptadorPractica(Context contexto, List<Ejercicio> listaEjercicios) {
        this.contexto = contexto;
        this.listaEjercicios = listaEjercicios;
        this.inflater = LayoutInflater.from(contexto);
    }

    public void actualizarLista(List<Ejercicio> nuevosEjercicios) {
        this.listaEjercicios = nuevosEjercicios;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return listaEjercicios != null ? listaEjercicios.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return listaEjercicios.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_element, null);
        }
        TextView textView = convertView.findViewById(R.id.textview);
        ImageView imagenEjercicio = convertView.findViewById(R.id.imageicon);

        Ejercicio ejercicio = listaEjercicios.get(position);
        textView.setText(ejercicio.getNombre());

        if (ejercicio.isHecho()) {
            imagenEjercicio.setImageResource(R.drawable.img_completado);
        }

        return convertView;
    }
}


