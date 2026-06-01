cmake_minimum_required(VERSION 3.10.2)
project("smartlearn_audio")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_C_STANDARD 11)
set(EI_ROOT ${CMAKE_CURRENT_SOURCE_DIR})

option(SMARTLEARN_FAST_BUILD "Enable faster native builds" ON)
option(SMARTLEARN_FAST_NO_DEBUG_SYMBOLS "Strip debug symbols from native debug builds" ON)

include_directories(
        ${EI_ROOT}
        ${EI_ROOT}/edge-impulse-sdk
        ${EI_ROOT}/edge-impulse-sdk/CMSIS/Core/Include
        ${EI_ROOT}/edge-impulse-sdk/CMSIS/DSP/Include
        ${EI_ROOT}/edge-impulse-sdk/third_party/flatbuffers/include
        ${EI_ROOT}/model-parameters
        ${EI_ROOT}/tflite-model
)

file(GLOB EI_CORE_SOURCES
        "edge-impulse-sdk/classifier/*.cpp"
        "edge-impulse-sdk/dsp/*.cpp"
        "edge-impulse-sdk/dsp/kissfft/*.cpp"
        "edge-impulse-sdk/dsp/dct/*.cpp"
        "tflite-model/*.cpp"
        "tflite-model/*.c"
)

file(GLOB EI_TFLM_COMMON_SOURCES
        "edge-impulse-sdk/tensorflow/lite/c/common.c"
        "edge-impulse-sdk/tensorflow/lite/core/api/*.cc"
        "edge-impulse-sdk/tensorflow/lite/kernels/internal/*.cc"
        "edge-impulse-sdk/tensorflow/lite/kernels/kernel_util_lite.cc"
        "edge-impulse-sdk/tensorflow/lite/kernels/custom/tree_ensemble_classifier.cc"
)

file(GLOB_RECURSE EI_TFLM_MICRO_SOURCES
        "edge-impulse-sdk/tensorflow/lite/micro/*.cc"
)

set(EI_TFLM_SOURCES
        ${EI_TFLM_COMMON_SOURCES}
        ${EI_TFLM_MICRO_SOURCES}
)

# Excluir fuentes de tests/mocks para evitar dependencias que no aplican al APK.
list(FILTER EI_TFLM_SOURCES EXCLUDE REGEX ".*[\\\\/](test|tests|testing)[\\\\/].*")
list(FILTER EI_TFLM_SOURCES EXCLUDE REGEX ".*_test\\.cc$")
list(FILTER EI_TFLM_SOURCES EXCLUDE REGEX ".*[\\\\/]mock_.*\\.cc$")
list(FILTER EI_TFLM_SOURCES EXCLUDE REGEX ".*[\\\\/]fake_.*\\.cc$")
list(FILTER EI_TFLM_SOURCES EXCLUDE REGEX ".*[\\\\/]tensorflow[\\\\/]lite[\\\\/]micro[\\\\/]kernels[\\\\/]kernel_runner\\.cc$")
list(FILTER EI_TFLM_SOURCES EXCLUDE REGEX ".*[\\\\/]tensorflow[\\\\/]lite[\\\\/]micro[\\\\/](test_helpers|test_helper_custom_ops)\\.cc$")
list(FILTER EI_TFLM_SOURCES EXCLUDE REGEX ".*[\\\\/]tensorflow[\\\\/]lite[\\\\/]micro[\\\\/]recording_.*\\.cc$")

add_library(
        smartlearn_audio
        SHARED
        native-lib.cpp
        edge-impulse-sdk/porting/android/ei_classifier_porting.cpp
        ${EI_CORE_SOURCES}
        ${EI_TFLM_SOURCES}
)

if(SMARTLEARN_FAST_BUILD)
    # En TFLM muchos kernels comparten simbolos internos (Init/Prepare/Eval);
    # con Unity Build se mezclan en una sola TU y chocan entre si.
    set_source_files_properties(${EI_TFLM_SOURCES} PROPERTIES SKIP_UNITY_BUILD_INCLUSION ON)

    # Acelera compilacion del resto de fuentes (JNI, DSP, classifier, etc.).
    set_target_properties(smartlearn_audio PROPERTIES
            UNITY_BUILD ON
            UNITY_BUILD_BATCH_SIZE 16
    )

    if(SMARTLEARN_FAST_NO_DEBUG_SYMBOLS)
        # Reduce significativamente tiempo de compilacion y enlace en Debug.
        target_compile_options(smartlearn_audio PRIVATE $<$<CONFIG:Debug>:-g0>)
    endif()
endif()

target_compile_definitions(smartlearn_audio PRIVATE EI_PORTING_ANDROID=1)

find_library(log-lib log)

target_link_libraries(
        smartlearn_audio
        ${log-lib}
        atomic
        m
)