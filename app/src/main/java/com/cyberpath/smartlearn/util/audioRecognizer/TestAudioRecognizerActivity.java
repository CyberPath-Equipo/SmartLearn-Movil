package com.cyberpath.smartlearn.util.audioRecognizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cyberpath.smartlearn.R;
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier;
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier.AudioClassifierOptions;
import com.google.mediapipe.tasks.audio.core.RunningMode;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.components.containers.ClassificationResult;
import com.google.mediapipe.tasks.components.containers.Classifications;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.components.containers.AudioData;

public class TestAudioRecognizerActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 1;
    private static final String TAG = "AudioTest";

    private TextView txtResultado;

    private AudioClassifier audioClassifier;
    private AudioRecord audioRecord;
    private Thread workerThread;
    private volatile boolean escuchando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_audio_recognizer);

        txtResultado = findViewById(R.id.txtResultado);

        if (txtResultado == null) {
            throw new IllegalStateException("No se encontró txtResultado en el layout.");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO
            );
        } else {
            iniciarClasificador();
        }
    }

    private void iniciarClasificador() {
        try {
            AudioClassifierOptions options = AudioClassifierOptions.builder()
                    .setBaseOptions(
                            BaseOptions.builder()
                                    .setModelAssetPath("model.tflite")
                                    .build()
                    )
                    .setRunningMode(RunningMode.AUDIO_STREAM)
                    .setMaxResults(3)
                    .setScoreThreshold(0.80f)
                    .setResultListener(result -> {  // result es AudioClassifierResult

                        if (result == null || result.classificationResults().isEmpty()) {
                            return;
                        }

                        // En modo AUDIO_STREAM normalmente solo hay 1 ClassificationResult
                        ClassificationResult classificationResult = result.classificationResults().get(0);

                        if (classificationResult.classifications().isEmpty()) {
                            return;
                        }

                        // Tomamos la primera cabeza de clasificación (normalmente solo hay una)
                        Classifications classifications = classificationResult.classifications().get(0);

                        if (classifications.categories().isEmpty()) {
                            return;
                        }

                        Category topCategory = classifications.categories().get(0);

                        String etiqueta = topCategory.categoryName();
                        float score = topCategory.score();

                        runOnUiThread(() -> {
                            txtResultado.setText(String.format("Detectado: %s (%.2f)", etiqueta, score));
                        });

                    })
                    .build();

            audioClassifier = AudioClassifier.createFromOptions(this, options);

            // Crear AudioRecord con la configuración recomendada por el clasificador
            audioRecord = audioClassifier.createAudioRecord();
            audioRecord.startRecording();

            escuchando = true;
            workerThread = new Thread(this::loopAudio);
            workerThread.start();

            txtResultado.setText("Escuchando...");

        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar clasificador", e);
            txtResultado.setText("Error: " + e.getMessage());
        }
    }

    private void loopAudio() {
        try {

            int sampleRate = audioRecord.getSampleRate();

            AudioData audioData =
                    AudioData.create(audioRecord.getFormat(), sampleRate);

            long timestampMs = 0;

            while (escuchando) {

                int samplesRead = audioData.load(audioRecord);

                if (samplesRead > 0) {

                    audioClassifier.classifyAsync(audioData, timestampMs);

                    // avanzar timestamp manualmente
                    timestampMs += (samplesRead * 1000L) / sampleRate;
                }
            }

        } catch (Exception e) {

            Log.e(TAG, "Error en loop de audio", e);

            runOnUiThread(() ->
                    txtResultado.setText("Error en audio: " + e.getMessage()));
        }
    }

    @Override
    protected void onDestroy() {
        escuchando = false;

        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception ignored) {}
            audioRecord.release();
        }

        if (audioClassifier != null) {
            audioClassifier.close();
        }

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarClasificador();
        } else {
            txtResultado.setText("Permiso de micrófono denegado");
        }
    }
}