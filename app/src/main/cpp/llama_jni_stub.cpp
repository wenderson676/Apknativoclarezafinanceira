#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "LlamaJNIStub"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_clareza_ai_runtime_LocalLLMRuntime_nativeInitModelContext(
        JNIEnv *env,
        jobject thiz,
        jstring model_path) {
    LOGW("llama_jni stub active: llama.cpp C++ source not bundled in APK. Returning 1L handle to pretend load succeeded.");
    return 1L;
}

JNIEXPORT void JNICALL
Java_com_example_clareza_ai_runtime_LocalLLMRuntime_nativeFreeModelContext(
        JNIEnv *env,
        jobject thiz,
        jlong handle) {
    LOGI("llama_jni stub free context");
}

JNIEXPORT jstring JNICALL
Java_com_example_clareza_ai_runtime_LocalLLMRuntime_nativeGenerateInference(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jstring prompt) {
    LOGW("llama_jni stub generate inference called on handle 0L. Returning empty string for instant fallback.");
    return env->NewStringUTF("");
}

}

