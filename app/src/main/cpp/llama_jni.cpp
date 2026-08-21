#include <jni.h>
#include <string>
#include <vector>
#include <cstring>
#include <android/log.h>
#include "llama.h"

#define LOG_TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct LlamaContextHolder {
    llama_model * model = nullptr;
    llama_context * ctx = nullptr;
    llama_sampler * smpl = nullptr;
};

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_clareza_ai_runtime_LocalLLMRuntime_nativeInitModelContext(
        JNIEnv *env,
        jobject thiz,
        jstring model_path) {
    if (model_path == nullptr) {
        LOGE("Model path is null");
        return 0L;
    }

    const char *path = env->GetStringUTFChars(model_path, nullptr);
    LOGI("Initializing llama.cpp model from path: %s", path);

    llama_backend_init();

    llama_model_params mparams = llama_model_default_params();
    llama_model *model = llama_model_load_from_file(path, mparams);

    env->ReleaseStringUTFChars(model_path, path);

    if (!model) {
        LOGE("Failed to load llama.cpp model from file");
        llama_backend_free();
        return 0L;
    }

    llama_context_params cparams = llama_context_default_params();
    cparams.n_ctx = 1024;
    cparams.n_threads = 4;
    cparams.n_threads_batch = 4;

    llama_context *ctx = llama_init_from_model(model, cparams);
    if (!ctx) {
        LOGE("Failed to initialize llama.cpp context from model");
        llama_model_free(model);
        llama_backend_free();
        return 0L;
    }

    LlamaContextHolder *holder = new LlamaContextHolder();
    holder->model = model;
    holder->ctx = ctx;

    auto sparams = llama_sampler_chain_default_params();
    holder->smpl = llama_sampler_chain_init(sparams);
    llama_sampler_chain_add(holder->smpl, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(holder->smpl, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(holder->smpl, llama_sampler_init_temp(0.7f));

    LOGI("Successfully initialized llama.cpp context holder: %p", holder);
    return reinterpret_cast<jlong>(holder);
}

JNIEXPORT void JNICALL
Java_com_example_clareza_ai_runtime_LocalLLMRuntime_nativeFreeModelContext(
        JNIEnv *env,
        jobject thiz,
        jlong handle) {
    if (handle == 0L) return;

    LlamaContextHolder *holder = reinterpret_cast<LlamaContextHolder *>(handle);
    LOGI("Freeing llama.cpp context holder: %p", holder);

    if (holder->smpl) {
        llama_sampler_free(holder->smpl);
        holder->smpl = nullptr;
    }
    if (holder->ctx) {
        llama_free(holder->ctx);
        holder->ctx = nullptr;
    }
    if (holder->model) {
        llama_model_free(holder->model);
        holder->model = nullptr;
    }

    delete holder;
    llama_backend_free();
    LOGI("Successfully freed llama.cpp context and backend");
}

JNIEXPORT jstring JNICALL
Java_com_example_clareza_ai_runtime_LocalLLMRuntime_nativeGenerateInference(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jstring prompt) {
    if (handle == 0L || prompt == nullptr) {
        return env->NewStringUTF("");
    }

    LlamaContextHolder *holder = reinterpret_cast<LlamaContextHolder *>(handle);
    if (!holder->model || !holder->ctx) {
        return env->NewStringUTF("");
    }

    const char *prompt_cstr = env->GetStringUTFChars(prompt, nullptr);
    int32_t prompt_len = static_cast<int32_t>(strlen(prompt_cstr));

    const struct llama_vocab *vocab = llama_model_get_vocab(holder->model);

    // Calculate token count
    int32_t n_tokens = -llama_tokenize(vocab, prompt_cstr, prompt_len, nullptr, 0, true, true);
    if (n_tokens <= 0) {
        env->ReleaseStringUTFChars(prompt, prompt_cstr);
        return env->NewStringUTF("");
    }

    std::vector<llama_token> tokens(n_tokens);
    llama_tokenize(vocab, prompt_cstr, prompt_len, tokens.data(), tokens.size(), true, true);
    env->ReleaseStringUTFChars(prompt, prompt_cstr);

    // Initial batch decode
    llama_batch batch = llama_batch_get_one(tokens.data(), static_cast<int32_t>(tokens.size()));
    if (llama_decode(holder->ctx, batch) != 0) {
        LOGE("Failed to decode prompt batch");
        return env->NewStringUTF("");
    }

    std::string response_text;
    const int max_tokens = 256;

    for (int i = 0; i < max_tokens; i++) {
        llama_token new_token = llama_sampler_sample(holder->smpl, holder->ctx, -1);
        if (llama_vocab_is_eog(vocab, new_token)) {
            break;
        }

        char buf[256];
        int n = llama_token_to_piece(vocab, new_token, buf, sizeof(buf), 0, true);
        if (n > 0) {
            response_text.append(buf, n);
        }

        llama_batch next_batch = llama_batch_get_one(&new_token, 1);
        if (llama_decode(holder->ctx, next_batch) != 0) {
            break;
        }
    }

    return env->NewStringUTF(response_text.c_str());
}

} // extern "C"
