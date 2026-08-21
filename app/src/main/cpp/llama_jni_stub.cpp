#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "LlamaJNIStub"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_clareza_ai_runtime_LocalLLMRuntime_nativeInitModelContext(
        JNIEnv *env,
        jobject thiz,
        jstring model_path) {
    LOGI("llama_jni stub init context");
    return 0L;
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
    LOGI("llama_jni stub generate inference");
    return env->NewStringUTF("");
}

}
