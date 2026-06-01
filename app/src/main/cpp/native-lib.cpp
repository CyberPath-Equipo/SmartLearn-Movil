#include <jni.h>
#include <string>
#include "edge-impulse-sdk/classifier/ei_run_classifier.h"

static jstring classify_audio(JNIEnv *env, jshortArray audioData) {

    jshort* buffer = env->GetShortArrayElements(audioData, nullptr);
    jsize length = env->GetArrayLength(audioData);

    float* float_buffer = new float[length];
    for (jsize i = 0; i < length; i++) {
        float_buffer[i] = static_cast<float>(buffer[i]);
    }

    signal_t signal;
    int err = numpy::signal_from_buffer(float_buffer, length, &signal);

    delete[] float_buffer;
    env->ReleaseShortArrayElements(audioData, buffer, JNI_ABORT);

    if (err != 0) {
        return env->NewStringUTF("Error creando señal");
    }

    ei_impulse_result_t result = { 0 };
    err = run_classifier(&signal, &result, false);

    if (err != EI_IMPULSE_OK) {
        return env->NewStringUTF("Error en clasificación");
    }

    int max_idx = 0;
    float max_val = 0.0;
    for (size_t ix = 0; ix < EI_CLASSIFIER_LABEL_COUNT; ix++) {
        if (result.classification[ix].value > max_val) {
            max_val = result.classification[ix].value;
            max_idx = ix;
        }
    }

    // Si ninguna clase supera el umbral del modelo, devolvemos cadena vacia.
    if (max_val < EI_CLASSIFIER_THRESHOLD) {
        return env->NewStringUTF("");
    }

    const char* detected_label = result.classification[max_idx].label;
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
