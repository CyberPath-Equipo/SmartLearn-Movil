package com.cyberpath.smartlearn.ui.carga;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.ui.acceso.AccesoActivity;
import com.cyberpath.smartlearn.ui.main.MainActivity;
import com.cyberpath.smartlearn.util.accesibilidad.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.SalidaAudio;
import com.cyberpath.smartlearn.util.network.NetworkUtils;

public class CargaActivity extends AppCompatActivity {
    private static final int duracion = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carga);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView logo = findViewById(R.id.img_carga);

        RotateAnimation rotate = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(1500);
        rotate.setRepeatCount(Animation.INFINITE);
        logo.startAnimation(rotate);

        new Handler().postDelayed(() -> {
            SalidaAudio.iniciarInstancia(this.getApplicationContext());
            EntradaAudio.iniciarInstancia(this.getApplicationContext());
            Intent intent;
            if (NetworkUtils.isInternetAvailable(this)) {
                intent = new Intent(this, AccesoActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, duracion);
    }
}