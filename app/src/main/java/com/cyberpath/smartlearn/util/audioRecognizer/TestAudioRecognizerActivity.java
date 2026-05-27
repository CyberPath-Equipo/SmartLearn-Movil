package com.cyberpath.smartlearn.util.audioRecognizer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
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

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TestAudioRecognizerActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 1;
    private static final int MODEL_INPUT_SIZE = 3960; // Según tu modelo

    private Interpreter interpreter = null;
    private List<String> labels = new ArrayList<>();
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

        // Inicializar el modelo
        try {
            setupClassifier(this);
        } catch (IOException e) {
            resultadoTextView.setText("Error cargando modelo o etiquetas");
            e.printStackTrace();
        }

        toggleButton.setOnClickListener(v -> {
            if (isRecording) {
                stopAudioRecording();
            } else {
                checkPermissionAndStart();
            }
        });
    }

    private void setupClassifier(Context context) throws IOException {
        // Cargar etiquetas desde labels.txt
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line.trim());
        }
        reader.close();

        // Cargar modelo
        AssetFileDescriptor afd = context.getAssets().openFd("model.tflite");
        FileInputStream fis = new FileInputStream(afd.getFileDescriptor());
        FileChannel fc = fis.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, afd.getStartOffset(), afd.getDeclaredLength());
        Interpreter.Options options = new Interpreter.Options();
        interpreter = new Interpreter(mbb, options);
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
        int sampleRate = 16000; // Ajústalo si tu modelo necesita otra frecuencia
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize < MODEL_INPUT_SIZE) bufferSize = MODEL_INPUT_SIZE;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        isRecording = true;
        audioRecord.startRecording();
        toggleButton.setText("Detener");

        recordingThread = new Thread(() -> {
            short[] audioBuffer = new short[MODEL_INPUT_SIZE];
            while (isRecording) {
                int offset = 0;
                while (offset < MODEL_INPUT_SIZE && isRecording) {
                    int read = audioRecord.read(audioBuffer, offset, MODEL_INPUT_SIZE - offset);
                    if (read > 0) offset += read;
                }
                if (!isRecording) break;

                String resultado = classifyAudio(audioBuffer);
                runOnUiThread(() -> resultadoTextView.setText("Detectado: " + resultado));
            }
        });
        recordingThread.start();
        resultadoTextView.setText("Escuchando...");
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

    public String classifyAudio(short[] audioBuffer) {
        if (interpreter == null || labels.isEmpty()) {
            return "Error: Modelo no cargado";
        }
        byte[][] input = new byte[1][3960];
        for (int i = 0; i < 3960; i++) {
            input[0][i] = (byte)(audioBuffer[i] >> 8);
        }

        // SALIDA: int8[1,35]
        byte[][] outputBuffer = new byte[1][35];
        interpreter.run(input, outputBuffer);

        // Busca la clase con mayor valor
        int maxIndex = 0;
        int maxScore = outputBuffer[0][0];
        for (int i = 1; i < 35; i++) {
            if (outputBuffer[0][i] > maxScore) {
                maxScore = outputBuffer[0][i];
                maxIndex = i;
            }
        }
        return labels.get(maxIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioRecording();
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}