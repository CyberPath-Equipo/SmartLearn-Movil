#include <jni.h>
#include <string>
#include <android/log.h>
#include "edge-impulse-sdk/classifier/ei_run_classifier.h"

#define LOG_TAG "SmartLearn-Audio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Constantes esperadas del modelo
static constexpr jsize EXPECTED_AUDIO_LENGTH = 16000;
static constexpr float NORMALIZATION_FACTOR = 32768.0f;

static jstring classify_audio(JNIEnv *env, jshortArray audioData) {

    // Validar entrada
    if (audioData == nullptr) {
        LOGE("Error: audioData es NULL");
        return env->NewStringUTF("Error: datos de audio vacíos");
    }

    jshort* buffer = env->GetShortArrayElements(audioData, nullptr);
    if (buffer == nullptr) {
        LOGE("Error: No se pudo acceder al buffer de audio");
        return env->NewStringUTF("Error: No se pudo acceder al audio");
    }

    jsize length = env->GetArrayLength(audioData);

    // Validación crítica: verificar tamaño exacto
    if (length != EXPECTED_AUDIO_LENGTH) {
        LOGE("Error: Audio length %ld != %d esperado", (long)length, EXPECTED_AUDIO_LENGTH);
        env->ReleaseShortArrayElements(audioData, buffer, JNI_ABORT);
        return env->NewStringUTF("Error: Tamaño de audio incorrecto");
    }

    float* float_buffer = new float[length];
    for (jsize i = 0; i < length; i++) {
        // CORRECCIÓN CRÍTICA: Normalizar short al rango [-1.0, 1.0]
        float_buffer[i] = static_cast<float>(buffer[i]) / NORMALIZATION_FACTOR;
    }

    env->ReleaseShortArrayElements(audioData, buffer, JNI_ABORT);

    signal_t signal;
    int err = numpy::signal_from_buffer(float_buffer, length, &signal);

    if (err != 0) {
        LOGE("Error creando señal desde buffer: %d", err);
        delete[] float_buffer;
        return env->NewStringUTF("Error creando señal");
    }

    ei_impulse_result_t result = { 0 };
    err = run_classifier(&signal, &result, false);

    delete[] float_buffer;

    if (err != EI_IMPULSE_OK) {
        LOGE("Error en clasificación del modelo: %d", err);
        return env->NewStringUTF("Error en clasificación");
    }

    int max_idx = 0;
    float max_val = 0.0f;
    for (size_t ix = 0; ix < EI_CLASSIFIER_LABEL_COUNT; ix++) {
        if (result.classification[ix].value > max_val) {
            max_val = result.classification[ix].value;
            max_idx = ix;
        }
    }

    // Si ninguna clase supera el umbral del modelo, devolvemos cadena vacia.
    if (max_val < EI_CLASSIFIER_THRESHOLD) {
        LOGI("No clasificacion: max_val=%.4f < threshold=%.4f", max_val, EI_CLASSIFIER_THRESHOLD);
        return env->NewStringUTF("");
    }

    const char* detected_label = result.classification[max_idx].label;
    LOGI("Detectado: %s (confianza: %.4f)", detected_label, max_val);

    return env->NewStringUTF(detected_label);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_cyberpath_smartlearn_util_audioRecognizer_TestAudioRecognizerActivity_classifyAudioNative(
        JNIEnv* env,
        jobject /* this */,
        jshortArray audioData) {
    return classify_audio(env, audioData);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_cyberpath_smartlearn_util_audioRecognizer_EdgeImpulseAudioClassifier_classifyAudioNative(
        JNIEnv* env,
        jobject /* this */,
        jshortArray audioData) {
    return classify_audio(env, audioData);
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_cyberpath_smartlearn_util_audioRecognizer_EdgeImpulseAudioClassifier_getModelLabelsNative(
        JNIEnv* env,
        jobject /* this */) {
    jclass string_class = env->FindClass("java/lang/String");
    jobjectArray labels = env->NewObjectArray(EI_CLASSIFIER_LABEL_COUNT, string_class, nullptr);

    for (jsize i = 0; i < EI_CLASSIFIER_LABEL_COUNT; i++) {
        env->SetObjectArrayElement(labels, i, env->NewStringUTF(ei_classifier_inferencing_categories[i]));
    }

    return labels;
}
