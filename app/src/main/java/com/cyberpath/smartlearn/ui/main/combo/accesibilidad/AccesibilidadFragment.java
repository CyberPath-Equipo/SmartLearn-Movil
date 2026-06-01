package com.cyberpath.smartlearn.ui.main.combo.accesibilidad;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.preferences.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class AccesibilidadFragment extends Fragment {
    private SwitchMaterial switchVisual;
    private SwitchMaterial switchAuditiva;

    private ImageView imgVisual;
    private ImageView imgAuditiva;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accesibilidad, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchVisual = view.findViewById(R.id.switch_visual);
        switchAuditiva = view.findViewById(R.id.switch_auditiva);

        imgVisual = view.findViewById(R.id.img_visual);
        imgAuditiva = view.findViewById(R.id.img_auditiva);

        boolean visualActiva = PreferencesManager.isAccesibilidadVisualActivada(requireContext());
        boolean auditivaActiva = PreferencesManager.isAccesibilidadAuditivaActivada(requireContext());

        switchVisual.setChecked(visualActiva);
        imgVisual.setImageResource(visualActiva ? R.drawable.ic_eye_closed : R.drawable.ic_eye);

        switchAuditiva.setChecked(auditivaActiva);
        imgAuditiva.setImageResource(auditivaActiva ? R.drawable.ic_volume_muted : R.drawable.ic_volume);

        switchVisual.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferencesManager.setAccesibilidadVisualActivada(requireContext(), isChecked);

            cambiarIconoConAnimacion(imgVisual, isChecked ? R.drawable.ic_eye_closed : R.drawable.ic_eye);

            ThemeManager.applyTheme(requireActivity());
            requireActivity().recreate();
        });

        switchAuditiva.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferencesManager.setAccesibilidadAuditivaActivada(requireContext(), isChecked);

            cambiarIconoConAnimacion(imgAuditiva, isChecked ? R.drawable.ic_volume_muted : R.drawable.ic_volume);

            requireActivity().recreate();
        });
    }

    private void cambiarIconoConAnimacion(ImageView imageView, int nuevoDrawableRes) {
        imageView.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    imageView.setImageResource(nuevoDrawableRes);

                    imageView.setScaleX(1.5f);
                    imageView.setScaleY(1.5f);

                    imageView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .alpha(1f)
                            .setDuration(200)
                            .start();
                }).start();
    }
}