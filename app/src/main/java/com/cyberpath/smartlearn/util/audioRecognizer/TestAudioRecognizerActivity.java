package com.cyberpath.smartlearn.util.audioRecognizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cyberpath.smartlearn.R;

public class TestAudioRecognizerActivity extends AppCompatActivity {

    // 1. Cargar la librería C++ compilada que creaste en CMakeLists.txt
    static {
        System.loadLibrary("smartlearn_audio");
    }

    // 2. Declarar la función nativa que conecta con tu archivo native-lib.cpp
    public native String classifyAudioNative(short[] audioData);

    private static final int REQUEST_RECORD_AUDIO = 1;

    // 3. Tamaño del audio crudo: 1 segundo a 16000Hz = 16000 muestras.
    // Edge Impulse procesará esto internamente para convertirlo a sus 3960 features MFE.
    private static final int SAMPLE_RATE = 16000;
    private static final int RAW_AUDIO_SAMPLE_COUNT = 16000;

    private AudioRecord audioRecord = null;
    private Thread recordingThread = null;
    private volatile boolean isRecording = false;

    private TextView resultadoTextView;
    private Button toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_audio_recognizer);

        resultadoTextView = findViewById(R.id.resultadoTextView);
        toggleButton = findViewById(R.id.toggleButton);

        // Ya no necesitas cargar TFLite ni labels.txt aquí. ¡C++ lo hace por ti!

        toggleButton.setOnClickListener(v -> {
            if (isRecording) {
                stopAudioRecording();
            } else {
                checkPermissionAndStart();
            }
        });
    }

    private void checkPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        } else {
            startAudioRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startAudioRecording();
        } else {
            resultadoTextView.setText("Permiso de micrófono denegado");
        }
    }

    private void startAudioRecording() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            resultadoTextView.setText("No se pudo inicializar el micrófono");
            return;
        }

        // Asegurarnos de que el buffer del sistema soporte nuestro bloque de 1 segundo
        if (bufferSize < RAW_AUDIO_SAMPLE_COUNT) {
            bufferSize = RAW_AUDIO_SAMPLE_COUNT;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            resultadoTextView.setText("Error al crear AudioRecord");
            audioRecord.release();
            audioRecord = null;
            return;
        }

        isRecording = true;
        audioRecord.startRecording();
        toggleButton.setText("Detener");
        resultadoTextView.setText("Escuchando...");

        recordingThread = new Thread(() -> {
            // Buffer exacto que espera el DSP de Edge Impulse (16000 shorts)
            short[] audioBuffer = new short[RAW_AUDIO_SAMPLE_COUNT];

            while (isRecording) {
                int offset = 0;
                // Llenar el buffer hasta tener exactamente 1 segundo de audio
                while (offset < RAW_AUDIO_SAMPLE_COUNT && isRecording) {
                    int read = audioRecord.read(audioBuffer, offset, RAW_AUDIO_SAMPLE_COUNT - offset);
                    if (read > 0) {
                        offset += read;
                    } else {
                        runOnUiThread(() -> resultadoTextView.setText("Error leyendo audio"));
                        isRecording = false;
                        break;
                    }
                }

                if (!isRecording) break;

                // 4. PASO MÁGICO: Enviar el audio crudo a C++.
                // C++ hace el MFE y la inferencia TFLite, devolviendo solo el String.
                String resultado = classifyAudioNative(audioBuffer);

                // Actualizar la interfaz con el resultado
                runOnUiThread(() -> resultadoTextView.setText("Detectado: " + resultado));
            }
        });
        recordingThread.start();
    }

    private void stopAudioRecording() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (recordingThread != null) {
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordingThread = null;
        }
        toggleButton.setText("Iniciar");
        resultadoTextView.setText("Grabación detenida");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioRecording();
    }
}