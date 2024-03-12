#ifndef KEEPALIVE_COMMON_H
#define KEEPALIVE_COMMON_H

#include <android/log.h>
#include <stdint.h>
#include <sys/types.h>

//#define NATIVE_LIB_DEBUG_TAG        "KeepAlive"
#define NATIVE_LIB_DEBUG_TAG        "KeepAlive_native"
#define LOGW(...)    __android_log_print(ANDROID_LOG_WARN, NATIVE_LIB_DEBUG_TAG, __VA_ARGS__)
#define LOGE(...)    __android_log_print(ANDROID_LOG_ERROR, NATIVE_LIB_DEBUG_TAG, __VA_ARGS__)

#ifdef NATIVE_LIB_DEBUG
#define LOGI(...)    __android_log_print(ANDROID_LOG_INFO, NATIVE_LIB_DEBUG_TAG, __VA_ARGS__)
#define LOGD(...)    __android_log_print(ANDROID_LOG_DEBUG, NATIVE_LIB_DEBUG_TAG, __VA_ARGS__)
#else
#define LOGI(...)
#define LOGD(...)
#endif

typedef unsigned short Char16;
typedef unsigned int Char32;
#endif //KEEPALIVE_COMMON_H
