package com.cyberpath.smartlearn.ui.main.combo.ayuda;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.R;

public class AyudaFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ayuda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.layout_registro_profesores).setOnClickListener(v ->
                abrirUrl("http://smartlearn.com/profesores/registro"));

        view.findViewById(R.id.layout_web).setOnClickListener(v ->
                abrirUrl("http://smartlearn.com"));

        view.findViewById(R.id.layout_instagram).setOnClickListener(v ->
                abrirUrl("https://instagram.com/cyberpath_contacto"));

        view.findViewById(R.id.layout_correo).setOnClickListener(v ->
                enviarCorreo("cyberpath_contacto@gmail.com"));

        Button btnEnviarFeedback = view.findViewById(R.id.btn_enviar_feedback);
        btnEnviarFeedback.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Gracias por tu feedback. Enviado exitosamente!", Toast.LENGTH_SHORT).show();
        });
    }

    private void abrirUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void enviarCorreo(String email) {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:" + email));
        startActivity(i);
    }
}