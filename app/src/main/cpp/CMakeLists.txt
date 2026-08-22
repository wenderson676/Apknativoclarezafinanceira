cmake_minimum_required(VERSION 3.22.1)
project("clareza_llm" C CXX)

if(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/CMakeLists.txt")
    set(BUILD_SHARED_LIBS OFF CACHE BOOL "Build shared libraries" FORCE)
    set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "Build examples" FORCE)
    set(LLAMA_BUILD_TESTS OFF CACHE BOOL "Build tests" FORCE)
    set(LLAMA_BUILD_SERVER OFF CACHE BOOL "Build server" FORCE)

    add_subdirectory(llama.cpp)

    add_library(llama_jni SHARED llama_jni.cpp)

    target_include_directories(llama_jni PRIVATE
        llama.cpp/include
        llama.cpp/ggml/include
    )

    target_link_libraries(llama_jni PRIVATE
        llama
        ggml
        log
    )
else()
    message(WARNING "llama.cpp source not found at ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp. Building stub library.")
    add_library(llama_jni SHARED llama_jni_stub.cpp)
    target_link_libraries(llama_jni PRIVATE log)
endif()
